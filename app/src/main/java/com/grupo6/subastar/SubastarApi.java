package com.grupo6.subastar;

import com.grupo6.subastar.model.Subasta;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import retrofit2.http.Path;

public interface SubastarApi {
    @GET("/v1/paises")
    Call<List<Pais>> obtenerPaises();

    @GET("/v1/subastas")
    Call<List<Subasta>> obtenerSubastas(
            @Header("Authorization") String token,
            @Query("estado") String estado,
            @Query("categoria") String categoria,
            @Query("moneda") String moneda
    );

    @GET("/v1/subastas/{id}")
    Call<Subasta> obtenerDetalleSubasta(
            @Path("id") Integer id,
            @Header("Authorization") String token
    );
}
