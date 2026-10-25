package com.proyecto.login.service;

import com.proyecto.login.entity.Usuario;
import com.proyecto.login.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IntentosServiceTest {

    private UsuarioRepository repository;
    private Usuario usuario;
    private IntentosService service;

    private final Clock reloj = Clock.fixed(
            Instant.parse("2026-10-25T15:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void preparar() {

        repository = mock(UsuarioRepository.class);

        usuario = new Usuario();
        usuario.setUsuario("Alumno01");
        usuario.setIntentosFallidos(0);

        when(repository.buscarParaActualizar("Alumno01"))
                .thenReturn(Optional.of(usuario));

        service = new IntentosService(repository, reloj);
    }

    @Test
    void primerErrorDejaCuatroIntentos() {

        var resultado = service.registrarFallo("Alumno01");

        assertEquals(4, resultado.intentosRestantes());
        assertEquals(1, usuario.getIntentosFallidos());

        verify(repository).save(usuario);
    }

    @Test
    void cuatroErroresDejanUnIntento() {

        for (int i = 0; i < 4; i++) {
            service.registrarFallo("Alumno01");
        }

        assertEquals(4, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
    }

    @Test
    void quintoErrorBloqueaCuenta() {

        IntentosService.ResultadoIntento resultado = null;

        for (int i = 0; i < 5; i++) {
            resultado = service.registrarFallo("Alumno01");
        }

        assertNotNull(resultado);

        assertEquals(
                IntentosService.Estado.CUENTA_BLOQUEADA,
                resultado.estado()
        );

        assertEquals(5, usuario.getIntentosFallidos());

        assertEquals(
                LocalDateTime.of(2026, 10, 25, 15, 15),
                usuario.getBloqueadoHasta()
        );
    }

    @Test
    void bloqueoVigenteNoIncrementaContador() {

        for (int i = 0; i < 5; i++) {
            service.registrarFallo("Alumno01");
        }

        var resultado = service.registrarFallo("Alumno01");

        assertEquals(
                IntentosService.Estado.CUENTA_BLOQUEADA,
                resultado.estado()
        );

        assertEquals(5, usuario.getIntentosFallidos());

        verify(repository, times(5)).save(usuario);
    }

    @Test
    void bloqueoVencidoPermiteNuevoIntento() {

        usuario.setIntentosFallidos(5);

        usuario.setBloqueadoHasta(
                LocalDateTime.of(2026, 10, 25, 14, 45)
        );

        var resultado = service.registrarFallo("Alumno01");

        assertEquals(
                IntentosService.Estado.INTENTO_REGISTRADO,
                resultado.estado()
        );

        assertEquals(1, usuario.getIntentosFallidos());
        assertEquals(4, resultado.intentosRestantes());
        assertNull(usuario.getBloqueadoHasta());
    }

    @Test
    void usuarioInexistenteNoModificaLaBase() {

        when(repository.buscarParaActualizar("Desconocido"))
                .thenReturn(Optional.empty());

        var resultado = service.registrarFallo("Desconocido");

        assertEquals(
                IntentosService.Estado.USUARIO_NO_ENCONTRADO,
                resultado.estado()
        );

        verify(repository, never()).save(any(Usuario.class));
    }
}