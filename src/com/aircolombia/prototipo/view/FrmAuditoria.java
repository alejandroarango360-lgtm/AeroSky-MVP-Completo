package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.service.ReservaService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

public class FrmAuditoria extends JFrame {

    private JTextField txtPnr;
    private JComboBox<String> cboEvento;
    private JTextField txtDesde, txtHasta;
    private JComboBox<String> cboLimite;
    private JTable tabla;
    private DefaultTableModel modelo;
    private JLabel lblResumen;

    private final ReservaService service = new ReservaService();
    private final SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private final SimpleDateFormat sdfOut = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public FrmAuditoria() {
        super("AeroSky - Auditoría de Eventos");
        initComponents();
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        cargarEventos();
        buscar();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0xC8102E));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel lblTitulo = new JLabel("Auditoría de Eventos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // FILTROS
        JPanel filtros = new JPanel(new GridBagLayout());
        filtros.setBackground(Color.WHITE);
        filtros.setBorder(new EmptyBorder(12, 20, 12, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtPnr = new JTextField(10);
        cboEvento = new JComboBox<>();
        cboEvento.addItem("(todos)");
        txtDesde = new JTextField("", 12);
        txtHasta = new JTextField("", 12);
        txtDesde.setToolTipText("Formato: yyyy-MM-dd HH:mm");
        txtHasta.setToolTipText("Formato: yyyy-MM-dd HH:mm");
        cboLimite = new JComboBox<>(new String[]{"100", "500", "1000", "5000"});
        cboLimite.setSelectedIndex(1);

        gbc.gridy = 0;
        gbc.gridx = 0; filtros.add(etiqueta("PNR:"), gbc);
        gbc.gridx = 1; filtros.add(txtPnr, gbc);
        gbc.gridx = 2; filtros.add(etiqueta("Evento:"), gbc);
        gbc.gridx = 3; filtros.add(cboEvento, gbc);
        gbc.gridx = 4; filtros.add(etiqueta("Desde:"), gbc);
        gbc.gridx = 5; filtros.add(txtDesde, gbc);
        gbc.gridx = 6; filtros.add(etiqueta("Hasta:"), gbc);
        gbc.gridx = 7; filtros.add(txtHasta, gbc);
        gbc.gridx = 8; filtros.add(etiqueta("Límite:"), gbc);
        gbc.gridx = 9; filtros.add(cboLimite, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0; gbc.gridwidth = 10;
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setBackground(Color.WHITE);

        JButton btnLimpiar  = new JButton("Limpiar filtros");
        JButton btnExportar = new JButton("Exportar CSV");
        JButton btnBuscar   = new JButton("Buscar");

        btnBuscar.setBackground(new Color(0x15803D));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBuscar.setFocusPainted(false);

        btnBuscar.addActionListener(e -> buscar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnExportar.addActionListener(e -> exportarCsv());

        botones.add(btnLimpiar);
        botones.add(btnExportar);
        botones.add(btnBuscar);
        filtros.add(botones, gbc);

        add(filtros, BorderLayout.NORTH);

        // TABLA
        modelo = new DefaultTableModel(
                new Object[]{"Fecha", "PNR", "Evento", "Resultado", "Detalle"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(24);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(70);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(190);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(450);

        tabla.setDefaultRenderer(Object.class, new RendererResultado());

        JScrollPane scroll = new JScrollPane(tabla);
        add(scroll, BorderLayout.CENTER);

        // PIE
        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(Color.WHITE);
        pie.setBorder(new EmptyBorder(8, 20, 8, 20));
        lblResumen = new JLabel(" ");
        lblResumen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblResumen.setForeground(new Color(0x555555));
        pie.add(lblResumen, BorderLayout.WEST);
        add(pie, BorderLayout.SOUTH);
    }

    private JLabel etiqueta(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    private void cargarEventos() {
        for (String ev : service.listarEventosAuditoria()) cboEvento.addItem(ev);
    }

    private void buscar() {
        String pnr = txtPnr.getText().trim();
        String evento = (String) cboEvento.getSelectedItem();
        if ("(todos)".equals(evento)) evento = null;

        Timestamp desde = parseFecha(txtDesde.getText(), true);
        Timestamp hasta = parseFecha(txtHasta.getText(), false);
        if (txtDesde.getText().trim().isEmpty() == false && desde == null) {
            JOptionPane.showMessageDialog(this, "Formato de 'Desde' inválido. Usa: yyyy-MM-dd HH:mm"); return;
        }
        if (txtHasta.getText().trim().isEmpty() == false && hasta == null) {
            JOptionPane.showMessageDialog(this, "Formato de 'Hasta' inválido. Usa: yyyy-MM-dd HH:mm"); return;
        }

        int limite = Integer.parseInt((String) cboLimite.getSelectedItem());
        List<Object[]> datos = service.listarAuditoria(pnr, evento, desde, hasta, limite);

        modelo.setRowCount(0);
        for (Object[] f : datos) {
            Timestamp ts = (Timestamp) f[0];
            modelo.addRow(new Object[]{
                ts == null ? "" : sdfOut.format(ts),
                f[1], f[2], f[3], f[4] == null ? "" : f[4]
            });
        }
        lblResumen.setText("Mostrando " + datos.size() + " evento(s).");
    }

    private Timestamp parseFecha(String txt, boolean esDesde) {
        if (txt == null || txt.trim().isEmpty()) return null;
        try {
            java.util.Date d = sdfIn.parse(txt.trim());
            if (!esDesde) d = new java.util.Date(d.getTime() + 59_999L);
            return new Timestamp(d.getTime());
        } catch (ParseException e) { return null; }
    }

    private void limpiar() {
        txtPnr.setText(""); cboEvento.setSelectedIndex(0); txtDesde.setText(""); txtHasta.setText(""); cboLimite.setSelectedIndex(1);
        buscar();
    }

    private void exportarCsv() {
        if (modelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para exportar."); return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("auditoria_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, false))) {
            pw.println("Fecha,PNR,Evento,Resultado,Detalle");
            for (int i = 0; i < modelo.getRowCount(); i++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < modelo.getColumnCount(); c++) {
                    if (c > 0) sb.append(",");
                    String v = String.valueOf(modelo.getValueAt(i, c)).replace("\"", "\"\"");
                    sb.append("\"").append(v).append("\"");
                }
                pw.println(sb);
            }
            JOptionPane.showMessageDialog(this, "Exportado a:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + e.getMessage());
        }
    }

    private class RendererResultado extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String res = String.valueOf(modelo.getValueAt(row, 3));
                if ("OK".equals(res)) c.setForeground(new Color(0x15803D));
                else if ("RECHAZADO".equals(res)) c.setForeground(new Color(0xB45309));
                else if ("BLOQUEADO".equals(res)) c.setForeground(new Color(0x7E22CE));
                else if ("ERROR".equals(res)) c.setForeground(new Color(0xC8102E));
                else c.setForeground(Color.BLACK);
            } else c.setForeground(Color.WHITE);
            return c;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FrmAuditoria().setVisible(true));
    }
}