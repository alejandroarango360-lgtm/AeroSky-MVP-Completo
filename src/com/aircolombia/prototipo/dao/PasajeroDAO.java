package com.aircolombia.prototipo.dao;

import com.aircolombia.prototipo.model.Pasajero;
import java.sql.*;

public class PasajeroDAO {

    public int obtenerOCrear(Pasajero p) {
        int existente = buscarIdPorDocumento(p.getTipoDocumento(), p.getNumeroDocumento());
        if (existente != -1) return existente;
        return insertar(p);
    }

    public int insertar(Pasajero p) {
        // Agregamos direccion y ciudad_residencia
        String sql = "INSERT INTO pasajero (tipo_documento, numero_documento, nombre_completo, email, telefono, direccion, ciudad_residencia) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, p.getTipoDocumento());
            pstmt.setString(2, p.getNumeroDocumento());
            pstmt.setString(3, p.getNombreCompleto());
            pstmt.setString(4, p.getEmail());
            pstmt.setString(5, p.getTelefono());
            pstmt.setString(6, p.getDireccion());
            pstmt.setString(7, p.getCiudadResidencia());
            
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Error insertar pasajero: " + e.getMessage());
        }
        return -1;
    }

    public int buscarIdPorDocumento(String tipoDoc, String numeroDoc) {
        String sql = "SELECT id_pasajero FROM pasajero WHERE tipo_documento = ? AND numero_documento = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tipoDoc);
            pstmt.setString(2, numeroDoc);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id_pasajero");
        } catch (SQLException e) {
            System.out.println("Error buscarPorDocumento: " + e.getMessage());
        }
        return -1;
    }
}