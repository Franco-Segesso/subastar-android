package com.grupo6.subastar.dto;

import com.google.gson.annotations.SerializedName;

public class PujaMensajeDTO {
    @SerializedName("importe")
    private Double importe;

    // Asumimos que el backend envía la fecha serializada como String (ISO-8601)
    @SerializedName("fechaHora")
    private String fechaHora;

    @SerializedName("asistente")
    private AsistenteDTO asistente;

    public Double getImporte() { return importe; }
    public String getFechaHora() { return fechaHora; }
    public AsistenteDTO getAsistente() { return asistente; }

    // Clase interna para mapear la estructura anidada del Asistente -> Cliente -> Persona
    public static class AsistenteDTO {
        @SerializedName("cliente")
        private ClienteDTO cliente;
        public ClienteDTO getCliente() { return cliente; }
    }

    public static class ClienteDTO {
        @SerializedName("identificador")
        private Integer identificador;
        // Agrega aquí los campos de nombre si tu backend los está enviando en el JSON
        public Integer getIdentificador() { return identificador; }
    }
}