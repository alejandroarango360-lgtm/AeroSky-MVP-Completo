package com.aircolombia.prototipo.dao;

import com.aircolombia.prototipo.model.Usuario;
import java.sql.*;

public class UsuarioDAO {

    public Usuario autenticar(String username, String password) {
        String sql = "SELECT * FROM usuario WHERE username = ? AND password = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Usuario(
                        rs.getInt("id_usuario"), rs.getString("username"),
                        rs.getString("rol"), rs.getString("email"),
                        rs.getInt("intentos_fallidos"), rs.getTimestamp("bloqueado_hasta")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error autenticar: " + e.getMessage());
        }
        return null;
    }

    public void registrarIntentoFallido(String username) {
        String sql = "UPDATE usuario SET intentos_fallidos = intentos_fallidos + 1 WHERE username = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error registrarIntentoFallido: " + e.getMessage());
        }
    }

    public void bloquearCuenta(String username, Timestamp hasta) {
        String sql = "UPDATE usuario SET bloqueado_hasta = ? WHERE username = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, hasta);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error bloquearCuenta: " + e.getMessage());
        }
    }

    public void reiniciarIntentos(String username) {
        String sql = "UPDATE usuario SET intentos_fallidos = 0, bloqueado_hasta = NULL WHERE username = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error reiniciarIntentos: " + e.getMessage());
        }
    }

    public void guardarToken2FA(String username, String token) {
        String sql = "UPDATE usuario SET token_2fa = ?, token_enviado_en = NOW() WHERE username = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error guardarToken2FA: " + e.getMessage());
        }
    }

    public boolean verificarToken2FA(String username, String token) {
        String sql = "SELECT token_2fa FROM usuario WHERE username = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String t = rs.getString("token_2fa");
                return t != null && t.equals(token);
            }
        } catch (SQLException e) {
            System.out.println("Error verificarToken2FA: " + e.getMessage());
        }
        return false;
    }
}