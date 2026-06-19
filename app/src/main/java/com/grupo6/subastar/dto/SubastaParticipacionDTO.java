package com.grupo6.subastar.dto;

public class SubastaParticipacionDTO {
    private SubastaDTO subasta;
    private Integer cantidadPujas;
    private Double mayorOfertaRealizada;
    private Boolean gano;
    private Integer loteGanado;
    private Double importePagado;
    private Integer compraId;
    private String estadoPago;

    public SubastaDTO getSubasta() { return subasta; }
    public Integer getCantidadPujas() { return cantidadPujas; }
    public Double getMayorOfertaRealizada() { return mayorOfertaRealizada; }
    public Boolean getGano() { return gano; }
    public Integer getLoteGanado() { return loteGanado; }
    public Double getImportePagado() { return importePagado; }
    public Integer getCompraId() { return compraId; }
    public String getEstadoPago() { return estadoPago; }

    public static class SubastaDTO {
        private Integer identificador;
        private String nombre;
        private String fecha;
        private String hora;
        private String categoria;
        private String moneda;

        public Integer getIdentificador() { return identificador; }
        public String getNombre() { return nombre; }
        public String getFecha() { return fecha; }
        public String getHora() { return hora; }
        public String getCategoria() { return categoria; }
        public String getMoneda() { return moneda; }
    }
}
