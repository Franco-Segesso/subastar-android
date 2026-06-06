package com.grupo6.subastar.model;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Producto {
    private String tipo;
    private List<Foto> fotos;
    private String historia;

    @SerializedName("nombreDuenioReal")
    private String nombreDuenioReal;

    public String getTipo() { return tipo; }
    public List<Foto> getFotos() { return fotos; }

    public String getHistoria() {
        return historia;
    }

    public String getNombreDuenioReal() { return nombreDuenioReal; }
    public void setNombreDuenioReal(String nombreDuenioReal) { this.nombreDuenioReal = nombreDuenioReal; }
}