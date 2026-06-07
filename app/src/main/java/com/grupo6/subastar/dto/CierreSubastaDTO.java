package com.grupo6.subastar.dto;

public class CierreSubastaDTO {
    private Integer itemId;
    private boolean hayGanador;
    private Integer idClienteGanador;
    private Double importeFinal;

    // Getters
    public Integer getItemId() { return itemId; }
    public boolean isHayGanador() { return hayGanador; }
    public Integer getIdClienteGanador() { return idClienteGanador; }
    public Double getImporteFinal() { return importeFinal; }
}