package com.aircolombia.prototipo.model;

import java.sql.Timestamp;

public class Usuario {
    private int idUsuario;
    private String username;
    private String rol;
    private String email;
    private int intentosFallidos;
    private Timestamp bloqueadoHasta;

    public Usuario(int idUsuario, String username, String rol, String email, int intentosFallidos, Timestamp bloqueadoHasta) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.rol = rol;
        this.email = email;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getUsername() { return username; }
    public String getRol() { return rol; }
    public String getEmail() { return email; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public Timestamp getBloqueadoHasta() { return bloqueadoHasta; }
    public boolean estaBloqueado() {
        return bloqueadoHasta != null && bloqueadoHasta.after(new Timestamp(System.currentTimeMillis()));
    }
}