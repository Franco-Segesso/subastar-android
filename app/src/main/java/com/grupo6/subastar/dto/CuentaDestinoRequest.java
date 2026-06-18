package com.grupo6.subastar.dto;

public class CuentaDestinoRequest {
    private String banco;
    private String cbu_iban;
    private String pais;
    private String moneda;

    public CuentaDestinoRequest(String banco, String cbuIban, String pais, String moneda) {
        this.banco = banco;
        this.cbu_iban = cbuIban;
        this.pais = pais;
        this.moneda = moneda;
    }
}
