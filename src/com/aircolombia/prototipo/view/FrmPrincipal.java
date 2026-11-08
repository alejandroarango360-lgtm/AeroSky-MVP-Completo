package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.dao.VueloDAO;
import com.aircolombia.prototipo.model.Vuelo;
import com.aircolombia.prototipo.service.ReservaService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FrmPrincipal extends JFrame {

    private static final Color PRINCIPAL = new Color(0xC8102E);
    private static final Color PRINCIPAL_HOVER = new Color(0xA00D24);
    private static final Color GRIS_BTN = new Color(0xE5E5E5);
    private static final Color GRIS_BTN_HOVER = new Color(0xD0D0D0);
    private static final Color FONDO = new Color(0xFAF7F0);
    private static final Color BORDE = new Color(0xD5CFC0);
    private static final Color TEXTO = new Color(0x1A1A1A);
    private static final Color AMARILLO_AVISO = new Color(0xFFF8E1);
    private static final Color BORDE_AVISO = new Color(0xE8D9A8);
    private static final Color TEXTO_AVISO = new Color(0x8B6914);

    // ===== Búsqueda =====
    private JComboBox<String> cmbOrigen;
    private JComboBox<String> cmbDestino;
    private JTextField txtFechaIda;
    private JTextField txtFechaRegreso;
    private JCheckBox chkIdaVuelta;
    private JSpinner spnAdultos;
    private JSpinner spnNinos; // Ahora "Jóvenes"
    private JSpinner spnInfantes;
    private JComboBox<String> cmbClase;

    // ===== RF-04: ordenamiento y filtros =====
    private JComboBox<String> cbOrden;
    private JComboBox<String> cbFiltroEscalas;
    private JComboBox<String> cbFiltroHorario;
    private JTextField txtPrecioMax;
    private JButton btnLimpiarFiltros;
    private JLabel lblEstadoResultados;

    // ===== RF-05: flujo de ida y vuelta =====
    private JPanel panelFase;
    private JLabel lblPasoInfo;
    private JLabel lblResumenIda;
    private JButton btnVolverIda;
    private Vuelo vueloIdaSeleccionado = null;
    private boolean faseSeleccionRegreso = false;
    private List<Vuelo> vuelosIdaCache = new ArrayList<>();
    private List<Vuelo> vuelosRegresoCache = new ArrayList<>();

    // Lista maestra de la vista actual (a la que se aplican filtros/orden)
    private List<Vuelo> vuelosActuales = new ArrayList<>();

    // ===== Tabla y detalles =====
    private JTable tablaVuelos;
    private DefaultTableModel modeloTabla;
    private JPanel panelDetalles;
    private JTextPane txtDetalles;

    // ===== Botones =====
    private JButton btnBuscar;
    private JButton btnComprar;
    private JButton btnAdmin;
    private JButton btnCancelar;
    private VueloDAO vueloDAO;

    public FrmPrincipal() {
        vueloDAO = new VueloDAO();
        
        // === LIMPIEZA AUTOMÁTICA DE RESERVAS HUÉRFANAS ===
        ReservaService rs = new ReservaService();
        int limpiadas = rs.limpiarReservasHuerfanas(15);
        if (limpiadas > 0) {
            System.out.println("Sistema: Se expiraron " + limpiadas + " reservas huérfanas (más de 15 min en Pendiente_Pago).");
        }

        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("AeroSky - Sistema de Gestión Aerocomercial");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 880);
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        // ========== HEADER ==========
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRINCIPAL);
        header.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel lblTitulo = new JLabel("AeroSky");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Sistema de Gestión Aerocomercial");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(0xF5D6B8));

        JPanel panelTitulo = new JPanel();
        panelTitulo.setLayout(new BoxLayout(panelTitulo, BoxLayout.Y_AXIS));
        panelTitulo.setOpaque(false);
        panelTitulo.add(lblTitulo);
        panelTitulo.add(lblSubtitulo);
        header.add(panelTitulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ========== CONTENEDOR ==========
        JPanel contenedor = new JPanel(new BorderLayout(0, 10));
        contenedor.setBackground(FONDO);
        contenedor.setBorder(new EmptyBorder(15, 20, 12, 20));

        // ---------- PANEL BÚSQUEDA ----------
        JPanel panelBusqueda = new JPanel(new GridBagLayout());
        panelBusqueda.setBackground(Color.WHITE);
        panelBusqueda.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(12, 15, 12, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        List<String> ciudades = vueloDAO.obtenerCiudades();
        String[] arr = ciudades.toArray(new String[0]);

        // Fila 1
        gbc.gridx = 0; gbc.gridy = 0;
        panelBusqueda.add(crearLabel("Origen:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cmbOrigen = new JComboBox<>(arr);
        estilizarCombo(cmbOrigen);
        panelBusqueda.add(cmbOrigen, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panelBusqueda.add(crearLabel("Destino:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0;
        cmbDestino = new JComboBox<>(arr);
        estilizarCombo(cmbDestino);
        if (arr.length > 1) cmbDestino.setSelectedIndex(1);
        panelBusqueda.add(cmbDestino, gbc);

        gbc.gridx = 4; gbc.weightx = 0;
        panelBusqueda.add(crearLabel("Fecha ida:"), gbc);
        gbc.gridx = 5; gbc.weightx = 1.0;
        txtFechaIda = new JTextField(10);
        estilizarCampo(txtFechaIda);
        txtFechaIda.setToolTipText("Formato: YYYY-MM-DD");
        panelBusqueda.add(txtFechaIda, gbc);

        // Fila 2
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        chkIdaVuelta = new JCheckBox("Ida y vuelta");
        chkIdaVuelta.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkIdaVuelta.setForeground(TEXTO);
        chkIdaVuelta.setBackground(Color.WHITE);
        panelBusqueda.add(chkIdaVuelta, gbc);

        gbc.gridx = 4; gbc.weightx = 0;
        panelBusqueda.add(crearLabel("Fecha regreso:"), gbc);
        gbc.gridx = 5; gbc.weightx = 1.0;
        txtFechaRegreso = new JTextField(10);
        estilizarCampo(txtFechaRegreso);
        txtFechaRegreso.setEnabled(false);
        txtFechaRegreso.setToolTipText("Formato: YYYY-MM-DD (posterior o igual a la ida)");
        panelBusqueda.add(txtFechaRegreso, gbc);

        // Fila 3: pasajeros y clase (RANGOS ACTUALIZADOS)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panelBusqueda.add(crearLabel("Adultos (18+):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0;
        spnAdultos = new JSpinner(new SpinnerNumberModel(1, 1, 9, 1));
        spnAdultos.setPreferredSize(new Dimension(70, 30));
        panelBusqueda.add(spnAdultos, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        panelBusqueda.add(crearLabel("Jóvenes (2-17):"), gbc);
        gbc.gridx = 3; gbc.weightx = 0;
        spnNinos = new JSpinner(new SpinnerNumberModel(0, 0, 8, 1));
        spnNinos.setPreferredSize(new Dimension(70, 30));
        panelBusqueda.add(spnNinos, gbc);

        gbc.gridx = 4; gbc.weightx = 0;
        panelBusqueda.add(crearLabel("Infantes (0-2):"), gbc);
        gbc.gridx = 5; gbc.weightx = 0;
        spnInfantes = new JSpinner(new SpinnerNumberModel(0, 0, 1, 1));
        spnInfantes.setPreferredSize(new Dimension(70, 30));
        panelBusqueda.add(spnInfantes, gbc);

        // Fila 4: clase + botón buscar
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        panelBusqueda.add(crearLabel("Clase:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0;
        cmbClase = new JComboBox<>(new String[]{"Economica", "Ejecutiva", "Primera Clase"});
        estilizarCombo(cmbClase);
        cmbClase.setPreferredSize(new Dimension(140, 30));
        panelBusqueda.add(cmbClase, gbc);

        gbc.gridx = 4; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 20, 4, 4);
        btnBuscar = crearBotonPrimario("Buscar vuelos");
        btnBuscar.setPreferredSize(new Dimension(150, 36));
        panelBusqueda.add(btnBuscar, gbc);
        gbc.gridwidth = 1;

        // ---------- PANEL ORDEN Y FILTROS (RF-04) ----------
        JPanel panelOrdenFiltros = new JPanel(new BorderLayout(10, 0));
        panelOrdenFiltros.setBackground(Color.WHITE);
        panelOrdenFiltros.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(8, 15, 8, 15)));

        JPanel filtrosIzq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtrosIzq.setBackground(Color.WHITE);

        filtrosIzq.add(crearLabel("Ordenar por:"));
        cbOrden = new JComboBox<>(new String[]{
            "Precio (menor a mayor)",
            "Precio (mayor a menor)",
            "Duración (menor a mayor)",
            "Hora de salida (más temprano)",
            "Escalas (menor a mayor)"
        });
        estilizarCombo(cbOrden);
        cbOrden.setPreferredSize(new Dimension(210, 30));
        filtrosIzq.add(cbOrden);

        filtrosIzq.add(crearLabel("Escalas:"));
        cbFiltroEscalas = new JComboBox<>(new String[]{
            "Todas", "Solo directos", "1 escala", "2 o más escalas"
        });
        estilizarCombo(cbFiltroEscalas);
        cbFiltroEscalas.setPreferredSize(new Dimension(140, 30));
        filtrosIzq.add(cbFiltroEscalas);

        filtrosIzq.add(crearLabel("Horario:"));
        cbFiltroHorario = new JComboBox<>(new String[]{
            "Todos", "Madrugada (00-05)", "Mañana (06-11)",
            "Tarde (12-17)", "Noche (18-23)"
        });
        estilizarCombo(cbFiltroHorario);
        cbFiltroHorario.setPreferredSize(new Dimension(160, 30));
        filtrosIzq.add(cbFiltroHorario);

        filtrosIzq.add(crearLabel("Precio máx:"));
        txtPrecioMax = new JTextField(8);
        estilizarCampo(txtPrecioMax);
        txtPrecioMax.setToolTipText("Ej: 500000. Déjelo vacío para no filtrar.");
        filtrosIzq.add(txtPrecioMax);

        btnLimpiarFiltros = new JButton("Limpiar");
        btnLimpiarFiltros.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLimpiarFiltros.setFocusPainted(false);
        btnLimpiarFiltros.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filtrosIzq.add(btnLimpiarFiltros);

        lblEstadoResultados = new JLabel("");
        lblEstadoResultados.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblEstadoResultados.setForeground(new Color(0x6B6B6B));

        panelOrdenFiltros.add(filtrosIzq, BorderLayout.WEST);
        panelOrdenFiltros.add(lblEstadoResultados, BorderLayout.EAST);

        // ---------- PANEL FASE (RF-05) ----------
        panelFase = new JPanel(new BorderLayout(10, 0));
        panelFase.setBackground(AMARILLO_AVISO);
        panelFase.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_AVISO),
                new EmptyBorder(8, 15, 8, 15)));
        panelFase.setVisible(false);

        lblPasoInfo = new JLabel("Paso 1 de 2: Seleccione el vuelo de ida");
        lblPasoInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPasoInfo.setForeground(TEXTO_AVISO);
        panelFase.add(lblPasoInfo, BorderLayout.WEST);

        lblResumenIda = new JLabel("");
        lblResumenIda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblResumenIda.setForeground(TEXTO);
        panelFase.add(lblResumenIda, BorderLayout.CENTER);

        btnVolverIda = new JButton("← Cambiar ida");
        btnVolverIda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnVolverIda.setFocusPainted(false);
        btnVolverIda.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolverIda.setVisible(false);
        panelFase.add(btnVolverIda, BorderLayout.EAST);

        // ---------- NORTH: búsqueda + filtros + fase ----------
        JPanel panelNorte = new JPanel();
        panelNorte.setLayout(new BoxLayout(panelNorte, BoxLayout.Y_AXIS));
        panelNorte.setBackground(FONDO);
        panelNorte.add(panelBusqueda);
        panelNorte.add(Box.createRigidArea(new Dimension(0, 8)));
        panelNorte.add(panelOrdenFiltros);
        panelNorte.add(Box.createRigidArea(new Dimension(0, 8)));
        panelNorte.add(panelFase);
        contenedor.add(panelNorte, BorderLayout.NORTH);

        // ---------- PANEL CENTRAL: TABLA + DETALLES ----------
        JPanel panelCentro = new JPanel(new BorderLayout(0, 8));
        panelCentro.setBackground(FONDO);

        String[] columnas = {
            "ID", "N° Vuelo", "Origen", "Destino",
            "Salida", "Llegada", "Duración", "Escalas",
            "Aeronave", "Precio", "Cupos"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaVuelos = new JTable(modeloTabla);
        estilizarTabla(tablaVuelos);

        tablaVuelos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mostrarDetalles();
        });

        JScrollPane scroll = new JScrollPane(tablaVuelos);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDE), "Vuelos disponibles",
                0, 0, new Font("Segoe UI", Font.BOLD, 12), PRINCIPAL));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(0, 240));
        panelCentro.add(scroll, BorderLayout.CENTER);

        panelDetalles = new JPanel(new BorderLayout());
        panelDetalles.setBackground(Color.WHITE);
        panelDetalles.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDE), "Detalles del vuelo y restricciones tarifarias",
                0, 0, new Font("Segoe UI", Font.BOLD, 12), PRINCIPAL));
        panelDetalles.setPreferredSize(new Dimension(0, 180));

        txtDetalles = new JTextPane();
        txtDetalles.setEditable(false);
        txtDetalles.setBackground(Color.WHITE);
        txtDetalles.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDetalles.setContentType("text/html");
        txtDetalles.setText("<html><body style='font-family:Segoe UI; padding:8px; color:#6B6B6B;'>"
                + "<i>Seleccione un vuelo de la tabla para ver sus detalles y restricciones tarifarias.</i>"
                + "</body></html>");

        JScrollPane scrollDetalles = new JScrollPane(txtDetalles);
        scrollDetalles.setBorder(null);
        panelDetalles.add(scrollDetalles, BorderLayout.CENTER);
        panelCentro.add(panelDetalles, BorderLayout.SOUTH);

        contenedor.add(panelCentro, BorderLayout.CENTER);

        // ---------- PANEL INFERIOR ----------
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(FONDO);

        JPanel panelIzq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelIzq.setBackground(FONDO);

        btnAdmin = crearBotonSecundario("Módulo Administrador");
        btnAdmin.setPreferredSize(new Dimension(200, 42));
        panelIzq.add(btnAdmin);

        btnCancelar = crearBotonSecundario("Cancelar Tiquete");
        btnCancelar.setPreferredSize(new Dimension(180, 42));
        panelIzq.add(btnCancelar);

        JPanel panelDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelDer.setBackground(FONDO);

        btnComprar = crearBotonPrimario("Comprar Tiquete");
        btnComprar.setPreferredSize(new Dimension(180, 42));
        btnComprar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelDer.add(btnComprar);

        panelInferior.add(panelIzq, BorderLayout.WEST);
        panelInferior.add(panelDer, BorderLayout.EAST);

        contenedor.add(panelInferior, BorderLayout.SOUTH);
        add(contenedor, BorderLayout.CENTER);

        // ========== EVENTOS ==========
        chkIdaVuelta.addActionListener(e -> {
            boolean activo = chkIdaVuelta.isSelected();
            txtFechaRegreso.setEnabled(activo);
            panelFase.setVisible(activo);

            if (!activo) {
                txtFechaRegreso.setText("");
                vueloIdaSeleccionado = null;
                faseSeleccionRegreso = false;
                vuelosRegresoCache.clear();
                lblResumenIda.setText("");
                btnVolverIda.setVisible(false);
            } else {
                lblPasoInfo.setText("Paso 1 de 2: Seleccione el vuelo de ida");
                lblResumenIda.setText("");
                btnVolverIda.setVisible(false);
            }
            actualizarUIParaFase();
        });

        spnAdultos.addChangeListener(e -> ajustarLimitesPasajeros());

        btnBuscar.addActionListener(e -> buscarVuelos());
        btnComprar.addActionListener(e -> manejarBotonPrincipal());
        btnVolverIda.addActionListener(e -> volverSeleccionIda());

        btnAdmin.addActionListener(e -> new FrmAdmin().setVisible(true));
        btnCancelar.addActionListener(e -> {
            FrmCancelacion fc = new FrmCancelacion(this);
            fc.setVisible(true);
        });

        // RF-04: listeners de orden y filtros
        cbOrden.addActionListener(e -> aplicarOrdenYFiltros());
        cbFiltroEscalas.addActionListener(e -> aplicarOrdenYFiltros());
        cbFiltroHorario.addActionListener(e -> aplicarOrdenYFiltros());
        txtPrecioMax.addActionListener(e -> aplicarOrdenYFiltros());
        txtPrecioMax.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { aplicarOrdenYFiltros(); }
        });

        btnLimpiarFiltros.addActionListener(e -> {
            cbOrden.setSelectedIndex(0);
            cbFiltroEscalas.setSelectedIndex(0);
            cbFiltroHorario.setSelectedIndex(0);
            txtPrecioMax.setText("");
            aplicarOrdenYFiltros();
        });

        ajustarLimitesPasajeros();
    }

    // ============ AJUSTES DE PASAJEROS ============
    private void ajustarLimitesPasajeros() {
        int adultos = (int) spnAdultos.getValue();
        int maxNinos = Math.min(9 - adultos, 8);
        spnNinos.setModel(new SpinnerNumberModel(
                Math.min((int) spnNinos.getValue(), maxNinos),
                0, maxNinos, 1));
        spnInfantes.setModel(new SpinnerNumberModel(
                Math.min((int) spnInfantes.getValue(), adultos),
                0, adultos, 1));
    }

    // ============ HELPERS ============
    private JLabel crearLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXTO);
        return l;
    }

    private void estilizarCampo(JTextField c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(5, 8, 5, 8)));
    }

    private void estilizarCombo(JComboBox<String> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(160, 30));
    }

    private JButton crearBotonPrimario(String t) { return crearBoton(t, PRINCIPAL, PRINCIPAL_HOVER, Color.WHITE); }
    private JButton crearBotonSecundario(String t) { return crearBoton(t, GRIS_BTN, GRIS_BTN_HOVER, TEXTO); }

    private JButton crearBoton(String t, Color normal, Color hover, Color fg) {
        JButton b = new JButton(t);
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setBackground(normal);
        b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(normal); }
        });
        return b;
    }

    private void estilizarTabla(JTable t) {
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(26);
        t.setGridColor(BORDE);
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setSelectionBackground(new Color(0xF8D7DD));
        t.setSelectionForeground(TEXTO);
        t.setBackground(Color.WHITE);
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 11));
        h.setBackground(new Color(0xF2E8D5));
        h.setForeground(TEXTO);
        h.setPreferredSize(new Dimension(h.getWidth(), 30));
        h.setReorderingAllowed(false);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE));

        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(SwingConstants.CENTER);
        for (String col : new String[]{"ID", "Duración", "Escalas", "Cupos"}) {
            int idx = t.getColumnModel().getColumnIndex(col);
            t.getColumnModel().getColumn(idx).setCellRenderer(centro);
        }
    }

    // ============ BUSCAR ============
    private void buscarVuelos() {
        String origen = (String) cmbOrigen.getSelectedItem();
        String destino = (String) cmbDestino.getSelectedItem();
        String fechaIda = txtFechaIda.getText().trim();

        if (origen == null || destino == null || origen.equals(destino)) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar origen y destino diferentes.",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try { java.time.LocalDate.parse(fechaIda); }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Fecha de ida inválida. Use formato YYYY-MM-DD.",
                    "Fecha inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Si es ida y vuelta, validar la fecha de regreso
        if (chkIdaVuelta.isSelected()) {
            String fechaRegreso = txtFechaRegreso.getText().trim();
            if (fechaRegreso.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Debe ingresar la fecha de regreso.",
                        "Fecha faltante", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                java.time.LocalDate fr = java.time.LocalDate.parse(fechaRegreso);
                java.time.LocalDate fi = java.time.LocalDate.parse(fechaIda);
                if (fr.isBefore(fi)) {
                    JOptionPane.showMessageDialog(this,
                            "La fecha de regreso no puede ser anterior a la fecha de ida.",
                            "Fechas inválidas", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Fecha de regreso inválida. Use formato YYYY-MM-DD.",
                        "Fecha inválida", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        int adultos = (int) spnAdultos.getValue();
        int ninos = (int) spnNinos.getValue();
        int infantes = (int) spnInfantes.getValue();
        String clase = (String) cmbClase.getSelectedItem();

        // Resetear flujo RF-05 al volver a buscar
        vueloIdaSeleccionado = null;
        faseSeleccionRegreso = false;
        vuelosRegresoCache.clear();
        if (chkIdaVuelta.isSelected()) {
            lblPasoInfo.setText("Paso 1 de 2: Seleccione el vuelo de ida");
            lblResumenIda.setText("");
            btnVolverIda.setVisible(false);
        }
        actualizarUIParaFase();

        modeloTabla.setRowCount(0);
        vuelosActuales.clear();
        mostrarDetalles();

        List<Vuelo> vuelos = vueloDAO.buscarVuelosPorFecha(
                origen, destino, fechaIda, adultos, ninos, infantes, clase);

        if (vuelos.isEmpty()) {
            List<Vuelo> cercanos = vueloDAO.buscarVuelosCercanos(
                    origen, destino, fechaIda, 3, adultos, ninos, infantes, clase);

            if (cercanos.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No hay vuelos disponibles ni en fechas cercanas (±3 días).",
                        "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
                if (lblEstadoResultados != null) lblEstadoResultados.setText("");
                return;
            }

            int op = JOptionPane.showConfirmDialog(this,
                    "No hay vuelos para el " + fechaIda + ".\n\n"
                    + "Encontramos " + cercanos.size() + " vuelo(s) en fechas cercanas (±3 días).\n"
                    + "¿Desea verlos?",
                    "Sugerencias de fechas cercanas (FA1)",
                    JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

            if (op != JOptionPane.YES_OPTION) {
                if (lblEstadoResultados != null) lblEstadoResultados.setText("");
                return;
            }
            vuelosIdaCache = new ArrayList<>(cercanos);
            mostrarLista(vuelosIdaCache);
        } else {
            vuelosIdaCache = new ArrayList<>(vuelos);
            mostrarLista(vuelosIdaCache);
        }
    }

    // Muestra en la tabla una lista (guardándola como la vista actual)
    private void mostrarLista(List<Vuelo> lista) {
        this.vuelosActuales = new ArrayList<>(lista);
        aplicarOrdenYFiltros();
    }

    // ============ RF-04: ORDENAMIENTO Y FILTROS ============
    private void aplicarOrdenYFiltros() {
        if (modeloTabla == null) return;
        modeloTabla.setRowCount(0);

        List<Vuelo> lista = new ArrayList<>(vuelosActuales);

        // Filtro por escalas
        String filtroEscalas = (String) cbFiltroEscalas.getSelectedItem();
        if (filtroEscalas != null) {
            switch (filtroEscalas) {
                case "Solo directos":
                    lista.removeIf(v -> v.getEscalas() != 0);
                    break;
                case "1 escala":
                    lista.removeIf(v -> v.getEscalas() != 1);
                    break;
                case "2 o más escalas":
                    lista.removeIf(v -> v.getEscalas() < 2);
                    break;
            }
        }

        // Filtro por horario
        String filtroHorario = (String) cbFiltroHorario.getSelectedItem();
        if (filtroHorario != null && !"Todos".equals(filtroHorario)) {
            lista.removeIf(v -> {
                int h = v.getHoraSalida();
                if (h < 0) return true;
                switch (filtroHorario) {
                    case "Madrugada (00-05)": return h >= 6;
                    case "Mañana (06-11)":    return h < 6  || h >= 12;
                    case "Tarde (12-17)":     return h < 12 || h >= 18;
                    case "Noche (18-23)":     return h < 18;
                }
                return false;
            });
        }

        // Filtro por precio máximo
        String precioStr = txtPrecioMax.getText().trim()
                .replace(".", "").replace(",", "").replace("$", "").replace(" ", "");
        if (!precioStr.isEmpty()) {
            try {
                double max = Double.parseDouble(precioStr);
                lista.removeIf(v -> v.getTarifaBase() > max);
            } catch (NumberFormatException ignored) {}
        }

        // Ordenamiento
        String criterio = (String) cbOrden.getSelectedItem();
        if (criterio != null) {
            switch (criterio) {
                case "Precio (menor a mayor)":
                    lista.sort(Comparator.comparingDouble(Vuelo::getTarifaBase));
                    break;
                case "Precio (mayor a menor)":
                    lista.sort(Comparator.comparingDouble(Vuelo::getTarifaBase).reversed());
                    break;
                case "Duración (menor a mayor)":
                    lista.sort(Comparator.comparingLong(Vuelo::getDuracionMinutos));
                    break;
                case "Hora de salida (más temprano)":
                    lista.sort(Comparator.comparingInt(Vuelo::getHoraSalida));
                    break;
                case "Escalas (menor a mayor)":
                    lista.sort(Comparator.comparingInt(Vuelo::getEscalas));
                    break;
            }
        }

        // Volcar a la tabla
        for (Vuelo v : lista) {
            modeloTabla.addRow(new Object[]{
                    v.getIdVuelo(),
                    v.getNumeroVuelo(),
                    v.getOrigenIata() + " (" + v.getOrigenCiudad() + ")",
                    v.getDestinoIata() + " (" + v.getDestinoCiudad() + ")",
                    v.getFechaSalida(),
                    v.getFechaLlegada(),
                    v.getDuracion(),
                    v.getTextoEscalas(),
                    v.getMatriculaAeronave(),
                    "$ " + String.format("%,.0f", v.getTarifaBase()),
                    v.getCuposDisponibles()
            });
        }

        // Estado informativo
        if (lblEstadoResultados != null) {
            int total = vuelosActuales.size();
            int mostrados = lista.size();
            if (total == 0) {
                lblEstadoResultados.setText("");
            } else if (mostrados == total) {
                lblEstadoResultados.setText("Mostrando " + total + " vuelo(s)");
            } else {
                lblEstadoResultados.setText("Mostrando " + mostrados + " de " + total + " vuelo(s)");
            }
        }
    }

    // ============ RF-05: FLUJO DE IDA Y VUELTA ============
    private void manejarBotonPrincipal() {
        int fila = tablaVuelos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un vuelo de la tabla.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idVueloFila = ((Number) modeloTabla.getValueAt(fila,
                tablaVuelos.getColumnModel().getColumnIndex("ID"))).intValue();

        if (!chkIdaVuelta.isSelected()) {
            procesarCompra(idVueloFila, null);
            return;
        }

        if (!faseSeleccionRegreso) {
            seleccionarVueloIda(idVueloFila);
        } else {
            procesarCompra(vueloIdaSeleccionado.getIdVuelo(), idVueloFila);
        }
    }

    private void seleccionarVueloIda(int idVueloIda) {
        Vuelo ida = vueloDAO.buscarPorId(idVueloIda);
        if (ida == null) return;

        String origen = (String) cmbOrigen.getSelectedItem();
        String destino = (String) cmbDestino.getSelectedItem();
        String fechaRegreso = txtFechaRegreso.getText().trim();
        int adultos = (int) spnAdultos.getValue();
        int ninos = (int) spnNinos.getValue();
        int infantes = (int) spnInfantes.getValue();
        String clase = (String) cmbClase.getSelectedItem();

        // CA-07: buscar regresos compatibles (destino→origen, fecha regreso,
        // salida >= llegada de la ida)
        List<Vuelo> regresos = vueloDAO.buscarVuelosRegresoCompatibles(
                destino, origen, fechaRegreso, ida.getFechaLlegada(),
                adultos, ninos, infantes, clase);

        if (regresos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay vuelos de regreso compatibles.\n\n"
                    + "El vuelo de ida (" + ida.getNumeroVuelo() + ") llega el "
                    + ida.getFechaLlegada() + ".\n"
                    + "Los vuelos de regreso deben despegar DESPUÉS de esa fecha/hora.\n\n"
                    + "Intente con otra fecha de regreso.",
                    "Sin regresos compatibles (CA-07)", JOptionPane.WARNING_MESSAGE);
            return;
        }

        vueloIdaSeleccionado = ida;
        vuelosRegresoCache = new ArrayList<>(regresos);
        faseSeleccionRegreso = true;

        lblPasoInfo.setText("Paso 2 de 2: Seleccione el vuelo de regreso");
        lblResumenIda.setText("<html><b>Ida:</b> " + ida.getNumeroVuelo()
                + " · " + ida.getOrigenIata() + "→" + ida.getDestinoIata()
                + " · " + ida.getFechaSalida()
                + " &nbsp;→&nbsp; <b>Regreso:</b> " + destino + "→" + origen
                + " · " + fechaRegreso + "</html>");
        btnVolverIda.setVisible(true);
        btnComprar.setText("Comprar Tiquete");
        btnComprar.setPreferredSize(new Dimension(180, 42));

        mostrarLista(vuelosRegresoCache);
    }

    private void volverSeleccionIda() {
        if (!chkIdaVuelta.isSelected()) return;

        vueloIdaSeleccionado = null;
        faseSeleccionRegreso = false;

        lblPasoInfo.setText("Paso 1 de 2: Seleccione el vuelo de ida");
        lblResumenIda.setText("");
        btnVolverIda.setVisible(false);
        btnComprar.setText("Seleccionar vuelo de ida →");
        btnComprar.setPreferredSize(new Dimension(210, 42));

        if (!vuelosIdaCache.isEmpty()) {
            mostrarLista(vuelosIdaCache);
        }
    }

    private void actualizarUIParaFase() {
        if (chkIdaVuelta.isSelected() && !faseSeleccionRegreso) {
            btnComprar.setText("Seleccionar vuelo de ida →");
            btnComprar.setPreferredSize(new Dimension(210, 42));
        } else {
            btnComprar.setText("Comprar Tiquete");
            btnComprar.setPreferredSize(new Dimension(180, 42));
        }
        panelFase.setVisible(chkIdaVuelta.isSelected());
    }

    // ============ COMPRA ============
    private void procesarCompra(int idVueloIda, Integer idVueloRegreso) {
        int adultos = (int) spnAdultos.getValue();
        int ninos = (int) spnNinos.getValue();
        int infantes = (int) spnInfantes.getValue();
        String clase = (String) cmbClase.getSelectedItem();

        ReservaService rs = new ReservaService();
        String pnr = rs.iniciarReserva(idVueloIda, idVueloRegreso, clase,
                                        adultos, ninos, infantes);

        if (pnr == null) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo iniciar la reserva.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FrmCompra fc = new FrmCompra(this, idVueloIda, idVueloRegreso, clase, pnr,
                                      adultos, ninos, infantes);
        fc.setVisible(true);

        // Resetear flujo tras la compra
        if (chkIdaVuelta.isSelected()) {
            volverSeleccionIda();
        }
        buscarVuelos();
    }

    // ============ DETALLES DEL VUELO ============
    private void mostrarDetalles() {
        int fila = tablaVuelos.getSelectedRow();
        if (fila == -1) {
            txtDetalles.setText("<html><body style='font-family:Segoe UI; padding:8px; color:#6B6B6B;'>"
                    + "<i>Seleccione un vuelo para ver sus detalles y restricciones tarifarias.</i>"
                    + "</body></html>");
            return;
        }

        int idVuelo = ((Number) modeloTabla.getValueAt(fila,
                tablaVuelos.getColumnModel().getColumnIndex("ID"))).intValue();
        Vuelo v = vueloDAO.buscarPorId(idVuelo);
        if (v == null) return;

        String clase = (String) cmbClase.getSelectedItem();
        double factorClase = ReservaService.obtenerMultiplicador(clase);
        double precioFinal = v.getTarifaBase() * factorClase;
        String restricciones = obtenerRestricciones(clase);

        String html = "<html><body style='font-family:Segoe UI; padding:10px; color:#1A1A1A;'>"
            + "<table style='width:100%; border-collapse:collapse;'>"
            + "<tr>"
            + "<td style='width:50%; vertical-align:top; padding-right:15px;'>"
            + "<b style='color:#C8102E; font-size:13px;'>Información operacional</b><br><br>"
            + "<b>Vuelo:</b> " + v.getNumeroVuelo() + "<br>"
            + "<b>Ruta:</b> " + v.getOrigenCiudad() + " (" + v.getOrigenIata() + ") → "
            + v.getDestinoCiudad() + " (" + v.getDestinoIata() + ")<br>"
            + "<b>Salida:</b> " + v.getFechaSalida() + "<br>"
            + "<b>Llegada:</b> " + v.getFechaLlegada() + "<br>"
            + "<b>Duración:</b> " + v.getDuracion() + "<br>"
            + "<b>Escalas:</b> " + v.getTextoEscalas() + "<br>"
            + "<b>Aeronave:</b> " + v.getMatriculaAeronave()
            + " (" + v.getCapacidadAeronave() + " pasajeros)<br>"
            + "<b>Estado:</b> " + v.getEstadoVuelo() + "<br>"
            + "<b>Cupos disponibles:</b> " + v.getCuposDisponibles() + "<br>"
            + "</td>"
            + "<td style='width:50%; vertical-align:top; padding-left:15px; border-left:1px solid #D5CFC0;'>"
            + "<b style='color:#C8102E; font-size:13px;'>Tarifa y restricciones (" + clase + ")</b><br><br>"
            + "<b>Tarifa base:</b> $ " + String.format("%,.0f", v.getTarifaBase()) + "<br>"
            + "<b>Factor clase:</b> ×" + factorClase + "<br>"
            + "<b style='color:#15803D; font-size:14px;'>Precio final: $ "
            + String.format("%,.0f", precioFinal) + "</b><br><br>"
            + restricciones
            + "</td>"
            + "</tr></table>"
            + "</body></html>";

        txtDetalles.setText(html);
        txtDetalles.setCaretPosition(0);
    }

    private String obtenerRestricciones(String clase) {
        switch (clase) {
            case "Ejecutiva":
                return "<span style='color:#6B6B6B; font-size:11px;'>"
                    + "• Cambios permitidos sin penalidad hasta 24h antes<br>"
                    + "• Reembolso con penalidad del 10%<br>"
                    + "• 2 maletas de 23 kg<br>"
                    + "• Prioridad en embarque y abordaje preferencial<br>"
                    + "• Asiento preferencial incluido"
                    + "</span>";
            case "Primera Clase":
                return "<span style='color:#6B6B6B; font-size:11px;'>"
                    + "• Cambios y cancelaciones sin penalidad<br>"
                    + "• Reembolso 100%<br>"
                    + "• 3 maletas de 32 kg<br>"
                    + "• Acceso a sala VIP<br>"
                    + "• Prioridad total y embarque exclusivo<br>"
                    + "• Servicio a bordo premium"
                    + "</span>";
            default:
                return "<span style='color:#6B6B6B; font-size:11px;'>"
                    + "• Cambios permitidos con penalidad del 30%<br>"
                    + "• No reembolsable<br>"
                    + "• 1 maleta de 23 kg<br>"
                    + "• Asiento estándar asignado<br>"
                    + "• Equipaje de mano de 10 kg"
                    + "</span>";
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            FrmPrincipal v = new FrmPrincipal();
            try {
                java.net.URL urlIcono = FrmPrincipal.class.getResource("/logo.png");
                if (urlIcono != null) v.setIconImage(new ImageIcon(urlIcono).getImage());
            } catch (Exception ignored) {}
            v.setVisible(true);
        });
    }
}