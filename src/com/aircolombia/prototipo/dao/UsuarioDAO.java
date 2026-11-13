package com.aircolombia.prototipo.dao;

import com.aircolombia.prototipo.model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario autenticar(String username, String password) {
        String sql = "SELECT * FROM usuario WHERE username = ? AND password = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapearUsuario(rs);
        } catch (SQLException e) {
            System.out.println("Error autenticar: " + e.getMessage());
        }
        return null;
    }

    public Usuario buscarPorUsername(String username) {
        String sql = "SELECT * FROM usuario WHERE username = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapearUsuario(rs);
        } catch (SQLException e) {
            System.out.println("Error buscarPorUsername: " + e.getMessage());
        }
        return null;
    }

    public boolean registrarUsuario(Usuario u) {
        String sql = "INSERT INTO usuario (username, password, rol, email, estado) VALUES (?, ?, ?, ?, 'ACTIVO')";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, u.getUsername());
            pstmt.setString(2, u.getPasswordHash());
            pstmt.setString(3, u.getRol());
            pstmt.setString(4, u.getEmail());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error registrarUsuario: " + e.getMessage());
            return false;
        }
    }

    // --- CORREGIDO: AHORA BLOQUEA Y USA LA HORA DE JAVA ---
    public void registrarIntentoFallido(String username) {
        Usuario u = buscarPorUsername(username);
        if (u == null) return;

        int nuevosIntentos = u.getIntentosFallidos() + 1;
        String sql;
        
        if (nuevosIntentos >= 3) {
            sql = "UPDATE usuario SET intentos_fallidos = ?, estado = 'BLOQUEADO', bloqueado_hasta = ? WHERE username = ?";
        } else {
            sql = "UPDATE usuario SET intentos_fallidos = ? WHERE username = ?";
        }

        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nuevosIntentos);
            
            if (nuevosIntentos >= 3) {
                // Bloqueo de 15 minutos desde la hora exacta de Java
                Timestamp bloqueo = new Timestamp(System.currentTimeMillis() + 15 * 60 * 1000L);
                pstmt.setTimestamp(2, bloqueo);
                pstmt.setString(3, username);
            } else {
                pstmt.setString(2, username);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error registrarIntentoFallido: " + e.getMessage());
        }
    }

    public void reiniciarIntentos(String username) {
        String sql = "UPDATE usuario SET intentos_fallidos = 0, estado = 'ACTIVO', bloqueado_hasta = NULL WHERE username = ?";
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

    public List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY id_usuario DESC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error listarUsuarios: " + e.getMessage());
        }
        return lista;
    }

    public boolean desbloquearUsuario(int idUsuario) {
        String sql = "UPDATE usuario SET estado = 'ACTIVO', intentos_fallidos = 0, bloqueado_hasta = NULL WHERE id_usuario = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error desbloquearUsuario: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarRol(int idUsuario, String nuevoRol) {
        String sql = "UPDATE usuario SET rol = ? WHERE id_usuario = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevoRol);
            pstmt.setInt(2, idUsuario);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error actualizarRol: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarUsuario(int idUsuario) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error eliminarUsuario: " + e.getMessage());
            return false;
        }
    }

    public boolean crearUsuarioAdmin(String username, String password, String email, String rol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPasswordHash(password);
        u.setEmail(email);
        u.setRol(rol);
        return registrarUsuario(u);
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password"));
        u.setRol(rs.getString("rol"));
        u.setEmail(rs.getString("email"));
        u.setEstado(rs.getString("estado"));
        u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        u.setBloqueadoHasta(rs.getTimestamp("bloqueado_hasta"));
        return u;
    }
}