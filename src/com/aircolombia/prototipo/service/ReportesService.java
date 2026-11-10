package com.aircolombia.prototipo.service;

import com.aircolombia.prototipo.dao.ReportesDAO;
import java.util.List;

public class ReportesService {
    private ReportesDAO reportesDAO = new ReportesDAO();

    public double getVentasTotales(String desde, String hasta) {
        return reportesDAO.obtenerVentasTotales(desde, hasta);
    }

    public int getTiquetesVendidos(String desde, String hasta) {
        return reportesDAO.obtenerTiquetesVendidos(desde, hasta);
    }

    public int getCancelaciones(String desde, String hasta) {
        return reportesDAO.obtenerCancelaciones(desde, hasta);
    }

    public List<Object[]> getReportePorRuta(String desde, String hasta, String origen, String destino) {
        return reportesDAO.obtenerReportePorRuta(desde, hasta, origen, destino);
    }
}