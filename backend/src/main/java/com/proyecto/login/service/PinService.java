
package com.proyecto.login.service;

import com.proyecto.login.entity.Usuario;
import com.proyecto.login.entity.VerificacionPin;
import com.proyecto.login.repository.UsuarioRepository;
import com.proyecto.login.repository.VerificacionPinRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class PinService {

    private static final int MINUTOS_VIGENCIA = 5;
    private static final int MAX_INTENTOS_PIN = 3;
    private static final int SEGUNDOS_ENTRE_PINES = 60;
    private static final int MAX_PINES_POR_HORA = 5;

    private final VerificacionPinRepository pinRepository;
    private final UsuarioRepository usuarioRepository;
    private final SecureRandom random;
    private final byte[] claveHmac;

    // Unico constructor utilizado por Spring Boot.
    public PinService(
            VerificacionPinRepository pinRepository,
            UsuarioRepository usuarioRepository,
            @Value("${app.pin.hmac-secret}") String secreto) {

        this.pinRepository = pinRepository;
        this.usuarioRepository = usuarioRepository;
        this.random = new SecureRandom();

        this.claveHmac = Base64.getDecoder().decode(secreto);

        if (this.claveHmac.length < 32) {
            throw new IllegalArgumentException(
                    "La clave HMAC debe tener al menos 32 bytes."
            );
        }
    }

    // Resultado interno para enviar el PIN por Gmail.
    // Nunca devolver el valor pin mediante HTTP.
    public record PinGenerado(
            UUID desafioId,
            String pin,
            LocalDateTime expiraUtc,
            String correoRegistrado
    ) {}

    // Estados posibles al verificar el PIN.
    public enum EstadoVerificacion {
        VERIFICADO,
        PIN_INCORRECTO,
        PIN_BLOQUEADO,
        PIN_EXPIRADO,
        PIN_UTILIZADO,
        PIN_INVALIDO
    }

    // Resultado de la validacion del PIN.
    public record ResultadoVerificacion(
            EstadoVerificacion estado,
            int intentosRestantes
    ) {}

    // =====================================================
    // 1. GENERAR UN NUEVO PIN
    // =====================================================

    @Transactional
    public PinGenerado crearPinParaUsuario(String nombreUsuario) {

        // Buscar usuario con bloqueo de escritura.
        Usuario usuario = usuarioRepository
                .buscarParaActualizar(nombreUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuario no encontrado."
                        )
                );

        LocalDateTime ahora =
                LocalDateTime.now(Clock.systemUTC());

        // Limitar a cinco desafios por hora.
        long pinesUltimaHora =
                pinRepository.countByUsuarioIdAndFechaCreacionUtcAfter(
                        usuario.getId(),
                        ahora.minusHours(1)
                );

        if (pinesUltimaHora >= MAX_PINES_POR_HORA) {
            throw new LimitePinException();
        }

        // Aplicar el intervalo de 60 segundos, incluso
        // si el ultimo desafio ya fue utilizado.
        pinRepository
                .findTopByUsuarioIdOrderByFechaCreacionUtcDesc(
                        usuario.getId()
                )
                .ifPresent(ultimo -> {

                    LocalDateTime siguientePermitido =
                            ultimo.getFechaCreacionUtc()
                                    .plusSeconds(
                                            SEGUNDOS_ENTRE_PINES
                                    );

                    if (ahora.isBefore(siguientePermitido)) {
                        throw new LimitePinException();
                    }
                });

        // Invalidar desafios anteriores que sigan activos.
        List<VerificacionPin> pendientes =
                pinRepository.findByUsuarioIdAndUsadoFalse(
                        usuario.getId()
                );

        for (VerificacionPin anterior : pendientes) {
            anterior.setUsado(true);
        }

        pinRepository.saveAll(pendientes);

        // Generar seis digitos aleatorios.
        String pin = String.format(
                "%06d",
                random.nextInt(1_000_000)
        );

        UUID desafioId = UUID.randomUUID();

        LocalDateTime expiracion =
                ahora.plusMinutes(MINUTOS_VIGENCIA);

        // Preparar entidad.
        VerificacionPin verificacion =
                new VerificacionPin();

        verificacion.setId(desafioId);
        verificacion.setUsuarioId(usuario.getId());

        // Guardar el HMAC, no el PIN original.
        verificacion.setPinHash(
                calcularHmac(
                        usuario.getId(),
                        desafioId,
                        pin
                )
        );

        verificacion.setFechaCreacionUtc(ahora);
        verificacion.setFechaExpiracionUtc(expiracion);
        verificacion.setIntentosFallidos(0);
        verificacion.setUsado(false);

        pinRepository.save(verificacion);

        return new PinGenerado(
                desafioId,
                pin,
                expiracion,
                usuario.getCorreo()
        );
    }

    // =====================================================
    // 2. VERIFICAR EL PIN RECIBIDO DESDE REACT
    // =====================================================

    @Transactional
    public ResultadoVerificacion verificarPin(
            UUID desafioId,
            String pin) {

        // Validar formato.
        if (desafioId == null
                || pin == null
                || !pin.matches("[0-9]{6}")) {

            return new ResultadoVerificacion(
                    EstadoVerificacion.PIN_INVALIDO,
                    0
            );
        }

        // Bloquear el desafio para evitar doble uso.
        VerificacionPin verificacion = pinRepository
                .buscarParaActualizar(desafioId)
                .orElse(null);

        if (verificacion == null) {
            return new ResultadoVerificacion(
                    EstadoVerificacion.PIN_INVALIDO,
                    0
            );
        }

        // Comprobar si el desafio ya fue consumido.
        if (verificacion.getIntentosFallidos()
                >= MAX_INTENTOS_PIN) {

            return new ResultadoVerificacion(
                    EstadoVerificacion.PIN_BLOQUEADO,
                    0
            );
        }

        if (verificacion.isUsado()) {
            return new ResultadoVerificacion(
                    EstadoVerificacion.PIN_UTILIZADO,
                    0
            );
        }

        LocalDateTime ahora =
                LocalDateTime.now(Clock.systemUTC());

        // Comprobar expiracion.
        if (!ahora.isBefore(
                verificacion.getFechaExpiracionUtc())) {

            verificacion.setUsado(true);
            pinRepository.save(verificacion);

            return new ResultadoVerificacion(
                    EstadoVerificacion.PIN_EXPIRADO,
                    0
            );
        }

        // Calcular el HMAC del codigo recibido.
        String hmacCalculado = calcularHmac(
                verificacion.getUsuarioId(),
                desafioId,
                pin
        );

        byte[] calculado =
                HexFormat.of().parseHex(hmacCalculado);

        byte[] almacenado =
                HexFormat.of().parseHex(
                        verificacion.getPinHash()
                );

        boolean pinCorrecto =
                MessageDigest.isEqual(calculado, almacenado);

        // PIN INCORRECTO.
        if (!pinCorrecto) {

            int intentos =
                    verificacion.getIntentosFallidos() + 1;

            verificacion.setIntentosFallidos(intentos);

            // Tercer intento fallido.
            if (intentos >= MAX_INTENTOS_PIN) {

                verificacion.setUsado(true);
                pinRepository.save(verificacion);

                return new ResultadoVerificacion(
                        EstadoVerificacion.PIN_BLOQUEADO,
                        0
                );
            }

            pinRepository.save(verificacion);

            return new ResultadoVerificacion(
                    EstadoVerificacion.PIN_INCORRECTO,
                    MAX_INTENTOS_PIN - intentos
            );
        }

        // PIN CORRECTO.
        // Marcarlo como utilizado.
        verificacion.setUsado(true);
        pinRepository.save(verificacion);

        // Reiniciar contador de intentos del login.
        Usuario usuario = usuarioRepository
                .findById(verificacion.getUsuarioId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Usuario asociado no encontrado."
                        )
                );

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);

        usuarioRepository.save(usuario);

        return new ResultadoVerificacion(
                EstadoVerificacion.VERIFICADO,
                0
        );
    }

    // =====================================================
    // 3. INVALIDAR DESAFIO
    // =====================================================

    @Transactional
    public void invalidarDesafio(UUID desafioId) {

        pinRepository.findById(desafioId)
                .ifPresent(verificacion -> {

                    verificacion.setUsado(true);
                    pinRepository.save(verificacion);
                });
    }

    // =====================================================
    // 4. CALCULAR HMAC-SHA256
    // =====================================================

    private String calcularHmac(
            Long usuarioId,
            UUID desafioId,
            String pin) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            SecretKeySpec key = new SecretKeySpec(
                    claveHmac,
                    "HmacSHA256"
            );

            mac.init(key);

            String contenido =
                    usuarioId + ":" +
                    desafioId + ":" +
                    pin;

            byte[] resultado = mac.doFinal(
                    contenido.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(resultado);

        } catch (GeneralSecurityException ex) {

            throw new IllegalStateException(
                    "No se pudo proteger el PIN.",
                    ex
            );
        }
    }
}
