package com.grupo6.subastar.dto;

import java.util.List;

public class ConsignacionDTO {
    private Integer identificador;
    private String estado;
    private String motivoRechazo;
    private Boolean condicionesAceptadas;
    private String fechaSolicitud;
    private ProductoConsignadoDTO producto;
    private CondicionesEmpresaDTO condicionesEmpresa;
    private UbicacionDepositoDTO ubicacionDeposito;
    private SeguroDTO seguro;
    private List<InstanciaDTO> instancias;

    public Integer getIdentificador() { return identificador; }
    public String getEstado() { return estado; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public Boolean getCondicionesAceptadas() { return condicionesAceptadas; }
    public String getFechaSolicitud() { return fechaSolicitud; }
    public ProductoConsignadoDTO getProducto() { return producto; }
    public CondicionesEmpresaDTO getCondicionesEmpresa() { return condicionesEmpresa; }
    public UbicacionDepositoDTO getUbicacionDeposito() { return ubicacionDeposito; }
    public SeguroDTO getSeguro() { return seguro; }
    public List<InstanciaDTO> getInstancias() { return instancias; }

    public static class ProductoConsignadoDTO {
        private Integer identificador;
        private String tipoBien;
        private String descripcion;
        private String artista;
        private String fechaCreacion;
        private String historia;
        private List<String> fotos;

        public Integer getIdentificador() { return identificador; }
        public String getTipoBien() { return tipoBien; }
        public String getDescripcion() { return descripcion; }
        public String getArtista() { return artista; }
        public String getFechaCreacion() { return fechaCreacion; }
        public String getHistoria() { return historia; }
        public List<String> getFotos() { return fotos; }
    }

    public static class CondicionesEmpresaDTO {
        private Double precioBase;
        private Double comisionEmpresa;
        private String seguroPoliza;
        private String contactoPoliza;
        private String subastaAsignada;
        private String moneda;

        public Double getPrecioBase() { return precioBase; }
        public Double getComisionEmpresa() { return comisionEmpresa; }
        public String getSeguroPoliza() { return seguroPoliza; }
        public String getContactoPoliza() { return contactoPoliza; }
        public String getSubastaAsignada() { return subastaAsignada; }
        public String getMoneda() { return moneda; }
    }

    public static class UbicacionDepositoDTO {
        private String nombre;
        private String direccion;
        public String getNombre() { return nombre; }
        public String getDireccion() { return direccion; }
    }

    public static class SeguroDTO {
        private String nroPoliza;
        private String compania;
        private Double importe;
        private String polizaCombinada;
        private String moneda;
        public String getNroPoliza() { return nroPoliza; }
        public String getCompania() { return compania; }
        public Double getImporte() { return importe; }
        public String getPolizaCombinada() { return polizaCombinada; }
        public String getMoneda() { return moneda; }
    }

    public static class InstanciaDTO {
        private String titulo;
        private String fecha;
        private Boolean completada;
        private Boolean actual;
        public String getTitulo() { return titulo; }
        public String getFecha() { return fecha; }
        public Boolean getCompletada() { return completada; }
        public Boolean getActual() { return actual; }
    }
}
