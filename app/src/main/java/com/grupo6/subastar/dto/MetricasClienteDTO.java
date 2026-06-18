package com.grupo6.subastar.dto;

import java.util.List;

public class MetricasClienteDTO {
    private Integer totalSubastas;
    private Integer subastaGanadas;
    private Double porcentajeExito;
    private Double totalPujado;
    private Double totalPagado;
    private Integer totalPujas;
    private Double promedioPujasPorSubasta;
    private Double pujaMaxima;
    private List<ActividadCategoriaDTO> actividadPorCategoria;
    private String categoriaActual;
    private Integer consignaciones;

    public Integer getTotalSubastas() { return totalSubastas; }
    public Integer getSubastaGanadas() { return subastaGanadas; }
    public Double getPorcentajeExito() { return porcentajeExito; }
    public Double getTotalPujado() { return totalPujado; }
    public Double getTotalPagado() { return totalPagado; }
    public Integer getTotalPujas() { return totalPujas; }
    public Double getPromedioPujasPorSubasta() { return promedioPujasPorSubasta; }
    public Double getPujaMaxima() { return pujaMaxima; }
    public List<ActividadCategoriaDTO> getActividadPorCategoria() { return actividadPorCategoria; }
    public String getCategoriaActual() { return categoriaActual; }
    public Integer getConsignaciones() { return consignaciones; }

    public static class ActividadCategoriaDTO {
        private String categoria;
        private Integer cantidadPujas;

        public String getCategoria() { return categoria; }
        public Integer getCantidadPujas() { return cantidadPujas; }
    }
}
