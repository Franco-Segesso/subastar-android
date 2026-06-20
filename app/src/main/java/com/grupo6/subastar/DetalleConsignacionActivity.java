package com.grupo6.subastar;

import android.content.Intent;
import android.net.Uri;
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

import java.text.NumberFormat;
import java.util.Locale;

public class DetalleConsignacionActivity extends AppCompatActivity {

    private Integer consignacionId;
    private TokenManager tokenManager;
    private SubastarApi api;
    private TextView tvTitulo, tvCondiciones, tvUbicacionTitulo, tvUbicacion;
    private TextView tvSeguroTitulo, tvSeguro;
    private Button btnContactarAseguradora;
    private LinearLayout layoutTimeline, layoutCondiciones, layoutAcciones;

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
        tvSeguroTitulo = findViewById(R.id.tvSeguroTitulo);
        tvSeguro = findViewById(R.id.tvSeguroDetalle);
        btnContactarAseguradora = findViewById(R.id.btnContactarAseguradora);
        layoutTimeline = findViewById(R.id.layoutTimeline);
        layoutCondiciones = findViewById(R.id.layoutCondiciones);
        layoutAcciones = findViewById(R.id.layoutAccionesCondiciones);

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

        boolean condicionesPendientes = "aceptado".equalsIgnoreCase(c.getEstado())
                && c.getSeguro() != null
                && c.getCondicionesEmpresa() != null
                && c.getCondicionesEmpresa().getPrecioBase() != null
                && c.getCondicionesEmpresa().getComisionEmpresa() != null
                && c.getCondicionesEmpresa().getSubastaAsignada() != null
                && !Boolean.TRUE.equals(c.getCondicionesAceptadas());
        if (c.getCondicionesEmpresa() != null) {
            layoutCondiciones.setVisibility(View.VISIBLE);
            ConsignacionDTO.CondicionesEmpresaDTO cond = c.getCondicionesEmpresa();
            tvCondiciones.setText(
                    "Precio base\n" + importe(cond.getMoneda(), cond.getPrecioBase()) + "\n\n" +
                    "Comision empresa\n" + porcentaje(cond.getComisionEmpresa()) + "\n\n" +
                    "Poliza\n" + texto(cond.getSeguroPoliza()) + "\n\n" +
                    "Compania aseguradora\n" + texto(cond.getContactoPoliza()) + "\n\n" +
                    "Subasta asignada\n" + texto(cond.getSubastaAsignada()));
        }
        if (c.getUbicacionDeposito() != null) {
            tvUbicacionTitulo.setVisibility(View.VISIBLE);
            tvUbicacion.setVisibility(View.VISIBLE);
            tvUbicacion.setText(c.getUbicacionDeposito().getNombre() + "\n" + c.getUbicacionDeposito().getDireccion());
        }
        if (c.getSeguro() != null) {
            ConsignacionDTO.SeguroDTO seguro = c.getSeguro();
            tvSeguroTitulo.setVisibility(View.VISIBLE);
            tvSeguro.setVisibility(View.VISIBLE);
            tvSeguro.setText(
                    "Nro. de poliza\n" + texto(seguro.getNroPoliza()) + "\n\n" +
                    "Compania\n" + texto(seguro.getCompania()) + "\n\n" +
                    "Valor asegurado\n" + importe(seguro.getMoneda(), seguro.getImporte()) + "\n\n" +
                    "Cobertura combinada\n" +
                    ("si".equalsIgnoreCase(seguro.getPolizaCombinada()) ? "Si" : "No"));
            if (seguro.getCompania() != null && !seguro.getCompania().isBlank()) {
                btnContactarAseguradora.setVisibility(View.VISIBLE);
                btnContactarAseguradora.setOnClickListener(v ->
                        abrirContactoAseguradora(seguro.getCompania()));
            }
        }
        layoutAcciones.setVisibility(condicionesPendientes ? View.VISIBLE : View.GONE);
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

    private String porcentaje(Double valor) {
        return valor == null ? "A confirmar" : entero(valor) + "%";
    }

    private String importe(String moneda, Double valor) {
        if (valor == null) return "A confirmar";
        NumberFormat formato = NumberFormat.getNumberInstance(new Locale("es", "AR"));
        formato.setMaximumFractionDigits(2);
        formato.setMinimumFractionDigits(0);
        String prefijo = moneda == null || moneda.isBlank() ? "" : moneda + " ";
        return prefijo + formato.format(valor);
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "A confirmar" : valor;
    }

    private void abrirContactoAseguradora(String compania) {
        Uri busqueda = Uri.parse(
                "https://www.google.com/search?q="
                        + Uri.encode("contacto oficial aseguradora " + compania));
        Intent intent = new Intent(Intent.ACTION_VIEW, busqueda);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this,
                    "No se pudo abrir el contacto de la aseguradora.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
