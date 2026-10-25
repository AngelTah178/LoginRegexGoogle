package com.proyecto.login.service;

public class LimitePinException extends RuntimeException {

    public LimitePinException() {
        super("Limite de generacion de PIN alcanzado.");
    }
}