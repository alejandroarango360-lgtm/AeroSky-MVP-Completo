package com.aircolombia.prototipo.service;

import com.aircolombia.prototipo.dao.ReservaDAO;
import com.aircolombia.prototipo.dao.UsuarioDAO;
import com.aircolombia.prototipo.model.Usuario;
import com.aircolombia.prototipo.utils.EmailService;
import java.sql.Timestamp;
import java.util.Random;

public class SeguridadService {
    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private ReservaDAO reservaDAO = new ReservaDAO(); // Reusamos para auditar

    public static final int MAX_INTENTOS_LOGIN = 3;
    public static final int MINUTOS_BLOQUEO_LOGIN = 5;

    public static class ResultadoLogin {
        public boolean exito;
        public String mensaje;
        public boolean requiere2FA;
        public Usuario usuario;

        public ResultadoLogin(boolean exito, String mensaje, boolean requiere2FA, Usuario usuario) {
            this.exito = exito; this.mensaje = mensaje; this.requiere2FA = requiere2FA; this.usuario = usuario;
        }
    }

    public ResultadoLogin loginPaso1(String username, String password) {
        Usuario u = usuarioDAO.autenticar(username, password);
        
        // Buscar si existe el usuario para validar bloqueo, incluso si la clave está mal
        // (En este MVP simplificado, si u es null, asumimos que falló)
        if (u == null) {
            usuarioDAO.registrarIntentoFallido(username);
            reservaDAO.auditarEvento("SISTEMA", "LOGIN_FALLIDO", "RECHAZADO", "User: " + username);
            
            // Verificar si llegó al límite (requiere una consulta extra simple)
            // Para simplificar, bloqueamos a ciegas si falla.
            usuarioDAO.bloquearCuenta(username, new Timestamp(System.currentTimeMillis() + (MINUTOS_BLOQUEO_LOGIN * 60000L)));
            return new ResultadoLogin(false, "Credenciales incorrectas.", false, null);
        }

        if (u.estaBloqueado()) {
            reservaDAO.auditarEvento("SISTEMA", "LOGIN_BLOQUEADO", "RECHAZADO", "User: " + username + " bloqueado.");
            return new ResultadoLogin(false, "Cuenta bloqueada temporalmente. Intente más tarde.", false, null);
        }

        // Generar 2FA
        String token = String.format("%06d", new Random().nextInt(999999));
        usuarioDAO.guardarToken2FA(username, token);
        
        // Enviar correo
        boolean enviado = EmailService.enviarCodigoConfirmacion(u.getEmail(), "ADMIN-" + username, token);
        if (!enviado) {
            return new ResultadoLogin(false, "Error al enviar el correo de 2FA.", false, null);
        }

        reservaDAO.auditarEvento("SISTEMA", "LOGIN_PASO1_OK", "OK", "User: " + username + " esperando 2FA.");
        return new ResultadoLogin(true, "Código enviado al correo institucional.", true, u);
    }

    public ResultadoLogin loginPaso2(String username, String token) {
        if (usuarioDAO.verificarToken2FA(username, token)) {
            usuarioDAO.reiniciarIntentos(username); // Limpiar intentos fallidos
            reservaDAO.auditarEvento("SISTEMA", "LOGIN_EXITOSO", "OK", "User: " + username + " accedió al sistema.");
            Usuario u = usuarioDAO.autenticar(username, ""); // Solo para traer los datos (aunque pase clave vacía si no valida)
            // En un caso real, buscaríamos el usuario por username.
            return new ResultadoLogin(true, "Acceso concedido.", false, null);
        } else {
            reservaDAO.auditarEvento("SISTEMA", "LOGIN_2FA_FALLIDO", "RECHAZADO", "User: " + username);
            return new ResultadoLogin(false, "Código 2FA incorrecto.", false, null);
        }
    }
}