package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.service.ReservaService;
import javax.swing.*;
import java.awt.*;

public class FrmModificarVuelo extends JDialog {
    private JTextField txtPnr, txtNuevoVuelo;
    private JTextArea txtResultado;
    private ReservaService service = new ReservaService();
    private JButton btnConfirmar;
    
    private int viejoIdVuelo = -1;
    private double nuevoTotal = 0;
    private int asientos = 0;

    public FrmModificarVuelo(JFrame padre) {
        super(padre, "Modificar Vuelo (RF-22 / RF-23)", true);
        setSize(450, 420);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel panelTop = new JPanel(new GridLayout(3, 2, 10, 10));
        panelTop.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelTop.setBackground(Color.WHITE);

        panelTop.add(new JLabel("PNR de la Reserva:"));
        txtPnr = new JTextField();
        panelTop.add(txtPnr);

        panelTop.add(new JLabel("ID del Nuevo Vuelo:"));
        txtNuevoVuelo = new JTextField();
        panelTop.add(txtNuevoVuelo);

        JButton btnEvaluar = new JButton("Calcular Diferencia");
        btnEvaluar.setBackground(new Color(0xC8102E));
        btnEvaluar.setForeground(Color.WHITE);
        panelTop.add(new JLabel("")); 
        panelTop.add(btnEvaluar);

        add(panelTop, BorderLayout.NORTH);

        txtResultado = new JTextArea();
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtResultado.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(new JScrollPane(txtResultado), BorderLayout.CENTER);

        btnConfirmar = new JButton("Confirmar y Pagar Diferencia");
        btnConfirmar.setEnabled(false);
        btnConfirmar.setBackground(new Color(0x15803D));
        btnConfirmar.setForeground(Color.WHITE);
        add(btnConfirmar, BorderLayout.SOUTH);

        btnEvaluar.addActionListener(e -> evaluar());
        btnConfirmar.addActionListener(e -> confirmar());
    }

    private void evaluar() {
        String pnr = txtPnr.getText().trim().toUpperCase();
        try {
            int idNuevo = Integer.parseInt(txtNuevoVuelo.getText().trim());
            Object[] res = service.evaluarCambioVuelo(pnr, idNuevo);
            
            if (!(Boolean) res[0]) {
                txtResultado.setText("❌ " + res[1]);
                btnConfirmar.setEnabled(false);
                return;
            }

            viejoIdVuelo = (Integer) res[2];
            nuevoTotal = (Double) res[3];
            double difPrecio = (Double) res[4];
            double penalidad = (Double) res[5];
            double totalAPagar = (Double) res[6];
            asientos = (Integer) res[7];

            String msg = "--- RESUMEN DEL CAMBIO ---\n";
            msg += "Vuelo Actual ID: " + viejoIdVuelo + "\n";
            msg += "Nuevo Vuelo ID: " + idNuevo + "\n\n";
            msg += "Diferencia de Tarifa: $" + String.format("%,.0f", difPrecio) + "\n";
            msg += "Penalidad por Cambio: $" + String.format("%,.0f", penalidad) + "\n";
            msg += "---------------------------------\n";
            
            if (totalAPagar > 0) msg += "TOTAL A PAGAR: $" + String.format("%,.0f", totalAPagar) + "\n(RF-23: Valor adicional)";
            else msg += "SALDO A FAVOR: $" + String.format("%,.0f", Math.abs(totalAPagar)) + "\n(RF-23: Saldo a favor)";

            txtResultado.setText(msg);
            btnConfirmar.setEnabled(true);
        } catch (Exception ex) {
            txtResultado.setText("❌ Error: Revisa el PNR y el ID del vuelo.");
            btnConfirmar.setEnabled(false);
        }
    }

    private void confirmar() {
        String pnr = txtPnr.getText().trim().toUpperCase();
        int idNuevo = Integer.parseInt(txtNuevoVuelo.getText().trim());
        
        if (service.procesarCambioVuelo(pnr, viejoIdVuelo, idNuevo, nuevoTotal, asientos)) {
            JOptionPane.showMessageDialog(this, "¡Vuelo modificado exitosamente!\nSe ha registrado en auditoría.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error al modificar el vuelo.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}