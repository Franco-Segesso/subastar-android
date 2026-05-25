package com.grupo6.subastar;

import com.grupo6.subastar.dto.LoginRequest;
import com.grupo6.subastar.dto.LoginResponse;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import okhttp3.ResponseBody;

public interface SubastarApi {

    @GET("v1/paises")
    Call<List<Pais>> getPaises();

    @POST("v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // NUEVO: Endpoint Multipart para el registro
    @Multipart
    @POST("v1/auth/registro")
    Call<ResponseBody> registrar(
            @Part("nombre") RequestBody nombre,
            @Part("apellido") RequestBody apellido,
            @Part("email") RequestBody email,
            @Part("clave") RequestBody clave,
            @Part("documento") RequestBody documento,
            @Part("direccion") RequestBody direccion,
            @Part("fechaNacimiento") RequestBody fechaNacimiento,
            @Part("numeroPais") RequestBody numeroPais,
            @Part MultipartBody.Part fotoFrente,
            @Part MultipartBody.Part fotoDorso
    );
}