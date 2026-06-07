package com.grupo6.subastar.dto;

public class PujaRequest {
    private Integer itemId;
    private Double importe;

    public PujaRequest(Integer itemId, Double importe) {
        this.itemId = itemId;
        this.importe = importe;
    }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public Double getImporte() { return importe; }
    public void setImporte(Double importe) { this.importe = importe; }
}