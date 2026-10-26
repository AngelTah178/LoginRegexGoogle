
package com.proyecto.login.service;

import com.proyecto.login.dto.LoginRequest;
import com.proyecto.login.entity.Usuario;
import com.proyecto.login.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.time.Clock;

@Service
public class AuthService {

    private static final int MAX_INTENTOS = 5;
    private static final int MINUTOS_BLOQUEO = 15;

    private final UsuarioRepository repository;
    private final RegexService regex;
    private final BCryptPasswordEncoder encoder;

    //
    public AuthService(
            UsuarioRepository repository,
            RegexService regex) {

        this.repository = repository;
        this.regex = regex;
        this.encoder = new BCryptPasswordEncoder(10);
    }

    public enum Estado {
        PENDIENTE_PIN,
        CREDENCIALES_INVALIDAS,
        CUENTA_BLOQUEADA
    }

    public record Resultado(
            Estado estado,
            int intentosRestantes
    ) {}

    @Transactional
    public Resultado validarCredenciales(LoginRequest request) {

        // validar usuario con regex
        if (request == null ||
                !regex.validarUsuario(request.usuario())) {

            return credencialesInvalidas();
        }

        // buscar user en sql
        Usuario usuario = repository
                .buscarParaActualizar(request.usuario())
                .orElse(null);

        if (usuario == null) {
            return credencialesInvalidas();
        }

        LocalDateTime ahora =
                LocalDateTime.now(Clock.systemUTC());

        //cuenta bloqueada
        LocalDateTime bloqueadoHasta =
                usuario.getBloqueadoHasta();

        if (bloqueadoHasta != null) {

            if (ahora.isBefore(bloqueadoHasta)) {

                return new Resultado(
                        Estado.CUENTA_BLOQUEADA,
                        0
                );
            }
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
        }

        // Validar formato de la contraseña.
        if (!regex.validarClave(request.clave())) {
            return registrarFallo(usuario, ahora);
        }

        // Comparar contraseña con BCrypt.
        boolean claveCorrecta = encoder.matches(
                request.clave(),
                usuario.getClaveHash()
        );

        if (!claveCorrecta) {
            return registrarFallo(usuario, ahora);
        }

        //Validar formato del correo.
        if (!regex.validarCorreo(request.correo())) {
            return registrarFallo(usuario, ahora);
        }

        // comparar con el correo
        String correoRegistrado = usuario.getCorreo();

        boolean correoCorrecto =
                correoRegistrado != null &&
                correoRegistrado.equalsIgnoreCase(
                        request.correo()
                );

        if (!correoCorrecto) {
            return registrarFallo(usuario, ahora);
        }

        // credenciales correctas

        return new Resultado(
                Estado.PENDIENTE_PIN,
                MAX_INTENTOS - usuario.getIntentosFallidos()
        );
    }

    // auth fallido
    private Resultado registrarFallo(
            Usuario usuario,
            LocalDateTime ahora) {

        int intentos = Math.min(
                MAX_INTENTOS,
                usuario.getIntentosFallidos() + 1
        );

        usuario.setIntentosFallidos(intentos);

        // quinto fallo vale pollo
        if (intentos >= MAX_INTENTOS) {

            usuario.setBloqueadoHasta(
                    ahora.plusMinutes(MINUTOS_BLOQUEO)
            );

            repository.save(usuario);

            return new Resultado(
                    Estado.CUENTA_BLOQUEADA,
                    0
            );
        }

        repository.save(usuario);

        return new Resultado(
                Estado.CREDENCIALES_INVALIDAS,
                MAX_INTENTOS - intentos
        );
    }

    // 
    private Resultado credencialesInvalidas() {

        return new Resultado(
                Estado.CREDENCIALES_INVALIDAS,
                0
        );
    }
}
