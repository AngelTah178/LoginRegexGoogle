package com.proyecto.login.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {

    private final JavaMailSender mailSender;
    private final String remitente;

    public CorreoService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String remitente) {

        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    public void enviarPin(String destinatario, String pin) {

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);

        mensaje.setSubject(
                "LoginRegexGoogle - Codigo de verificacion"
        );

        mensaje.setText(
                "Hola,\n\n" +
                "Recibimos una solicitud de inicio de sesion.\n\n" +
                "Tu codigo de verificacion es: " + pin + "\n\n" +
                "El codigo tiene una vigencia de 5 minutos.\n\n" +
                "Si no solicitaste este codigo, ignora el mensaje.\n\n" +
                "LoginRegexGoogle"
        );

        mailSender.send(mensaje);
    }
}