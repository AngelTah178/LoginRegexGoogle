package com.proyecto.login.config;

import com.proyecto.login.entity.Usuario;
import com.proyecto.login.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "app.seed.enabled",
    havingValue = "true"
)
public class UsuarioInicializador implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public UsuarioInicializador(
            UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {

        String usuario = System.getenv("LOGIN_SEED_USUARIO");
        String clave = System.getenv("LOGIN_SEED_CLAVE");
        String correo = System.getenv("LOGIN_SEED_CORREO");

        if (usuario == null || usuario.isBlank()
                || clave == null || clave.isBlank()
                || correo == null || correo.isBlank()) {

            throw new IllegalArgumentException(
                "Faltan datos para registrar el usuario."
            );
        }

        // Evitar duplicados
        if (usuarioRepository.existsByUsuario(usuario)
                || usuarioRepository.existsByCorreo(correo)) {

            System.out.println(
                "El usuario o correo ya esta registrado."
            );
            return;
        }

        // Generar hash BCrypt
        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder(10);

        String claveHash = encoder.encode(clave);

        // Crear entidad
        Usuario nuevoUsuario = new Usuario();

        nuevoUsuario.setUsuario(usuario);
        nuevoUsuario.setClaveHash(claveHash);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setIntentosFallidos(0);
        nuevoUsuario.setBloqueadoHasta(null);

        // Guardar en SQL Server
        Usuario guardado =
                usuarioRepository.saveAndFlush(nuevoUsuario);

        // Verificar el hash antes de finalizar
        boolean claveCorrecta =
                encoder.matches(clave, guardado.getClaveHash());

        System.out.println(
            "=================================="
        );

        System.out.println(
            "USUARIO REGISTRADO CORRECTAMENTE"
        );

        System.out.println(
            "ID: " + guardado.getId()
        );

        System.out.println(
            "Usuario: " + guardado.getUsuario()
        );

        System.out.println(
            "Correo: " + guardado.getCorreo()
        );

        System.out.println(
            "Hash BCrypt valido: " + claveCorrecta
        );

        System.out.println(
            "=================================="
        );
    }
}