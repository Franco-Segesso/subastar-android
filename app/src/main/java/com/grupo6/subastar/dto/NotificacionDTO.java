package com.grupo6.subastar.dto;

public class NotificacionDTO {
    private Integer identificador;
    private String titulo;
    private String mensaje;
    private String tipo;
    private Integer referenciaId;
    private String fechaEnvio;
    private Boolean leido;

    // --- GETTERS ---
    public Integer getIdentificador() { return identificador; }
    public String getTitulo() { return titulo; }
    public String getMensaje() { return mensaje; }
    public String getTipo() { return tipo; }
    public Integer getReferenciaId() { return referenciaId; }
    public String getFechaEnvio() { return fechaEnvio; }
    public Boolean getLeido() { return leido; }
}