package com.aircolombia.prototipo.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FrmConfirmacion extends JDialog {

    private static final Color PRINCIPAL = new Color(0xC8102E);
    private static final Color PRINCIPAL_HOVER = new Color(0xA00D24);
    private static final Color VERDE_EXITO = new Color(0x15803D);
    private static final Color DORADO = new Color(0xD4AF37);
    private static final Color TEXTO = new Color(0x1A1A1A);

    public FrmConfirmacion(JFrame padre, String correo, String pnr) {
        super(padre, "Confirmación de Compra y Tiquete", true);
        initComponents(correo, pnr);
        setLocationRelativeTo(padre);
    }

    private void initComponents(String correo, String pnr) {
        setSize(440, 400);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        try {
            java.net.URL urlIcono = getClass().getResource("/logo.png");
            if (urlIcono != null) setIconImage(new ImageIcon(urlIcono).getImage());
        } catch (Exception ignored) {}

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRINCIPAL);
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblHeader = new JLabel("Confirmación de Pago y Tiquete");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeader.setForeground(Color.WHITE);
        header.add(lblHeader, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // CENTRO
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(Color.WHITE);
        contenido.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblCheck = new JLabel("OK", SwingConstants.CENTER);
        lblCheck.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblCheck.setForeground(Color.WHITE);
        lblCheck.setOpaque(true);
        lblCheck.setBackground(VERDE_EXITO);
        lblCheck.setPreferredSize(new Dimension(70, 70));
        lblCheck.setMaximumSize(new Dimension(70, 70));
        lblCheck.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblCheck.setBorder(BorderFactory.createLineBorder(DORADO, 3));
        contenido.add(lblCheck);

        contenido.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel lblTitulo = new JLabel("¡Pago Aprobado y Tiquete Emitido!");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(VERDE_EXITO);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(lblTitulo);

        contenido.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel lblPnr = new JLabel("PNR de Reserva: " + pnr);
        lblPnr.setFont(new Font("Consolas", Font.BOLD, 18));
        lblPnr.setForeground(TEXTO);
        lblPnr.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(lblPnr);

        // Tiquete Electrónico (RF-20)
        JLabel lblTiquete = new JLabel("Nro. Tiquete Electrónico: TKT-" + pnr + "-1");
        lblTiquete.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTiquete.setForeground(new Color(0x6B6B6B));
        lblTiquete.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(lblTiquete);

        contenido.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblCorreo = new JLabel("<html><div style='text-align:center;color:#6B6B6B;'>"
                + "La confirmación, el itinerario y su tiquete electrónico<br>"
                + "han sido enviados a:<br>"
                + "<b style='color:#1A1A1A;'>" + correo + "</b></div></html>");
        lblCorreo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCorreo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contenido.add(lblCorreo);

        add(contenido, BorderLayout.CENTER);

        // BOTONES
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        panelBoton.setBackground(Color.WHITE);
        panelBoton.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE5E5E5)));

        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setBackground(PRINCIPAL);
        btnAceptar.setForeground(Color.WHITE);
        btnAceptar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorderPainted(false);
        btnAceptar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAceptar.setPreferredSize(new Dimension(150, 38));
        btnAceptar.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnAceptar.setBackground(PRINCIPAL_HOVER); }
            @Override public void mouseExited(MouseEvent e) { btnAceptar.setBackground(PRINCIPAL); }
        });
        btnAceptar.addActionListener(e -> dispose());

        panelBoton.add(btnAceptar);
        add(panelBoton, BorderLayout.SOUTH);
    }
}