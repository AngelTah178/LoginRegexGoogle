package com.proyecto.login.service;

import com.proyecto.login.entity.Usuario;
import com.proyecto.login.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Duration;

@Service
public class IntentosService {

    private static final int MAX_INTENTOS = 5;

    private static final Duration DURACION_BLOQUEO =
            Duration.ofMinutes(15);

    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    @Autowired
    public IntentosService(
            UsuarioRepository usuarioRepository) {

        this(usuarioRepository, Clock.systemUTC());
    }

    IntentosService(
            UsuarioRepository usuarioRepository,
            Clock clock) {

        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    public enum Estado {
        INTENTO_REGISTRADO,
        CUENTA_BLOQUEADA,
        USUARIO_NO_ENCONTRADO
    }

    public record ResultadoIntento(
            Estado estado,
            int intentosRestantes,
            LocalDateTime bloqueadoHasta
    ) {}

    @Transactional
    public ResultadoIntento registrarFallo(String nombreUsuario) {

        Usuario usuario = usuarioRepository
                .buscarParaActualizar(nombreUsuario)
                .orElse(null);

        if (usuario == null) {
            return new ResultadoIntento(
                    Estado.USUARIO_NO_ENCONTRADO,
                    0,
                    null
            );
        }

        LocalDateTime ahora =
                LocalDateTime.now(clock);

        LocalDateTime bloqueo =
                usuario.getBloqueadoHasta();

        //comprobar bloqueo vigente
        if (bloqueo != null && ahora.isBefore(bloqueo)) {

            return new ResultadoIntento(
                    Estado.CUENTA_BLOQUEADA,
                    0,
                    bloqueo
            );
        }

        // reiniciar contador
        if (bloqueo != null) {
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
        }

        // Registrar el intento incorrecto.
        int intentos = Math.min(
                MAX_INTENTOS,
                usuario.getIntentosFallidos() + 1
        );

        usuario.setIntentosFallidos(intentos);

        // blouqo
        if (intentos >= MAX_INTENTOS) {

            LocalDateTime hasta =
                    ahora.plus(DURACION_BLOQUEO);

            usuario.setBloqueadoHasta(hasta);

            usuarioRepository.save(usuario);

            return new ResultadoIntento(
                    Estado.CUENTA_BLOQUEADA,
                    0,
                    hasta
            );
        }

        usuarioRepository.save(usuario);

        return new ResultadoIntento(
                Estado.INTENTO_REGISTRADO,
                MAX_INTENTOS - intentos,
                null
        );
    }
}