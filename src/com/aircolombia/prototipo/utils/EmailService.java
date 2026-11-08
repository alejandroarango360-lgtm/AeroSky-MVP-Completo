package com.aircolombia.prototipo.utils;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {

    // ⚠️ IMPORTANTE: Genera una NUEVA contraseña de aplicación en tu cuenta de Google.
    // La anterior fue expuesta y Google probablemente la bloqueó.
    private static final String REMITENTE = "arangoalejandro360@gmail.com"; 
    private static final String PASSWORD = "ixim uqwk eegu gudn"; // CAMBIA ESTO POR UNA NUEVA

    public static boolean enviarCodigoConfirmacion(String destinatario, String pnr, String codigo) {
        if (destinatario == null || destinatario.trim().isEmpty() || !destinatario.contains("@")) {
            System.out.println("Error local: El correo del destinatario es nulo o inválido.");
            return false; 
        }
        
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true"); // Forzar SSL

        // Usar getInstance en lugar de getDefaultInstance para evitar problemas de caché
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario.trim()));
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
            System.out.println("✅ Correo de confirmación enviado a: " + destinatario);
            return true;

        } catch (MessagingException e) {
            System.out.println("❌ Error al enviar correo: " + e.getMessage());
            e.printStackTrace(); // <-- ESTO TE DIRÁ EL ERROR REAL EN LA CONSOLA
            return false;
        }
    }

    // ---------- CORREO DE BLOQUEO DE SEGURIDAD ----------
    public static boolean enviarCorreoBloqueo(String destinatario, String pnr,
                                              java.sql.Timestamp bloqueadoHasta,
                                              String motivo) {
        if (destinatario == null || destinatario.trim().isEmpty() || !destinatario.contains("@")) {
            System.out.println("Error local: correo destinatario inválido para notificar bloqueo.");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(REMITENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario.trim()));
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
                + "<p style='color:#888;'>Este es un mensaje automático, no respondas a este correo.</p>"
                + "</div>";

            message.setContent(html, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("✅ Correo de bloqueo enviado a: " + destinatario);
            return true;
        } catch (MessagingException e) {
            System.out.println("❌ Error al enviar correo de bloqueo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}