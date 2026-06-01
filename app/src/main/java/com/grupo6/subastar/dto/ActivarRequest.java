package com.grupo6.subastar.dto;

public class ActivarRequest {
    private String email;
    private String clave;
    private String claveConfirmacion;

    public ActivarRequest(String email, String clave, String claveConfirmacion) {
        this.email = email;
        this.clave = clave;
        this.claveConfirmacion = claveConfirmacion;
    }
}