package com.grupo6.subastar.dto;

public class ClienteDTO {
    private Integer identificador;
    private String nombre;
    private String apellido;
    private String email;
    private String categoria;
    private String admitido;

    private String documento;
    private String direccion;
    private String pais;

    private String foto;

    public Integer getIdentificador() { return identificador; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public String getCategoria() { return categoria; }
    public String getAdmitido() { return admitido; }

    public String getDocumento() { return documento; }
    public String getDireccion() { return direccion; }
    public String getPais() { return pais; }

    public String getFoto() { return foto; }
}