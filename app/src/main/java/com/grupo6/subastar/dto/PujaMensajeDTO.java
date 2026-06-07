package com.grupo6.subastar.dto;

import com.google.gson.annotations.SerializedName;

public class PujaMensajeDTO {
    @SerializedName("itemId")
    private Integer itemId;

    @SerializedName("importe")
    private Double importe;

    @SerializedName("fechaHora")
    private String fechaHora;

    @SerializedName("asistente")
    private AsistenteDTO asistente;

    public Integer getItemId() { return itemId; }
    public Double getImporte() { return importe; }
    public String getFechaHora() { return fechaHora; }
    public AsistenteDTO getAsistente() { return asistente; }

    public static class AsistenteDTO {
        @SerializedName("cliente")
        private ClienteDTO cliente;

        public ClienteDTO getCliente() { return cliente; }
    }

    public static class ClienteDTO {
        @SerializedName("identificador")
        private Integer identificador;

        @SerializedName("nombre")
        private String nombre;

        @SerializedName("apellido")
        private String apellido;

        public Integer getIdentificador() { return identificador; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }

        public String getNombreCompleto() {
            String nombreSeguro = nombre != null ? nombre : "";
            String apellidoSeguro = apellido != null ? apellido : "";
            String completo = (nombreSeguro + " " + apellidoSeguro).trim();
            return completo.isEmpty() ? "Postor #" + identificador : completo;
        }
    }
}
