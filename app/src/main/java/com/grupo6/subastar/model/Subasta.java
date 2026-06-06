package com.grupo6.subastar.model;

public class Subasta {
    private Integer id;
    private String fecha;
    private String hora;
    private String estado;
    private String categoria;
    private String moneda;
    private String ubicacion;
    private Catalogo catalogo;

    public Integer getId() { return id; }
    public String getEstado() { return estado; }
    public String getCategoria() { return categoria; }
    public String getMoneda() { return moneda; }
    public String getUbicacion() { return ubicacion; }
    public Catalogo getCatalogo() { return catalogo; }
    private Double mejorOferta;
    private Integer cantidadPostores;
    private Integer itemActual;
    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public Double getMejorOferta() { return mejorOferta; }
    public void setMejorOferta(Double mejorOferta) { this.mejorOferta = mejorOferta; }

    public Integer getCantidadPostores() { return cantidadPostores; }
    public void setCantidadPostores(Integer cantidadPostores) { this.cantidadPostores = cantidadPostores; }

    public Integer getItemActual() { return itemActual; }
    public void setItemActual(Integer itemActual) { this.itemActual = itemActual; }
}