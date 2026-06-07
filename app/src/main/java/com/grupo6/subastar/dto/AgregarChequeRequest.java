package com.grupo6.subastar.dto;

import java.math.BigDecimal;

public class AgregarChequeRequest {
    private String nroCheque;
    private String banco;
    private String moneda;
    private BigDecimal montoGarantia;
    private String fechaEntrega;

    public AgregarChequeRequest(String nroCheque, String banco,
                                String moneda, BigDecimal montoGarantia, String fechaEntrega) {
        this.nroCheque = nroCheque;
        this.banco = banco;
        this.moneda = moneda;
        this.montoGarantia = montoGarantia;
        this.fechaEntrega = fechaEntrega;
    }
}