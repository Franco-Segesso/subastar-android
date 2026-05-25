package com.grupo6.subastar;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface SubastarApi {
    @GET("/v1/paises")
    Call<List<Pais>> obtenerPaises();
}
