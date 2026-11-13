package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.dao.ReservaDAO;
import com.aircolombia.prototipo.service.ReservaService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FrmConsultarTiquete extends JDialog {
    private JTextField txtPnr, txtApellido;
    private JTextPane txtResultado;
    private ReservaDAO reservaDAO = new ReservaDAO();
    private ReservaService reservaService = new ReservaService();

    public FrmConsultarTiquete(JFrame padre) {
        super(padre, "Consultar Mi Tiquete", true);
        setSize(550, 500);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x0369A1)); // Color azul para diferenciar
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel lblTit = new JLabel("Estado de mi Reserva");
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTit.setForeground(Color.WHITE);
        header.add(lblTit, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel panelBusqueda = new JPanel(new GridBagLayout());
        panelBusqueda.setBackground(new Color(0xFAF7F0));
        panelBusqueda.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xD5CFC0)));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelBusqueda.add(new JLabel("PNR:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtPnr = new JTextField();
        panelBusqueda.add(txtPnr, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panelBusqueda.add(new JLabel("Apellido Titular:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0;
        txtApellido = new JTextField();
        panelBusqueda.add(txtApellido, gbc);

        gbc.gridx = 4;
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setBackground(new Color(0x0369A1));
        btnBuscar.setForeground(Color.WHITE);
        panelBusqueda.add(btnBuscar, gbc);

        add(panelBusqueda, BorderLayout.NORTH);

        txtResultado = new JTextPane();
        txtResultado.setEditable(false);
        txtResultado.setContentType("text/html");
        txtResultado.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(new JScrollPane(txtResultado), BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> buscar());
    }

    private void buscar() {
        String pnr = txtPnr.getText().trim().toUpperCase();
        String apellido = txtApellido.getText().trim();

        if (pnr.isEmpty() || apellido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar PNR y Apellido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!reservaService.consultarReservaSegura(pnr, apellido)) {
            txtResultado.setText("<html><body style='font-family:Segoe UI; color:#B91C1C; text-align:center; padding-top:50px;'>"
                    + "<h2>❌ No encontrado</h2><p>El PNR o el apellido no coinciden.</p></body></html>");
            return;
        }

        Object[] info = reservaDAO.obtenerInfoReserva(pnr);
        if (info != null) {
            String estado = reservaDAO.buscarPorPNR(pnr)[2].toString();
            String colorEstado = "#B45309"; // Naranja para pendiente
            if ("Confirmada".equals(estado)) colorEstado = "#15803D"; // Verde
            if ("Cancelada".equals(estado) || "Expirada".equals(estado)) colorEstado = "#B91C1C"; // Rojo

            String html = "<html><body style='font-family:Segoe UI; padding:15px;'>"
                    + "<div style='text-align:center; margin-bottom:20px;'>"
                    + "<h3 style='color:#6B6B6B; margin-bottom:5px;'>Estado del Tiquete</h3>"
                    + "<h1 style='color:" + colorEstado + "; text-transform:uppercase;'>" + estado + "</h1>"
                    + "</div>"
                    + "<hr style='border:1px solid #D5CFC0;'>"
                    + "<p><b>PNR:</b> " + pnr + "</p>"
                    + "<p><b>Pasajero:</b> " + info[5] + "</p>"
                    + "<p><b>Correo:</b> " + info[6] + "</p>"
                    + "<p><b>Ruta:</b> " + info[1] + " ➔ " + info[2] + "</p>"
                    + "<p><b>Fecha de Salida:</b> " + info[3] + "</p>"
                    + "<p><b>Clase:</b> " + info[7] + "</p>"
                    + "<p><b>Asientos:</b> " + info[9] + "</p>"
                    + "<p><b>Total Pagado:</b> $" + String.format("%,.0f", (Double)info[4]) + "</p>"
                    + "</body></html>";
            txtResultado.setText(html);
        }
    }
}