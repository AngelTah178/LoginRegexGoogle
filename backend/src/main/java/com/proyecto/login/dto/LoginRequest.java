package com.proyecto.login.dto;

public record LoginRequest(
    String usuario,
    String clave,
    String correo
) {}