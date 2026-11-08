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

    // ==================== CONSTANTES DE SEGURIDAD ====================
    public static final int MAX_INTENTOS_VERIFICACION = 3;
    public static final int MINUTOS_BLOQUEO           = 15;
    public static final int VIGENCIA_CODIGO_SEG       = 600; // 10 minutos
    public static final int COOLDOWN_REENVIO_SEG      = 30;  // 30 segundos

    // Clases
    public static final String CLASE_ECONOMICA = "Economica";
    public static final String CLASE_EJECUTIVA = "Ejecutiva";
    public static final String CLASE_PRIMERA = "Primera Clase";

    public static double obtenerMultiplicador(String clase) {
        if (CLASE_EJECUTIVA.equals(clase)) return 1.6;
        if (CLASE_PRIMERA.equals(clase)) return 2.5;
        return 1.0;
    }

    // RN-07: Penalidades de cancelación por clase
    public static final double PENALIDAD_ECONOMICA = 0.30;
    public static final double PENALIDAD_EJECUTIVA = 0.10;
    public static final double PENALIDAD_PRIMERA   = 0.00;

    public static double obtenerPenalidadCancelacion(String clase) {
        if (CLASE_PRIMERA.equals(clase))   return PENALIDAD_PRIMERA;
        if (CLASE_EJECUTIVA.equals(clase)) return PENALIDAD_EJECUTIVA;
        return PENALIDAD_ECONOMICA;
    }

    // RF-02: multiplicadores por tipo de pasajero
    public static final double FACTOR_ADULTO = 1.00;
    public static final double FACTOR_NINO   = 0.75;
    public static final double FACTOR_INFANTE = 0.10;

    // ==================== CLASES INTERNAS (RESULTADO / VIEW) ====================

    public static class ResultadoVerificacion {
        public enum Estado {
            EXITO, CODIGO_INVALIDO, CODIGO_EXPIRADO,
            BLOQUEADA, INTENTO_EN_BLOQUEADA, ERROR
        }
        public final Estado estado;
        public final String mensaje;
        public final int intentosRestantes;
        public final Timestamp bloqueadoHasta;

        public ResultadoVerificacion(Estado estado, String mensaje,
                                     int intentosRestantes, Timestamp bloqueadoHasta) {
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
            this.intentosVerificacion = i;
            this.codigoEnviadoEn = c;
            this.bloqueadoHasta = b;
        }
    }

    // ==================== GENERAR PNR (RF-08 / CA-10) ====================
    private String generarPNR() {
        String pnr;
        String caracteresPermitidos = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        do {
            StringBuilder sb = new StringBuilder("AV");
            for (int i = 0; i < 4; i++) {
                int index = random.nextInt(caracteresPermitidos.length());
                sb.append(caracteresPermitidos.charAt(index));
            }
            pnr = sb.toString();
        } while (reservaDAO.buscarPorPNR(pnr) != null);
        return pnr;
    }

    // ==================== CALCULAR TOTAL (RF-02) ====================
    public double calcularTotal(int idVueloIda, Integer idVueloRegreso,
                                String claseViaje, int adultos, int ninos, int infantes) {
        Vuelo ida = vueloDAO.buscarPorId(idVueloIda);
        if (ida == null) return 0;

        double factorClase = obtenerMultiplicador(claseViaje);
        double factorPasajeros = adultos * FACTOR_ADULTO
                               + ninos   * FACTOR_NINO
                               + infantes * FACTOR_INFANTE;

        double total = ida.getTarifaBase() * factorClase * factorPasajeros;

        if (idVueloRegreso != null) {
            Vuelo regreso = vueloDAO.buscarPorId(idVueloRegreso);
            if (regreso != null) {
                total += regreso.getTarifaBase() * factorClase * factorPasajeros;
            }
        }
        return total;
    }

    public double calcularTotal(int idVueloIda, Integer idVueloRegreso, String claseViaje) {
        return calcularTotal(idVueloIda, idVueloRegreso, claseViaje, 1, 0, 0);
    }

    // ==================== INICIAR RESERVA (RF-02) ====================
    public String iniciarReserva(int idVueloIda, Integer idVueloRegreso,
                                 String claseViaje, int adultos, int ninos, int infantes) {
        int asientos = adultos + ninos;

        if (!vueloDAO.actualizarAsientos(idVueloIda, asientos)) {
            return null;
        }
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

        if (!reservaDAO.crearReservaPendiente(pnr, idVueloIda, idVueloRegreso,
                total, claseViaje, tipoViaje, asientos)) {
            vueloDAO.devolverAsiento(idVueloIda, asientos);
            if (idVueloRegreso != null) vueloDAO.devolverAsiento(idVueloRegreso, asientos);
            return null;
        }

        reservaDAO.auditarEvento(pnr, "RESERVA_INICIADA", "OK",
                "Reserva creada. Total: " + total + " | Asientos: " + asientos);

        return pnr;
    }

    public String iniciarReserva(int idVueloIda, Integer idVueloRegreso, String claseViaje) {
        return iniciarReserva(idVueloIda, idVueloRegreso, claseViaje, 1, 0, 0);
    }

    // ==================== RF-02 & RF-07: PROCESAR PAGO ====================
    public Object[] procesarPagoConPasajeros(String pnr, List<Pasajero> pasajeros,
                                             Pasajero comprador, String metodo, String tarjeta) {
        if (pasajeros == null || pasajeros.isEmpty()) {
            return new Object[]{false, "Debe registrar al menos un pasajero.", 0.0};
        }

        ReservaDAO.BloqueoInfo bInfo = reservaDAO.obtenerEstadoBloqueo(pnr);
        if (bInfo != null && bInfo.bloqueadoHasta != null
                && bInfo.bloqueadoHasta.after(new Timestamp(System.currentTimeMillis()))) {
            reservaDAO.auditarEvento(pnr, "PAGO_RECHAZADO", "RECHAZADO",
                    "Intento de pago en reserva bloqueada hasta " + bInfo.bloqueadoHasta);
            return new Object[]{false, "La reserva está bloqueada temporalmente por seguridad.", 0.0};
        }

        double total = reservaDAO.obtenerTotalReserva(pnr);
        boolean aprobado = simularPasarela(metodo, tarjeta);
        String motivo = aprobado ? "Transacción aprobada por la pasarela"
                                 : obtenerMotivoRechazo(metodo, tarjeta);

        intentoPagoDAO.registrar(pnr, total, metodo,
                (tarjeta == null || tarjeta.isEmpty()) ? "N/A" : tarjeta,
                aprobado ? "Aprobado" : "Rechazado", motivo);

        if (!aprobado) {
            reservaDAO.auditarEvento(pnr, "PAGO_RECHAZADO", "RECHAZADO", motivo);
            return new Object[]{false, motivo, total};
        }

        if (!reservaDAO.cambiarEstado(pnr, "Confirmada")) {
            reservaDAO.auditarEvento(pnr, "ERROR_CONFIRMACION", "ERROR",
                    "No se pudo cambiar el estado a Confirmada.");
            return new Object[]{false, "Error interno al confirmar la reserva.", total};
        }

        reservaDAO.auditarEvento(pnr, "PAGO_APROBADO", "OK",
                "Total: " + total + " | Método: " + metodo);

        int idComprador = -1;
        if (comprador != null) {
            idComprador = pasajeroDAO.obtenerOCrear(comprador);
        } else {
            idComprador = pasajeroDAO.obtenerOCrear(pasajeros.get(0));
        }

        if (idComprador == -1) {
            return new Object[]{false, "Error al registrar los datos del comprador.", total};
        }
        reservaDAO.asociarComprador(pnr, idComprador);

        int index = 0;
        for (Pasajero p : pasajeros) {
            int idPasajero = pasajeroDAO.obtenerOCrear(p);
            if (idPasajero == -1) {
                return new Object[]{false, "Error al registrar el pasajero " + p.getNombreCompleto(), total};
            }
            reservaPasajeroDAO.agregar(pnr, idPasajero, index == 0);
            index++;
        }

        reservaDAO.limpiarToken(pnr);
        reservaDAO.auditarEvento(pnr, "TOKEN_CONSUMIDO", "OK",
                "Token invalidado tras pago exitoso.");

        return new Object[]{true,
                "Pago aprobado. Reserva confirmada con " + pasajeros.size() + " pasajero(s).",
                total};
    }

    public Object[] procesarPago(String pnr, String nombre, String numeroDoc,
                                 String correo, String metodo, String tarjeta) {
        java.util.List<Pasajero> lista = new java.util.ArrayList<>();
        lista.add(new Pasajero("CC", numeroDoc, nombre, correo, null));
        Object[] r = procesarPagoConPasajeros(pnr, lista, null, metodo, tarjeta);
        return new Object[]{r[0], r[1]};
    }

    // ==================== EXPIRAR RESERVA (manual) ====================
    public void expirarReserva(String pnr) {
        Object[] info = reservaDAO.buscarPorPNR(pnr);
        if (info != null) {
            int idVuelo = ((Number) info[0]).intValue();
            Integer idRegreso = (info[1] == null) ? null : ((Number) info[1]).intValue();
            int asientos = obtenerAsientos(info);

            reservaDAO.cambiarEstado(pnr, "Expirada");
            vueloDAO.devolverAsiento(idVuelo, asientos);
            if (idRegreso != null) vueloDAO.devolverAsiento(idRegreso, asientos);

            reservaDAO.auditarEvento(pnr, "RESERVA_EXPIRADA_MANUAL", "OK",
                    "Reserva expirada manualmente (cancelación del usuario).");
        }
    }

    // ==================== LIMPIAR RESERVAS HUÉRFANAS ====================
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
                reservaDAO.auditarEvento(pnr, "RESERVA_EXPIRADA_AUTO", "OK",
                        "Limpieza automática: reserva pendiente > " + minutos + " min.");
                count++;
            }
        }
        return count;
    }

    // ==================== RN-07: CANCELAR CON PENALIDAD ====================
    public Object[] cancelarTiqueteConDetalle(String pnr) {
        Object[] info = reservaDAO.buscarPorPNR(pnr);
        if (info == null) {
            return new Object[]{false, "No existe una reserva con el PNR " + pnr, 0.0, 0.0, 0.0};
        }
        int idVuelo = ((Number) info[0]).intValue();
        Integer idRegreso = (info[1] == null) ? null : ((Number) info[1]).intValue();
        String estado = (String) info[2];
        int asientos = obtenerAsientos(info);

        if ("Cancelada".equals(estado)) {
            return new Object[]{false, "La reserva con PNR " + pnr + " ya fue cancelada.", 0.0, 0.0, 0.0};
        }
        if ("Expirada".equals(estado)) {
            return new Object[]{false, "La reserva con PNR " + pnr + " ya expiró.", 0.0, 0.0, 0.0};
        }
        if (!"Confirmada".equals(estado)) {
            return new Object[]{false, "Solo se pueden cancelar reservas confirmadas. Estado actual: " + estado, 0.0, 0.0, 0.0};
        }

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

        if (!reservaDAO.cambiarEstado(pnr, "Cancelada")) {
            return new Object[]{false, "No se pudo actualizar el estado de la reserva.", 0.0, 0.0, 0.0};
        }

        vueloDAO.devolverAsiento(idVuelo, asientos);
        if (idRegreso != null) vueloDAO.devolverAsiento(idRegreso, asientos);

        reservaDAO.auditarEvento(pnr, "RESERVA_CANCELADA", "OK",
                "Cancelación con penalidad. Total: " + total
                + " | Penalidad: " + penalidad + " | Reembolso: " + reembolso);

        int pct = (int) Math.round(factorPenalidad * 100);
        String mensaje = "Reserva " + pnr + " cancelada correctamente.\n"
                + "Cupos devueltos: " + asientos + "\n\n"
                + "Total pagado: $ " + String.format("%,.0f", total) + "\n"
                + "Penalidad (" + pct + "%): -$ " + String.format("%,.0f", penalidad) + "\n"
                + "Reembolso: $ " + String.format("%,.0f", reembolso);

        return new Object[]{true, mensaje, total, penalidad, reembolso};
    }

    public String cancelarTiquete(String pnr) {
        Object[] r = cancelarTiqueteConDetalle(pnr);
        boolean exito = (Boolean) r[0];
        String msg = (String) r[1];
        return (exito ? "OK: " : "ERROR: ") + msg;
    }

    // ==================== HELPERS ====================
    private int obtenerAsientos(Object[] info) {
        if (info.length >= 4 && info[3] != null) {
            return ((Number) info[3]).intValue();
        }
        return 1;
    }

    private boolean simularPasarela(String metodo, String numeroTarjeta) {
        if ("PSE".equals(metodo)) return true;
        if (numeroTarjeta == null || numeroTarjeta.isEmpty()) return false;
        return numeroTarjeta.charAt(numeroTarjeta.length() - 1) != '0';
    }

    private String obtenerMotivoRechazo(String metodo, String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.isEmpty()) {
            return "Número de tarjeta no proporcionado.";
        }
        if (numeroTarjeta.length() < 16) {
            return "El número de tarjeta debe tener 16 dígitos.";
        }
        if (numeroTarjeta.charAt(numeroTarjeta.length() - 1) == '0') {
            return "Fondos insuficientes o tarjeta bloqueada (código 51).";
        }
        return "Transacción rechazada por la pasarela.";
    }

    // ==================== ENVIAR CORREO CON TOKEN (AUDITADO) ====================
    public boolean enviarCorreoConfirmacion(String pnr, String emailDestinatario) {
        String token = String.format("%06d", new java.util.Random().nextInt(999999));

        if (!reservaDAO.guardarToken(pnr, token)) {
            reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CODIGO", "ERROR",
                    "No se pudo guardar el token en BD.");
            return false;
        }

        boolean enviado = com.aircolombia.prototipo.utils.EmailService
                .enviarCodigoConfirmacion(emailDestinatario, pnr, token);

        if (enviado) {
            reservaDAO.auditarEvento(pnr, "CODIGO_ENVIADO", "OK",
                    "Correo enviado a " + emailDestinatario);
        } else {
            reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CODIGO", "ERROR",
                    "JavaMail retornó false para " + emailDestinatario);
        }
        return enviado;
    }

    // ==================== VERIFICAR CÓDIGO (BLOQUEO + AUDITORÍA) ====================
    // Ahora recibe el emailDestinatario
    public ResultadoVerificacion verificarCodigo(String pnr, String codigoIngresado, String emailDestinatario) {
        ReservaDAO.BloqueoInfo info = reservaDAO.obtenerEstadoBloqueo(pnr);
        if (info == null) {
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.ERROR,
                    "Reserva no encontrada.", 0, null);
        }

        Timestamp ahora = new Timestamp(System.currentTimeMillis());

        // Auto-desbloqueo si ya expiró el bloqueo
        if (info.bloqueadoHasta != null && info.bloqueadoHasta.before(ahora)) {
            reservaDAO.limpiarBloqueoExpirado(pnr);
            reservaDAO.auditarEvento(pnr, "DESBLOQUEO_AUTOMATICO", "OK",
                    "El bloqueo temporal expiró y fue levantado.");
            info = reservaDAO.obtenerEstadoBloqueo(pnr);
        }

        // Sigue bloqueada -> rechazar sin tocar contador
        if (info.bloqueadoHasta != null && info.bloqueadoHasta.after(ahora)) {
            reservaDAO.auditarEvento(pnr, "INTENTO_EN_RESERVA_BLOQUEADA", "RECHAZADO",
                    "Intento de verificación en reserva bloqueada hasta " + info.bloqueadoHasta);
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.BLOQUEADA,
                    "La reserva está bloqueada temporalmente por seguridad.",
                    0, info.bloqueadoHasta);
        }

        // Validar vigencia del código
        if (info.codigoEnviadoEn != null) {
            long segTranscurridos = (ahora.getTime() - info.codigoEnviadoEn.getTime()) / 1000;
            if (segTranscurridos > VIGENCIA_CODIGO_SEG) {
                Timestamp hasta = new Timestamp(
                        System.currentTimeMillis() + MINUTOS_BLOQUEO * 60_000L);
                reservaDAO.bloquearReserva(pnr, "CODIGO_EXPIRADO", hasta);
                reservaDAO.auditarEvento(pnr, "TOKEN_EXPIRADO", "BLOQUEADO",
                        "Código expirado tras " + segTranscurridos + "s. Reserva bloqueada.");
                notificarBloqueo(pnr, "CODIGO_EXPIRADO", hasta, emailDestinatario);
                return new ResultadoVerificacion(ResultadoVerificacion.Estado.CODIGO_EXPIRADO,
                        "El código expiró. La reserva se bloqueó por seguridad.",
                        0, hasta);
            }
        }

        // Verificar token
        boolean ok = reservaDAO.verificarToken(pnr, codigoIngresado);
        if (ok) {
            reservaDAO.reiniciarIntentos(pnr);
            reservaDAO.auditarEvento(pnr, "VERIFICACION_EXITOSA", "OK",
                    "Código verificado correctamente.");
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.EXITO,
                    "Código verificado correctamente.", MAX_INTENTOS_VERIFICACION, null);
        }

        // Código incorrecto -> incrementar
        int intentos = reservaDAO.registrarIntentoFallido(pnr, MAX_INTENTOS_VERIFICACION, MINUTOS_BLOQUEO);

        if (intentos < 0) {
            ReservaDAO.BloqueoInfo post = reservaDAO.obtenerEstadoBloqueo(pnr);
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.BLOQUEADA,
                    "La reserva está bloqueada temporalmente.",
                    0, post != null ? post.bloqueadoHasta : null);
        }

        reservaDAO.auditarEvento(pnr, "VERIFICACION_FALLIDA", "RECHAZADO",
                "Intento fallido " + intentos + " de " + MAX_INTENTOS_VERIFICACION);

        if (intentos >= MAX_INTENTOS_VERIFICACION) {
            ReservaDAO.BloqueoInfo post = reservaDAO.obtenerEstadoBloqueo(pnr);
            Timestamp hasta = (post != null && post.bloqueadoHasta != null)
                    ? post.bloqueadoHasta
                    : new Timestamp(System.currentTimeMillis() + MINUTOS_BLOQUEO * 60_000L);
            reservaDAO.auditarEvento(pnr, "RESERVA_BLOQUEADA", "BLOQUEADO",
                    "Bloqueada por " + MAX_INTENTOS_VERIFICACION + " intentos fallidos.");
            notificarBloqueo(pnr, "MAX_INTENTOS", hasta, emailDestinatario);
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.BLOQUEADA,
                    "Demasiados intentos fallidos. Reserva bloqueada por "
                            + MINUTOS_BLOQUEO + " minutos.",
                    0, hasta);
        }

        int restantes = MAX_INTENTOS_VERIFICACION - intentos;
        return new ResultadoVerificacion(ResultadoVerificacion.Estado.CODIGO_INVALIDO,
                "Código incorrecto. Intentos restantes: " + restantes,
                restantes, null);
    }

    // ==================== REENVIAR CÓDIGO (con cooldown) ====================
    public ResultadoVerificacion reenviarCodigo(String pnr, String email) {
        ReservaDAO.BloqueoInfo info = reservaDAO.obtenerEstadoBloqueo(pnr);
        if (info == null) {
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.ERROR,
                    "Reserva no encontrada.", 0, null);
        }

        Timestamp ahora = new Timestamp(System.currentTimeMillis());

        if (info.bloqueadoHasta != null && info.bloqueadoHasta.after(ahora)) {
            reservaDAO.auditarEvento(pnr, "INTENTO_EN_RESERVA_BLOQUEADA", "RECHAZADO",
                    "Intento de reenvío en reserva bloqueada.");
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.BLOQUEADA,
                    "La reserva está bloqueada. Espera al desbloqueo.", 0, info.bloqueadoHasta);
        }

        // Cooldown anti-abuso
        if (info.codigoEnviadoEn != null) {
            long seg = (ahora.getTime() - info.codigoEnviadoEn.getTime()) / 1000;
            if (seg < COOLDOWN_REENVIO_SEG) {
                long esperar = COOLDOWN_REENVIO_SEG - seg;
                return new ResultadoVerificacion(ResultadoVerificacion.Estado.CODIGO_INVALIDO,
                        "Debes esperar " + esperar + "s para reenviar el código.",
                        0, null);
            }
        }

        String token = String.format("%06d", new java.util.Random().nextInt(999999));
        if (!reservaDAO.guardarToken(pnr, token)) {
            reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CODIGO", "ERROR",
                    "No se pudo guardar el token al reenviar.");
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.ERROR,
                    "No se pudo reenviar el código.", 0, null);
        }

        boolean enviado = com.aircolombia.prototipo.utils.EmailService
                .enviarCodigoConfirmacion(email, pnr, token);

        if (enviado) {
            reservaDAO.auditarEvento(pnr, "CODIGO_REENVIADO", "OK",
                    "Nuevo código enviado a " + email);
            return new ResultadoVerificacion(ResultadoVerificacion.Estado.EXITO,
                    "Nuevo código enviado a tu correo.", MAX_INTENTOS_VERIFICACION, null);
        }
        reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CODIGO", "ERROR",
                "Fallo al reenviar código a " + email);
        return new ResultadoVerificacion(ResultadoVerificacion.Estado.ERROR,
                "No se pudo reenviar el código. Intenta más tarde.", 0, null);
    }

    // ==================== VISTA: ESTADO ACTUAL (solo lectura) ====================
    public BloqueoInfoView obtenerEstadoActual(String pnr) {
        ReservaDAO.BloqueoInfo info = reservaDAO.obtenerEstadoBloqueo(pnr);
        if (info == null) return null;
        return new BloqueoInfoView(
                info.intentosVerificacion,
                info.codigoEnviadoEn,
                info.bloqueadoHasta);
    }

    // ==================== NOTIFICAR BLOQUEO (ahora recibe el correo) ====================
    private void notificarBloqueo(String pnr, String motivo, Timestamp hasta, String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CORREO_BLOQUEO", "ERROR",
                    "Correo del comprador no disponible.");
            return;
        }

        try {
            boolean enviado = com.aircolombia.prototipo.utils.EmailService
                    .enviarCorreoBloqueo(correo, pnr, hasta, motivo);
            if (enviado) {
                reservaDAO.auditarEvento(pnr, "BLOQUEO_NOTIFICADO", "OK",
                        "Correo de bloqueo enviado a " + correo);
            } else {
                reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CORREO_BLOQUEO", "ERROR",
                        "JavaMail retornó false.");
            }
        } catch (Exception e) {
            reservaDAO.auditarEvento(pnr, "ERROR_ENVIO_CORREO_BLOQUEO", "ERROR",
                    e.getMessage());
        }
    }

    // ==================== AUDITORÍA (para el panel admin) ====================
    public java.util.List<Object[]> listarAuditoria(String pnr, String evento,
                                                    Timestamp desde, Timestamp hasta,
                                                    int limite) {
        return reservaDAO.listarAuditoria(pnr, evento, desde, hasta, limite);
    }

    public java.util.List<String> listarEventosAuditoria() {
        return reservaDAO.listarEventosDistintos();
    }
}