package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.model.Usuario;
import com.aircolombia.prototipo.service.SeguridadService;
import com.aircolombia.prototipo.service.SeguridadService.ResultadoLogin;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FrmLogin extends JDialog {
    private JTextField txtUser, txt2FA;
    private JPasswordField txtPass;
    private JButton btnLogin, btnIrRegistro;
    private SeguridadService service = new SeguridadService();
    private String currentUser;
    private boolean paso2 = false;
    private Usuario usuarioLogueado = null; 
    private String requiredRole; // NUEVO: Para validar si es ADMIN o CLIENTE

    public FrmLogin(JFrame padre, String requiredRole) {
        super(padre, "Acceso al Sistema - AeroSky", true);
        this.requiredRole = requiredRole;
        
        setSize(380, 350);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUser = new JTextField(15);
        txtPass = new JPasswordField(15);
        txt2FA = new JTextField(15);
        txt2FA.setEnabled(false);
        
        btnLogin = new JButton("Ingresar");
        btnLogin.setBackground(new Color(0xC8102E)); 
        btnLogin.setForeground(Color.WHITE);
        
        btnIrRegistro = new JButton("Crear Cuenta Nueva");
        btnIrRegistro.setContentAreaFilled(false);
        btnIrRegistro.setBorderPainted(false);
        btnIrRegistro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIrRegistro.setForeground(new Color(0x0369A1));

        gbc.gridx=0; gbc.gridy=0; add(new JLabel("Usuario:"), gbc);
        gbc.gridx=1; add(txtUser, gbc);
        gbc.gridx=0; gbc.gridy=1; add(new JLabel("Contraseña:"), gbc);
        gbc.gridx=1; add(txtPass, gbc);
        gbc.gridx=0; gbc.gridy=2; add(new JLabel("Código 2FA:"), gbc);
        gbc.gridx=1; add(txt2FA, gbc);
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2; add(btnLogin, gbc);
        gbc.gridy=4; add(btnIrRegistro, gbc);

        btnLogin.addActionListener(e -> procesar());
        btnIrRegistro.addActionListener(e -> new FrmRegistro(this).setVisible(true));
        
        // Si es la ventana principal (padre == null) y se cierra, cerramos todo el sistema
        if (padre == null) {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
        }
    }
    
    public Usuario getUsuarioLogueado() { return usuarioLogueado; }

    private void procesar() {
        if (!paso2) {
            ResultadoLogin res = service.loginPaso1(txtUser.getText().trim(), new String(txtPass.getPassword()), requiredRole);
            JOptionPane.showMessageDialog(this, res.mensaje, res.exito ? "Info" : "Error", res.exito ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            
            if (res.exito) {
                currentUser = txtUser.getText().trim();
                paso2 = true;
                txtUser.setEnabled(false); txtPass.setEnabled(false);
                txt2FA.setEnabled(true); txt2FA.requestFocus();
                btnLogin.setText("Verificar Código 2FA");
            }
        } else {
            ResultadoLogin res = service.loginPaso2(currentUser, txt2FA.getText().trim());
            JOptionPane.showMessageDialog(this, res.mensaje, res.exito ? "Éxito" : "Error", res.exito ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            
            if (res.exito) {
                usuarioLogueado = res.usuario;
                dispose(); 
            }
        }
    }
}