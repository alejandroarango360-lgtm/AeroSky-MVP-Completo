package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.service.SeguridadService;
import javax.swing.*;
import java.awt.*;

public class FrmRegistro extends JDialog {
    private JTextField txtUser, txtEmail, txtCode;
    private JPasswordField txtPass;
    private JButton btnSolicitar, btnConfirmar;
    private SeguridadService service = new SeguridadService();
    private boolean paso2 = false;

    public FrmRegistro(JDialog padre) {
        super(padre, "Crear Cuenta - AeroSky", true);
        setSize(380, 350);
        setLocationRelativeTo(padre);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUser = new JTextField(15);
        txtEmail = new JTextField(15);
        txtPass = new JPasswordField(15);
        txtCode = new JTextField(15);
        txtCode.setEnabled(false); // Deshabilitado hasta el paso 2

        btnSolicitar = new JButton("Enviar Código de Verificación");
        btnSolicitar.setBackground(new Color(0xC8102E));
        btnSolicitar.setForeground(Color.WHITE);

        btnConfirmar = new JButton("Confirmar y Crear Cuenta");
        btnConfirmar.setBackground(new Color(0x15803D));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setEnabled(false); // Deshabilitado hasta el paso 2

        gbc.gridx=0; gbc.gridy=0; add(new JLabel("Usuario:"), gbc);
        gbc.gridx=1; add(txtUser, gbc);
        gbc.gridx=0; gbc.gridy=1; add(new JLabel("Correo:"), gbc);
        gbc.gridx=1; add(txtEmail, gbc);
        gbc.gridx=0; gbc.gridy=2; add(new JLabel("Contraseña:"), gbc);
        gbc.gridx=1; add(txtPass, gbc);
        
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2; 
        add(new JLabel("<html><hr><b>Verificación por correo:</b></html>"), gbc);

        gbc.gridy=4; gbc.gridwidth=1;
        gbc.gridx=0; add(new JLabel("Código (6 dígitos):"), gbc);
        gbc.gridx=1; add(txtCode, gbc);

        gbc.gridx=0; gbc.gridy=5; gbc.gridwidth=2; add(btnSolicitar, gbc);
        gbc.gridy=6; add(btnConfirmar, gbc);

        btnSolicitar.addActionListener(e -> solicitarCodigo());
        btnConfirmar.addActionListener(e -> confirmarRegistro());
    }

    private void solicitarCodigo() {
        String u = txtUser.getText().trim();
        String m = txtEmail.getText().trim();
        String p = new String(txtPass.getPassword());

        if(u.isEmpty() || m.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Llene todos los campos antes de solicitar el código.", "Aviso", JOptionPane.WARNING_MESSAGE); 
            return;
        }

        boolean enviado = service.solicitarRegistro(u, p, m);
        
        if (enviado) {
            JOptionPane.showMessageDialog(this, "Código enviado a " + m + ".\nRevisa tu bandeja de entrada o spam.", "Correo Enviado", JOptionPane.INFORMATION_MESSAGE);
            
            // Bloquear campos y habilitar verificación
            paso2 = true;
            txtUser.setEnabled(false);
            txtEmail.setEnabled(false);
            txtPass.setEnabled(false);
            btnSolicitar.setEnabled(false);
            
            txtCode.setEnabled(true);
            txtCode.requestFocus();
            btnConfirmar.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "El nombre de usuario ya está en uso.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmarRegistro() {
        if (!paso2) return;

        String u = txtUser.getText().trim();
        String token = txtCode.getText().trim();

        if (token.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el código enviado a su correo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean exito = service.confirmarRegistro(u, token);

        if (exito) {
            JOptionPane.showMessageDialog(this, "¡Cuenta creada exitosamente!\nYa puedes iniciar sesión.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "El código es incorrecto. Intente de nuevo.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}