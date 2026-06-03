package com.grupo6.subastar.model;
import java.util.List;

public class Producto {
    private String tipo;
    private List<Foto> fotos;
    private String historia;

    public String getTipo() { return tipo; }
    public List<Foto> getFotos() { return fotos; }

    public String getHistoria() {
        return historia;
    }
}