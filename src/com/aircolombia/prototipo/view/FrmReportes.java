package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.dao.VueloDAO;
import com.aircolombia.prototipo.service.ReportesService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmReportes extends JFrame {

    private static final Color PRINCIPAL = new Color(0xC8102E);
    private static final Color FONDO = new Color(0xFAF7F0);
    private static final Color BORDE = new Color(0xD5CFC0);
    private static final Color VERDE = new Color(0x15803D);
    private static final Color AZUL = new Color(0x1D4ED8);
    private static final Color ROJO = new Color(0xB91C1C);

    private JTextField txtDesde, txtHasta;
    private JComboBox<String> cmbOrigen, cmbDestino;
    private JLabel lblKpiVentas, lblKpiTiquetes, lblKpiCancelaciones;
    private JTable tabla;
    private DefaultTableModel modelo;

    private ReportesService service = new ReportesService();

    public FrmReportes() {
        setTitle("AeroSky - Tablero Ejecutivo (Gerencia)");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRINCIPAL);
        header.setBorder(new EmptyBorder(15, 25, 15, 25));
        JLabel lblTitulo = new JLabel("Tablero Gerencial y Analítica");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 15));
        centro.setBackground(FONDO);
        centro.setBorder(new EmptyBorder(15, 20, 15, 20));

        // --- KPIs (RF-34) ---
        JPanel panelKpis = new JPanel(new GridLayout(1, 3, 15, 0));
        panelKpis.setBackground(FONDO);
        panelKpis.setPreferredSize(new Dimension(0, 100));

        lblKpiVentas = new JLabel("$ 0", SwingConstants.CENTER);
        lblKpiTiquetes = new JLabel("0", SwingConstants.CENTER);
        lblKpiCancelaciones = new JLabel("0", SwingConstants.CENTER);

        panelKpis.add(crearTarjetaKpi("Ingresos Totales", lblKpiVentas, VERDE));
        panelKpis.add(crearTarjetaKpi("Tiquetes Vendidos", lblKpiTiquetes, AZUL));
        panelKpis.add(crearTarjetaKpi("Cancelaciones", lblKpiCancelaciones, ROJO));

        centro.add(panelKpis, BorderLayout.NORTH);

        // --- FILTROS (RF-33) ---
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFiltros.setBackground(Color.WHITE);
        panelFiltros.setBorder(BorderFactory.createLineBorder(BORDE));

        panelFiltros.add(new JLabel("Desde (YYYY-MM-DD):"));
        txtDesde = new JTextField("2026-01-01", 10);
        panelFiltros.add(txtDesde);

        panelFiltros.add(new JLabel("Hasta:"));
        txtHasta = new JTextField(java.time.LocalDate.now().toString(), 10);
        panelFiltros.add(txtHasta);

        panelFiltros.add(new JLabel("Origen:"));
        cmbOrigen = new JComboBox<>();
        cmbOrigen.addItem("Todas");
        for (String c : new VueloDAO().obtenerCiudades()) cmbOrigen.addItem(c);
        panelFiltros.add(cmbOrigen);

        panelFiltros.add(new JLabel("Destino:"));
        cmbDestino = new JComboBox<>();
        cmbDestino.addItem("Todas");
        for (String c : new VueloDAO().obtenerCiudades()) cmbDestino.addItem(c);
        panelFiltros.add(cmbDestino);

        JButton btnFiltrar = new JButton("Aplicar Filtros");
        btnFiltrar.setBackground(PRINCIPAL);
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFiltrar.setFocusPainted(false);
        btnFiltrar.addActionListener(e -> cargarDatos());
        panelFiltros.add(btnFiltrar);

        // --- TABLA (RF-32) ---
        String[] columnas = {"Origen", "Destino", "Reservas", "Tiquetes", "Ingresos ($)"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.setSelectionBackground(new Color(0xF8D7DD));
        
        // Alinear números a la derecha
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tabla.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tabla.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Rentabilidad y Ventas por Ruta"));

        JPanel panelTabla = new JPanel(new BorderLayout(0, 10));
        panelTabla.setBackground(FONDO);
        panelTabla.add(panelFiltros, BorderLayout.NORTH);
        panelTabla.add(scroll, BorderLayout.CENTER);

        centro.add(panelTabla, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        cargarDatos(); // Carga inicial
    }

    private JPanel crearTarjetaKpi(String titulo, JLabel lblValor, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTit.setForeground(new Color(0x6B6B6B));
        
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValor.setForeground(color);

        p.add(lblTit, BorderLayout.NORTH);
        p.add(lblValor, BorderLayout.CENTER);
        return p;
    }

    private void cargarDatos() {
        String desde = txtDesde.getText().trim();
        String hasta = txtHasta.getText().trim();
        String origen = (String) cmbOrigen.getSelectedItem();
        String destino = (String) cmbDestino.getSelectedItem();

        // 1. Actualizar KPIs
        double ventas = service.getVentasTotales(desde, hasta);
        int tiquetes = service.getTiquetesVendidos(desde, hasta);
        int cancelaciones = service.getCancelaciones(desde, hasta);

        lblKpiVentas.setText("$ " + String.format("%,.0f", ventas));
        lblKpiTiquetes.setText(String.valueOf(tiquetes));
        lblKpiCancelaciones.setText(String.valueOf(cancelaciones));

        // 2. Actualizar Tabla
        modelo.setRowCount(0);
        List<Object[]> datos = service.getReportePorRuta(desde, hasta, origen, destino);
        for (Object[] fila : datos) {
            modelo.addRow(new Object[]{
                fila[0], // Origen
                fila[1], // Destino
                fila[2], // Reservas
                fila[3], // Tiquetes
                String.format("%,.0f", (Double) fila[4]) // Ingresos
            });
        }
    }
}