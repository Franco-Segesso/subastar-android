package com.grupo6.subastar.dto;

public class RegistroRequest {



    private String nombre;
    private String apellido;
    private String email;
    private String clave;
    private String documento;



    public RegistroRequest(String nombre, String apellido, String email, String clave, String documento) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.clave = clave;
        this.documento = documento;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public String getClave() { return clave; }
    public String getDocumento() { return documento; }
}