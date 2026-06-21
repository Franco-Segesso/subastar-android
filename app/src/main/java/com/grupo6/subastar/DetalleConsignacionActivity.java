package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.grupo6.subastar.dto.ConsignacionDTO;
import com.grupo6.subastar.dto.RespuestaConsignacionRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetalleConsignacionActivity extends AppCompatActivity {

    private Integer consignacionId;
    private TokenManager tokenManager;
    private SubastarApi api;
    private TextView tvTitulo, tvCondiciones, tvUbicacionTitulo, tvUbicacion, tvImporteFinal;
    private LinearLayout layoutTimeline, layoutCondiciones, layoutAcciones, layoutVendida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_consignacion);

        consignacionId = getIntent().getIntExtra("CONSIGNACION_ID", -1);
        tokenManager = new TokenManager(this);
        api = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubastarApi.class);

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        Button btnAceptar = findViewById(R.id.btnAceptarCondiciones);
        Button btnRechazar = findViewById(R.id.btnRechazarCondiciones);
        tvTitulo = findViewById(R.id.tvDetalleTituloBien);
        tvCondiciones = findViewById(R.id.tvCondicionesDetalle);
        tvUbicacionTitulo = findViewById(R.id.tvUbicacionTitulo);
        tvUbicacion = findViewById(R.id.tvUbicacionDetalle);
        tvImporteFinal = findViewById(R.id.tvImporteFinalObtenido);
        layoutTimeline = findViewById(R.id.layoutTimeline);
        layoutCondiciones = findViewById(R.id.layoutCondiciones);
        layoutAcciones = findViewById(R.id.layoutAccionesCondiciones);
        layoutVendida = findViewById(R.id.layoutVendida);

        btnVolver.setOnClickListener(v -> finish());
        btnAceptar.setOnClickListener(v -> responder(true));
        btnRechazar.setOnClickListener(v -> responder(false));

        cargarDetalle();
    }

    private void cargarDetalle() {
        String token = tokenManager.getToken();
        if (token == null || consignacionId == null || consignacionId <= 0) {
            finish();
            return;
        }
        api.obtenerDetalleConsignacion("Bearer " + token, consignacionId).enqueue(new Callback<ConsignacionDTO>() {
            @Override
            public void onResponse(Call<ConsignacionDTO> call, Response<ConsignacionDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pintar(response.body());
                } else {
                    Toast.makeText(DetalleConsignacionActivity.this, "No se pudo cargar el detalle.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ConsignacionDTO> call, Throwable t) {
                Toast.makeText(DetalleConsignacionActivity.this, "Error de conexion.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void pintar(ConsignacionDTO c) {
        String nombre = c.getProducto() != null ? c.getProducto().getDescripcion() : "Bien consignado";
        tvTitulo.setText(nombre);
        layoutTimeline.removeAllViews();
        if (c.getInstancias() != null) {
            for (ConsignacionDTO.InstanciaDTO instancia : c.getInstancias()) {
                TextView row = new TextView(this);
                String marca = Boolean.TRUE.equals(instancia.getCompletada()) ? "✓  " : "○  ";
                row.setText(marca + instancia.getTitulo() + "\n" + instancia.getFecha());
                row.setTextColor(getResources().getColor(R.color.texto_ppal));
                row.setTextSize(15);
                row.setPadding(12, 10, 12, 10);
                layoutTimeline.addView(row);
            }
        }

        boolean esVendida = "vendida".equalsIgnoreCase(c.getEstado());
        boolean condicionesPendientes = "aceptado".equalsIgnoreCase(c.getEstado()) && !Boolean.TRUE.equals(c.getCondicionesAceptadas());
        if (c.getCondicionesEmpresa() != null) {
            layoutCondiciones.setVisibility(View.VISIBLE);
            ConsignacionDTO.CondicionesEmpresaDTO cond = c.getCondicionesEmpresa();
            tvCondiciones.setText(
                    "Precio base                                      USD " + entero(cond.getPrecioBase()) + "\n" +
                    "Comision empresa                         " + entero(cond.getComisionEmpresa()) + "%\n" +
                    "Seguro poliza                              " + cond.getSeguroPoliza() + "\n" +
                    "Contacto poliza                            " + cond.getContactoPoliza() + "\n" +
                    "Subasta asignada                         " + cond.getSubastaAsignada());
        }
        if (c.getUbicacionDeposito() != null) {
            tvUbicacionTitulo.setVisibility(View.VISIBLE);
            tvUbicacion.setVisibility(View.VISIBLE);
            tvUbicacion.setText(c.getUbicacionDeposito().getNombre() + "\n" + c.getUbicacionDeposito().getDireccion());
        }
        layoutAcciones.setVisibility(condicionesPendientes ? View.VISIBLE : View.GONE);

        if (esVendida && layoutVendida != null) {
            layoutVendida.setVisibility(View.VISIBLE);
            if (tvImporteFinal != null) {
                if (c.getImporteFinalObtenido() != null) {
                    tvImporteFinal.setText(String.format(java.util.Locale.US,
                            "Importe recibido: $ %.2f\n(precio venta menos 10%% de comisión de la empresa)",
                            c.getImporteFinalObtenido()));
                } else {
                    tvImporteFinal.setText("El pago del comprador está pendiente.");
                }
            }
        } else if (layoutVendida != null) {
            layoutVendida.setVisibility(View.GONE);
        }
    }

    private void responder(boolean acepta) {
        String token = "Bearer " + tokenManager.getToken();
        api.responderConsignacion(token, consignacionId, new RespuestaConsignacionRequest(acepta)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (acepta) {
                        Intent intent = new Intent(DetalleConsignacionActivity.this, CuentaDestinoConsignacionActivity.class);
                        intent.putExtra("CONSIGNACION_ID", consignacionId);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(DetalleConsignacionActivity.this, ResultadoConsignacionActivity.class);
                        intent.putExtra("RESULTADO", "rechazada");
                        startActivity(intent);
                    }
                    finish();
                } else {
                    Toast.makeText(DetalleConsignacionActivity.this, "No se pudo registrar la respuesta.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(DetalleConsignacionActivity.this, "Error de conexion.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String entero(Double valor) {
        if (valor == null) return "-";
        return String.valueOf(valor.intValue());
    }
}
