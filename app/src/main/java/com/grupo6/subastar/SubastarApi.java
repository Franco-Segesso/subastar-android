package com.grupo6.subastar;

import com.grupo6.subastar.dto.LoginRequest;
import com.grupo6.subastar.dto.LoginResponse;
import java.util.List;

import com.grupo6.subastar.dto.RegistroRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SubastarApi {

    // Tu Bala Trazadora original
    @GET("v1/paises")
    Call<List<Pais>> getPaises();

    // El nuevo endpoint de la Vertical de Seguridad
    @POST("v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("v1/auth/registro")
    Call<String> registrar(@Body RegistroRequest request);
}