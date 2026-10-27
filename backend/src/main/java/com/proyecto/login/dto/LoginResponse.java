package com.proyecto.login.dto;

import java.util.UUID;

public record LoginResponse(
        String estado,
        String mensaje,
        UUID solicitudPinId
) {}