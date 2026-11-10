package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.service.SeguridadService;
import com.aircolombia.prototipo.service.SeguridadService.ResultadoLogin;
import javax.swing.*;
import java.awt.*;

public class FrmLogin extends JDialog {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JTextField txt2FA;
    private JButton btnLogin;
    private SeguridadService service = new SeguridadService();
    private String currentUser;
    private boolean paso2 = false;

    // ESTE ES EL CONSTRUCTOR QUE FALTABA (recibe la ventana padre)
    public FrmLogin(JFrame padre) {
        super(padre, "Acceso Administrativo (RF-26 al RF-29)", true);
        setSize(380, 300);
        setLocationRelativeTo(padre);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblUser = new JLabel("Usuario:");
        gbc.gridx = 0; gbc.gridy = 0;
        add(lblUser, gbc);

        txtUser = new JTextField(15);
        gbc.gridx = 1;
        add(txtUser, gbc);

        JLabel lblPass = new JLabel("Contraseña:");
        gbc.gridx = 0; gbc.gridy = 1;
        add(lblPass, gbc);

        txtPass = new JPasswordField(15);
        gbc.gridx = 1;
        add(txtPass, gbc);

        JLabel lbl2FA = new JLabel("Código 2FA:");
        gbc.gridx = 0; gbc.gridy = 2;
        add(lbl2FA, gbc);

        txt2FA = new JTextField(15);
        txt2FA.setEnabled(false); // Deshabilitado hasta pasar el paso 1
        gbc.gridx = 1;
        add(txt2FA, gbc);

        btnLogin = new JButton("Ingresar");
        btnLogin.setBackground(new Color(0xC8102E));
        btnLogin.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(btnLogin, gbc);

        btnLogin.addActionListener(e -> procesar());
    }

    private void procesar() {
        if (!paso2) {
            // PASO 1
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());
            ResultadoLogin res = service.loginPaso1(u, p);

            if (res.exito) {
                JOptionPane.showMessageDialog(this, res.mensaje);
                currentUser = u;
                paso2 = true;
                txtUser.setEnabled(false);
                txtPass.setEnabled(false);
                txt2FA.setEnabled(true);
                txt2FA.requestFocus();
                btnLogin.setText("Verificar Código");
            } else {
                JOptionPane.showMessageDialog(this, res.mensaje, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // PASO 2
            String token = txt2FA.getText().trim();
            ResultadoLogin res = service.loginPaso2(currentUser, token);

            if (res.exito) {
                JOptionPane.showMessageDialog(this, "Bienvenido al sistema.");
                dispose();
                // Abrir el panel de administrador
                new FrmAdmin().setVisible(true); 
            } else {
                JOptionPane.showMessageDialog(this, res.mensaje, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}