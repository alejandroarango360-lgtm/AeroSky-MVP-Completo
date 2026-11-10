package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.dao.VueloDAO;
import com.aircolombia.prototipo.model.Vuelo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FrmEditarVuelo extends JDialog {

    private static final Color PRINCIPAL = new Color(0xC8102E);
    private static final Color PRINCIPAL_HOVER = new Color(0xA00D24);
    private static final Color GRIS_BTN = new Color(0xE5E5E5);
    private static final Color GRIS_BTN_HOVER = new Color(0xD0D0D0);
    private static final Color BORDE = new Color(0xD5CFC0);
    private static final Color TEXTO = new Color(0x1A1A1A);

    private static final String[] AEROPUERTOS = {
        "BOG - Bogotá", "MDE - Medellín", "CLO - Cali", "CTG - Cartagena"
    };
    private static final String[] AERONAVES = {
        "1 - HK-5321 (180 pas.)", "2 - HK-4522 (220 pas.)", "3 - HK-1234 (300 pas.)"
    };
    private static final String[] ESTADOS = {
        "Programado", "En Vuelo", "Aterrizado", "Cancelado"
    };

    private JTextField txtNumeroVuelo;
    private JComboBox<String> cmbOrigen;
    private JComboBox<String> cmbDestino;
    private JComboBox<String> cmbAeronave;
    private JTextField txtFechaSalida;
    private JTextField txtFechaLlegada;
    private JComboBox<String> cmbEstado;
    private JSpinner spnEscalas;
    private JTextField txtTarifa;
    private JTextField txtCupos;

    private Vuelo vueloExistente;
    private VueloDAO vueloDAO;

    public FrmEditarVuelo(JFrame padre, Vuelo vuelo, VueloDAO vueloDAO) {
        super(padre, vuelo == null ? "Nuevo Vuelo" : "Editar Vuelo", true);
        this.vueloExistente = vuelo;
        this.vueloDAO = vueloDAO;
        initComponents();
        setLocationRelativeTo(padre);
        if (vuelo != null) cargarDatos();
    }

    private void initComponents() {
        setSize(520, 700);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        try {
            java.net.URL urlIcono = getClass().getResource("/logo.png");
            if (urlIcono != null) setIconImage(new ImageIcon(urlIcono).getImage());
        } catch (Exception ignored) {}

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRINCIPAL);
        header.setBorder(new EmptyBorder(18, 25, 18, 25));
        JLabel lblTitulo = new JLabel(vueloExistente == null
                ? "Crear Nuevo Vuelo" : "Editar Vuelo #" + vueloExistente.getIdVuelo());
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(Color.WHITE);
        panelForm.setBorder(new EmptyBorder(15, 35, 15, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // ============ N° Vuelo ============
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panelForm.add(crearLabel("N° Vuelo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNumeroVuelo = new JTextField(20); estilizarCampo(txtNumeroVuelo);
        panelForm.add(txtNumeroVuelo, gbc);

        // ============ Origen ============
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panelForm.add(crearLabel("Origen:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cmbOrigen = new JComboBox<>(AEROPUERTOS);
        cmbOrigen.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbOrigen.setBackground(Color.WHITE);
        panelForm.add(cmbOrigen, gbc);

        // ============ Destino ============
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panelForm.add(crearLabel("Destino:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cmbDestino = new JComboBox<>(AEROPUERTOS);
        cmbDestino.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbDestino.setBackground(Color.WHITE);
        cmbDestino.setSelectedIndex(1);
        panelForm.add(cmbDestino, gbc);

        // ============ Aeronave ============
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        panelForm.add(crearLabel("Aeronave:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cmbAeronave = new JComboBox<>(AERONAVES);
        cmbAeronave.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbAeronave.setBackground(Color.WHITE);
        panelForm.add(cmbAeronave, gbc);

        // ============ Fecha Salida ============
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        panelForm.add(crearLabel("Fecha Salida:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtFechaSalida = new JTextField(20); estilizarCampo(txtFechaSalida);
        panelForm.add(txtFechaSalida, gbc);

        // ============ Fecha Llegada ============
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        panelForm.add(crearLabel("Fecha Llegada:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtFechaLlegada = new JTextField(20); estilizarCampo(txtFechaLlegada);
        panelForm.add(txtFechaLlegada, gbc);

        // ============ Hint formato ============
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0;
        JLabel lblHint = new JLabel("Formato fecha: YYYY-MM-DD HH:MM:SS");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(new Color(0x6B6B6B));
        panelForm.add(lblHint, gbc);

        // ============ Estado Vuelo ============
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0;
        panelForm.add(crearLabel("Estado Vuelo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cmbEstado = new JComboBox<>(ESTADOS);
        cmbEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbEstado.setBackground(Color.WHITE);
        panelForm.add(cmbEstado, gbc);

        // ============ Escalas (NUEVO) ============
        gbc.gridx = 0; gbc.gridy = 8; gbc.weightx = 0;
        panelForm.add(crearLabel("Escalas:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        spnEscalas = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        spnEscalas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spnEscalas.setPreferredSize(new Dimension(80, 30));
        panelForm.add(spnEscalas, gbc);

        // ============ Hint escalas ============
        gbc.gridx = 1; gbc.gridy = 9; gbc.weightx = 1.0;
        JLabel lblHintEscalas = new JLabel("0 = vuelo directo");
        lblHintEscalas.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHintEscalas.setForeground(new Color(0x6B6B6B));
        panelForm.add(lblHintEscalas, gbc);

        // ============ Tarifa Base ============
        gbc.gridx = 0; gbc.gridy = 10; gbc.weightx = 0;
        panelForm.add(crearLabel("Tarifa Base:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTarifa = new JTextField(20); estilizarCampo(txtTarifa);
        panelForm.add(txtTarifa, gbc);

        // ============ Cupos ============
        gbc.gridx = 0; gbc.gridy = 11; gbc.weightx = 0;
        panelForm.add(crearLabel("Cupos:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtCupos = new JTextField(20); estilizarCampo(txtCupos);
        panelForm.add(txtCupos, gbc);

        add(panelForm, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelBotones.setBackground(Color.WHITE);
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE));

        JButton btnCancelar = crearBoton("Cancelar", GRIS_BTN, GRIS_BTN_HOVER, TEXTO);
        JButton btnGuardar = crearBoton("Guardar", PRINCIPAL, PRINCIPAL_HOVER, Color.WHITE);
        btnGuardar.setPreferredSize(new Dimension(130, 38));
        btnCancelar.setPreferredSize(new Dimension(130, 38));

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        add(panelBotones, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
    }

    private void cargarDatos() {
        txtNumeroVuelo.setText(vueloExistente.getNumeroVuelo());

        for (int i = 0; i < AEROPUERTOS.length; i++) {
            if (AEROPUERTOS[i].startsWith(vueloExistente.getOrigenIata())) {
                cmbOrigen.setSelectedIndex(i); break;
            }
        }
        for (int i = 0; i < AEROPUERTOS.length; i++) {
            if (AEROPUERTOS[i].startsWith(vueloExistente.getDestinoIata())) {
                cmbDestino.setSelectedIndex(i); break;
            }
        }
        for (int i = 0; i < AERONAVES.length; i++) {
            if (AERONAVES[i].startsWith(String.valueOf(vueloExistente.getIdAeronave()))) {
                cmbAeronave.setSelectedIndex(i); break;
            }
        }
        for (int i = 0; i < ESTADOS.length; i++) {
            if (ESTADOS[i].equals(vueloExistente.getEstadoVuelo())) {
                cmbEstado.setSelectedIndex(i); break;
            }
        }

        txtFechaSalida.setText(vueloExistente.getFechaSalida());
        txtFechaLlegada.setText(vueloExistente.getFechaLlegada());
        spnEscalas.setValue(vueloExistente.getEscalas());
        txtTarifa.setText(String.valueOf((long) vueloExistente.getTarifaBase()));
        txtCupos.setText(String.valueOf(vueloExistente.getCuposDisponibles()));
    }

    private void guardar() {
        String numero = txtNumeroVuelo.getText().trim();
        String origenIata = ((String) cmbOrigen.getSelectedItem()).substring(0, 3);
        String destinoIata = ((String) cmbDestino.getSelectedItem()).substring(0, 3);
        int idAeronave = Integer.parseInt(((String) cmbAeronave.getSelectedItem()).substring(0, 1));
        String fechaSalida = txtFechaSalida.getText().trim();
        String fechaLlegada = txtFechaLlegada.getText().trim();
        String estado = (String) cmbEstado.getSelectedItem();
        int escalas = (int) spnEscalas.getValue();
        String tarifaStr = txtTarifa.getText().trim();
        String cuposStr = txtCupos.getText().trim();

        // ============ Validaciones ============
        if (numero.isEmpty() || fechaSalida.isEmpty() || fechaLlegada.isEmpty()
                || tarifaStr.isEmpty() || cuposStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (origenIata.equals(destinoIata)) {
            JOptionPane.showMessageDialog(this,
                    "El origen y el destino no pueden ser iguales.",
                    "Ruta inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!fechaSalida.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")
                || !fechaLlegada.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                    "Formato de fecha incorrecto.\nDebe ser: YYYY-MM-DD HH:MM:SS",
                    "Fecha inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double tarifa;
        int cupos;
        try {
            tarifa = Double.parseDouble(tarifaStr);
            if (tarifa <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La tarifa debe ser un número mayor a 0.",
                    "Tarifa inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            cupos = Integer.parseInt(cuposStr);
            if (cupos <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Los cupos deben ser un entero mayor a 0.",
                    "Cupos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean exito;
        if (vueloExistente == null) {
            // ============ NUEVO VUELO (10 parámetros con escalas) ============
            Vuelo nuevo = new Vuelo(numero, origenIata, destinoIata, idAeronave,
                    fechaSalida, fechaLlegada, estado, escalas, tarifa, cupos);
            exito = vueloDAO.insertar(nuevo);
        } else {
            // ============ EDITAR VUELO (15 parámetros completos) ============
            Vuelo actualizado = new Vuelo(
                    vueloExistente.getIdVuelo(), numero,
                    origenIata, destinoIata,
                    vueloExistente.getOrigenCiudad(), vueloExistente.getDestinoCiudad(),
                    idAeronave,
                    vueloExistente.getMatriculaAeronave(),
                    vueloExistente.getCapacidadAeronave(),
                    fechaSalida, fechaLlegada, estado,
                    escalas, tarifa, cupos);
            exito = vueloDAO.actualizar(actualizado);
        }

        if (exito) {
            JOptionPane.showMessageDialog(this, "Vuelo guardado correctamente.",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el vuelo.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXTO);
        return label;
    }

    private void estilizarCampo(JTextField campo) {
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                new EmptyBorder(6, 10, 6, 10)
        ));
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