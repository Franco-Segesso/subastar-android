package com.grupo6.subastar;

import com.grupo6.subastar.dto.LoginRequest;
import com.grupo6.subastar.dto.LoginResponse;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface SubastarApi {

    @GET("v1/paises")
    Call<List<Pais>> getPaises();

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
}