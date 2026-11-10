package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.model.Pasajero;
import com.aircolombia.prototipo.service.ReservaService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FrmCompra extends JDialog {

    private static final Color PRINCIPAL = new Color(0xC8102E);
    private static final Color PRINCIPAL_HOVER = new Color(0xA00D24);
    private static final Color GRIS_BTN = new Color(0xE5E5E5);
    private static final Color GRIS_BTN_HOVER = new Color(0xD0D0D0);
    private static final Color VERDE_EXITO = new Color(0x15803D);
    private static final Color NARANJA = new Color(0xEA580C);
    private static final Color ROJO = new Color(0xC8102E);
    private static final Color FONDO = Color.WHITE;
    private static final Color BORDE = new Color(0xD5CFC0);
    private static final Color TEXTO = new Color(0x1A1A1A);

    private static final int TIEMPO_TOTAL_SEGUNDOS = 15 * 60;

    private JComboBox<String> cmbMetodoPago;
    private JTextField txtTarjeta;
    private JCheckBox chkEsPasajero;

    private final List<CampoPasajero> camposPasajeros = new ArrayList<>();
    private CampoPasajero campoComprador;
    private JPanel panelComprador;

    private JButton btnPagar;
    private JButton btnCancelar;
    private JLabel lblTemporizador;

    private int idVueloIda;
    private Integer idVueloRegreso;
    private String claseViaje;
    private String pnr;
    private ReservaService reservaService;

    private int adultos, ninos, infantes;

    private Timer timer;
    private int segundosRestantes;
    private boolean pagoConfirmado = false;

    private static class CampoPasajero {
        String tipo;
        boolean esTitular;
        JTextField txtNombre, txtDocumento, txtCorreo, txtTelefono, txtDireccion, txtCiudad;

        CampoPasajero(String tipo, boolean esTitular, JTextField n, JTextField d, JTextField c,
                      JTextField t, JTextField dir, JTextField ciu) {
            this.tipo = tipo;
            this.esTitular = esTitular;
            this.txtNombre = n;
            this.txtDocumento = d;
            this.txtCorreo = c;
            this.txtTelefono = t;
            this.txtDireccion = dir;
            this.txtCiudad = ciu;
        }
    }

    public FrmCompra(JFrame padre, int idVueloIda, Integer idVueloRegreso,
                     String claseViaje, String pnr, int adultos, int ninos, int infantes) {
        super(padre, "Datos de Pasajeros y Pago", true);
        this.idVueloIda = idVueloIda;
        this.idVueloRegreso = idVueloRegreso;
        this.claseViaje = claseViaje;
        this.pnr = pnr;
        this.adultos = adultos;
        this.ninos = ninos;
        this.infantes = infantes;
        this.reservaService = new ReservaService();
        this.segundosRestantes = TIEMPO_TOTAL_SEGUNDOS;
        initComponents();
        iniciarTemporizador();
        setLocationRelativeTo(padre);
    }

    private void initComponents() {
        setSize(620, 850);
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        try {
            java.net.URL urlIcono = getClass().getResource("/logo.png");
            if (urlIcono != null) setIconImage(new ImageIcon(urlIcono).getImage());
        } catch (Exception ignored) {}

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRINCIPAL);
        header.setBorder(new EmptyBorder(18, 25, 18, 25));

        JLabel lblTitulo = new JLabel("Complete los datos de la reserva");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo, BorderLayout.WEST);

        JLabel lblPnrHeader = new JLabel("PNR: " + pnr);
        lblPnrHeader.setFont(new Font("Consolas", Font.BOLD, 14));
        lblPnrHeader.setForeground(Color.WHITE);
        header.add(lblPnrHeader, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // CENTRO
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(FONDO);

        // Aviso con timer
        JPanel panelAviso = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        panelAviso.setBackground(new Color(0xFDF6E3));
        panelAviso.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE8D9A8)));
        JLabel lblTexto = new JLabel("Oferta válida por:");
        lblTexto.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTexto.setForeground(new Color(0x8B6914));
        panelAviso.add(lblTexto);
        lblTemporizador = new JLabel("15:00");
        lblTemporizador.setFont(new Font("Consolas", Font.BOLD, 22));
        lblTemporizador.setForeground(VERDE_EXITO);
        panelAviso.add(lblTemporizador);
        JLabel lblSufijo = new JLabel("minutos");
        lblSufijo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSufijo.setForeground(new Color(0x8B6914));
        panelAviso.add(lblSufijo);
        panelCentral.add(panelAviso, BorderLayout.NORTH);

        // Contenido scrolleable
        JPanel panelScrollContent = new JPanel();
        panelScrollContent.setLayout(new BoxLayout(panelScrollContent, BoxLayout.Y_AXIS));
        panelScrollContent.setBackground(FONDO);
        panelScrollContent.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Info reserva
        double total = reservaService.calcularTotal(idVueloIda, idVueloRegreso, claseViaje, adultos, ninos, infantes);
        StringBuilder resumen = new StringBuilder();
        resumen.append(adultos).append(" adulto(s)");
        if (ninos > 0) resumen.append(", ").append(ninos).append(" niño(s)");
        if (infantes > 0) resumen.append(", ").append(infantes).append(" infante(s)");

        JLabel lblInfo = new JLabel("<html><b>Clase:</b> " + claseViaje + " &nbsp;|&nbsp; <b>Tipo:</b> "
                + (idVueloRegreso == null ? "Solo ida" : "Ida y vuelta")
                + "<br><b>Pasajeros:</b> " + resumen
                + "<br><b>Total a pagar:</b> $ " + String.format("%,.0f", total) + "</html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInfo.setForeground(new Color(0x6B6B6B));
        lblInfo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), new EmptyBorder(8, 10, 8, 10)));
        lblInfo.setOpaque(true);
        lblInfo.setBackground(new Color(0xFAF7F0));
        lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        panelScrollContent.add(lblInfo);
        panelScrollContent.add(Box.createRigidArea(new Dimension(0, 10)));

        // CheckBox RF-07
        chkEsPasajero = new JCheckBox("El comprador es uno de los pasajeros", true);
        chkEsPasajero.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkEsPasajero.setBackground(FONDO);
        chkEsPasajero.setForeground(PRINCIPAL);
        chkEsPasajero.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelScrollContent.add(chkEsPasajero);
        panelScrollContent.add(Box.createRigidArea(new Dimension(0, 8)));

        panelComprador = new JPanel();
        panelComprador.setLayout(new BoxLayout(panelComprador, BoxLayout.Y_AXIS));
        panelComprador.setBackground(FONDO);
        panelComprador.setVisible(false);
        panelComprador.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblComp = new JLabel("Datos del Comprador");
        lblComp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblComp.setForeground(PRINCIPAL);
        panelComprador.add(lblComp);
        panelComprador.add(Box.createRigidArea(new Dimension(0, 5)));
        panelComprador.add(crearBloqueGenerico("Comprador", false));
        panelComprador.add(Box.createRigidArea(new Dimension(0, 10)));
        panelScrollContent.add(panelComprador);

        // Sección Pasajeros
        JLabel lblSeccion = new JLabel("Datos de los pasajeros");
        lblSeccion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSeccion.setForeground(PRINCIPAL);
        lblSeccion.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelScrollContent.add(lblSeccion);
        panelScrollContent.add(Box.createRigidArea(new Dimension(0, 10)));

        int contAdulto = 0, contNino = 0, contInfante = 0;
        for (int i = 0; i < adultos; i++) {
            contAdulto++;
            panelScrollContent.add(crearBloquePasajero("Adulto " + contAdulto, contAdulto == 1));
            panelScrollContent.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        for (int i = 0; i < ninos; i++) {
            contNino++;
            panelScrollContent.add(crearBloquePasajero("Niño " + contNino, false));
            panelScrollContent.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        for (int i = 0; i < infantes; i++) {
            contInfante++;
            panelScrollContent.add(crearBloquePasajero("Infante " + contInfante, false));
            panelScrollContent.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        JScrollPane scrollPasajeros = new JScrollPane(panelScrollContent);
        scrollPasajeros.setBorder(null);
        scrollPasajeros.getVerticalScrollBar().setUnitIncrement(16);
        scrollPasajeros.getViewport().setBackground(FONDO);
        panelCentral.add(scrollPasajeros, BorderLayout.CENTER);

        // Panel de pago
        JPanel panelPago = new JPanel(new GridBagLayout());
        panelPago.setBackground(FONDO);
        panelPago.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE), new EmptyBorder(12, 25, 12, 25)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panelPago.add(crearLabel("Método de pago:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cmbMetodoPago = new JComboBox<>(new String[]{"Tarjeta de Crédito", "Tarjeta de Débito", "PSE"});
        cmbMetodoPago.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbMetodoPago.setBackground(Color.WHITE);
        cmbMetodoPago.setPreferredSize(new Dimension(200, 30));
        panelPago.add(cmbMetodoPago, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelPago.add(crearLabel("N° de tarjeta:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTarjeta = new JTextField(20);
        estilizarCampo(txtTarjeta);
        panelPago.add(txtTarjeta, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        JLabel lblHint = new JLabel("Simulación: si termina en 0 → pago rechazado.");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(new Color(0x6B6B6B));
        panelPago.add(lblHint, gbc);

        panelCentral.add(panelPago, BorderLayout.SOUTH);
        add(panelCentral, BorderLayout.CENTER);

        // BOTONES
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));
        panelBotones.setBackground(FONDO);
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE));

        btnCancelar = crearBoton("Cancelar", GRIS_BTN, GRIS_BTN_HOVER, TEXTO);
        btnCancelar.setPreferredSize(new Dimension(130, 38));

        btnPagar = crearBoton("Continuar al Pago", PRINCIPAL, PRINCIPAL_HOVER, Color.WHITE);
        btnPagar.setPreferredSize(new Dimension(150, 38));

        panelBotones.add(btnCancelar);
        panelBotones.add(btnPagar);
        add(panelBotones, BorderLayout.SOUTH);

        // EVENTOS
        btnCancelar.addActionListener(e -> cerrarVentana(false));
        btnPagar.addActionListener(e -> procesarPago());

        chkEsPasajero.addActionListener(e -> panelComprador.setVisible(!chkEsPasajero.isSelected()));

        cmbMetodoPago.addActionListener(e -> {
            boolean esPSE = "PSE".equals(cmbMetodoPago.getSelectedItem());
            txtTarjeta.setEnabled(!esPSE);
            if (esPSE) txtTarjeta.setText("");
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { cerrarVentana(false); }
        });
    }

       private JPanel crearBloquePasajero(String etiqueta, boolean esTitular) {
        // IMPORTANTE: Al final debe decir 'esTitular', NO 'false'
        return crearBloqueGenerico(etiqueta + (esTitular ? " (titular)" : ""), esTitular);
    }

    private JPanel crearBloqueGenerico(String etiqueta, boolean esTitular) {
        JPanel bloque = new JPanel(new GridBagLayout());
        bloque.setBackground(FONDO);
        bloque.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), new EmptyBorder(10, 12, 10, 12)));
        bloque.setAlignmentX(Component.LEFT_ALIGNMENT);
        bloque.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 3, 3, 3);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(PRINCIPAL);
        bloque.add(lbl, g);
        g.gridwidth = 1;

        JTextField txtNombre = new JTextField(18); estilizarCampo(txtNombre);
        JTextField txtDocumento = new JTextField(18); estilizarCampo(txtDocumento);
        JTextField txtTelefono = new JTextField(18); estilizarCampo(txtTelefono);
        JTextField txtDireccion = new JTextField(18); estilizarCampo(txtDireccion);
        JTextField txtCiudad = new JTextField(18); estilizarCampo(txtCiudad);
        JTextField txtCorreo = new JTextField(18); estilizarCampo(txtCorreo);

        g.gridx = 0; g.gridy = 1; g.weightx = 0; bloque.add(crearLabel("Nombre:"), g);
        g.gridx = 1; g.weightx = 1.0; bloque.add(txtNombre, g);

        g.gridx = 0; g.gridy = 2; g.weightx = 0; bloque.add(crearLabel("Documento:"), g);
        g.gridx = 1; g.weightx = 1.0; bloque.add(txtDocumento, g);

        g.gridx = 0; g.gridy = 3; g.weightx = 0; bloque.add(crearLabel("Teléfono:"), g);
        g.gridx = 1; g.weightx = 1.0; bloque.add(txtTelefono, g);

        g.gridx = 0; g.gridy = 4; g.weightx = 0; bloque.add(crearLabel("Dirección:"), g);
        g.gridx = 1; g.weightx = 1.0; bloque.add(txtDireccion, g);

        g.gridx = 0; g.gridy = 5; g.weightx = 0; bloque.add(crearLabel("Ciudad:"), g);
        g.gridx = 1; g.weightx = 1.0; bloque.add(txtCiudad, g);

        g.gridx = 0; g.gridy = 6; g.weightx = 0; bloque.add(crearLabel("Correo:"), g);
        g.gridx = 1; g.weightx = 1.0; bloque.add(txtCorreo, g);

        CampoPasajero cp = new CampoPasajero(etiqueta, esTitular, txtNombre, txtDocumento, txtCorreo, txtTelefono, txtDireccion, txtCiudad);
        
        if (etiqueta.equals("Comprador")) {
            campoComprador = cp;
        } else {
            camposPasajeros.add(cp);
        }

        return bloque;
    }

    private void iniciarTemporizador() {
        timer = new Timer(1000, e -> {
            segundosRestantes--;
            actualizarContador();
            if (segundosRestantes <= 0) {
                timer.stop();
                expirarReserva();
            }
        });
        timer.start();
    }

    private void actualizarContador() {
        int minutos = segundosRestantes / 60;
        int segundos = segundosRestantes % 60;
        lblTemporizador.setText(String.format("%02d:%02d", minutos, segundos));

        if (segundosRestantes > 300) lblTemporizador.setForeground(VERDE_EXITO);
        else if (segundosRestantes > 120) lblTemporizador.setForeground(NARANJA);
        else lblTemporizador.setForeground(ROJO);
    }

    private void expirarReserva() {
        reservaService.expirarReserva(pnr);
        JOptionPane.showMessageDialog(this, "El tiempo para completar la compra ha expirado.\n\nLa reserva " + pnr + " ha sido cancelada.", "Reserva expirada", JOptionPane.WARNING_MESSAGE);
        pagoConfirmado = true;
        dispose();
    }

    private void cerrarVentana(boolean fueExitoso) {
        if (pagoConfirmado) {
            if (timer != null) timer.stop();
            dispose();
            return;
        }
        if (!fueExitoso) {
            int confirm = JOptionPane.showConfirmDialog(this, "Si cierra esta ventana, la reserva " + pnr + "\nserá cancelada.\n\n¿Desea salir?", "Confirmar cancelación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            reservaService.expirarReserva(pnr);
        }
        if (timer != null) timer.stop();
        pagoConfirmado = true;
        dispose();
    }

    // Método que usa FrmResumenCompra para cerrar esta ventana al terminar el pago
    public void forzarCierrePorPago() {
        pagoConfirmado = true;
        if (timer != null) timer.stop();
        dispose();
    }

       private void procesarPago() {
        // 1. Validar datos del COMPRADOR (si aplica)
        Pasajero comprador = null;
        if (!chkEsPasajero.isSelected()) {
            String nombre = campoComprador.txtNombre.getText().trim();
            String doc = campoComprador.txtDocumento.getText().trim();
            String telefono = campoComprador.txtTelefono.getText().trim();
            String direccion = campoComprador.txtDireccion.getText().trim();
            String ciudad = campoComprador.txtCiudad.getText().trim();
            String correo = campoComprador.txtCorreo.getText().trim();

            if (nombre.isEmpty() || doc.isEmpty() || telefono.isEmpty() || direccion.isEmpty() || ciudad.isEmpty() || correo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los datos del Comprador.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            comprador = new Pasajero("CC", doc, nombre, correo, telefono, direccion, ciudad);
        }

        // 2. Validar datos de los PASAJEROS
        List<Pasajero> pasajeros = new ArrayList<>();
        Set<String> docsVistos = new HashSet<>();

        for (CampoPasajero c : camposPasajeros) {
            String nombre = c.txtNombre.getText().trim();
            String doc = c.txtDocumento.getText().trim();
            String telefono = c.txtTelefono.getText().trim();
            String direccion = c.txtDireccion.getText().trim();
            String ciudad = c.txtCiudad.getText().trim();
            String correo = c.txtCorreo.getText().trim(); // <--- AQUÍ SE LEE EL CORREO DE LA CAJA DE TEXTO

            if (nombre.isEmpty() || doc.isEmpty() || telefono.isEmpty() || direccion.isEmpty() || ciudad.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos de:\n" + c.tipo, "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!docsVistos.add(doc)) {
                JOptionPane.showMessageDialog(this, "El documento " + doc + " está repetido entre pasajeros.", "Documento duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            pasajeros.add(new Pasajero("CC", doc, nombre, correo.isEmpty() ? null : correo, telefono, direccion, ciudad));
        }

        // 3. Validar método de pago
        String metodo = (String) cmbMetodoPago.getSelectedItem();
        String tarjeta = txtTarjeta.getText().trim().replaceAll("\\s+", "");
        if (!"PSE".equals(metodo)) {
            if (tarjeta.isEmpty() || !tarjeta.matches("\\d{16}")) {
                JOptionPane.showMessageDialog(this, "El número de tarjeta debe tener 16 dígitos.", "Tarjeta inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            tarjeta = "N/A";
        }

        // 4. DEFINIR EL CORREO DESTINO
        // Si el comprador es otra persona, usamos su correo. Si es pasajero, usamos el correo del primer pasajero (Adulto 1).
        String correoDestinoFinal = (comprador != null) ? comprador.getEmail() : pasajeros.get(0).getEmail();

        // Validación de seguridad: si la caja de texto estaba vacía, avisa y detiene.
        if (correoDestinoFinal == null || correoDestinoFinal.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, escribe un correo electrónico válido en los datos del pasajero.", "Falta el correo", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double total = reservaService.calcularTotal(idVueloIda, idVueloRegreso, claseViaje, adultos, ninos, infantes);

        // 5. Abrir el resumen pasando el correo EXACTO que se leyó de la caja de texto
        FrmResumenCompra resumen = new FrmResumenCompra(this, pnr, idVueloIda, idVueloRegreso, claseViaje, pasajeros, comprador, total, metodo, tarjeta, correoDestinoFinal);
        resumen.setVisible(true);
    }

    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXTO);
        return label;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE), new EmptyBorder(6, 10, 6, 10)));
        campo.setBackground(Color.WHITE);
        campo.setForeground(TEXTO);
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