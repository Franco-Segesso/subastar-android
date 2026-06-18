package com.grupo6.subastar.dto;

public class RespuestaConsignacionRequest {
    private boolean acepta;

    public RespuestaConsignacionRequest(boolean acepta) {
        this.acepta = acepta;
    }

    public boolean isAcepta() { return acepta; }
}
