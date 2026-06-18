package com.grupo6.subastar.model;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Producto {
    private Integer id;
    private String tipo;
    private String descripcion;
    private String artista;
    private List<Foto> fotos;
    private String historia;

    @SerializedName("nombreDuenioReal")
    private String nombreDuenioReal;

    public Integer getId() { return id; }
    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public String getArtista() { return artista; }
    public List<Foto> getFotos() { return fotos; }

    public String getHistoria() {
        return historia;
    }

    public String getNombreDuenioReal() { return nombreDuenioReal; }
    public void setNombreDuenioReal(String nombreDuenioReal) { this.nombreDuenioReal = nombreDuenioReal; }
}
