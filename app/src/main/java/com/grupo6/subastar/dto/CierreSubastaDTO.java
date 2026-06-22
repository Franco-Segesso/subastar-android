package com.grupo6.subastar.dto;

public class CierreSubastaDTO {
    private Integer itemId;
    private boolean hayGanador;
    private Integer idClienteGanador;
    private Double importeFinal;
    private Integer compraId;
    private boolean multaGenerada;

    // Getters
    public Integer getItemId() { return itemId; }
    public boolean isHayGanador() { return hayGanador; }
    public Integer getIdClienteGanador() { return idClienteGanador; }
    public Double getImporteFinal() { return importeFinal; }
    public Integer getCompraId() { return compraId; }
    public boolean isMultaGenerada() { return multaGenerada; }
}
