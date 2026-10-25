
package com.proyecto.login.controller;

import com.proyecto.login.dto.LoginRequest;
import com.proyecto.login.dto.LoginResponse;
import com.proyecto.login.dto.VerificarPinRequest;
import com.proyecto.login.dto.VerificarPinResponse;

import com.proyecto.login.service.AuthService;
import com.proyecto.login.service.PinService;
import com.proyecto.login.service.CorreoService;
import com.proyecto.login.service.LimitePinException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PinService pinService;
    private final CorreoService correoService;

    public AuthController(
            AuthService authService,
            PinService pinService,
            CorreoService correoService) {

        this.authService = authService;
        this.pinService = pinService;
        this.correoService = correoService;
    }

    // PRIMER ENDPOINT: VALIDAR CREDENCIALES Y ENVIAR PIN
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody(required = false) LoginRequest request) {

        if (request == null
                || request.usuario() == null
                || request.clave() == null
                || request.correo() == null
                || request.usuario().isBlank()
                || request.clave().isBlank()
                || request.correo().isBlank()) {

            return ResponseEntity.badRequest().body(
                    new LoginResponse(
                            "DATOS_INVALIDOS",
                            "Debes completar los tres campos.",
                            null
                    )
            );
        }

        AuthService.Resultado resultado =
                authService.validarCredenciales(request);

        if (resultado.estado()
                != AuthService.Estado.PENDIENTE_PIN) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(
                            "AUTENTICACION_FALLIDA",
                            "No fue posible autenticarte. Revisa tus datos o intenta mas tarde.",
                            null
                    ));
        }
        // Credenciales incorrectas.
if (resultado.estado()
        == AuthService.Estado.CREDENCIALES_INVALIDAS) {

    return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new LoginResponse(
                    "AUTENTICACION_FALLIDA",
                    "Usuario, contrasena o correo incorrectos.",
                    null
            ));
}

        PinService.PinGenerado generado;

        try {
            generado = pinService.crearPinParaUsuario(
                    request.usuario()
            );

        } catch (LimitePinException ex) {

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new LoginResponse(
                            "SOLICITUD_LIMITADA",
                            "No se puede solicitar otro codigo por ahora. Intenta mas tarde.",
                            null
                    ));
        }

        try {
            correoService.enviarPin(
                    generado.correoRegistrado(),
                    generado.pin()
            );

        } catch (MailException ex) {

            pinService.invalidarDesafio(
                    generado.desafioId()
            );

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new LoginResponse(
                            "SERVICIO_NO_DISPONIBLE",
                            "No fue posible procesar el envio del codigo.",
                            null
                    ));
        }

        return ResponseEntity.ok(
                new LoginResponse(
                        "PIN_ENVIADO",
                        "Si la entrega se completa, recibiras un codigo de verificacion.",
                        generado.desafioId()
                )
        );
    }

    // SEGUNDO ENDPOINT: VERIFICAR PIN
    @PostMapping("/verificar-pin")
    public ResponseEntity<VerificarPinResponse> verificarPin(
            @RequestBody(required = false)
            VerificarPinRequest request) {

        if (request == null
                || request.desafioId() == null
                || request.pin() == null
                || !request.pin().matches("[0-9]{6}")) {

            return ResponseEntity
                    .badRequest()
                    .body(new VerificarPinResponse(
                            "PIN_INVALIDO",
                            "Introduce un PIN valido de seis digitos.",
                            null
                    ));
        }

        PinService.ResultadoVerificacion resultado =
                pinService.verificarPin(
                        request.desafioId(),
                        request.pin()
                );

        switch (resultado.estado()) {

            case VERIFICADO:
                return ResponseEntity.ok(
                        new VerificarPinResponse(
                                "VERIFICADO",
                                "Identidad verificada correctamente.",
                                null
                        )
                );

            case PIN_INCORRECTO:
                return ResponseEntity
                        .badRequest()
                        .body(new VerificarPinResponse(
                                "PIN_INCORRECTO",
                                "El codigo es incorrecto.",
                                resultado.intentosRestantes()
                        ));

            case PIN_BLOQUEADO:
                return ResponseEntity
                        .status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new VerificarPinResponse(
                                "PIN_BLOQUEADO",
                                "Se agotaron los intentos permitidos.",
                                0
                        ));

            case PIN_EXPIRADO:
                return ResponseEntity
                        .badRequest()
                        .body(new VerificarPinResponse(
                                "PIN_EXPIRADO",
                                "El codigo ha expirado.",
                                null
                        ));

            case PIN_UTILIZADO:
                return ResponseEntity
                        .badRequest()
                        .body(new VerificarPinResponse(
                                "PIN_UTILIZADO",
                                "Este codigo ya no esta disponible.",
                                null
                        ));

            default:
                return ResponseEntity
                        .badRequest()
                        .body(new VerificarPinResponse(
                                "PIN_INVALIDO",
                                "No fue posible verificar el codigo.",
                                null
                        ));
        }
    }
}
