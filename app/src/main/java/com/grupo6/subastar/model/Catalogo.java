package com.grupo6.subastar.model;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Catalogo {
    private List<ItemCatalogo> items;

    @SerializedName("description")
    private String descripcion;
    public List<ItemCatalogo> getItems() { return items; }

    public String getDescripcion() {
        return descripcion;
    }
}