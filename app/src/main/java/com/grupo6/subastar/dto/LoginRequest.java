package com.grupo6.subastar.dto;

public class LoginRequest {
    private String email;
    private String clave;

    public LoginRequest(String email, String clave) {
        this.email = email;
        this.clave = clave;
    }

    public String getEmail() { return email; }
    public String getClave() { return clave; }
}