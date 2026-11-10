package com.aircolombia.prototipo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class IntentoPagoDAO {

    public boolean registrar(String codigoPnr, double monto, String metodo,
                             String numeroTarjeta, String resultado, String motivo) {
        String sql = "INSERT INTO intentos_pago " +
                     "(codigo_pnr, monto, metodo_pago, numero_tarjeta, resultado, motivo) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoPnr);
            pstmt.setDouble(2, monto);
            pstmt.setString(3, metodo);
            pstmt.setString(4, numeroTarjeta);
            pstmt.setString(5, resultado);
            pstmt.setString(6, motivo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error intento pago: " + e.getMessage());
            return false;
        }
    }
}