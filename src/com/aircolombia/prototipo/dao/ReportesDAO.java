package com.aircolombia.prototipo.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportesDAO {

    // RF-34: KPIs en tiempo real
    public double obtenerVentasTotales(String desde, String hasta) {
        String sql = "SELECT SUM(total) FROM reserva WHERE estado_reserva = 'Confirmada' AND DATE(fecha_creacion) BETWEEN ? AND ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, desde);
            pstmt.setString(2, hasta);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.out.println("Error obtenerVentasTotales: " + e.getMessage());
        }
        return 0.0;
    }

    public int obtenerTiquetesVendidos(String desde, String hasta) {
        String sql = "SELECT SUM(asientos) FROM reserva WHERE estado_reserva = 'Confirmada' AND DATE(fecha_creacion) BETWEEN ? AND ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, desde);
            pstmt.setString(2, hasta);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Error obtenerTiquetesVendidos: " + e.getMessage());
        }
        return 0;
    }

    public int obtenerCancelaciones(String desde, String hasta) {
        String sql = "SELECT COUNT(*) FROM reserva WHERE estado_reserva = 'Cancelada' AND DATE(fecha_creacion) BETWEEN ? AND ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, desde);
            pstmt.setString(2, hasta);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Error obtenerCancelaciones: " + e.getMessage());
        }
        return 0;
    }

    // RF-32 & RF-33: Reporte por ruta con filtros de origen/destino
    public List<Object[]> obtenerReportePorRuta(String desde, String hasta, String ciudadOrigen, String ciudadDestino) {
        List<Object[]> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT ao.ciudad AS origen, ad.ciudad AS destino, " +
            "       COUNT(r.codigo_pnr) AS cant_reservas, " +
            "       SUM(r.asientos) AS tiquetes, " +
            "       SUM(r.total) AS ingresos " +
            "FROM reserva r " +
            "INNER JOIN vuelo v ON r.id_vuelo = v.id_vuelo " +
            "INNER JOIN aeropuerto ao ON v.origen_iata = ao.codigo_iata " +
            "INNER JOIN aeropuerto ad ON v.destino_iata = ad.codigo_iata " +
            "WHERE r.estado_reserva = 'Confirmada' " +
            "AND DATE(r.fecha_creacion) BETWEEN ? AND ? "
        );

        if (ciudadOrigen != null && !ciudadOrigen.equals("Todas")) sql.append("AND ao.ciudad = ? ");
        if (ciudadDestino != null && !ciudadDestino.equals("Todas")) sql.append("AND ad.ciudad = ? ");
        
        sql.append("GROUP BY ao.ciudad, ad.ciudad ORDER BY ingresos DESC");

        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            pstmt.setString(idx++, desde);
            pstmt.setString(idx++, hasta);
            if (ciudadOrigen != null && !ciudadOrigen.equals("Todas")) pstmt.setString(idx++, ciudadOrigen);
            if (ciudadDestino != null && !ciudadDestino.equals("Todas")) pstmt.setString(idx++, ciudadDestino);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getString("origen"),
                    rs.getString("destino"),
                    rs.getInt("cant_reservas"),
                    rs.getInt("tiquetes"),
                    rs.getDouble("ingresos")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error obtenerReportePorRuta: " + e.getMessage());
        }
        return lista;
    }
}