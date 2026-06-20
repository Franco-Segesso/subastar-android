package com.grupo6.subastar;

import com.grupo6.subastar.dto.LoginRequest;
import com.grupo6.subastar.dto.LoginResponse;
import com.grupo6.subastar.dto.NotificacionDTO;
import com.grupo6.subastar.dto.PujaMensajeDTO;
import com.grupo6.subastar.dto.ConsignacionDTO;
import com.grupo6.subastar.dto.CuentaDestinoRequest;
import com.grupo6.subastar.dto.RespuestaConsignacionRequest;
import com.grupo6.subastar.dto.HistorialPujasClienteDTO;
import com.grupo6.subastar.dto.MetricasClienteDTO;
import com.grupo6.subastar.dto.SubastaParticipacionDTO;
import com.grupo6.subastar.dto.CompraDTO;
import com.grupo6.subastar.dto.ModalidadEntregaRequest;
import com.grupo6.subastar.dto.PagarCompraRequest;
import com.grupo6.subastar.dto.MultaDTO;
import com.grupo6.subastar.dto.PagarMultaRequest;
import com.grupo6.subastar.model.ItemCatalogo;
import com.grupo6.subastar.model.Subasta;

import com.grupo6.subastar.dto.MedioPagoDTO;
import com.grupo6.subastar.dto.AgregarTarjetaRequest;
import com.grupo6.subastar.dto.AgregarCuentaRequest;
import com.grupo6.subastar.dto.AgregarChequeRequest;
import retrofit2.http.DELETE;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
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

    @POST("v1/subastas/{id}/ingresar")
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


    // MEDIOS DE PAGO
    @GET("/v1/medios-pago/cliente/{clienteId}")
    Call<List<MedioPagoDTO>> obtenerMediosPago(
            @Path("clienteId") Integer clienteId,
            @Header("Authorization") String token
    );

    @POST("/v1/medios-pago/tarjeta/{clienteId}")
    Call<ResponseBody> agregarTarjeta(
            @Path("clienteId") Integer clienteId,
            @Header("Authorization") String token,
            @Body AgregarTarjetaRequest request
    );

    @POST("/v1/medios-pago/cuenta/{clienteId}")
    Call<ResponseBody> agregarCuenta(
            @Path("clienteId") Integer clienteId,
            @Header("Authorization") String token,
            @Body AgregarCuentaRequest request
    );

    @POST("/v1/medios-pago/cheque/{clienteId}")
    Call<ResponseBody> agregarCheque(
            @Path("clienteId") Integer clienteId,
            @Header("Authorization") String token,
            @Body AgregarChequeRequest request
    );

    @DELETE("/v1/medios-pago/{id}")
    Call<ResponseBody> darDeBajaMedioPago(
            @Path("id") Integer id,
            @Header("Authorization") String token
    );

    @GET("/v1/consignaciones")
    Call<List<ConsignacionDTO>> obtenerConsignaciones(@Header("Authorization") String token);

    @GET("/v1/consignaciones/{id}")
    Call<ConsignacionDTO> obtenerDetalleConsignacion(
            @Header("Authorization") String token,
            @Path("id") Integer id
    );

    @Multipart
    @POST("/v1/consignaciones")
    Call<ConsignacionDTO> crearConsignacion(
            @Header("Authorization") String token,
            @Part("tipoBien") RequestBody tipoBien,
            @Part("descripcion") RequestBody descripcion,
            @Part("artista") RequestBody artista,
            @Part("fechaCreacion") RequestBody fechaCreacion,
            @Part("historia") RequestBody historia,
            @Part("declaraPropiedad") RequestBody declaraPropiedad,
            @Part List<MultipartBody.Part> fotos
    );

    @PATCH("/v1/consignaciones/{id}/respuesta")
    Call<ResponseBody> responderConsignacion(
            @Header("Authorization") String token,
            @Path("id") Integer id,
            @Body RespuestaConsignacionRequest request
    );

    @POST("/v1/consignaciones/{id}/cuenta-destino")
    Call<ResponseBody> registrarCuentaDestino(
            @Header("Authorization") String token,
            @Path("id") Integer id,
            @Body CuentaDestinoRequest request
    );

    @Multipart
    @POST("/v1/consignaciones/{id}/documentacion-origen")
    Call<ResponseBody> registrarDocumentacionOrigen(
            @Header("Authorization") String token,
            @Path("id") Integer id,
            @Part List<MultipartBody.Part> archivos,
            @Part("descripcion") RequestBody descripcion
    );

    @GET("/v1/clientes/me/subastas")
    Call<List<SubastaParticipacionDTO>> obtenerMisPujas(
            @Header("Authorization") String token,
            @Query("resultado") String resultado
    );

    @GET("/v1/clientes/me/subastas/{subastaId}/pujas")
    Call<HistorialPujasClienteDTO> obtenerDetalleMisPujas(
            @Header("Authorization") String token,
            @Path("subastaId") Integer subastaId
    );

    @GET("/v1/clientes/me/metricas")
    Call<MetricasClienteDTO> obtenerMisMetricas(
            @Header("Authorization") String token
    );

    @GET("/v1/clientes/me/compras/{compraId}")
    Call<CompraDTO> obtenerCompra(
            @Header("Authorization") String token,
            @Path("compraId") Integer compraId
    );

    @PATCH("/v1/clientes/me/compras/{compraId}/entrega")
    Call<ResponseBody> definirEntregaCompra(
            @Header("Authorization") String token,
            @Path("compraId") Integer compraId,
            @Body ModalidadEntregaRequest request
    );

    @POST("/v1/clientes/me/compras/{compraId}/pagar")
    Call<ResponseBody> pagarCompra(
            @Header("Authorization") String token,
            @Path("compraId") Integer compraId,
            @Body PagarCompraRequest request
    );

    @GET("/v1/clientes/me/multas")
    Call<List<MultaDTO>> obtenerMultas(
            @Header("Authorization") String token
    );

    @POST("/v1/clientes/me/multas/{id}/pagar")
    Call<ResponseBody> pagarMulta(
            @Header("Authorization") String token,
            @Path("id") Integer id,
            @Body PagarMultaRequest request
    );

    @GET("/v1/medios-pago")
    Call<List<MedioPagoDTO>> obtenerMediosPagoAutenticado(
            @Header("Authorization") String token
    );


    //NOTIFICACIONES
    // GET Notificaciones (Con filtro opcional)
    @GET("v1/clientes/me/notificaciones")
    Call<List<NotificacionDTO>> getNotificaciones(
            @Header("Authorization") String token,
            @Query("leidas") Boolean leidas
    );

    // PATCH Marcar todas como leídas
    @PATCH("v1/clientes/me/notificaciones/leer-todas")
    Call<java.util.Map<String, Object>> marcarTodasComoLeidas(
            @Header("Authorization") String token
    );

    // PATCH Marcar una como leída
    @PATCH("v1/clientes/me/notificaciones/{id}/leer")
    Call<java.util.Map<String, String>> marcarComoLeida(
            @Header("Authorization") String token,
            @Path("id") Integer id
    );
}

