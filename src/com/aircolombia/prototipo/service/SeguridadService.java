package com.aircolombia.prototipo.service;

import com.aircolombia.prototipo.dao.ReservaDAO;
import com.aircolombia.prototipo.dao.UsuarioDAO;
import com.aircolombia.prototipo.model.Usuario;
import com.aircolombia.prototipo.utils.EmailService;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SeguridadService {
    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private ReservaDAO reservaDAO = new ReservaDAO();

    private static Map<String, RegistroPendiente> registrosPendientes = new HashMap<>();

    private static class RegistroPendiente {
        String username, password, email, token;
        public RegistroPendiente(String username, String password, String email, String token) {
            this.username = username; this.password = password; this.email = email; this.token = token;
        }
    }

    public static class ResultadoLogin {
        public boolean exito;
        public String mensaje;
        public boolean requiere2FA;
        public Usuario usuario;

        public ResultadoLogin(boolean exito, String mensaje, boolean requiere2FA, Usuario usuario) {
            this.exito = exito; this.mensaje = mensaje; this.requiere2FA = requiere2FA; this.usuario = usuario;
        }
    }

    public ResultadoLogin loginPaso1(String username, String password, String requiredRole) {
        Usuario u = usuarioDAO.buscarPorUsername(username);
        if (u == null) return new ResultadoLogin(false, "El usuario no existe.", false, null);
        if (u.estaBloqueado()) return new ResultadoLogin(false, "Cuenta bloqueada temporalmente. Intente más tarde.", false, null);

        Usuario auth = usuarioDAO.autenticar(username, password);
        if (auth == null) {
            usuarioDAO.registrarIntentoFallido(username);
            reservaDAO.auditarEvento("SISTEMA", "LOGIN_FALLIDO", "RECHAZADO", "User: " + username + " pass incorrecta.");
            
            Usuario check = usuarioDAO.buscarPorUsername(username);
            if (check.estaBloqueado()) {
                return new ResultadoLogin(false, "Límite de intentos alcanzado. Cuenta BLOQUEADA por 15 min.", false, null);
            }
            return new ResultadoLogin(false, "Contraseña incorrecta. Intento " + check.getIntentosFallidos() + " de 3.", false, null);
        }

        // VALIDACIÓN DE ROL ANTES DE ENVIAR EL CORREO
        if (requiredRole != null && !requiredRole.equalsIgnoreCase(u.getRol())) {
            reservaDAO.auditarEvento("SISTEMA", "LOGIN_RECHAZADO_ROL", "RECHAZADO", "User: " + username + " intentó acceder como " + requiredRole);
            return new ResultadoLogin(false, "Acceso Denegado. Tu cuenta no tiene permisos de " + requiredRole + ".", false, null);
        }

        String token = String.format("%06d", new Random().nextInt(999999));
        usuarioDAO.guardarToken2FA(username, token);
        
        boolean enviado = EmailService.enviarCodigoAcceso(u.getEmail(), username, token);
        if (!enviado) return new ResultadoLogin(false, "Error al enviar el correo 2FA.", false, null);

        return new ResultadoLogin(true, "Código enviado a tu correo.", true, u);
    }

    public ResultadoLogin loginPaso2(String username, String token) {
        Usuario u = usuarioDAO.buscarPorUsername(username);
        if (u.estaBloqueado()) {
            return new ResultadoLogin(false, "Cuenta BLOQUEADA. Intente de nuevo más tarde.", false, null);
        }

        if (usuarioDAO.verificarToken2FA(username, token)) {
            usuarioDAO.reiniciarIntentos(username);
            reservaDAO.auditarEvento("SISTEMA", "LOGIN_EXITOSO", "OK", "User: " + username + " accedió.");
            return new ResultadoLogin(true, "Bienvenido " + u.getUsername(), false, u);
        } else {
            usuarioDAO.registrarIntentoFallido(username);
            reservaDAO.auditarEvento("SISTEMA", "LOGIN_2FA_FALLIDO", "RECHAZADO", "User: " + username + " token incorrecto.");
            
            Usuario check = usuarioDAO.buscarPorUsername(username);
            if (check.estaBloqueado()) {
                return new ResultadoLogin(false, "Código incorrecto. Límite de intentos alcanzado. Cuenta BLOQUEADA.", false, null);
            }
            return new ResultadoLogin(false, "Código incorrecto. Intento " + check.getIntentosFallidos() + " de 3.", false, null);
        }
    }

    public boolean solicitarRegistro(String username, String password, String email) {
        if (usuarioDAO.buscarPorUsername(username) != null) return false;
        String token = String.format("%06d", new Random().nextInt(999999));
        registrosPendientes.put(username, new RegistroPendiente(username, password, email, token));
        return EmailService.enviarCodigoAcceso(email, username, token);
    }

    public boolean confirmarRegistro(String username, String tokenIngresado) {
        RegistroPendiente pendiente = registrosPendientes.get(username);
        if (pendiente == null) return false;

        if (pendiente.token.equals(tokenIngresado)) {
            Usuario u = new Usuario();
            u.setUsername(pendiente.username);
            u.setPasswordHash(pendiente.password);
            u.setEmail(pendiente.email);
            u.setRol("CLIENTE");
            
            boolean guardado = usuarioDAO.registrarUsuario(u);
            if (guardado) {
                registrosPendientes.remove(username);
                reservaDAO.auditarEvento("SISTEMA", "REGISTRO_EXITOSO", "OK", "Nuevo usuario: " + username);
                return true;
            }
        }
        return false;
    }
}