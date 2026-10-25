package com.proyecto.login.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Usuarios", schema = "dbo")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Usuario", nullable = false, unique = true)
    private String usuario;

    @Column(name = "ClaveHash", nullable = false)
    private String claveHash;

    @Column(name = "Correo", nullable = false, unique = true)
    private String correo;

    @Column(name = "IntentosFallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "BloqueadoHasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "FechaRegistro", insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    public Usuario() {
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public void setClaveHash(String claveHash) {
        this.claveHash = claveHash;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}