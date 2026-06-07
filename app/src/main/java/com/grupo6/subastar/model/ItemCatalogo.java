package com.grupo6.subastar.model;

public class ItemCatalogo {
    private Integer id;
    private Double precioBase;
    private Producto producto;
    private String subastado;
    private Double precioFinal;

    public Integer getId() { return id; }
    public Double getPrecioBase() { return precioBase; }
    public Producto getProducto() { return producto; }
    public String getSubastado() { return subastado; }
    public Double getPrecioFinal() { return precioFinal; }
    public void setPrecioFinal(Double precioFinal) { this.precioFinal = precioFinal; }
}