package com.aircolombia.prototipo.model;

import java.sql.Timestamp;

public class Usuario {
    private int idUsuario;
    private String username;
    private String passwordHash;
    private String rol;
    private String email;
    private String estado;
    private int intentosFallidos;
    private Timestamp bloqueadoHasta;

    public Usuario() {}

    // Getters y Setters
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public Timestamp getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(Timestamp bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }

    public boolean estaBloqueado() {
        return "BLOQUEADO".equals(estado) && bloqueadoHasta != null && bloqueadoHasta.after(new Timestamp(System.currentTimeMillis()));
    }
}