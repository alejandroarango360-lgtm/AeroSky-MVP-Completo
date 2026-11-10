package com.aircolombia.prototipo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReservaPasajeroDAO {

    public boolean agregar(String codigoPnr, int idPasajero, boolean esTitular) {
        String sql = "INSERT INTO reserva_pasajero (codigo_pnr, id_pasajero, es_titular) VALUES (?, ?, ?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, codigoPnr);
            pstmt.setInt(2, idPasajero);
            pstmt.setBoolean(3, esTitular);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error agregar reserva_pasajero: " + e.getMessage());
            return false;
        }
    }
}