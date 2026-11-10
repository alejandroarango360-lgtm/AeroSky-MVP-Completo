package com.aircolombia.prototipo.dao;

import com.aircolombia.prototipo.model.Vuelo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VueloDAO {

    // ====== SELECT BASE con JOIN a aeronave ======
    private static final String SELECT_BASE =
        "SELECT v.id_vuelo, v.numero_vuelo, v.origen_iata, v.destino_iata, " +
        "       ao.ciudad AS origen_ciudad, ad.ciudad AS destino_ciudad, " +
        "       v.id_aeronave, a.matricula, a.capacidad_pasajeros, " +
        "       v.fecha_salida, v.fecha_llegada, v.estado_vuelo, v.escalas, " +
        "       v.tarifa_base, v.cupos_disponibles " +
        "FROM vuelo v " +
        "INNER JOIN aeropuerto ao ON v.origen_iata = ao.codigo_iata " +
        "INNER JOIN aeropuerto ad ON v.destino_iata = ad.codigo_iata " +
        "LEFT JOIN aeronave a ON v.id_aeronave = a.id_aeronave ";

    // ====== MAPEO ======
    private Vuelo mapear(ResultSet rs) throws SQLException {
        int idAer = rs.getInt("id_aeronave");
        String matricula = rs.getString("matricula");
        if (matricula == null) matricula = "N/D";
        int capacidad = rs.getInt("capacidad_pasajeros");

        return new Vuelo(
                rs.getInt("id_vuelo"),
                rs.getString("numero_vuelo"),
                rs.getString("origen_iata"),
                rs.getString("destino_iata"),
                rs.getString("origen_ciudad"),
                rs.getString("destino_ciudad"),
                idAer, matricula, capacidad,
                rs.getString("fecha_salida"),
                rs.getString("fecha_llegada"),
                rs.getString("estado_vuelo"),
                rs.getInt("escalas"),
                rs.getDouble("tarifa_base"),
                rs.getInt("cupos_disponibles")
        );
    }

    // ====== CIUDADES (para ComboBox) ======
    public List<String> obtenerCiudades() {
        List<String> ciudades = new ArrayList<>();
        String sql = "SELECT DISTINCT ciudad FROM aeropuerto WHERE ciudad IS NOT NULL ORDER BY ciudad";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) ciudades.add(rs.getString("ciudad"));
        } catch (SQLException e) {
            System.out.println("Error obtenerCiudades: " + e.getMessage());
        }
        return ciudades;
    }

    // ====== BÚSQUEDA POR FECHA ======
    public List<Vuelo> buscarVuelosPorFecha(String origen, String destino, String fecha) {
        return buscarVuelosPorFecha(origen, destino, fecha, 1, 0, 0, "Economica");
    }

    public List<Vuelo> buscarVuelosPorFecha(String origen, String destino, String fecha,
                                            int adultos, int ninos, int infantes, String clase) {
        List<Vuelo> lista = new ArrayList<>();
        int asientosRequeridos = adultos + ninos;

        String sql = SELECT_BASE +
            "WHERE ao.ciudad = ? AND ad.ciudad = ? " +
            "AND DATE(v.fecha_salida) = ? " +
            "AND v.cupos_disponibles >= ? " +
            "ORDER BY v.fecha_salida";

        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, origen);
            pstmt.setString(2, destino);
            pstmt.setString(3, fecha);
            pstmt.setInt(4, asientosRequeridos);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error buscarVuelosPorFecha: " + e.getMessage());
        }
        return lista;
    }

    // ====== BÚSQUEDA EN RANGO (FA1) ======
    public List<Vuelo> buscarVuelosCercanos(String origen, String destino, String fechaBase, int diasRango) {
        return buscarVuelosCercanos(origen, destino, fechaBase, diasRango, 1, 0, 0, "Economica");
    }

    public List<Vuelo> buscarVuelosCercanos(String origen, String destino, String fechaBase,
                                            int diasRango, int adultos, int ninos,
                                            int infantes, String clase) {
        List<Vuelo> lista = new ArrayList<>();
        int asientosRequeridos = adultos + ninos;

        try {
            java.time.LocalDate fecha = java.time.LocalDate.parse(fechaBase);
            String fechaMin = fecha.minusDays(diasRango).toString();
            String fechaMax = fecha.plusDays(diasRango).toString();

            String sql = SELECT_BASE +
                "WHERE ao.ciudad = ? AND ad.ciudad = ? " +
                "AND DATE(v.fecha_salida) BETWEEN ? AND ? " +
                "AND DATE(v.fecha_salida) <> ? " +
                "AND v.cupos_disponibles >= ? " +
                "ORDER BY v.fecha_salida";

            try (Connection conn = Conexion.getConexion();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, origen);
                pstmt.setString(2, destino);
                pstmt.setString(3, fechaMin);
                pstmt.setString(4, fechaMax);
                pstmt.setString(5, fechaBase);
                pstmt.setInt(6, asientosRequeridos);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (Exception e) {
            System.out.println("Error buscarVuelosCercanos: " + e.getMessage());
        }
        return lista;
    }

    // ====== LISTAR TODOS ======
    public List<Vuelo> listarTodos() {
        List<Vuelo> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY v.id_vuelo DESC";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.out.println("Error listarTodos: " + e.getMessage());
        }
        return lista;
    }

    // ====== BUSCAR POR ID ======
    public Vuelo buscarPorId(int idVuelo) {
        String sql = SELECT_BASE + "WHERE v.id_vuelo = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idVuelo);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            System.out.println("Error buscarPorId: " + e.getMessage());
        }
        return null;
    }

    // ====== INSERTAR ======
    public boolean insertar(Vuelo v) {
        String sql = "INSERT INTO vuelo (numero_vuelo, origen_iata, destino_iata, id_aeronave, " +
                     "fecha_salida, fecha_llegada, estado_vuelo, escalas, tarifa_base, cupos_disponibles) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, v.getNumeroVuelo());
            pstmt.setString(2, v.getOrigenIata());
            pstmt.setString(3, v.getDestinoIata());
            pstmt.setInt(4, v.getIdAeronave());
            pstmt.setString(5, v.getFechaSalida());
            pstmt.setString(6, v.getFechaLlegada());
            pstmt.setString(7, v.getEstadoVuelo());
            pstmt.setInt(8, v.getEscalas());
            pstmt.setDouble(9, v.getTarifaBase());
            pstmt.setInt(10, v.getCuposDisponibles());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error insertar vuelo: " + e.getMessage());
            return false;
        }
    }

    // ====== ACTUALIZAR ======
    public boolean actualizar(Vuelo v) {
        String sql = "UPDATE vuelo SET numero_vuelo = ?, origen_iata = ?, destino_iata = ?, " +
                     "id_aeronave = ?, fecha_salida = ?, fecha_llegada = ?, estado_vuelo = ?, " +
                     "escalas = ?, tarifa_base = ?, cupos_disponibles = ? WHERE id_vuelo = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, v.getNumeroVuelo());
            pstmt.setString(2, v.getOrigenIata());
            pstmt.setString(3, v.getDestinoIata());
            pstmt.setInt(4, v.getIdAeronave());
            pstmt.setString(5, v.getFechaSalida());
            pstmt.setString(6, v.getFechaLlegada());
            pstmt.setString(7, v.getEstadoVuelo());
            pstmt.setInt(8, v.getEscalas());
            pstmt.setDouble(9, v.getTarifaBase());
            pstmt.setInt(10, v.getCuposDisponibles());
            pstmt.setInt(11, v.getIdVuelo());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error actualizar vuelo: " + e.getMessage());
            return false;
        }
    }

    // ====== ELIMINAR ======
    public boolean eliminar(int idVuelo) {
        String sql = "DELETE FROM vuelo WHERE id_vuelo = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idVuelo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error eliminar vuelo: " + e.getMessage());
            return false;
        }
    }

    // ====== ACTUALIZAR ASIENTOS (restar) ======
    public boolean actualizarAsientos(int idVuelo, int cantidad) {
        String sql = "UPDATE vuelo SET cupos_disponibles = cupos_disponibles - ? " +
                     "WHERE id_vuelo = ? AND cupos_disponibles >= ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cantidad);
            pstmt.setInt(2, idVuelo);
            pstmt.setInt(3, cantidad);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ====== DEVOLVER ASIENTOS (sumar) ======
    public boolean devolverAsiento(int idVuelo, int cantidad) {
        String sql = "UPDATE vuelo SET cupos_disponibles = cupos_disponibles + ? WHERE id_vuelo = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cantidad);
            pstmt.setInt(2, idVuelo);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error devolverAsiento: " + e.getMessage());
            return false;
        }
    }
    // ====== RF-05: VUELOS DE REGRESO COMPATIBLES ======
// Regla CA-07: se excluyen vuelos de regreso cuyo despegue sea anterior
// a la hora de llegada del vuelo de ida (el pasajero debe haber llegado).
public List<Vuelo> buscarVuelosRegresoCompatibles(String origen, String destino,
                                                   String fecha,
                                                   String fechaMinSalida,
                                                   int adultos, int ninos, int infantes,
                                                   String clase) {
    List<Vuelo> lista = new ArrayList<>();
    int asientosRequeridos = adultos + ninos;

    String sql = SELECT_BASE +
        "WHERE ao.ciudad = ? AND ad.ciudad = ? " +
        "AND DATE(v.fecha_salida) = ? " +
        "AND v.fecha_salida >= ? " +
        "AND v.cupos_disponibles >= ? " +
        "ORDER BY v.fecha_salida";

    try (Connection conn = Conexion.getConexion();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, origen);
        pstmt.setString(2, destino);
        pstmt.setString(3, fecha);
        pstmt.setString(4, fechaMinSalida);
        pstmt.setInt(5, asientosRequeridos);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) lista.add(mapear(rs));
    } catch (SQLException e) {
        System.out.println("Error buscarVuelosRegresoCompatibles: " + e.getMessage());
    }
    return lista;
}
}