package com.grupo6.subastar.dto;

public class MultaDTO {
    private Integer identificador;
    private Double importe;
    private String estado;
    private String fechaGeneracion;
    private String fechaVencimiento;
    private String fechaPago;
    private String subasta;
    private Double importeOfertado;
    private Integer compraId;
    private String estadoCompra;
    private Double totalCompra;

    public Integer getIdentificador() { return identificador; }
    public Double getImporte() { return importe; }
    public String getEstado() { return estado; }
    public String getFechaGeneracion() { return fechaGeneracion; }
    public String getFechaVencimiento() { return fechaVencimiento; }
    public String getFechaPago() { return fechaPago; }
    public String getSubasta() { return subasta; }
    public Double getImporteOfertado() { return importeOfertado; }
    public Integer getCompraId() { return compraId; }
    public String getEstadoCompra() { return estadoCompra; }
    public Double getTotalCompra() { return totalCompra; }
}
