package com.grupo6.subastar;

import com.grupo6.subastar.dto.LoginRequest;
import com.grupo6.subastar.dto.LoginResponse;
import com.grupo6.subastar.dto.PujaMensajeDTO;
import com.grupo6.subastar.model.ItemCatalogo;
import com.grupo6.subastar.model.Subasta;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SubastarApi {
    @GET("/v1/paises")
    Call<List<Pais>> getPaises();

    @GET("/v1/subastas")
    Call<List<Subasta>> obtenerSubastas(
            @Header("Authorization") String token,
            @Query("estado") String estado,
            @Query("categoria") String categoria,
            @Query("moneda") String moneda,
            @Query("fecha") String fecha
    );

    @GET("/v1/subastas/{id}")
    Call<Subasta> obtenerDetalleSubasta(
            @Path("id") Integer id,
            @Header("Authorization") String token
    );

    @GET("/v1/subastas/{id}/items/{itemId}")
    Call<ItemCatalogo> obtenerDetalleItem(
            @Path("id") Integer id,
            @Path("itemId") Integer itemId,
            @Header("Authorization") String token
    );

    @POST("v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // ETAPA 1 DEL REGISTRO: Solo 8 parámetros, sin clave y sin fecha.
    // Nombres exactos de las fotos según documentación.
    @Multipart
    @POST("v1/auth/registro")
    Call<ResponseBody> registrar(
            @Part("nombre") RequestBody nombre,
            @Part("apellido") RequestBody apellido,
            @Part("email") RequestBody email,
            @Part("documento") RequestBody documento,
            @Part("direccion") RequestBody direccion,
            @Part("fechaNacimiento") RequestBody fechaNacimiento,
            @Part("numeroPais") RequestBody numeroPais,
            @Part MultipartBody.Part fotoDniFrente,
            @Part MultipartBody.Part fotoDniDorso
    );

    @POST("v1/auth/activar")
    Call<ResponseBody> activarCuenta(@Body com.grupo6.subastar.dto.ActivarRequest request);


    @POST("v1/subastas/{id}/ingresar") // Ajustá el prefijo de la URL según tu backend
    Call<Void> ingresarSubasta(@Header("Authorization") String token, @Path("id") Integer id);

    @POST("v1/subastas/{id}/salir")
    Call<Void> salirSubasta(@Header("Authorization") String token, @Path("id") Integer id);

    @POST("v1/subastas/{id}/pujas")
    Call<PujaMensajeDTO> registrarPuja(
            @Header("Authorization") String token,
            @Path("id") Integer id,
            @Body com.grupo6.subastar.dto.PujaRequest request
    );

    @GET("v1/subastas/{id}/pujas/{itemId}")
    Call<List<PujaMensajeDTO>> obtenerHistorialPujas(
            @Header("Authorization") String token,
            @Path("id") Integer id,
            @Path("itemId") Integer itemId
    );


}
