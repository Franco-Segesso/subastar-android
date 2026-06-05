package com.grupo6.subastar.dto;

public class AgregarCuentaRequest {
    private String cbuIban;
    private String alias;
    private String banco;
    private String paisBanco;
    private String moneda;

    public AgregarCuentaRequest(String cbuIban, String alias,
                                String banco, String paisBanco, String moneda) {
        this.cbuIban = cbuIban;
        this.alias = alias;
        this.banco = banco;
        this.paisBanco = paisBanco;
        this.moneda = moneda;
    }
}