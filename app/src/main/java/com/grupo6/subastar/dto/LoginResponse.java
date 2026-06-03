package com.grupo6.subastar.dto;

public class LoginResponse {
    private String token;
    private ClienteDTO cliente;

    public String getToken() { return token; }
    public ClienteDTO getCliente() { return cliente; }
}