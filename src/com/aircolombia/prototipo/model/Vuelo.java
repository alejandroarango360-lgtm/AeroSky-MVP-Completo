package com.aircolombia.prototipo.model;

public class Vuelo {
    private int idVuelo;
    private String numeroVuelo;
    private String origenIata;
    private String destinoIata;
    private String origenCiudad;
    private String destinoCiudad;
    private int idAeronave;
    private String matriculaAeronave;
    private int capacidadAeronave;
    private String fechaSalida;
    private String fechaLlegada;
    private String estadoVuelo;
    private int escalas;
    private double tarifaBase;
    private int cuposDisponibles;

    public Vuelo(int idVuelo, String numeroVuelo, String origenIata, String destinoIata,
                 String origenCiudad, String destinoCiudad,
                 int idAeronave, String matriculaAeronave, int capacidadAeronave,
                 String fechaSalida, String fechaLlegada, String estadoVuelo,
                 int escalas, double tarifaBase, int cuposDisponibles) {
        this.idVuelo = idVuelo;
        this.numeroVuelo = numeroVuelo;
        this.origenIata = origenIata;
        this.destinoIata = destinoIata;
        this.origenCiudad = origenCiudad;
        this.destinoCiudad = destinoCiudad;
        this.idAeronave = idAeronave;
        this.matriculaAeronave = matriculaAeronave;
        this.capacidadAeronave = capacidadAeronave;
        this.fechaSalida = fechaSalida;
        this.fechaLlegada = fechaLlegada;
        this.estadoVuelo = estadoVuelo;
        this.escalas = escalas;
        this.tarifaBase = tarifaBase;
        this.cuposDisponibles = cuposDisponibles;
    }

    // Constructor simplificado para inserciones
    public Vuelo(String numeroVuelo, String origenIata, String destinoIata, int idAeronave,
                 String fechaSalida, String fechaLlegada, String estadoVuelo,
                 int escalas, double tarifaBase, int cuposDisponibles) {
        this(0, numeroVuelo, origenIata, destinoIata, origenIata, destinoIata,
              idAeronave, "", 0, fechaSalida, fechaLlegada, estadoVuelo,
              escalas, tarifaBase, cuposDisponibles);
    }

    public int getIdVuelo() { return idVuelo; }
    public String getNumeroVuelo() { return numeroVuelo; }
    public String getOrigenIata() { return origenIata; }
    public String getDestinoIata() { return destinoIata; }
    public String getOrigenCiudad() { return origenCiudad; }
    public String getDestinoCiudad() { return destinoCiudad; }
    public int getIdAeronave() { return idAeronave; }
    public String getMatriculaAeronave() { return matriculaAeronave; }
    public int getCapacidadAeronave() { return capacidadAeronave; }
    public String getFechaSalida() { return fechaSalida; }
    public String getFechaLlegada() { return fechaLlegada; }
    public String getEstadoVuelo() { return estadoVuelo; }
    public int getEscalas() { return escalas; }
    public double getTarifaBase() { return tarifaBase; }
    public int getCuposDisponibles() { return cuposDisponibles; }

    // ---------- UTILIDADES ----------

    // Duración del vuelo en formato "Xh Ym"
    public String getDuracion() {
        try {
            java.time.LocalDateTime salida = java.time.LocalDateTime.parse(
                    fechaSalida.replace(" ", "T"));
            java.time.LocalDateTime llegada = java.time.LocalDateTime.parse(
                    fechaLlegada.replace(" ", "T"));
            long minutos = java.time.Duration.between(salida, llegada).toMinutes();
            long h = minutos / 60;
            long m = minutos % 60;
            return h + "h " + m + "m";
        } catch (Exception e) {
            return "N/D";
        }
    }

    // Texto de escalas
    public String getTextoEscalas() {
        if (escalas == 0) return "Directo";
        return escalas + (escalas == 1 ? " escala" : " escalas");
    }

    @Override
    public String toString() {
        return numeroVuelo + " | " + origenCiudad + " → " + destinoCiudad
                + " | " + fechaSalida + " | $" + tarifaBase;
    }
    // ---------- UTILIDADES PARA ORDENAMIENTO (RF-04) ----------

// Duración del vuelo en minutos (para ordenar numéricamente)
public long getDuracionMinutos() {
    try {
        java.time.LocalDateTime salida = java.time.LocalDateTime.parse(
                fechaSalida.replace(" ", "T"));
        java.time.LocalDateTime llegada = java.time.LocalDateTime.parse(
                fechaLlegada.replace(" ", "T"));
        return java.time.Duration.between(salida, llegada).toMinutes();
    } catch (Exception e) {
        return Long.MAX_VALUE;
    }
}

// Hora de salida (0-23) para ordenar / filtrar por franja horaria
public int getHoraSalida() {
    try {
        java.time.LocalDateTime salida = java.time.LocalDateTime.parse(
                fechaSalida.replace(" ", "T"));
        return salida.getHour();
    } catch (Exception e) {
        return -1;
    }
}
}