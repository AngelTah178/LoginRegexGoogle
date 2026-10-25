
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

    // Unico constructor utilizado por Spring Boot.
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

        // 1. Validar el formato del usuario con Regex.
        if (request == null ||
                !regex.validarUsuario(request.usuario())) {

            return credencialesInvalidas();
        }

        // 2. Buscar usuario en SQL Server.
        Usuario usuario = repository
                .buscarParaActualizar(request.usuario())
                .orElse(null);

        if (usuario == null) {
            return credencialesInvalidas();
        }

        LocalDateTime ahora =
                LocalDateTime.now(Clock.systemUTC());

        // 3. Comprobar si la cuenta esta bloqueada.
        LocalDateTime bloqueadoHasta =
                usuario.getBloqueadoHasta();

        if (bloqueadoHasta != null) {

            if (ahora.isBefore(bloqueadoHasta)) {

                return new Resultado(
                        Estado.CUENTA_BLOQUEADA,
                        0
                );
            }

            // El bloqueo ya vencio.
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
        }

        // 4. Validar formato de la contraseña.
        if (!regex.validarClave(request.clave())) {
            return registrarFallo(usuario, ahora);
        }

        // 5. Comparar contraseña con BCrypt.
        boolean claveCorrecta = encoder.matches(
                request.clave(),
                usuario.getClaveHash()
        );

        if (!claveCorrecta) {
            return registrarFallo(usuario, ahora);
        }

        // 6. Validar formato del correo.
        if (!regex.validarCorreo(request.correo())) {
            return registrarFallo(usuario, ahora);
        }

        // 7. Comparar con el correo almacenado.
        String correoRegistrado = usuario.getCorreo();

        boolean correoCorrecto =
                correoRegistrado != null &&
                correoRegistrado.equalsIgnoreCase(
                        request.correo()
                );

        if (!correoCorrecto) {
            return registrarFallo(usuario, ahora);
        }

        // 8. Las credenciales son correctas.
        // Todavia falta la verificacion mediante PIN.
        // No reiniciamos intentos hasta completar el PIN.

        return new Resultado(
                Estado.PENDIENTE_PIN,
                MAX_INTENTOS - usuario.getIntentosFallidos()
        );
    }

    // Registrar un intento de autenticacion fallido.
    private Resultado registrarFallo(
            Usuario usuario,
            LocalDateTime ahora) {

        int intentos = Math.min(
                MAX_INTENTOS,
                usuario.getIntentosFallidos() + 1
        );

        usuario.setIntentosFallidos(intentos);

        // Quinto fallo: bloquear durante 15 minutos.
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

    // Respuesta generica para usuarios inexistentes
    // o nombres de usuario con formato incorrecto.
    private Resultado credencialesInvalidas() {

        return new Resultado(
                Estado.CREDENCIALES_INVALIDAS,
                0
        );
    }
}
