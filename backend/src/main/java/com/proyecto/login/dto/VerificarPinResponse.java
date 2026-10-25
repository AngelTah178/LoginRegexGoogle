package com.proyecto.login.dto;

public record VerificarPinResponse(
        String estado,
        String mensaje,
        Integer intentosRestantes
) {}