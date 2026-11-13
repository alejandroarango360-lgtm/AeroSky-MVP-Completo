package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.dao.UsuarioDAO;
import com.aircolombia.prototipo.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FrmGestionUsuarios extends JDialog {

    private JTable tabla;
    private DefaultTableModel modelo;
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    public FrmGestionUsuarios(JFrame padre) {
        super(padre, "Gestión de Usuarios", true);
        setSize(850, 500);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xFAF7F0));

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x6B21A8));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel lblTitulo = new JLabel("Administración de Usuarios y Roles");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // TABLA
        String[] columnas = {"ID", "Usuario", "Correo", "Rol", "Estado", "Intentos Fallidos"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(25);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // BOTONES
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBotones.setBackground(new Color(0xFAF7F0));

        JButton btnCrear = crearBoton("Crear Usuario", new Color(0x15803D));
        JButton btnDesbloquear = crearBoton("Desbloquear", new Color(0x0369A1));
        JButton btnCambiarRol = crearBoton("Cambiar Rol", new Color(0xB45309));
        JButton btnEliminar = crearBoton("Eliminar", new Color(0xB91C1C)); // NUEVO BOTÓN
        JButton btnRefrescar = crearBoton("Refrescar", new Color(0x6B6B6B));

        panelBotones.add(btnCrear);
        panelBotones.add(btnDesbloquear);
        panelBotones.add(btnCambiarRol);
        panelBotones.add(btnEliminar); // AGREGADO
        panelBotones.add(btnRefrescar);

        add(panelBotones, BorderLayout.SOUTH);

        btnRefrescar.addActionListener(e -> cargarUsuarios());
        btnDesbloquear.addActionListener(e -> desbloquear());
        btnCambiarRol.addActionListener(e -> cambiarRol());
        btnCrear.addActionListener(e -> crearUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario()); // EVENTO

        cargarUsuarios();
    }

    private JButton crearBoton(String texto, Color bg) {
        JButton b = new JButton(texto);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void cargarUsuarios() {
        modelo.setRowCount(0);
        List<Usuario> lista = usuarioDAO.listarUsuarios();
        for (Usuario u : lista) {
            modelo.addRow(new Object[]{
                u.getIdUsuario(), u.getUsername(), u.getEmail(), u.getRol(), u.getEstado(), u.getIntentosFallidos()
            });
        }
    }

    private int getSelectedId() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return (int) modelo.getValueAt(fila, 0);
    }

    private void desbloquear() {
        int id = getSelectedId();
        if (id == -1) return;

        if (usuarioDAO.desbloquearUsuario(id)) {
            JOptionPane.showMessageDialog(this, "Usuario desbloqueado y activado correctamente.");
            cargarUsuarios();
        } else {
            JOptionPane.showMessageDialog(this, "Error al desbloquear.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarRol() {
        int id = getSelectedId();
        if (id == -1) return;

        String[] opciones = {"ADMIN", "CLIENTE"};
        String nuevoRol = (String) JOptionPane.showInputDialog(this, 
                "Seleccione el nuevo rol para este usuario:", 
                "Cambiar Rol", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[1]);

        if (nuevoRol != null) {
            if (usuarioDAO.actualizarRol(id, nuevoRol)) {
                JOptionPane.showMessageDialog(this, "Rol actualizado a: " + nuevoRol);
                cargarUsuarios();
            }
        }
    }

    // NUEVO MÉTODO
    private void eliminarUsuario() {
        int id = getSelectedId();
        if (id == -1) return;

        int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de eliminar este usuario permanentemente?\nEsta acción no se puede deshacer.", 
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (usuarioDAO.eliminarUsuario(id)) {
                JOptionPane.showMessageDialog(this, "Usuario eliminado del sistema.");
                cargarUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar. Puede tener registros asociados.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void crearUsuario() {
        JTextField txtUser = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        String[] opciones = {"ADMIN", "CLIENTE"};
        JComboBox<String> cbRol = new JComboBox<>(opciones);

        Object[] mensaje = {
            "Usuario:", txtUser,
            "Correo:", txtEmail,
            "Contraseña:", txtPass,
            "Rol:", cbRol
        };

        int op = JOptionPane.showConfirmDialog(this, mensaje, "Crear Nuevo Usuario", JOptionPane.OK_CANCEL_OPTION);
        
        if (op == JOptionPane.OK_OPTION) {
            String u = txtUser.getText().trim();
            String e = txtEmail.getText().trim();
            String p = new String(txtPass.getPassword());
            String r = (String) cbRol.getSelectedItem();

            if (u.isEmpty() || e.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (usuarioDAO.buscarPorUsername(u) != null) {
                JOptionPane.showMessageDialog(this, "Ese nombre de usuario ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (usuarioDAO.crearUsuarioAdmin(u, p, e, r)) {
                JOptionPane.showMessageDialog(this, "Usuario " + r + " creado exitosamente.");
                cargarUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear el usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}