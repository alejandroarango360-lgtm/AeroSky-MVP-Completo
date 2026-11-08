package com.aircolombia.prototipo.view;

import com.aircolombia.prototipo.model.Pasajero;
import com.aircolombia.prototipo.service.ReservaService;
import com.aircolombia.prototipo.service.ReservaService.ResultadoVerificacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

public class FrmVerificarPago extends JDialog {

    private JTextField txtCodigo;
    private JLabel lblEstado;
    private JButton btnVerificar, btnReenviar;
    private final String pnr, metodoPago, tarjeta, correoDestino;
    private final List<Pasajero> pasajeros;
    private final Pasajero comprador;
    private final ReservaService reservaService;
    private final JDialog ventanaResumen;
    private boolean pagoExitoso = false;

    private Timestamp bloqueadoHasta;
    private Timestamp codigoEnviadoEn;
    private int intentosRestantes;
    private Timer timer;

    public FrmVerificarPago(JDialog padre, String pnr, List<Pasajero> pasajeros, Pasajero comprador,
                            String metodoPago, String tarjeta, String correoDestino) {
        super(padre, "Confirmación de Pago", true);
        this.ventanaResumen = padre;
        this.pnr = pnr;
        this.pasajeros = pasajeros;
        this.comprador = comprador;
        this.metodoPago = metodoPago;
        this.tarjeta = tarjeta;
        this.correoDestino = correoDestino;
        this.reservaService = new ReservaService();

        initComponents();
        sincronizarEstado();
        iniciarTimer();
        setLocationRelativeTo(padre);
    }

    public boolean isPagoExitoso() { return pagoExitoso; }

    // ==================== UI ====================
    private void initComponents() {
        setSize(460, 380);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0xC8102E));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel lblTitulo = new JLabel("Verificación de Seguridad");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(Color.WHITE);
        header.add(lblTitulo, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setBackground(Color.WHITE);
        centro.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblInfo = new JLabel("<html><div style='text-align:center;'>"
                + "Enviamos un código de 6 dígitos a:<br>"
                + "<b style='color:#C8102E;'>" + correoDestino + "</b><br><br>"
                + "Ingrésalo a continuación para completar el pago:</div></html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(lblInfo);
        centro.add(Box.createRigidArea(new Dimension(0, 15)));

        txtCodigo = new JTextField();
        txtCodigo.setFont(new Font("Consolas", Font.BOLD, 24));
        txtCodigo.setHorizontalAlignment(JTextField.CENTER);
        txtCodigo.setMaximumSize(new Dimension(200, 40));
        txtCodigo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD5CFC0)),
                new EmptyBorder(5, 10, 5, 10)));
        centro.add(txtCodigo);

        centro.add(Box.createRigidArea(new Dimension(0, 12)));

        lblEstado = new JLabel(" ");
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEstado.setAlignmentX(Component.CENTER_ALIGNMENT);
        centro.add(lblEstado);

        centro.add(Box.createRigidArea(new Dimension(0, 10)));

        btnReenviar = new JButton("Reenviar código");
        btnReenviar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnReenviar.setFocusPainted(false);
        btnReenviar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReenviar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnReenviar.addActionListener(e -> reenviarCodigo());
        centro.add(btnReenviar);

        add(centro, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));
        botones.setBackground(Color.WHITE);
        botones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xD5CFC0)));

        JButton btnCancelar = new JButton("Cancelar Pago");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancelar.setBackground(new Color(0xE5E5E5));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> cancelarProceso());

        btnVerificar = new JButton("Verificar y Pagar");
        btnVerificar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnVerificar.setBackground(new Color(0x15803D));
        btnVerificar.setForeground(Color.WHITE);
        btnVerificar.setFocusPainted(false);
        btnVerificar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVerificar.addActionListener(e -> verificarPago());

        botones.add(btnCancelar);
        botones.add(btnVerificar);
        add(botones, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { cancelarProceso(); }
        });
    }

    // ==================== SINCRONIZACIÓN CON BD ====================
    private void sincronizarEstado() {
        ReservaService.BloqueoInfoView info = reservaService.obtenerEstadoActual(pnr);
        if (info == null) return;
        this.bloqueadoHasta = info.bloqueadoHasta;
        this.codigoEnviadoEn = info.codigoEnviadoEn;
        this.intentosRestantes = ReservaService.MAX_INTENTOS_VERIFICACION - info.intentosVerificacion;
    }

    // ==================== TIMER ====================
    private void iniciarTimer() {
        timer = new Timer(1000, e -> actualizarUI());
        timer.start();
        actualizarUI();
    }

    private void actualizarUI() {
        Timestamp ahora = new Timestamp(System.currentTimeMillis());

        // 1) Bloqueada
        if (bloqueadoHasta != null && bloqueadoHasta.after(ahora)) {
            long seg = (bloqueadoHasta.getTime() - ahora.getTime()) / 1000;
            lblEstado.setForeground(new Color(0xC8102E));
            lblEstado.setText("🔒 Reserva bloqueada. Desbloqueo en " + formatMMSS(seg));
            txtCodigo.setEnabled(false);
            btnVerificar.setEnabled(false);
            btnReenviar.setEnabled(false);
            return;
        }

        // 2) Auto-desbloqueo local
        if (bloqueadoHasta != null && bloqueadoHasta.before(ahora)) {
            bloqueadoHasta = null;
            sincronizarEstado(); // refrescar contadores reales
        }

        // 3) Sin código activo
        if (codigoEnviadoEn == null) {
            lblEstado.setForeground(new Color(0xB45309));
            lblEstado.setText("No hay código activo. Solicita uno nuevo.");
            txtCodigo.setEnabled(false);
            btnVerificar.setEnabled(false);
            btnReenviar.setEnabled(true);
            return;
        }

        // 4) Código expirado (aún no bloqueada)
        long segTranscurridos = (ahora.getTime() - codigoEnviadoEn.getTime()) / 1000;
        if (segTranscurridos > ReservaService.VIGENCIA_CODIGO_SEG) {
            lblEstado.setForeground(new Color(0xB45309));
            lblEstado.setText("El código expiró. Reenvía uno nuevo.");
            txtCodigo.setEnabled(true);
            btnVerificar.setEnabled(true);
            btnReenviar.setEnabled(true);
            return;
        }

        // 5) Estado normal con código vigente
        long segRestantes = ReservaService.VIGENCIA_CODIGO_SEG - segTranscurridos;
        lblEstado.setForeground(new Color(0x15803D));
        lblEstado.setText("Válido por " + formatMMSS(segRestantes)
                + " · Intentos restantes: " + Math.max(intentosRestantes, 0)
                + "/" + ReservaService.MAX_INTENTOS_VERIFICACION);
        txtCodigo.setEnabled(true);
        btnVerificar.setEnabled(true);
        btnReenviar.setEnabled(segTranscurridos >= ReservaService.COOLDOWN_REENVIO_SEG);
    }

    private String formatMMSS(long seg) {
        if (seg < 0) seg = 0;
        return String.format("%02d:%02d", seg / 60, seg % 60);
    }

    // ==================== ACCIÓN: VERIFICAR ====================
    private void verificarPago() {
        String codigo = txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa el código enviado a tu correo.");
            return;
        }

        // AQUI PASAMOS EL CORREO DESTINO AL SERVICIO
        ResultadoVerificacion r = reservaService.verificarCodigo(pnr, codigo, correoDestino);

        if (r.esExito()) {
            Object[] pago = reservaService.procesarPagoConPasajeros(
                    pnr, pasajeros, comprador, metodoPago, tarjeta);
            boolean exito = (boolean) pago[0];
            String mensaje = (String) pago[1];

            if (exito) {
                pagoExitoso = true;
                JOptionPane.showMessageDialog(this,
                        "Pago aprobado correctamente.", "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al procesar el pago: " + mensaje,
                        "Pago rechazado", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // Actualizar UI con el nuevo estado que reporta el servicio
        this.bloqueadoHasta = r.bloqueadoHasta;
        if (r.estado == ResultadoVerificacion.Estado.CODIGO_INVALIDO) {
            this.intentosRestantes = r.intentosRestantes;
            txtCodigo.setText("");
            txtCodigo.requestFocus();
        }
        actualizarUI();

        String titulo;
        switch (r.estado) {
            case BLOQUEADA:               titulo = "Reserva bloqueada"; break;
            case CODIGO_EXPIRADO:         titulo = "Código expirado";   break;
            case CODIGO_INVALIDO:         titulo = "Código incorrecto"; break;
            default:                      titulo = "Error";             break;
        }
        JOptionPane.showMessageDialog(this, r.mensaje, titulo, JOptionPane.WARNING_MESSAGE);

        // Refrescar contadores reales desde BD tras la operación
        sincronizarEstado();
    }

    // ==================== ACCIÓN: REENVIAR ====================
    private void reenviarCodigo() {
        ResultadoVerificacion r = reservaService.reenviarCodigo(pnr, correoDestino);
        if (r.esExito()) {
            txtCodigo.setText("");
            sincronizarEstado();
            actualizarUI();
            JOptionPane.showMessageDialog(this,
                    r.mensaje, "Código reenviado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    r.mensaje, "No se pudo reenviar", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ==================== ACCIÓN: CANCELAR ====================
    private void cancelarProceso() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Seguro que deseas cancelar el pago?\nSe liberarán los cupos reservados.",
                "Cancelar Pago", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            reservaService.expirarReserva(pnr);
            pagoExitoso = false;
            if (timer != null) timer.stop();
            dispose();
        }
    }

    @Override
    public void dispose() {
        if (timer != null) timer.stop();
        super.dispose();
    }
}