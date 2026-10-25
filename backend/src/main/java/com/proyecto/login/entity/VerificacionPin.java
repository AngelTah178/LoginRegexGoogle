package com.proyecto.login.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "VerificacionesPin", schema = "dbo")
public class VerificacionPin {

    @Id
    @Column(name = "Id", nullable = false)
    private UUID id;

    @Column(name = "UsuarioId", nullable = false)
    private Long usuarioId;

    @Column(name = "PinHash", nullable = false, length = 64)
    private String pinHash;

    @Column(name = "FechaCreacionUtc", nullable = false)
    private LocalDateTime fechaCreacionUtc;

    @Column(name = "FechaExpiracionUtc", nullable = false)
    private LocalDateTime fechaExpiracionUtc;

    @Column(name = "IntentosFallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "Usado", nullable = false)
    private boolean usado;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public LocalDateTime getFechaCreacionUtc() {
        return fechaCreacionUtc;
    }

    public void setFechaCreacionUtc(LocalDateTime fechaCreacionUtc) {
        this.fechaCreacionUtc = fechaCreacionUtc;
    }

    public LocalDateTime getFechaExpiracionUtc() {
        return fechaExpiracionUtc;
    }

    public void setFechaExpiracionUtc(LocalDateTime fechaExpiracionUtc) {
        this.fechaExpiracionUtc = fechaExpiracionUtc;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }
}