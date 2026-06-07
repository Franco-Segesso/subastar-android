package com.grupo6.subastar.dto;

import com.google.gson.annotations.SerializedName;

public class EstadoPujaDTO {
    @SerializedName("subastaId")
    private Integer subastaId;

    @SerializedName("itemId")
    private Integer itemId;

    @SerializedName("tiempoRestanteSegundos")
    private Integer tiempoRestanteSegundos;

    @SerializedName("importeActual")
    private Double importeActual;

    @SerializedName("cerrado")
    private boolean cerrado;

    public Integer getSubastaId() { return subastaId; }
    public Integer getItemId() { return itemId; }
    public Integer getTiempoRestanteSegundos() { return tiempoRestanteSegundos; }
    public Double getImporteActual() { return importeActual; }
    public boolean isCerrado() { return cerrado; }
}
