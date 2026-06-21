package com.grupo6.subastar.dto;

import java.util.List;

public class HistorialPujasClienteDTO {
    private SubastaDTO subasta;
    private ResumenDTO resumen;
    private List<PujaDTO> pujas;

    public SubastaDTO getSubasta() { return subasta; }
    public ResumenDTO getResumen() { return resumen; }
    public List<PujaDTO> getPujas() { return pujas; }

    public static class SubastaDTO {
        private Integer identificador;
        private String nombre;
        private String moneda;

        public Integer getIdentificador() { return identificador; }
        public String getNombre() { return nombre; }
        public String getMoneda() { return moneda; }
    }

    public static class ResumenDTO {
        private Double totalPujado;
        private Double totalPagado;
        private Integer cantidadPujas;
        private Boolean gano;

        public Double getTotalPujado() { return totalPujado; }
        public Double getTotalPagado() { return totalPagado; }
        public Integer getCantidadPujas() { return cantidadPujas; }
        public Boolean getGano() { return gano; }
    }

    public static class PujaDTO {
        private Integer orden;
        private Integer itemId;
        private String descripcionItem;
        private Double importe;
        private String fechaHora;
        private Boolean esGanadora;
        private Integer compraId;
        private String estadoPago;
        private SuperadaPorDTO superadaPor;

        public Integer getOrden() { return orden; }
        public Integer getItemId() { return itemId; }
        public String getDescripcionItem() { return descripcionItem; }
        public Double getImporte() { return importe; }
        public String getFechaHora() { return fechaHora; }
        public Boolean getEsGanadora() { return esGanadora; }
        public Integer getCompraId() { return compraId; }
        public String getEstadoPago() { return estadoPago; }
        public SuperadaPorDTO getSuperadaPor() { return superadaPor; }
    }

    public static class SuperadaPorDTO {
        private String postor;
        private Double importe;

        public String getPostor() { return postor; }
        public Double getImporte() { return importe; }
    }
}
