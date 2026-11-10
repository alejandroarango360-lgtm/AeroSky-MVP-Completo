package com.aircolombia.prototipo.model;

public class Reserva {
    private String codigoPnr;
    private int idVuelo;
    private Integer idVueloRegreso; // null si es solo ida
    private String fechaCreacion;
    private String estadoReserva;
    private String claseViaje;
    private String tipoViaje; // "Solo_Ida" o "Ida_Y_Vuelta"
    private double total;

    public Reserva(String codigoPnr, int idVuelo, Integer idVueloRegreso,
                   String fechaCreacion, String estadoReserva, String claseViaje,
                   String tipoViaje, double total) {
        this.codigoPnr = codigoPnr;
        this.idVuelo = idVuelo;
        this.idVueloRegreso = idVueloRegreso;
        this.fechaCreacion = fechaCreacion;
        this.estadoReserva = estadoReserva;
        this.claseViaje = claseViaje;
        this.tipoViaje = tipoViaje;
        this.total = total;
    }

    public String getCodigoPnr() { return codigoPnr; }
    public int getIdVuelo() { return idVuelo; }
    public Integer getIdVueloRegreso() { return idVueloRegreso; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getEstadoReserva() { return estadoReserva; }
    public String getClaseViaje() { return claseViaje; }
    public String getTipoViaje() { return tipoViaje; }
    public double getTotal() { return total; }
}