package com.aircolombia.prototipo.model;

public class Pasajero {
    private int idPasajero;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String direccion;
    private String ciudadResidencia;

    // Constructor completo (para RF-06)
    public Pasajero(int idPasajero, String tipoDocumento, String numeroDocumento,
                    String nombreCompleto, String email, String telefono,
                    String direccion, String ciudadResidencia) {
        this.idPasajero = idPasajero;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ciudadResidencia = ciudadResidencia;
    }

    // Constructor sin ID (para inserciones)
    public Pasajero(String tipoDocumento, String numeroDocumento,
                    String nombreCompleto, String email, String telefono,
                    String direccion, String ciudadResidencia) {
        this(0, tipoDocumento, numeroDocumento, nombreCompleto, email,
             telefono, direccion, ciudadResidencia);
    }

    // Constructor legacy (compatibilidad con código anterior)
    public Pasajero(String tipoDocumento, String numeroDocumento,
                    String nombreCompleto, String email, String telefono) {
        this(0, tipoDocumento, numeroDocumento, nombreCompleto, email,
             telefono, null, null);
    }

    public int getIdPasajero() { return idPasajero; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getCiudadResidencia() { return ciudadResidencia; }
}