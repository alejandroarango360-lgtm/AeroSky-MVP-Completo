package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.dao.VueloDAO;
import com.aircolombia.prototipo.model.Vuelo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FrmAdmin extends JFrame {

    private static final Color PRINCIPAL = new Color(0xC8102E);
    private static final Color PRINCIPAL_HOVER = new Color(0xA00D24);
    private static final Color ROJO_PELIGRO = new Color(0xB91C1C);
    private static final Color ROJO_PELIGRO_HOVER = new Color(0x991B1B);
    private static final Color GRIS_BTN = new Color(0xE5E5E5);
    private static final Color GRIS_BTN_HOVER = new Color(0xD0D0D0);
    private static final Color FONDO = new Color(0xFAF7F0);
    private static final Color BORDE = new Color(0xD5CFC0);
    private static final Color TEXTO = new Color(0x1A1A1A);

    private JTable tablaVuelos;
    private DefaultTableModel modeloTabla;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnRefrescar;
    private VueloDAO vueloDAO;

    public FrmAdmin() {
        vueloDAO = new VueloDAO();
        initComponents();
        setLocationRelativeTo(null);
        cargarVuelos();
    }

    private void initComponents() {
        setTitle("AeroSky - Módulo Administrador");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 620);
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        try {
            java.net.URL urlIcono = getClass().getResource("/logo.png");
            if (urlIcono != null) setIconImage(new ImageIcon(urlIcono).getImage());
        } catch (Exception ignored) {}

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRINCIPAL);
        header.setBorder(new EmptyBorder(18, 25, 18, 25));

        JLabel lblTitulo = new JLabel("Panel de Administración");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Gestión de rutas, frecuencias y vuelos");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(0xF5D6B8));

        JPanel panelTitulo = new JPanel();
        panelTitulo.setLayout(new BoxLayout(panelTitulo, BoxLayout.Y_AXIS));
        panelTitulo.setOpaque(false);
        panelTitulo.add(lblTitulo);
        panelTitulo.add(lblSubtitulo);
        header.add(panelTitulo, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        JPanel contenedor = new JPanel(new BorderLayout(0, 15));
        contenedor.setBackground(FONDO);
        contenedor.setBorder(new EmptyBorder(20, 25, 20, 25));

        String[] columnas = {"ID", "N° Vuelo", "Origen", "Destino", "Fecha Salida", "Precio", "Cupos"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaVuelos = new JTable(modeloTabla);
        estilizarTabla(tablaVuelos);

        JScrollPane scrollPane = new JScrollPane(tablaVuelos);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDE));
        scrollPane.getViewport().setBackground(Color.WHITE);
        contenedor.add(scrollPane, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new BorderLayout());
        panelBotones.setBackground(FONDO);

        JPanel panelIzq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelIzq.setBackground(FONDO);
        btnNuevo = crearBoton("+ Nuevo Vuelo", PRINCIPAL, PRINCIPAL_HOVER, Color.WHITE);
        btnNuevo.setPreferredSize(new Dimension(150, 40));
        btnEditar = crearBoton("Editar", GRIS_BTN, GRIS_BTN_HOVER, TEXTO);
        btnEditar.setPreferredSize(new Dimension(110, 40));
        btnEliminar = crearBoton("Eliminar", ROJO_PELIGRO, ROJO_PELIGRO_HOVER, Color.WHITE);
        btnEliminar.setPreferredSize(new Dimension(110, 40));
        panelIzq.add(btnNuevo);
        panelIzq.add(btnEditar);
        panelIzq.add(btnEliminar);

        JPanel panelDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelDer.setBackground(FONDO);
        btnRefrescar = crearBoton("Refrescar", GRIS_BTN, GRIS_BTN_HOVER, TEXTO);
        btnRefrescar.setPreferredSize(new Dimension(120, 40));
        panelDer.add(btnRefrescar);

        panelBotones.add(panelIzq, BorderLayout.WEST);
        panelBotones.add(panelDer, BorderLayout.EAST);
        contenedor.add(panelBotones, BorderLayout.SOUTH);

        add(contenedor, BorderLayout.CENTER);

        btnRefrescar.addActionListener(e -> cargarVuelos());
        btnNuevo.addActionListener(e -> abrirEditor(null));
        btnEditar.addActionListener(e -> {
            Vuelo seleccionado = obtenerVueloSeleccionado();
            if (seleccionado != null) abrirEditor(seleccionado);
        });
        btnEliminar.addActionListener(e -> eliminarSeleccionado());
    }

    private void cargarVuelos() {
        modeloTabla.setRowCount(0);
        List<Vuelo> vuelos = vueloDAO.listarTodos();
        for (Vuelo v : vuelos) {
            modeloTabla.addRow(new Object[]{
                    v.getIdVuelo(),
                    v.getNumeroVuelo(),
                    v.getOrigenCiudad() + " (" + v.getOrigenIata() + ")",
                    v.getDestinoCiudad() + " (" + v.getDestinoIata() + ")",
                    v.getFechaSalida(),
                    "$ " + String.format("%,.0f", v.getTarifaBase()),
                    v.getCuposDisponibles()
            });
        }
    }

    private Vuelo obtenerVueloSeleccionado() {
        int fila = tablaVuelos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un vuelo de la tabla.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) modeloTabla.getValueAt(fila, 0);
        return vueloDAO.buscarPorId(id);
    }

    private void abrirEditor(Vuelo vuelo) {
        FrmEditarVuelo editor = new FrmEditarVuelo(this, vuelo, vueloDAO);
        editor.setVisible(true);
        cargarVuelos();
    }

    private void eliminarSeleccionado() {
        Vuelo v = obtenerVueloSeleccionado();
        if (v == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el vuelo?\n\n"
                + "N° " + v.getNumeroVuelo() + "\n"
                + v.getOrigenCiudad() + " → " + v.getDestinoCiudad() + "\n"
                + "Fecha: " + v.getFechaSalida(),
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (vueloDAO.eliminar(v.getIdVuelo())) {
                JOptionPane.showMessageDialog(this,
                        "Vuelo eliminado correctamente.",
                        "Eliminación exitosa", JOptionPane.INFORMATION_MESSAGE);
                cargarVuelos();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No se pudo eliminar el vuelo.\n"
                        + "Es posible que tenga reservas asociadas.",
                        "Error al eliminar", JOptionPane.ERROR_MESSAGE);
            }
        }
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

    private void estilizarTabla(JTable tabla) {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setRowHeight(32);
        tabla.setGridColor(BORDE);
        tabla.setShowHorizontalLines(true);
        tabla.setShowVerticalLines(false);
        tabla.setSelectionBackground(new Color(0xF8D7DD));
        tabla.setSelectionForeground(TEXTO);
        tabla.setBackground(Color.WHITE);
        tabla.setForeground(TEXTO);
        tabla.setFillsViewportHeight(true);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(0xF2E8D5));
        header.setForeground(TEXTO);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE));

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.getColumnModel().getColumn(0).setCellRenderer(centrado);
        tabla.getColumnModel().getColumn(6).setCellRenderer(centrado);
    }
}