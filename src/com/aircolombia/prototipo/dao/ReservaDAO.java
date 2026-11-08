package com.aircolombia.prototipo.dao;

import java.sql.*;

public class ReservaDAO {

    // ---------- CREAR RESERVA PENDIENTE (con asientos) ----------
    public boolean crearReservaPendiente(String codigoPnr, int idVuelo, Integer idVueloRegreso,
                                         double total, String claseViaje, String tipoViaje,
                                         int asientos) {
        String sql = "INSERT INTO reserva (codigo_pnr, id_vuelo, id_vuelo_regreso, " +
                     "estado_reserva, clase_viaje, tipo_viaje, asientos, total) " +
                     "VALUES (?, ?, ?, 'Pendiente_Pago', ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoPnr);
            pstmt.setInt(2, idVuelo);
            if (idVueloRegreso == null) {
                pstmt.setNull(3, Types.INTEGER);
            } else {
                pstmt.setInt(3, idVueloRegreso);
            }
            pstmt.setString(4, claseViaje);
            pstmt.setString(5, tipoViaje);
            pstmt.setInt(6, asientos);
            pstmt.setDouble(7, total);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error crearReservaPendiente: " + e.getMessage());
            return false;
        }
    }

    // ---------- CAMBIAR ESTADO ----------
    public boolean cambiarEstado(String codigoPnr, String nuevoEstado) {
        String sql = "UPDATE reserva SET estado_reserva = ? WHERE codigo_pnr = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoEstado);
            pstmt.setString(2, codigoPnr);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error cambiarEstado: " + e.getMessage());
            return false;
        }
    }

    // ---------- BUSCAR POR PNR (con asientos) ----------
    // Retorna: [idVuelo, idVueloRegreso(Integer o null), estado, asientos, tipoViaje]
    public Object[] buscarPorPNR(String pnr) {
        String sql = "SELECT id_vuelo, id_vuelo_regreso, estado_reserva, asientos, tipo_viaje " +
                     "FROM reserva WHERE codigo_pnr = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pnr);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int idRegreso = rs.getInt("id_vuelo_regreso");
                Integer regreso = rs.wasNull() ? null : idRegreso;
                return new Object[]{
                    rs.getInt("id_vuelo"),
                    regreso,
                    rs.getString("estado_reserva"),
                    rs.getInt("asientos"),
                    rs.getString("tipo_viaje")
                };
            }
        } catch (SQLException e) {
            System.out.println("Error buscarPorPNR: " + e.getMessage());
        }
        return null;
    }

    // ---------- OBTENER TOTAL DE UNA RESERVA ----------
    public double obtenerTotalReserva(String codigoPnr) {
        String sql = "SELECT total FROM reserva WHERE codigo_pnr = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoPnr);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException e) {
            System.out.println("Error obtenerTotal: " + e.getMessage());
        }
        return 0;
    }

    // ---------- INFO COMPLETA PARA CANCELACIÓN ----------
    // Retorna: {pnr, origen, destino, fechaSalida, total, nombre, email, clase, tipoViaje, asientos}
    public Object[] obtenerInfoReserva(String codigoPnr) {
        String sql = "SELECT r.codigo_pnr, r.total, r.clase_viaje, r.tipo_viaje, " +
                     "       r.estado_reserva, r.asientos, " +
                     "       v.fecha_salida, ao.ciudad AS origen_ciudad, ad.ciudad AS destino_ciudad, " +
                     "       p.nombre_completo, p.email " +
                     "FROM reserva r " +
                     "INNER JOIN vuelo v ON r.id_vuelo = v.id_vuelo " +
                     "INNER JOIN aeropuerto ao ON v.origen_iata = ao.codigo_iata " +
                     "INNER JOIN aeropuerto ad ON v.destino_iata = ad.codigo_iata " +
                     "LEFT JOIN reserva_pasajero rp ON r.codigo_pnr = rp.codigo_pnr " +
                     "LEFT JOIN pasajero p ON rp.id_pasajero = p.id_pasajero " +
                     "WHERE r.codigo_pnr = ? LIMIT 1";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoPnr);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Object[]{
                        rs.getString("codigo_pnr"),
                        rs.getString("origen_ciudad"),
                        rs.getString("destino_ciudad"),
                        rs.getString("fecha_salida"),
                        rs.getDouble("total"),
                        rs.getString("nombre_completo"),
                        rs.getString("email"),
                        rs.getString("clase_viaje"),
                        rs.getString("tipo_viaje"),
                        rs.getInt("asientos")
                };
            }
        } catch (SQLException e) {
            System.out.println("Error info reserva: " + e.getMessage());
        }
        return null;
    }

    // ---------- OBTENER RESERVAS HUÉRFANAS ----------
    // Retorna una lista de Object[]: {codigo_pnr, id_vuelo, id_vuelo_regreso, asientos}
    public java.util.List<Object[]> obtenerReservasPendientesAntiguas(int minutos) {
        java.util.List<Object[]> lista = new java.util.ArrayList<>();
        String sql = "SELECT codigo_pnr, id_vuelo, id_vuelo_regreso, asientos " +
                     "FROM reserva " +
                     "WHERE estado_reserva = 'Pendiente_Pago' " +
                     "AND fecha_creacion < NOW() - INTERVAL ? MINUTE";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, minutos);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int idRegreso = rs.getInt("id_vuelo_regreso");
                Integer regreso = rs.wasNull() ? null : idRegreso;
                lista.add(new Object[]{
                    rs.getString("codigo_pnr"),
                    rs.getInt("id_vuelo"),
                    regreso,
                    rs.getInt("asientos")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error obtenerReservasPendientesAntiguas: " + e.getMessage());
        }
        return lista;
    }

    // ---------- ASOCIAR COMPRADOR (RF-07) ----------
    public boolean asociarComprador(String codigoPnr, int idComprador) {
        String sql = "UPDATE reserva SET id_comprador = ? WHERE codigo_pnr = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idComprador);
            pstmt.setString(2, codigoPnr);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error asociarComprador: " + e.getMessage());
            return false;
        }
    }

    // ---------- GUARDAR TOKEN DE CONFIRMACIÓN (Corregido TimeZone) ----------
    public boolean guardarToken(String pnr, String token) {
        String sql = "UPDATE reserva SET token_confirmacion = ?, codigo_enviado_en = ?, " +
                     "intentos_verificacion = 0 WHERE codigo_pnr = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            // Usamos la hora de Java para evitar desfases con MySQL
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(3, pnr);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error guardarToken: " + e.getMessage());
            return false;
        }
    }

    // ---------- VERIFICAR TOKEN ----------
    public boolean verificarToken(String pnr, String token) {
        String sql = "SELECT token_confirmacion FROM reserva WHERE codigo_pnr = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pnr);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String tokenGuardado = rs.getString("token_confirmacion");
                return tokenGuardado != null && tokenGuardado.equals(token);
            }
        } catch (SQLException e) {
            System.out.println("Error verificarToken: " + e.getMessage());
        }
        return false;
    }

    // ================================================================
    // ============ MÉTODOS NUEVOS: BLOQUEO + AUDITORÍA ===============
    // ================================================================

    // ---------- ESTRUCTURA DE ESTADO DE BLOQUEO ----------
    public static class BloqueoInfo {
        public int intentosVerificacion;
        public Timestamp codigoEnviadoEn;
        public Timestamp bloqueadoHasta;
        public String motivoBloqueo;
    }

    // ---------- OBTENER ESTADO DE BLOQUEO ----------
    public BloqueoInfo obtenerEstadoBloqueo(String pnr) {
        String sql = "SELECT intentos_verificacion, codigo_enviado_en, " +
                     "bloqueado_hasta, motivo_bloqueo FROM reserva WHERE codigo_pnr = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pnr);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                BloqueoInfo info = new BloqueoInfo();
                info.intentosVerificacion = rs.getInt("intentos_verificacion");
                info.codigoEnviadoEn = rs.getTimestamp("codigo_enviado_en");
                info.bloqueadoHasta  = rs.getTimestamp("bloqueado_hasta");
                info.motivoBloqueo   = rs.getString("motivo_bloqueo");
                return info;
            }
        } catch (SQLException e) {
            System.out.println("Error obtenerEstadoBloqueo: " + e.getMessage());
        }
        return null;
    }

    // ---------- REGISTRAR INTENTO FALLIDO (Corregido TimeZone) ----------
    public int registrarIntentoFallido(String pnr, int maxIntentos, int minutosBloqueo) {
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            conn.setAutoCommit(false);

            String sqlSel = "SELECT intentos_verificacion, bloqueado_hasta " +
                            "FROM reserva WHERE codigo_pnr = ? FOR UPDATE";
            int intentos = 0;
            boolean yaBloqueada = false;
            try (PreparedStatement ps = conn.prepareStatement(sqlSel)) {
                ps.setString(1, pnr);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return -1;
                    }
                    intentos = rs.getInt("intentos_verificacion");
                    Timestamp blq = rs.getTimestamp("bloqueado_hasta");
                    if (blq != null && blq.after(new Timestamp(System.currentTimeMillis()))) {
                        yaBloqueada = true;
                    }
                }
            }
            if (yaBloqueada) {
                conn.rollback();
                return -1;
            }

            intentos++;
            boolean hayQueBloquear = (intentos >= maxIntentos);

            String sqlUpd = hayQueBloquear
                ? "UPDATE reserva SET intentos_verificacion = ?, " +
                  "bloqueado_hasta = ?, " + 
                  "motivo_bloqueo = 'MAX_INTENTOS' WHERE codigo_pnr = ?"
                : "UPDATE reserva SET intentos_verificacion = ? WHERE codigo_pnr = ?";

            try (PreparedStatement ps = conn.prepareStatement(sqlUpd)) {
                if (hayQueBloquear) {
                    ps.setInt(1, intentos);
                    // Calculamos la hora de desbloqueo usando Java
                    Timestamp hasta = new Timestamp(System.currentTimeMillis() + (minutosBloqueo * 60_000L));
                    ps.setTimestamp(2, hasta);
                    ps.setString(3, pnr);
                } else {
                    ps.setInt(1, intentos);
                    ps.setString(2, pnr);
                }
                ps.executeUpdate();
            }
            conn.commit();
            return intentos;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            System.out.println("Error registrarIntentoFallido: " + e.getMessage());
            return -1;
        } finally {
            if (conn != null) try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ex) {}
        }
    }

    // ---------- BLOQUEAR RESERVA (manual) ----------
    public boolean bloquearReserva(String pnr, String motivo, Timestamp hasta) {
        String sql = "UPDATE reserva SET bloqueado_hasta = ?, motivo_bloqueo = ? " +
                     "WHERE codigo_pnr = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, hasta);
            pstmt.setString(2, motivo);
            pstmt.setString(3, pnr);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error bloquearReserva: " + e.getMessage());
            return false;
        }
    }

    // ---------- LIMPIAR BLOQUEO EXPIRADO (Corregido TimeZone) ----------
    public boolean limpiarBloqueoExpirado(String pnr) {
        String sql = "UPDATE reserva SET bloqueado_hasta = NULL, motivo_bloqueo = NULL, " +
                     "intentos_verificacion = 0 " +
                     "WHERE codigo_pnr = ? AND bloqueado_hasta IS NOT NULL AND bloqueado_hasta < ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pnr);
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error limpiarBloqueoExpirado: " + e.getMessage());
            return false;
        }
    }

    // ---------- REINICIAR INTENTOS ----------
    public boolean reiniciarIntentos(String pnr) {
        String sql = "UPDATE reserva SET intentos_verificacion = 0 WHERE codigo_pnr = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pnr);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error reiniciarIntentos: " + e.getMessage());
            return false;
        }
    }

    // ---------- LIMPIAR TOKEN TRAS PAGO EXITOSO (anti-replay) ----------
    public boolean limpiarToken(String pnr) {
        String sql = "UPDATE reserva SET token_confirmacion = NULL, codigo_enviado_en = NULL " +
                     "WHERE codigo_pnr = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pnr);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error limpiarToken: " + e.getMessage());
            return false;
        }
    }

    // ---------- AUDITORÍA ----------
    public boolean auditarEvento(String pnr, String evento, String resultado, String detalle) {
        String sql = "INSERT INTO auditoria_eventos " +
                     "(codigo_pnr, evento, resultado, detalle, fecha_evento) " +
                     "VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pnr);
            pstmt.setString(2, evento);
            pstmt.setString(3, resultado);
            pstmt.setString(4, detalle);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error auditarEvento: " + e.getMessage());
            return false;
        }
    }
        // ---------- LISTAR AUDITORÍA (con filtros opcionales) ----------
    public java.util.List<Object[]> listarAuditoria(String pnr, String evento,
                                                    Timestamp desde, Timestamp hasta,
                                                    int limite) {
        java.util.List<Object[]> lista = new java.util.ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT fecha_evento, codigo_pnr, evento, resultado, detalle " +
            "FROM auditoria_eventos WHERE 1=1 ");

        if (pnr != null && !pnr.trim().isEmpty())  sql.append("AND codigo_pnr = ? ");
        if (evento != null && !evento.trim().isEmpty()) sql.append("AND evento = ? ");
        if (desde != null) sql.append("AND fecha_evento >= ? ");
        if (hasta != null) sql.append("AND fecha_evento <= ? ");
        sql.append("ORDER BY fecha_evento DESC LIMIT ?");

        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (pnr != null && !pnr.trim().isEmpty())       pstmt.setString(idx++, pnr.trim().toUpperCase());
            if (evento != null && !evento.trim().isEmpty()) pstmt.setString(idx++, evento);
            if (desde != null)                              pstmt.setTimestamp(idx++, desde);
            if (hasta != null)                              pstmt.setTimestamp(idx++, hasta);
            pstmt.setInt(idx, limite <= 0 ? 500 : limite);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getTimestamp("fecha_evento"),
                        rs.getString("codigo_pnr"),
                        rs.getString("evento"),
                        rs.getString("resultado"),
                        rs.getString("detalle")
                    });
                }
            }
        } catch (SQLException e) {
            System.out.println("Error listarAuditoria: " + e.getMessage());
        }
        return lista;
    }

    // ---------- LISTAR EVENTOS DISTINTOS ----------
    public java.util.List<String> listarEventosDistintos() {
        java.util.List<String> lista = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT evento FROM auditoria_eventos ORDER BY evento";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) lista.add(rs.getString("evento"));
        } catch (SQLException e) {
            System.out.println("Error listarEventosDistintos: " + e.getMessage());
        }
        return lista;
    }
}