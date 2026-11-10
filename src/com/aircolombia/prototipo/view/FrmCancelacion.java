package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.dao.ReservaDAO;
import com.aircolombia.prototipo.service.ReservaService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FrmCancelacion extends JDialog {

    private static final Color PRINCIPAL = new Color(0xC8102E);
    private static final Color PRINCIPAL_HOVER = new Color(0xA00D24);
    private static final Color ROJO_PELIGRO = new Color(0xB91C1C);
    private static final Color ROJO_PELIGRO_HOVER = new Color(0x991B1B);
    private static final Color GRIS_BTN = new Color(0xE5E5E5);
    private static final Color GRIS_BTN_HOVER = new Color(0xD0D0D0);
    private static final Color BORDE = new Color(0xD5CFC0);
    private static final Color TEXTO = new Color(0x1A1A1A);
    private static final Color VERDE = new Color(0x15803D);

    private JTextField txtPNR;
    private JTextField txtApellido; // NUEVO CAMPO RF-21
    private JButton btnBuscar;
    private JButton btnCancelarTiquete;
    private JButton btnCerrar;
    private JPanel panelInfo;
    private ReservaDAO reservaDAO;
    private ReservaService reservaService;

    public FrmCancelacion(JFrame padre) {
        super(padre, "Cancelar Tiquete", true);
        reservaDAO = new ReservaDAO();
        reservaService = new ReservaService();
        initComponents();
        setLocationRelativeTo(padre);
    }

    private void initComponents() {
        setSize(540, 660); // Ajusté un poco el alto para que quepa el nuevo campo
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        try {
            java.net.URL urlIcono = getClass().getResource("/logo.png");
            if (urlIcono != null) setIconImage(new ImageIcon(urlIcono).getImage());
        } catch (Exception ignored) {}

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRINCIPAL);
        header.setBorder(new EmptyBorder(18, 25, 18, 25));
        JLabel lblTitulo = new JLabel("Cancelar Tiquete");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(0, 15));
        contenido.setBackground(Color.WHITE);
        contenido.setBorder(new EmptyBorder(25, 30, 20, 30));

        JPanel panelBusqueda = new JPanel(new GridBagLayout());
        panelBusqueda.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // FILA 0: PNR
        JLabel lblPNR = new JLabel("Ingrese su PNR:");
        lblPNR.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPNR.setForeground(TEXTO);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panelBusqueda.add(lblPNR, gbc);

        txtPNR = new JTextField(15);
        txtPNR.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPNR.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(6, 10, 6, 10)));
        gbc.gridx = 1; gbc.weightx = 1.0;
        panelBusqueda.add(txtPNR, gbc);

        btnBuscar = crearBoton("Buscar", PRINCIPAL, PRINCIPAL_HOVER, Color.WHITE);
        btnBuscar.setPreferredSize(new Dimension(110, 36));
        gbc.gridx = 2; gbc.weightx = 0;
        panelBusqueda.add(btnBuscar, gbc);

        // FILA 1: APELLIDO (RF-21)
        JLabel lblApellido = new JLabel("Apellido del Titular:");
        lblApellido.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblApellido.setForeground(TEXTO);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelBusqueda.add(lblApellido, gbc);

        txtApellido = new JTextField(15);
        txtApellido.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtApellido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(6, 10, 6, 10)));
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panelBusqueda.add(txtApellido, gbc);

        contenido.add(panelBusqueda, BorderLayout.NORTH);

        panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(new Color(0xFAF7F0));
        panelInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(15, 20, 15, 20)));
        panelInfo.setVisible(false);

        contenido.add(panelInfo, BorderLayout.CENTER);
        add(contenido, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelBotones.setBackground(Color.WHITE);
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE));

        btnCancelarTiquete = crearBoton("Cancelar Tiquete", ROJO_PELIGRO, ROJO_PELIGRO_HOVER, Color.WHITE);
        btnCancelarTiquete.setPreferredSize(new Dimension(170, 38));
        btnCancelarTiquete.setEnabled(false);

        btnCerrar = crearBoton("Cerrar", GRIS_BTN, GRIS_BTN_HOVER, TEXTO);
        btnCerrar.setPreferredSize(new Dimension(130, 38));

        panelBotones.add(btnCancelarTiquete);
        panelBotones.add(btnCerrar);
        add(panelBotones, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscarReserva());
        txtPNR.addActionListener(e -> buscarReserva());
        txtApellido.addActionListener(e -> buscarReserva()); // Enter en apellido también busca
        btnCancelarTiquete.addActionListener(e -> confirmarCancelacion());
        btnCerrar.addActionListener(e -> dispose());
    }

    private void buscarReserva() {
        String pnr = txtPNR.getText().trim().toUpperCase();
        String apellido = txtApellido.getText().trim();

        if (pnr.isEmpty() || apellido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el PNR y el apellido del titular.",
                    "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // RF-21: Validar seguridad (PNR + Apellido)
        if (!reservaService.consultarReservaSegura(pnr, apellido)) {
            JOptionPane.showMessageDialog(this, "El código PNR o el apellido no coinciden con nuestra base de datos.",
                    "Error de Seguridad", JOptionPane.ERROR_MESSAGE);
            limpiarInfo();
            return;
        }

        Object[] info = reservaDAO.buscarPorPNR(pnr);
        if (info == null) {
            JOptionPane.showMessageDialog(this, "No existe una reserva con el PNR: " + pnr,
                    "PNR no encontrado", JOptionPane.ERROR_MESSAGE);
            limpiarInfo();
            return;
        }

        String estado = (String) info[2];

        if ("Cancelada".equals(estado)) {
            JOptionPane.showMessageDialog(this, "La reserva con PNR " + pnr + " ya fue cancelada.",
                    "Reserva cancelada", JOptionPane.WARNING_MESSAGE);
            limpiarInfo();
            return;
        }
        if ("Expirada".equals(estado)) {
            JOptionPane.showMessageDialog(this, "La reserva con PNR " + pnr + " ya expiró.",
                    "Reserva expirada", JOptionPane.WARNING_MESSAGE);
            limpiarInfo();
            return;
        }
        if (!"Confirmada".equals(estado)) {
            JOptionPane.showMessageDialog(this,
                    "La reserva no se puede cancelar.\nEstado actual: " + estado,
                    "Estado no válido", JOptionPane.WARNING_MESSAGE);
            limpiarInfo();
            return;
        }

        mostrarInfo(pnr);
        btnCancelarTiquete.setEnabled(true);
    }

    private void mostrarInfo(String pnr) {
        Object[] info = reservaDAO.obtenerInfoReserva(pnr);
        if (info == null) {
            panelInfo.setVisible(false);
            return;
        }

        panelInfo.removeAll();

        String origen = (String) info[1];
        String destino = (String) info[2];
        String fecha = (String) info[3];
        double total = (Double) info[4];
        String nombre = (String) info[5];
        String correo = (String) info[6];
        String clase = (String) info[7];
        String tipo = (String) info[8];
        if (clase == null) clase = ReservaService.CLASE_ECONOMICA;

        // RN-07: penalidad
        double factorPenalidad = ReservaService.obtenerPenalidadCancelacion(clase);
        double penalidad = total * factorPenalidad;
        double reembolso = total - penalidad;
        int pct = (int) Math.round(factorPenalidad * 100);

        panelInfo.add(crearFilaInfo("PNR:", pnr, true));
        panelInfo.add(Box.createRigidArea(new Dimension(0, 6)));
        panelInfo.add(crearFilaInfo("Pasajero:", nombre != null ? nombre : "N/D", false));
        panelInfo.add(Box.createRigidArea(new Dimension(0, 6)));
        panelInfo.add(crearFilaInfo("Correo:", correo != null ? correo : "N/D", false));
        panelInfo.add(Box.createRigidArea(new Dimension(0, 6)));
        panelInfo.add(crearFilaInfo("Ruta:", origen + " → " + destino, false));
        panelInfo.add(Box.createRigidArea(new Dimension(0, 6)));
        panelInfo.add(crearFilaInfo("Fecha de ida:", fecha, false));
        panelInfo.add(Box.createRigidArea(new Dimension(0, 6)));
        panelInfo.add(crearFilaInfo("Clase:", clase, false));
        panelInfo.add(Box.createRigidArea(new Dimension(0, 6)));
        panelInfo.add(crearFilaInfo("Tipo de viaje:",
                "Ida_Y_Vuelta".equals(tipo) ? "Ida y vuelta" : "Solo ida", false));
        panelInfo.add(Box.createRigidArea(new Dimension(0, 10)));

        // Desglose RN-07
        JLabel lblDesglose = new JLabel("<html><b style='color:#C8102E;'>Desglose de cancelación (RN-07)</b></html>");
        lblDesglose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDesglose.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelInfo.add(lblDesglose);
        panelInfo.add(Box.createRigidArea(new Dimension(0, 6)));

        panelInfo.add(crearFilaInfo("Total pagado:", "$ " + String.format("%,.0f", total), false));
        panelInfo.add(Box.createRigidArea(new Dimension(0, 4)));
        panelInfo.add(crearFilaInfo("Penalidad (" + pct + "%):",
                "-$ " + String.format("%,.0f", penalidad), false));
        panelInfo.add(Box.createRigidArea(new Dimension(0, 4)));
        panelInfo.add(crearFilaInfoDestacada("Reembolso:",
                "$ " + String.format("%,.0f", reembolso), VERDE));
        panelInfo.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel lblAviso = new JLabel("<html><i>La penalidad se aplica según la clase tarifaria "
                + "adquirida. Al confirmar, la reserva queda cancelada y los cupos se liberan.</i></html>");
        lblAviso.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblAviso.setForeground(new Color(0x6B6B6B));
        lblAviso.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelInfo.add(lblAviso);

        panelInfo.setVisible(true);
        panelInfo.revalidate();
        panelInfo.repaint();
    }

    private JPanel crearFilaInfo(String etiqueta, String valor, boolean destacado) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setBackground(new Color(0xFAF7F0));
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEtiqueta.setForeground(new Color(0x6B6B6B));
        lblEtiqueta.setPreferredSize(new Dimension(140, 20));

        JLabel lblValor = new JLabel(valor);
        if (destacado) {
            lblValor.setFont(new Font("Consolas", Font.BOLD, 15));
            lblValor.setForeground(PRINCIPAL);
        } else {
            lblValor.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblValor.setForeground(TEXTO);
        }

        fila.add(lblEtiqueta, BorderLayout.WEST);
        fila.add(lblValor, BorderLayout.CENTER);
        return fila;
    }

    private JPanel crearFilaInfoDestacada(String etiqueta, String valor, Color color) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setBackground(new Color(0xFAF7F0));
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lblEtiqueta = new JLabel(etiqueta);
        lblEtiqueta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEtiqueta.setForeground(new Color(0x6B6B6B));
        lblEtiqueta.setPreferredSize(new Dimension(140, 20));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblValor.setForeground(color);

        fila.add(lblEtiqueta, BorderLayout.WEST);
        fila.add(lblValor, BorderLayout.CENTER);
        return fila;
    }

    private void confirmarCancelacion() {
        String pnr = txtPNR.getText().trim().toUpperCase();

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de cancelar este tiquete?\n\n"
                + "Se aplicará la penalidad según la clase (RN-07).\n"
                + "Esta acción no se puede deshacer.",
                "Confirmar cancelación", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        Object[] resultado = reservaService.cancelarTiqueteConDetalle(pnr);
        boolean exito = (Boolean) resultado[0];
        String mensaje = (String) resultado[1];

        if (exito) {
            JOptionPane.showMessageDialog(this, mensaje,
                    "Cancelación exitosa", JOptionPane.INFORMATION_MESSAGE);
            limpiarInfo();
            txtPNR.setText("");
            txtApellido.setText("");
        } else {
            JOptionPane.showMessageDialog(this, mensaje,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarInfo() {
        panelInfo.setVisible(false);
        btnCancelarTiquete.setEnabled(false);
    }

    private JButton crearBoton(String texto, Color normal, Color hover, Color fg) {
        JButton boton = new JButton(texto);
        boton.setBackground(normal);
        boton.setForeground(fg);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { boton.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { boton.setBackground(normal); }
        });
        return boton;
    }
}