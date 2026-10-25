package com.proyecto.login.service;

import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class RegexService {

    private static final Pattern USUARIO_PATTERN =
        Pattern.compile("^[A-Za-z][A-Za-z0-9_]{4,15}$");

    private static final Pattern CLAVE_PATTERN =
        Pattern.compile( "^(?=.*[A-Z])(?=(?:\\D*\\d){2,})" + "(?=.*[$%&])[A-Za-z\\d$%&]{6,16}$");

    private static final Pattern CORREO_PATTERN =
        Pattern.compile( "^[A-Za-z0-9._%+-]+@" +"[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public boolean validarUsuario(String usuario) {
        return usuario != null &&
            USUARIO_PATTERN.matcher(usuario).matches();
    }

    public boolean validarClave(String clave) {
        return clave != null &&
            CLAVE_PATTERN.matcher(clave).matches();
    }

    public boolean validarCorreo(String correo) {
        return correo != null &&
            CORREO_PATTERN.matcher(correo).matches();
    }
}