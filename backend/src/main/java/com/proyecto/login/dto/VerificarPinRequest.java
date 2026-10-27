package com.proyecto.login.dto;

import java.util.UUID;

public record VerificarPinRequest(
        UUID solicitudPinId,
        String pin
) {}