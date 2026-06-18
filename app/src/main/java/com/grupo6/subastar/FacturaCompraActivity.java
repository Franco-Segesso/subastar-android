package com.grupo6.subastar;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.grupo6.subastar.dto.CompraDTO;
import com.grupo6.subastar.dto.ModalidadEntregaRequest;
import com.grupo6.subastar.util.FormatoPujas;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FacturaCompraActivity extends AppCompatActivity {

    private SubastarApi api;
    private TokenManager tokenManager;
    private int compraId;
    private TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factura_compra);

        compraId = getIntent().getIntExtra("COMPRA_ID", -1);
        if (compraId < 0) {
            finish();
            return;
        }

        tokenManager = new TokenManager(this);
        api = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubastarApi.class);
        error = findViewById(R.id.tvFacturaError);

        findViewById(R.id.btnVolverFactura).setOnClickListener(v -> finish());
        findViewById(R.id.btnElegirEnvio).setOnClickListener(v ->
                confirmarModalidad("envio"));
        findViewById(R.id.btnElegirRetiro).setOnClickListener(v ->
                confirmarModalidad("retiro"));

        cargarCompra();
    }

    private void cargarCompra() {
        api.obtenerCompra(token(), compraId).enqueue(new Callback<CompraDTO>() {
            @Override
            public void onResponse(Call<CompraDTO> call, Response<CompraDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    error.setVisibility(View.GONE);
                    mostrarCompra(response.body());
                } else {
                    mostrarError(response.code() == 404
                            ? "La compra todavía no está disponible."
                            : "No se pudo cargar la factura.");
                }
            }

            @Override
            public void onFailure(Call<CompraDTO> call, Throwable t) {
                mostrarError("No se pudo conectar con el servidor.");
            }
        });
    }

    private void mostrarCompra(CompraDTO compra) {
        String moneda = compra.getSubasta() == null
                ? "" : compra.getSubasta().getMoneda();
        String nombreSubasta = compra.getSubasta() == null
                ? "Subasta" : compra.getSubasta().getNombre();
        String item = "Ítem";
        if (compra.getItem() != null) {
            item = "Ítem #" + compra.getItem().getNumeroPieza()
                    + " · " + compra.getItem().getDescripcionCatalogo();
        }

        ((TextView) findViewById(R.id.tvFacturaSubasta)).setText(nombreSubasta);
        ((TextView) findViewById(R.id.tvFacturaItem)).setText(item);
        ((TextView) findViewById(R.id.tvFacturaImporte)).setText(
                "Importe pujado\n" + FormatoPujas.moneda(
                        moneda, compra.getImportePujado()));
        ((TextView) findViewById(R.id.tvFacturaComision)).setText(
                "Comisión\n" + FormatoPujas.moneda(
                        moneda, compra.getComision()));
        ((TextView) findViewById(R.id.tvFacturaEnvio)).setText(
                compra.getCostoEnvio() == null
                        ? "Costo de envío\nPendiente de cotización"
                        : "Costo de envío\n" + FormatoPujas.moneda(
                        moneda, compra.getCostoEnvio()));
        ((TextView) findViewById(R.id.tvFacturaTotal)).setText(
                "TOTAL\n" + FormatoPujas.moneda(moneda, compra.getTotal()));

        String modalidad = compra.getModalidadEntrega() == null
                ? "pendiente" : compra.getModalidadEntrega();
        ((TextView) findViewById(R.id.tvFacturaModalidad)).setText(
                "Modalidad de entrega: " + modalidad.toUpperCase());
        ((LinearLayout) findViewById(R.id.contenedorModalidad)).setVisibility(
                "pendiente".equalsIgnoreCase(modalidad) ? View.VISIBLE : View.GONE);

        TextView direccion = findViewById(R.id.tvFacturaDireccion);
        if (compra.getDireccionEnvio() == null || compra.getDireccionEnvio().isBlank()) {
            direccion.setVisibility(View.GONE);
        } else {
            direccion.setText("Dirección de envío: " + compra.getDireccionEnvio());
            direccion.setVisibility(View.VISIBLE);
        }
        ((TextView) findViewById(R.id.tvFacturaSeguro)).setText(
                compra.getAvisoSeguro() == null ? "" : compra.getAvisoSeguro());
    }

    private void confirmarModalidad(String modalidad) {
        boolean retiro = "retiro".equals(modalidad);
        String mensaje = retiro
                ? "Al retirar el bien personalmente, la cobertura del seguro finalizará al entregarlo. Esta elección no se puede cambiar."
                : "El costo del envío estará a tu cargo y se incorporará al total cuando sea cotizado. Esta elección no se puede cambiar.";

        new AlertDialog.Builder(this)
                .setTitle(retiro ? "Confirmar retiro" : "Confirmar envío")
                .setMessage(mensaje)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) ->
                        guardarModalidad(modalidad))
                .show();
    }

    private void guardarModalidad(String modalidad) {
        api.definirEntregaCompra(
                token(),
                compraId,
                new ModalidadEntregaRequest(modalidad))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            cargarCompra();
                        } else {
                            mostrarError(response.code() == 409
                                    ? "La modalidad de entrega ya fue definida."
                                    : "No se pudo registrar la modalidad.");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        mostrarError("No se pudo conectar con el servidor.");
                    }
                });
    }

    private void mostrarError(String mensaje) {
        error.setText(mensaje);
        error.setVisibility(View.VISIBLE);
    }

    private String token() {
        return "Bearer " + tokenManager.getToken();
    }
}
