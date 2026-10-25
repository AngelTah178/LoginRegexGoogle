package com.proyecto.login.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegexServiceTest {

    private final RegexService regex =
        new RegexService();

    @Test
    void usuarioValido() {
        assertTrue(regex.validarUsuario("Alumno01"));
    }

    @Test
    void usuarioNoPuedeIniciarConNumero() {
        assertFalse(regex.validarUsuario("1Alumno"));
    }

    @Test
    void usuarioNoPuedeSerMuyCorto() {
        assertFalse(regex.validarUsuario("Ana"));
    }

    @Test
    void claveValida() {
        assertTrue(regex.validarClave("Alumno12$"));
    }

    @Test
    void claveSinMayuscula() {
        assertFalse(regex.validarClave("alumno12$"));
    }

    @Test
    void claveSinCaracterEspecial() {
        assertFalse(regex.validarClave("Alumno12"));
    }

    @Test
    void claveConUnSoloNumero() {
        assertFalse(regex.validarClave("Alumno1$"));
    }

    @Test
    void correoValido() {
        assertTrue(regex.validarCorreo("alumno@gmail.com"));
    }

    @Test
    void correoInvalido() {
        assertFalse(regex.validarCorreo("alumnogmail.com"));
    }

    @Test
    void entradasNulas() {
        assertFalse(regex.validarUsuario(null));
        assertFalse(regex.validarClave(null));
        assertFalse(regex.validarCorreo(null));
    }
}