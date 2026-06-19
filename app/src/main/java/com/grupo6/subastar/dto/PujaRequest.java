package com.grupo6.subastar.dto;

public class PujaRequest {
    private Integer itemId;
    private Double importe;
    private Integer medioPagoId;

    public PujaRequest(Integer itemId, Double importe, Integer medioPagoId) {
        this.itemId = itemId;
        this.importe = importe;
        this.medioPagoId = medioPagoId;
    }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public Double getImporte() { return importe; }
    public void setImporte(Double importe) { this.importe = importe; }
    public Integer getMedioPagoId() { return medioPagoId; }
}
