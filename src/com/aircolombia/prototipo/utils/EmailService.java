package com.aircolombia.prototipo.utils;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

    // ⚠️ CAMBIA ESTOS DATOS POR LOS TUYOS O USA TU CONFIG.PROPERTIES
    private static final String REMITENTE = "arangoalejandro360@gmail.com"; 
    private static final String PASSWORD = "ixim uqwk eegu gudn"; 

    public static boolean enviarCodigoConfirmacion(String destinatario, String pnr, String codigo) {
        if (destinatario == null || destinatario.trim().isEmpty() || !destinatario.contains("@")) {
            System.out.println("Error: El correo del destinatario es nulo o inválido.");
            return false; 
        }
        
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("Código de Confirmación de Pago - AeroSky");

            String htmlContent = "<div style='font-family: Arial, sans-serif; text-align: center; padding: 20px;'>"
                    + "<h2 style='color: #C8102E;'>AeroSky</h2>"
                    + "<p>Hemos recibido una solicitud de pago para tu reserva con PNR: <b>" + pnr + "</b></p>"
                    + "<p>Para completar la transacción, ingresa el siguiente código en la aplicación:</p>"
                    + "<h1 style='color: #15803D; letter-spacing: 5px; font-size: 36px;'>" + codigo + "</h1>"
                    + "<p style='color: #888;'>Si no solicitaste este pago, puedes ignorar este correo.</p>"
                    + "</div>";

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("Correo de confirmación enviado a: " + destinatario);
            return true;

        } catch (MessagingException e) {
            System.out.println("Error al enviar correo: " + e.getMessage());
            return false;
        }
    }

    // ---------- CORREO DE BLOQUEO DE SEGURIDAD ----------
    public static boolean enviarCorreoBloqueo(String destinatario, String pnr,
                                              java.sql.Timestamp bloqueadoHasta,
                                              String motivo) {
        if (destinatario == null || destinatario.trim().isEmpty() || !destinatario.contains("@")) {
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("AeroSky - Reserva bloqueada temporalmente por seguridad");

            String motivoTexto = "MAX_INTENTOS".equals(motivo)
                ? "Se detectaron varios intentos fallidos de verificación del código."
                : "El código de verificación expiró antes de ser utilizado.";

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String horaDesbloqueo = (bloqueadoHasta != null) ? sdf.format(bloqueadoHasta) : "N/A";

            String html = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2 style='color: #C8102E;'>AeroSky</h2>"
                + "<h3>Reserva bloqueada temporalmente</h3>"
                + "<p>Por motivos de seguridad, la reserva con PNR <b>" + pnr + "</b> "
                + "ha sido bloqueada temporalmente.</p>"
                + "<p><b>Motivo:</b> " + motivoTexto + "</p>"
                + "<p><b>Hora de desbloqueo automático:</b> " + horaDesbloqueo + "</p>"
                + "<p>Si no reconoces esta actividad, por favor contacta a nuestro equipo "
                + "de atención al cliente.</p>"
                + "</div>";

            message.setContent(html, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("Correo de bloqueo enviado a: " + destinatario);
            return true;
        } catch (MessagingException e) {
            System.out.println("Error al enviar correo de bloqueo: " + e.getMessage());
            return false;
        }
    }

    // ---------- NUEVO: CORREO DE EMISIÓN DE TIQUETE (RF-20) ----------
    public static boolean enviarCorreoTiquete(String destinatario, String pnr, String origen, 
                                              String destino, String fechaSalida, double total) {
        if (destinatario == null || destinatario.trim().isEmpty() || !destinatario.contains("@")) {
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("¡Tu tiquete AeroSky está listo! - PNR: " + pnr);

            String html = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ccc; max-width: 600px;'>"
                + "<h2 style='color: #C8102E; text-align: center;'>AeroSky - Confirmación de Compra</h2>"
                + "<p>¡Gracias por volar con nosotros! Tu pago ha sido aprobado y tu tiquete electrónico ha sido emitido.</p>"
                + "<div style='background-color: #f9f9f9; padding: 15px; border-radius: 5px;'>"
                + "<p><b>PNR (Código de Reserva):</b> <span style='font-size: 20px; color: #15803D; font-weight: bold;'>" + pnr + "</span></p>"
                + "<p><b>Itinerario:</b> " + origen + " ➔ " + destino + "</p>"
                + "<p><b>Fecha de Salida:</b> " + fechaSalida + "</p>"
                + "<p><b>Total Pagado:</b> $ " + String.format("%,.0f", total) + "</p>"
                + "</div>"
                + "<p style='text-align: center; margin-top: 20px;'>"
                + "<a href='#' style='background-color: #C8102E; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Descargar Tiquete PDF (Próximamente)</a>"
                + "</p>"
                + "<p style='color: #888; font-size: 12px; text-align: center;'>Presenta este código PNR en el aeropuerto para tu check-in.</p>"
                + "</div>";

            message.setContent(html, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("Correo de tiquete emitido enviado a: " + destinatario);
            return true;
        } catch (MessagingException e) {
            System.out.println("Error al enviar correo de tiquete: " + e.getMessage());
            return false;
        }
    }
        // ---------- RF-25: CORREO DE VUELO CANCELADO ----------
    public static boolean enviarCorreoVueloCancelado(String destinatario, int idVuelo, String motivo) {
        if (destinatario == null || destinatario.trim().isEmpty() || !destinatario.contains("@")) return false;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("AeroSky - ALERTA: Cambios en tu vuelo operacional");

            String html = "<div style='font-family: Arial, sans-serif; padding: 20px; border-left: 5px solid #C8102E;'>"
                + "<h2 style='color: #C8102E;'>AeroSky</h2>"
                + "<h3>Afectación Operacional en tu vuelo</h3>"
                + "<p>Te informamos que tu vuelo (ID: " + idVuelo + ") ha presentado la siguiente novedad:</p>"
                + "<p style='background-color: #f9f9f9; padding: 10px;'><b>Motivo:</b> " + motivo + "</p>"
                + "<p>Por favor, comunícate con nuestra línea de atención para gestionar tu reubicación, cambio de fecha o solicitud de reembolso sin penalidad.</p>"
                + "<p>Lamentamos los inconvenientes ocasionados.</p>"
                + "</div>";

            message.setContent(html, "text/html; charset=utf-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.out.println("Error al enviar correo de vuelo cancelado: " + e.getMessage());
            return false;
        }
    }
}