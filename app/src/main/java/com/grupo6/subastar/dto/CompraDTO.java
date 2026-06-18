package com.grupo6.subastar.dto;

public class CompraDTO {
    private Integer identificador;
    private SubastaDTO subasta;
    private ItemDTO item;
    private Double importePujado;
    private Double comision;
    private Double costoEnvio;
    private Double total;
    private String modalidadEntrega;
    private String direccionEnvio;
    private String avisoSeguro;

    public Integer getIdentificador() { return identificador; }
    public SubastaDTO getSubasta() { return subasta; }
    public ItemDTO getItem() { return item; }
    public Double getImportePujado() { return importePujado; }
    public Double getComision() { return comision; }
    public Double getCostoEnvio() { return costoEnvio; }
    public Double getTotal() { return total; }
    public String getModalidadEntrega() { return modalidadEntrega; }
    public String getDireccionEnvio() { return direccionEnvio; }
    public String getAvisoSeguro() { return avisoSeguro; }

    public static class SubastaDTO {
        private Integer identificador;
        private String nombre;
        private String moneda;

        public Integer getIdentificador() { return identificador; }
        public String getNombre() { return nombre; }
        public String getMoneda() { return moneda; }
    }

    public static class ItemDTO {
        private Integer identificador;
        private Integer numeroPieza;
        private String descripcionCatalogo;

        public Integer getIdentificador() { return identificador; }
        public Integer getNumeroPieza() { return numeroPieza; }
        public String getDescripcionCatalogo() { return descripcionCatalogo; }
    }
}
