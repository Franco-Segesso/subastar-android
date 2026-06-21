package com.grupo6.subastar.dto;

import com.google.gson.annotations.SerializedName;

public class MedioPagoDTO {
    private Integer identificador;
    private String tipo;
    @SerializedName(value = "verificado", alternate = {"verificadoCheque"})
    private String verificado;
    private String activo;
    private String fechaAlta;

    // Campos de TarjetaCredito
    private String ultimosDigitos;
    private String vencimiento;
    private String titular;
    private String esExtranjera;
    private String paisEmisor;

    // Campos de CuentaBancaria
    private String cbuIban;
    private String alias;
    private String banco;
    private String paisBanco;
    private String moneda;
    private Double fondosReservados;
    private Double fondosDisponibles;

    // Campos de ChequeCertificado
    private String nroCheque;
    private Double montoGarantia;
    private String fechaEntrega;

    public Integer getIdentificador() { return identificador; }
    public String getTipo() { return tipo; }
    public String getVerificado() { return verificado; }
    public String getActivo() { return activo; }
    public String getFechaAlta() { return fechaAlta; }
    public String getUltimosDigitos() { return ultimosDigitos; }
    public String getVencimiento() { return vencimiento; }
    public String getTitular() { return titular; }
    public String getEsExtranjera() { return esExtranjera; }
    public String getPaisEmisor() { return paisEmisor; }
    public String getCbuIban() { return cbuIban; }
    public String getAlias() { return alias; }
    public String getBanco() { return banco; }
    public String getPaisBanco() { return paisBanco; }
    public String getMoneda() { return moneda; }
    public Double getFondosReservados() { return fondosReservados; }
    public Double getFondosDisponibles() { return fondosDisponibles; }
    public String getNroCheque() { return nroCheque; }
    public Double getMontoGarantia() { return montoGarantia; }
    public String getFechaEntrega() { return fechaEntrega; }
}
