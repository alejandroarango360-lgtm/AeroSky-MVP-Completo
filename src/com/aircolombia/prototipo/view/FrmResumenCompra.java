package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.dao.VueloDAO;
import com.aircolombia.prototipo.model.Pasajero;
import com.aircolombia.prototipo.model.Vuelo;
import com.aircolombia.prototipo.service.ReservaService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FrmResumenCompra extends JDialog {

    private static final Color PRINCIPAL = new Color(0xC8102E);
    private static final Color PRINCIPAL_HOVER = new Color(0xA00D24);
    private static final Color GRIS_BTN = new Color(0xE5E5E5);
    private static final Color GRIS_BTN_HOVER = new Color(0xD0D0D0);
    private static final Color FONDO = Color.WHITE;
    private static final Color BORDE = new Color(0xD5CFC0);
    private static final Color TEXTO = new Color(0x1A1A1A);
    private static final Color VERDE = new Color(0x15803D);

    private ReservaService reservaService;
    private String pnr;
    private List<Pasajero> pasajeros;
    private Pasajero comprador; // <--- NUEVO: Para guardar los datos del comprador (RF-07)
    private String metodoPago;
    private String tarjeta;
    private String correoTitular;
    private JDialog ventanaPadre; // Referencia a FrmCompra para cerrarlo

    public FrmResumenCompra(JDialog padre, String pnr, int idVueloIda, Integer idVueloRegreso,
                            String clase, List<Pasajero> pasajeros, Pasajero comprador, double total,
                            String metodo, String tarjeta, String correoTitular) {
        super(padre, "Resumen de Compra", true);
        this.ventanaPadre = padre;
        this.pnr = pnr;
        this.pasajeros = pasajeros;
        this.comprador = comprador;
        this.metodoPago = metodo;
        this.tarjeta = tarjeta;
        this.correoTitular = correoTitular;
        this.reservaService = new ReservaService();
        
        initComponents(idVueloIda, idVueloRegreso, clase, total);
        setLocationRelativeTo(padre);
    }

    private void initComponents(int idVueloIda, Integer idVueloRegreso, String clase, double total) {
        setSize(520, 650);
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRINCIPAL);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel lblTitulo = new JLabel("Resumen de la Compra");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // CENTRO (SCROLL)
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(FONDO);
        content.setBorder(new EmptyBorder(15, 25, 15, 25));

        // 1. Itinerario
        VueloDAO vueloDAO = new VueloDAO();
        Vuelo ida = vueloDAO.buscarPorId(idVueloIda);
        Vuelo regreso = (idVueloRegreso != null) ? vueloDAO.buscarPorId(idVueloRegreso) : null;

        content.add(crearTituloSeccion("Itinerario"));
        if (ida != null) {
            content.add(crearFila("Vuelo Ida:", ida.getNumeroVuelo() + " - " + ida.getFechaSalida()));
        }
        if (regreso != null) {
            content.add(crearFila("Vuelo Regreso:", regreso.getNumeroVuelo() + " - " + regreso.getFechaSalida()));
        }
        content.add(crearFila("Clase:", clase));
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        // 2. Pasajeros
        content.add(crearTituloSeccion("Pasajeros"));
        for (Pasajero p : pasajeros) {
            content.add(crearFila("• " + p.getNombreCompleto(), "Doc: " + p.getNumeroDocumento()));
        }
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        // 3. Desglose Financiero (RF-17)
        content.add(crearTituloSeccion("Desglose Financiero"));
        // Simulamos que el total ya incluye un 19% de IVA
        double base = total / 1.19;
        double impuestos = total - base;

        content.add(crearFila("Tarifa Base:", String.format("$ %,.0f", base)));
        content.add(crearFila("Servicios Adicionales:", "$ 0"));
        content.add(crearFila("Descuentos:", "$ 0"));
        content.add(crearFila("Impuestos (19%):", String.format("$ %,.0f", impuestos)));
        content.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JPanel filaTotal = new JPanel(new BorderLayout());
        filaTotal.setBackground(FONDO);
        filaTotal.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblTotal = new JLabel("Total a Pagar:");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel lblTotalValor = new JLabel(String.format("$ %,.0f", total));
        lblTotalValor.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalValor.setForeground(VERDE);
        filaTotal.add(lblTotal, BorderLayout.WEST);
        filaTotal.add(lblTotalValor, BorderLayout.EAST);
        content.add(filaTotal);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(FONDO);
        add(scroll, BorderLayout.CENTER);

        // BOTONES
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelBotones.setBackground(FONDO);
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDE));

        JButton btnVolver = crearBoton("Volver", GRIS_BTN, GRIS_BTN_HOVER, TEXTO);
        btnVolver.setPreferredSize(new Dimension(120, 38));
        btnVolver.addActionListener(e -> dispose());

        JButton btnConfirmar = crearBoton("Pagar Ahora", PRINCIPAL, PRINCIPAL_HOVER, Color.WHITE);
        btnConfirmar.setPreferredSize(new Dimension(150, 38));
        btnConfirmar.addActionListener(e -> ejecutarPago());

        panelBotones.add(btnVolver);
        panelBotones.add(btnConfirmar);
        add(panelBotones, BorderLayout.SOUTH);
    }

        private void ejecutarPago() {
        // 1. Enviar el correo con el código (usando el correoTitular)
        boolean correoEnviado = reservaService.enviarCorreoConfirmacion(pnr, correoTitular);
        
        if (correoEnviado) {
            JOptionPane.showMessageDialog(this, 
                    "Hemos enviado un código de confirmación a:\n" + correoTitular + "\n\nRevisa tu bandeja de entrada o spam.", 
                    "Código Enviado", JOptionPane.INFORMATION_MESSAGE);
            
            // 2. Abrir la ventana para que el usuario ingrese el código
            FrmVerificarPago frmVerificar = new FrmVerificarPago(this, pnr, pasajeros, comprador, metodoPago, tarjeta, correoTitular);
            frmVerificar.setVisible(true);
            
            // 3. Evaluar si el pago se completó
            if (frmVerificar.isPagoExitoso()) {
                // Si fue exitoso, cerramos el FrmCompra original
                if (ventanaPadre instanceof FrmCompra) {
                    ((FrmCompra) ventanaPadre).forzarCierrePorPago();
                }
                dispose(); // Cerramos este resumen
                
                // Mostrar confirmación final
                JFrame framePrincipal = (JFrame) SwingUtilities.getWindowAncestor(ventanaPadre);
                FrmConfirmacion frmConf = new FrmConfirmacion(framePrincipal, correoTitular, pnr);
                frmConf.setVisible(true);
            } else {
                // Si el usuario canceló el pago en la ventana de verificación, 
                // ya se liberaron los cupos. Cerramos este resumen y el FrmCompra.
                if (ventanaPadre instanceof FrmCompra) {
                    ((FrmCompra) ventanaPadre).forzarCierrePorPago();
                }
                dispose();
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                    "No se pudo enviar el correo de confirmación.\n"
                    + "Verifica tu conexión a internet o intenta de nuevo.", 
                    "Error de Red", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel crearTituloSeccion(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(PRINCIPAL);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel crearFila(String izq, String der) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setBackground(FONDO);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel l1 = new JLabel(izq);
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JLabel l2 = new JLabel(der);
        l2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fila.add(l1, BorderLayout.WEST);
        fila.add(l2, BorderLayout.EAST);
        return fila;
    }

    private JButton crearBoton(String texto, Color normal, Color hover, Color fg) {
        JButton btn = new JButton(texto);
        btn.setBackground(normal);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(normal); }
        });
        return btn;
    }
}