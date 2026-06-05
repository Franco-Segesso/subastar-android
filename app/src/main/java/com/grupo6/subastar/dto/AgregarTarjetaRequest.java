package com.grupo6.subastar.dto;

public class AgregarTarjetaRequest {
    private String ultimosDigitos;
    private String vencimiento;
    private String titular;
    private String esExtranjera;
    private String paisEmisor;

    public AgregarTarjetaRequest(String ultimosDigitos, String vencimiento,
                                 String titular, String esExtranjera, String paisEmisor) {
        this.ultimosDigitos = ultimosDigitos;
        this.vencimiento = vencimiento;
        this.titular = titular;
        this.esExtranjera = esExtranjera;
        this.paisEmisor = paisEmisor;
    }
}