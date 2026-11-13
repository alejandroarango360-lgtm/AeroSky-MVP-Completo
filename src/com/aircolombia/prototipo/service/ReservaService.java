package com.aircolombia.prototipo.service;

import com.aircolombia.prototipo.dao.*;
import com.aircolombia.prototipo.model.Pasajero;
import com.aircolombia.prototipo.model.Vuelo;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

public class ReservaService {
    private VueloDAO vueloDAO = new VueloDAO();
    private ReservaDAO reservaDAO = new ReservaDAO();
    private PasajeroDAO pasajeroDAO = new PasajeroDAO();
    private ReservaPasajeroDAO reservaPasajeroDAO = new ReservaPasajeroDAO();
    private IntentoPagoDAO intentoPagoDAO = new IntentoPagoDAO();

    public static final int MAX_INTENTOS_VERIFICACION = 3;
    public static final int MINUTOS_BLOQUEO           = 15;
    public static final int VIGENCIA_CODIGO_SEG       = 600;
    public static final int COOLDOWN_REENVIO_SEG      = 30;

    public static final String CLASE_ECONOMICA = "Economica";
    public static final String CLASE_EJECUTIVA = "Ejecutiva";
    public static final String CLASE_PRIMERA = "Primera Clase";

    public static double obtenerMultiplicador(String clase) {
        if (CLASE_EJECUTIVA.equals(clase)) return 1.6;
        if (CLASE_PRIMERA.equals(clase)) return 2.5;
        return 1.0;
    }

    public static final double PENALIDAD_ECONOMICA = 0.30;
    public static final double PENALIDAD_EJECUTIVA = 0.10;
    public static final double PENALIDAD_PRIMERA   = 0.00;

    public static double obtenerPenalidadCancelacion(String clase) {
        if (CLASE_PRIMERA.equals(clase))   return PENALIDAD_PRIMERA;
        if (CLASE_EJECUTIVA.equals(clase)) return PENALIDAD_EJECUTIVA;
        return PENALIDAD_ECONOMICA;
    }

    public static final double FACTOR_ADULTO = 1.00;
    public static final double FACTOR_NINO   = 0.75;
    public static final double FACTOR_INFANTE = 0.10;

    public static class ResultadoVerificacion {
        public enum Estado { EXITO, CODIGO_INVALIDO, CODIGO_EXPIRADO, BLOQUEADA, INTENTO_EN_BLOQUEADA, ERROR }
        public final Estado estado;
        public final String mensaje;
        public final int intentosRestantes;
        public final Timestamp bloqueadoHasta;

        public ResultadoVerificacion(Estado estado, String mensaje, int intentosRestantes, Timestamp bloqueadoHasta) {
            this.estado = estado;
            this.mensaje = mensaje;
            this.intentosRestantes = intentosRestantes;
            this.bloqueadoHasta = bloqueadoHasta;
        }
        public boolean esExito() { return estado == Estado.EXITO; }
    }

    public static class BloqueoInfoView {
        public final int intentosVerificacion;
        public final Timestamp codigoEnviadoEn;
        public final Timestamp bloqueadoHasta;
        public BloqueoInfoView(int i, Timestamp c, Timestamp b) {
            this.intentosVerificacion = i; this.codigoEnviadoEn = c; this.bloqueadoHasta = b;
        }
    }

    private String generarPNR() {
        String pnr;
        String caracteresPermitidos = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        do {
            StringBuilder sb = new StringBuilder("AV");
            for (int i = 0; i < 4; i++) {
                sb.append(caracteresPermitidos.charAt(random.nextInt(caracteresPermitidos.length())));
            }
            pnr = sb.toString();
        } while (reservaDAO.buscarPorPNR(pnr) != null);
        return pnr;
    }

    public double calcularTotal(int idVueloIda, Integer idVueloRegreso, String claseViaje, int adultos, int ninos, int infantes) {
        Vuelo ida = vueloDAO.buscarPorId(idVueloIda);
        if (ida == null) return 0;
        double factorClase = obtenerMultiplicador(claseViaje);
        double factorPasajeros = adultos * FACTOR_ADULTO + ninos * FACTOR_NINO + infantes * FACTOR_INFANTE;
        double total = ida.getTarifaBase() * factorClase * factorPasajeros;
        if (idVueloRegreso != null) {
            Vuelo regreso = vueloDAO.buscarPorId(idVueloRegreso);
            if (regreso != null) total += regreso.getTarifaBase() * factorClase * factorPasajeros;
        }
        return total;
    }

    public double calcularTotal(int idVueloIda, Integer idVueloRegreso, String claseViaje) {
        return calcularTotal(idVueloIda, idVueloRegreso, claseViaje, 1, 0, 0);
    }

    public String iniciarReserva(int idVueloIda, Integer idVueloRegreso, String claseViaje, int adultos, int ninos, int infantes) {
        int asientos = adultos + ninos;
        if (!vueloDAO.actualizarAsientos(idVueloIda, asientos)) return null;
        if (idVueloRegreso != null) {
            if (!vueloDAO.actualizarAsientos(idVueloRegreso, asientos)) {
                vueloDAO.devolverAsiento(idVueloIda, asientos);
                return null;
            }
        }
        double total = calcularTotal(idVueloIda, idVueloRegreso, claseViaje, adultos, ninos, infantes);
        if (total <= 0) {
            vueloDAO.devolverAsiento(idVueloIda, asientos);
            if (idVueloRegreso != null) vueloDAO.devolverAsiento(idVueloRegreso, asientos);
            return null;
        }
        String pnr = generarPNR();
        String tipoViaje = (idVueloRegreso == null) ? "Solo_Ida" : "Ida_Y_Vuelta";
        if (!reservaDAO.crearReservaPendiente(pnr, idVueloIda, idVueloRegreso, total, claseViaje, tipoViaje, asientos)) {
            vueloDAO.devolverAsiento(idVueloIda, asientos);
            if (idVueloRegreso != null) vueloDAO.devolverAsiento(idVueloRegreso, asientos);
            return null;
        }
        reservaDAO.auditarEvento(pnr, "RESERVA_INICIADA", "OK", "Reserva creada. Total: " + total + " | Asientos: " + asientos);
        return pnr;
    }

    public String iniciarReserva(int idVueloIda, Integer idVueloRegreso, String claseViaje) {
        return iniciarReserva(idVueloIda, idVueloRegreso, claseViaje, 1, 0, 0);
    }

    public Object[] procesarPagoConPasajeros(String pnr, List<Pasajero> pasajeros, Pasajero comprador, String metodo, String tarjeta) {
        if (pasajeros == null || pasajeros.isEmpty()) return new Object[]{false, "Debe registrar al menos un pasajero.", 0.0};

        ReservaDAO.BloqueoInfo bInfo = reservaDAO.obtenerEstadoBloqueo(pnr);
        if (bInfo != null && bInfo.bloqueadoHasta != null && bInfo.bloqueadoHasta.after(new Timestamp(System.currentTimeMillis()))) {
            reservaDAO.auditarEvento(pnr, "PAGO_RECHAZADO", "RECHAZADO", "Intento de pago en reserva bloqueada hasta " + bInfo.bloqueadoHasta);
            return new Object[]{false, "La reserva está bloqueada temporalmente por seguridad.", 0.0};
        }

        double total = reservaDAO.obtenerTotalReserva(pnr);
        boolean aprobado = simularPasarela(metodo, tarjeta);
        String motivo = aprobado ? "Transacción aprobada por la pasarela" : obtenerMotivoRechazo(metodo, tarjeta);
        intentoPagoDAO.registrar(pnr, total, metodo, (tarjeta == null || tarjeta.isEmpty()) ? "N/A" : tarjeta, aprobado ? "Aprobado" : "Rechazado", motivo);

        if (!aprobado) {
            reservaDAO.auditarEvento(pnr, "PAGO_RECHAZADO", "RECHAZADO", motivo);
            return new Object[]{false, motivo, total};
        }
        if (!reservaDAO.cambiarEstado(pnr, "Confirmada")) {
            reservaDAO.auditarEvento(pnr, "ERROR_CONFIRMACION", "ERROR", "No se pudo cambiar el estado a Confirmada.");
            return new Object[]{false, "Error interno al confirmar la reserva.", total};
        }

        reservaDAO.auditarEvento(pnr, "PAGO_APROBADO", "OK", "Total: " + total + " | Método: " + metodo);

        int idComprador = comprador != null ? pasajeroDAO.obtenerOCrear(comprador) : pasajeroDAO.obtenerOCrear(pasajeros.get(0));
        if (idComprador == -1) return new Object[]{false, "Error al registrar los datos del comprador.", total};
        reservaDAO.asociarComprador(pnr, idComprador);

        int index = 0;
        for (Pasajero p : pasajeros) {
            int idPasajero = pasajeroDAO.obtenerOCrear(p);
            if (idPasajero == -1) return new Object[]{false, "Error al registrar el pasajero " + p.getNombreCompleto(), total};
            reservaPasajeroDAO.agregar(pnr, idPasajero, index == 0);
            index++;
        }

        reservaDAO.limpiarToken(pnr);
        reservaDAO.auditarEvento(pnr, "TOKEN_CONSUMIDO", "OK", "Token invalidado tras pago exitoso.");

        // RF-20: Enviar correo de emisión de tiquete
        try {
            String emailDestino = (comprador != null) ? comprador.getEmail() : pasajeros.get(0).getEmail();
            Object[] info = reservaDAO.obtenerInfoReserva(pnr);
            if (info != null && emailDestino != null) {
                boolean enviado = com.aircolombia.prototipo.utils.EmailService.enviarCorreoTiquete(
                        emailDestino, pnr, (String) info[1], (String) info[2], (String) info[3], total);
                if (enviado) reservaDAO.auditarEvento(pnr, "TIQUETE_ENVIADO", "OK", "Correo enviado a " + emailDestino);
                else reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_TIQUETE", "ERROR", "Fallo al enviar tiquete a " + emailDestino);
            }
        } catch (Exception e) {
            reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_TIQUETE", "ERROR", e.getMessage());
        }

        return new Object[]{true, "Pago aprobado. Reserva confirmada con " + pasajeros.size() + " pasajero(s).", total};
    }

    public Object[] procesarPago(String pnr, String nombre, String numeroDoc, String correo, String metodo, String tarjeta) {
        java.util.List<Pasajero> lista = new java.util.ArrayList<>();
        lista.add(new Pasajero("CC", numeroDoc, nombre, correo, null));
        Object[] r = procesarPagoConPasajeros(pnr, lista, null, metodo, tarjeta);
        return new Object[]{r[0], r[1]};
    }

    public void expirarReserva(String pnr) {
        Object[] info = reservaDAO.buscarPorPNR(pnr);
        if (info != null) {
            int idVuelo = ((Number) info[0]).intValue();
            Integer idRegreso = (info[1] == null) ? null : ((Number) info[1]).intValue();
            int asientos = obtenerAsientos(info);
            reservaDAO.cambiarEstado(pnr, "Expirada");
            vueloDAO.devolverAsiento(idVuelo, asientos);
            if (idRegreso != null) vueloDAO.devolverAsiento(idRegreso, asientos);
            reservaDAO.auditarEvento(pnr, "RESERVA_EXPIRADA_MANUAL", "OK", "Reserva expirada manualmente.");
        }
    }

    public int limpiarReservasHuerfanas(int minutos) {
        List<Object[]> pendientes = reservaDAO.obtenerReservasPendientesAntiguas(minutos);
        int count = 0;
        for (Object[] r : pendientes) {
            String pnr = (String) r[0];
            int idVuelo = ((Number) r[1]).intValue();
            Integer idRegreso = (r[2] == null) ? null : ((Number) r[2]).intValue();
            int asientos = ((Number) r[3]).intValue();
            if (reservaDAO.cambiarEstado(pnr, "Expirada")) {
                vueloDAO.devolverAsiento(idVuelo, asientos);
                if (idRegreso != null) vueloDAO.devolverAsiento(idRegreso, asientos);
                reservaDAO.auditarEvento(pnr, "RESERVA_EXPIRADA_AUTO", "OK", "Limpieza automática > " + minutos + " min.");
                count++;
            }
        }
        return count;
    }

    public Object[] cancelarTiqueteConDetalle(String pnr) {
        Object[] info = reservaDAO.buscarPorPNR(pnr);
        if (info == null) return new Object[]{false, "No existe una reserva con el PNR " + pnr, 0.0, 0.0, 0.0};
        int idVuelo = ((Number) info[0]).intValue();
        Integer idRegreso = (info[1] == null) ? null : ((Number) info[1]).intValue();
        String estado = (String) info[2];
        int asientos = obtenerAsientos(info);

        if ("Cancelada".equals(estado)) return new Object[]{false, "La reserva ya fue cancelada.", 0.0, 0.0, 0.0};
        if ("Expirada".equals(estado)) return new Object[]{false, "La reserva ya expiró.", 0.0, 0.0, 0.0};
        if (!"Confirmada".equals(estado)) return new Object[]{false, "Solo se pueden cancelar confirmadas.", 0.0, 0.0, 0.0};

        double total = 0.0;
        String clase = CLASE_ECONOMICA;
        Object[] detalle = reservaDAO.obtenerInfoReserva(pnr);
        if (detalle != null) {
            total = (Double) detalle[4];
            String c = (String) detalle[7];
            if (c != null) clase = c;
        }

        double factorPenalidad = obtenerPenalidadCancelacion(clase);
        double penalidad = total * factorPenalidad;
        double reembolso = total - penalidad;

        if (!reservaDAO.cambiarEstado(pnr, "Cancelada")) return new Object[]{false, "No se pudo actualizar el estado.", 0.0, 0.0, 0.0};

        vueloDAO.devolverAsiento(idVuelo, asientos);
        if (idRegreso != null) vueloDAO.devolverAsiento(idRegreso, asientos);

        reservaDAO.auditarEvento(pnr, "RESERVA_CANCELADA", "OK", "Cancelación. Total: " + total + " | Penalidad: " + penalidad + " | Reembolso: " + reembolso);

        int pct = (int) Math.round(factorPenalidad * 100);
        String mensaje = "Reserva " + pnr + " cancelada.\nCupos devueltos: " + asientos + "\n\nTotal: $ " + String.format("%,.0f", total) + "\nPenalidad (" + pct + "%): -$ " + String.format("%,.0f", penalidad) + "\nReembolso: $ " + String.format("%,.0f", reembolso);
        return new Object[]{true, mensaje, total, penalidad, reembolso};
    }

    public String cancelarTiquete(String pnr) {
        Object[] r = cancelarTiqueteConDetalle(pnr);
        return ((Boolean) r[0] ? "OK: " : "ERROR: ") + r[1];
    }

    private int obtenerAsientos(Object[] info) {
        if (info.length >= 4 && info[3] != null) return ((Number) info[3]).intValue();
        return 1;
    }

    private boolean simularPasarela(String metodo, String numeroTarjeta) {
        if ("PSE".equals(metodo)) return true;
        if (numeroTarjeta == null || numeroTarjeta.isEmpty()) return false;
        return numeroTarjeta.charAt(numeroTarjeta.length() - 1) != '0';
    }

    private String obtenerMotivoRechazo(String metodo, String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.isEmpty()) return "Número de tarjeta no proporcionado.";
        if (numeroTarjeta.length() < 16) return "El número de tarjeta debe tener 16 dígitos.";
        if (numeroTarjeta.charAt(numeroTarjeta.length() - 1) == '0') return "Fondos insuficientes o tarjeta bloqueada (código 51).";
        return "Transacción rechazada por la pasarela.";
    }

    public boolean enviarCorreoConfirmacion(String pnr, String emailDestinatario) {
        String token = String.format("%06d", new java.util.Random().nextInt(999999));
        if (!reservaDAO.guardarToken(pnr, token)) {
            reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CODIGO", "ERROR", "No se pudo guardar el token en BD.");
            return false;
        }
        boolean enviado = com.aircolombia.prototipo.utils.EmailService.enviarCodigoConfirmacion(emailDestinatario, pnr, token);
        if (enviado) reservaDAO.auditarEvento(pnr, "CODIGO_ENVIADO", "OK", "Correo enviado a " + emailDestinatario);
        else reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CODIGO", "ERROR", "JavaMail retornó false para " + emailDestinatario);
        return enviado;
    }

    public ResultadoVerificacion verificarCodigo(String pnr, String codigoIngresado, String emailDestinatario) {
        ReservaDAO.BloqueoInfo info = reservaDAO.obtenerEstadoBloqueo(pnr);
        if (info == null) return new ResultadoVerificacion(ResultadoVerificacion.Estado.ERROR, "Reserva no encontrada.", 0, null);

        Timestamp ahora = new Timestamp(System.currentTimeMillis());

        if (info.bloqueadoHasta != null && info.bloqueadoHasta.before(ahora)) {
            reservaDAO.limpiarBloqueoExpirado(pnr);
            reservaDAO.auditarEvento(pnr, "DESBLOQUEO_AUTOMATICO", "OK", "El bloqueo temporal expiró y fue levantado.");
            info = reservaDAO.obtenerEstadoBloqueo(pnr);
        }

        if (info.bloqueadoHasta != null && info.bloqueadoHasta.after(ahora)) {
            reservaDAO.auditarEvento(pnr, "INTENTO_EN_RESERVA_BLOQUEADA", "RECHAZADO", "Intento en reserva bloqueada hasta " + info.bloqueadoHasta);
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.BLOQUEADA, "La reserva está bloqueada temporalmente por seguridad.", 0, info.bloqueadoHasta);
        }

        if (info.codigoEnviadoEn != null) {
            long segTranscurridos = (ahora.getTime() - info.codigoEnviadoEn.getTime()) / 1000;
            if (segTranscurridos > VIGENCIA_CODIGO_SEG) {
                Timestamp hasta = new Timestamp(System.currentTimeMillis() + MINUTOS_BLOQUEO * 60_000L);
                reservaDAO.bloquearReserva(pnr, "CODIGO_EXPIRADO", hasta);
                reservaDAO.auditarEvento(pnr, "TOKEN_EXPIRADO", "BLOQUEADO", "Código expirado tras " + segTranscurridos + "s. Reserva bloqueada.");
                notificarBloqueo(pnr, "CODIGO_EXPIRADO", hasta, emailDestinatario);
                return new ResultadoVerificacion(ResultadoVerificacion.Estado.CODIGO_EXPIRADO, "El código expiró. La reserva se bloqueó por seguridad.", 0, hasta);
            }
        }

        if (reservaDAO.verificarToken(pnr, codigoIngresado)) {
            reservaDAO.reiniciarIntentos(pnr);
            reservaDAO.auditarEvento(pnr, "VERIFICACION_EXITOSA", "OK", "Código verificado correctamente.");
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.EXITO, "Código verificado correctamente.", MAX_INTENTOS_VERIFICACION, null);
        }

        int intentos = reservaDAO.registrarIntentoFallido(pnr, MAX_INTENTOS_VERIFICACION, MINUTOS_BLOQUEO);
        if (intentos < 0) {
            ReservaDAO.BloqueoInfo post = reservaDAO.obtenerEstadoBloqueo(pnr);
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.BLOQUEADA, "La reserva está bloqueada temporalmente.", 0, post != null ? post.bloqueadoHasta : null);
        }

        reservaDAO.auditarEvento(pnr, "VERIFICACION_FALLIDA", "RECHAZADO", "Intento fallido " + intentos + " de " + MAX_INTENTOS_VERIFICACION);

        if (intentos >= MAX_INTENTOS_VERIFICACION) {
            ReservaDAO.BloqueoInfo post = reservaDAO.obtenerEstadoBloqueo(pnr);
            Timestamp hasta = (post != null && post.bloqueadoHasta != null) ? post.bloqueadoHasta : new Timestamp(System.currentTimeMillis() + MINUTOS_BLOQUEO * 60_000L);
            reservaDAO.auditarEvento(pnr, "RESERVA_BLOQUEADA", "BLOQUEADO", "Bloqueada por " + MAX_INTENTOS_VERIFICACION + " intentos fallidos.");
            notificarBloqueo(pnr, "MAX_INTENTOS", hasta, emailDestinatario);
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.BLOQUEADA, "Demasiados intentos fallidos. Reserva bloqueada por " + MINUTOS_BLOQUEO + " minutos.", 0, hasta);
        }

        return new ResultadoVerificacion(ResultadoVerificacion.Estado.CODIGO_INVALIDO, "Código incorrecto. Intentos restantes: " + (MAX_INTENTOS_VERIFICACION - intentos), MAX_INTENTOS_VERIFICACION - intentos, null);
    }

    public ResultadoVerificacion reenviarCodigo(String pnr, String email) {
        ReservaDAO.BloqueoInfo info = reservaDAO.obtenerEstadoBloqueo(pnr);
        if (info == null) return new ResultadoVerificacion(ResultadoVerificacion.Estado.ERROR, "Reserva no encontrada.", 0, null);
        Timestamp ahora = new Timestamp(System.currentTimeMillis());

        if (info.bloqueadoHasta != null && info.bloqueadoHasta.after(ahora)) {
            reservaDAO.auditarEvento(pnr, "INTENTO_EN_RESERVA_BLOQUEADA", "RECHAZADO", "Intento de reenvío en reserva bloqueada.");
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.BLOQUEADA, "La reserva está bloqueada. Espera al desbloqueo.", 0, info.bloqueadoHasta);
        }

        if (info.codigoEnviadoEn != null) {
            long seg = (ahora.getTime() - info.codigoEnviadoEn.getTime()) / 1000;
            if (seg < COOLDOWN_REENVIO_SEG) {
                return new ResultadoVerificacion(ResultadoVerificacion.Estado.CODIGO_INVALIDO, "Debes esperar " + (COOLDOWN_REENVIO_SEG - seg) + "s para reenviar el código.", 0, null);
            }
        }

        String token = String.format("%06d", new java.util.Random().nextInt(999999));
        if (!reservaDAO.guardarToken(pnr, token)) {
            reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CODIGO", "ERROR", "No se pudo guardar el token al reenviar.");
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.ERROR, "No se pudo reenviar el código.", 0, null);
        }

        if (com.aircolombia.prototipo.utils.EmailService.enviarCodigoConfirmacion(email, pnr, token)) {
            reservaDAO.auditarEvento(pnr, "CODIGO_REENVIADO", "OK", "Nuevo código enviado a " + email);
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.EXITO, "Nuevo código enviado a tu correo.", MAX_INTENTOS_VERIFICACION, null);
        }
        reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CODIGO", "ERROR", "Fallo al reenviar código a " + email);
        return new ResultadoVerificacion(ResultadoVerificacion.Estado.ERROR, "No se pudo reenviar el código. Intenta más tarde.", 0, null);
    }

    public BloqueoInfoView obtenerEstadoActual(String pnr) {
        ReservaDAO.BloqueoInfo info = reservaDAO.obtenerEstadoBloqueo(pnr);
        return info == null ? null : new BloqueoInfoView(info.intentosVerificacion, info.codigoEnviadoEn, info.bloqueadoHasta);
    }

    private void notificarBloqueo(String pnr, String motivo, Timestamp hasta, String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CORREO_BLOQUEO", "ERROR", "Correo del comprador no disponible.");
            return;
        }
        try {
            if (com.aircolombia.prototipo.utils.EmailService.enviarCorreoBloqueo(correo, pnr, hasta, motivo)) {
                reservaDAO.auditarEvento(pnr, "BLOQUEO_NOTIFICADO", "OK", "Correo de bloqueo enviado a " + correo);
            } else {
                reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CORREO_BLOQUEO", "ERROR", "JavaMail retornó false.");
            }
        } catch (Exception e) {
            reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CORREO_BLOQUEO", "ERROR", e.getMessage());
        }
    }

    public java.util.List<Object[]> listarAuditoria(String pnr, String evento, Timestamp desde, Timestamp hasta, int limite) {
        return reservaDAO.listarAuditoria(pnr, evento, desde, hasta, limite);
    }

    public java.util.List<String> listarEventosAuditoria() {
        return reservaDAO.listarEventosDistintos();
    }

    // ================================================================
    // ============ MÉTODOS NUEVOS (RF-21, RF-22, RF-23, RF-25) =======
    // ================================================================

    public boolean consultarReservaSegura(String pnr, String apellido) {
        if (pnr == null || apellido == null || apellido.trim().isEmpty()) return false;
        return reservaDAO.verificarApellidoTitular(pnr, apellido);
    }

    public int notificarCancelacionVuelo(int idVuelo, String motivo) {
        java.util.List<String> correos = reservaDAO.obtenerCorreosPasajerosPorVuelo(idVuelo);
        int enviados = 0;
        for (String correo : correos) {
            try {
                if (com.aircolombia.prototipo.utils.EmailService.enviarCorreoVueloCancelado(correo, idVuelo, motivo)) enviados++;
            } catch (Exception e) {
                System.out.println("Error al notificar a " + correo + ": " + e.getMessage());
            }
        }
        return enviados;
    }

    public Object[] evaluarCambioVuelo(String pnr, int nuevoIdVuelo) {
        Object[] info = reservaDAO.obtenerInfoReserva(pnr);
        if (info == null) return new Object[]{false, "Reserva no encontrada."};
        
        Object[] rInfo = reservaDAO.buscarPorPNR(pnr);
        int viejoIdVuelo = ((Number) rInfo[0]).intValue();

        double totalViejo = (Double) info[4];
        String clase = (String) info[7];
        int asientos = (Integer) info[9];

        Vuelo nuevoVuelo = vueloDAO.buscarPorId(nuevoIdVuelo);
        if (nuevoVuelo == null) return new Object[]{false, "El nuevo vuelo no existe."};
        if (nuevoVuelo.getIdVuelo() == viejoIdVuelo) return new Object[]{false, "Ya tienes ese vuelo seleccionado."};

        double factorClase = obtenerMultiplicador(clase);
        double nuevoTotal = nuevoVuelo.getTarifaBase() * factorClase * asientos;
        
        double diferenciaPrecio = nuevoTotal - totalViejo;
        double penalidadCambio = 0;
        if (CLASE_ECONOMICA.equals(clase)) penalidadCambio = 100000;
        else if (CLASE_EJECUTIVA.equals(clase)) penalidadCambio = 50000;

        double totalAPagar = diferenciaPrecio + penalidadCambio;
        
        return new Object[]{true, "Evaluación exitosa", viejoIdVuelo, nuevoTotal, diferenciaPrecio, penalidadCambio, totalAPagar, asientos};
    }

    public boolean procesarCambioVuelo(String pnr, int viejoIdVuelo, int nuevoIdVuelo, double nuevoTotal, int asientos) {
        vueloDAO.devolverAsiento(viejoIdVuelo, asientos);
        if (!vueloDAO.actualizarAsientos(nuevoIdVuelo, asientos)) {
            vueloDAO.devolverAsiento(nuevoIdVuelo, asientos);
            return false;
        }
        boolean ok = reservaDAO.actualizarVueloReserva(pnr, nuevoIdVuelo, nuevoTotal);
        if (ok) {
            reservaDAO.auditarEvento(pnr, "VUELO_MODIFICADO", "OK", "Cambio del vuelo " + viejoIdVuelo + " al " + nuevoIdVuelo + ". Nuevo total: " + nuevoTotal);
        }
        return ok;
    }
        // ==================== 2FA CANCELACIÓN ====================
    public Object[] solicitarCodigoCancelacion(String pnr) {
        Object[] info = reservaDAO.buscarPorPNR(pnr);
        if (info == null) return new Object[]{false, "No existe la reserva."};
        
        String estado = (String) info[2];
        if (!"Confirmada".equals(estado)) return new Object[]{false, "Solo se pueden cancelar reservas confirmadas."};

        Object[] detalle = reservaDAO.obtenerInfoReserva(pnr);
        if (detalle == null) return new Object[]{false, "No se encontró el correo del pasajero."};
        
        String correo = (String) detalle[6]; // El correo está en la posición 6
        if (correo == null || correo.isEmpty()) return new Object[]{false, "El pasajero no tiene correo registrado."};

        // Generar código de 6 dígitos y guardarlo
        String token = String.format("%06d", new java.util.Random().nextInt(999999));
        if (!reservaDAO.guardarTokenCancelacion(pnr, token)) {
            return new Object[]{false, "Error interno al generar el código."};
        }

        // Enviar correo
        boolean enviado = com.aircolombia.prototipo.utils.EmailService.enviarCodigoCancelacion(correo, pnr, token);
        if (enviado) {
            reservaDAO.auditarEvento(pnr, "CANCELACION_2FA_SOLICITADA", "OK", "Código enviado a " + correo);
            return new Object[]{true, "Código enviado a " + correo};
        } else {
            return new Object[]{false, "Error al enviar el correo. Intenta de nuevo."};
        }
    }

    public Object[] confirmarCancelacion(String pnr, String token) {
        // 1. Validar el token
        if (!reservaDAO.verificarTokenCancelacion(pnr, token)) {
            reservaDAO.auditarEvento(pnr, "CANCELACION_2FA_FALLIDA", "RECHAZADO", "Token incorrecto.");
            return new Object[]{false, "El código ingresado es incorrecto.", 0.0, 0.0, 0.0};
        }

        // 2. Limpiar token para que no se re-use
        reservaDAO.limpiarTokenCancelacion(pnr);

        // 3. Ejecutar la cancelación real (usando el método original que ya teníamos)
        return cancelarTiqueteConDetalle(pnr);
    }
}