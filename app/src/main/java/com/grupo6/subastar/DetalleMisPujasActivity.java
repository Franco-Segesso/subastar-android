package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.grupo6.subastar.adapter.HistorialMisPujasAdapter;
import com.grupo6.subastar.dto.HistorialPujasClienteDTO;
import com.grupo6.subastar.model.ItemCatalogo;
import com.grupo6.subastar.model.Subasta;
import com.grupo6.subastar.util.FormatoPujas;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetalleMisPujasActivity extends AppCompatActivity {

    private SubastarApi api;
    private TokenManager tokenManager;
    private HistorialMisPujasAdapter adapter;
    private TextView titulo;
    private TextView subtitulo;
    private TextView resultado;
    private TextView vacio;
    private TextView itemGanado;
    private int subastaId;
    private int loteGanado;
    private int compraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_mis_pujas);

        subastaId = getIntent().getIntExtra("SUBASTA_ID", -1);
        loteGanado = getIntent().getIntExtra("LOTE_GANADO", -1);
        compraId = getIntent().getIntExtra("COMPRA_ID", -1);
        if (subastaId < 0) {
            finish();
            return;
        }

        tokenManager = new TokenManager(this);
        api = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubastarApi.class);

        titulo = findViewById(R.id.tvDetallePujasTitulo);
        subtitulo = findViewById(R.id.tvDetallePujasSubtitulo);
        resultado = findViewById(R.id.tvDetallePujasResultado);
        vacio = findViewById(R.id.tvDetallePujasVacio);
        itemGanado = findViewById(R.id.tvDetalleItemGanado);

        RecyclerView recycler = findViewById(R.id.recyclerDetalleMisPujas);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistorialMisPujasAdapter();
        recycler.setAdapter(adapter);

        findViewById(R.id.btnVolverDetallePujas).setOnClickListener(v -> finish());
        TextView btnFactura = findViewById(R.id.btnVerFacturaCompra);
        if (compraId > 0) {
            btnFactura.setVisibility(View.VISIBLE);
            String estadoPago = getIntent().getStringExtra("ESTADO_PAGO");
            btnFactura.setText("pagada".equalsIgnoreCase(estadoPago)
                    ? "VER FACTURA DE COMPRA"
                    : "PAGAR");
            btnFactura.setOnClickListener(v -> {
                Intent intent = new Intent(this, FacturaCompraActivity.class);
                intent.putExtra("COMPRA_ID", compraId);
                startActivity(intent);
            });
        }
        subtitulo.setText(FormatoPujas.fechaHoraSubasta(
                getIntent().getStringExtra("SUBASTA_FECHA"),
                getIntent().getStringExtra("SUBASTA_HORA")));

        cargarHistorial();
        if (loteGanado > 0) cargarDetalleSubasta();
    }

    private void cargarHistorial() {
        api.obtenerDetalleMisPujas(token(), subastaId)
                .enqueue(new Callback<HistorialPujasClienteDTO>() {
                    @Override
                    public void onResponse(
                            Call<HistorialPujasClienteDTO> call,
                            Response<HistorialPujasClienteDTO> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            mostrarError("No se pudo cargar el historial de esta subasta.");
                            return;
                        }
                        mostrarHistorial(response.body());
                    }

                    @Override
                    public void onFailure(
                            Call<HistorialPujasClienteDTO> call,
                            Throwable t) {
                        mostrarError("No se pudo conectar con el servidor.");
                    }
                });
    }

    private void mostrarHistorial(HistorialPujasClienteDTO detalle) {
        String moneda = detalle.getSubasta() == null
                ? "" : detalle.getSubasta().getMoneda();
        if (detalle.getSubasta() != null) {
            titulo.setText(detalle.getSubasta().getNombre());
        }

        HistorialPujasClienteDTO.ResumenDTO resumen = detalle.getResumen();
        boolean gano = resumen != null && Boolean.TRUE.equals(resumen.getGano());
        resultado.setText(gano ? "GANADA" : "PERDIDA");
        resultado.setBackgroundResource(
                gano ? R.drawable.bg_chip_activo : R.drawable.bg_chip_inactivo);
        resultado.setTextColor(ContextCompat.getColor(
                this, gano ? R.color.secundario : R.color.error));

        ((TextView) findViewById(R.id.tvDetalleTotalPujado)).setText(
                "TOTAL PUJADO\n" + FormatoPujas.moneda(
                        moneda, resumen == null ? 0.0 : resumen.getTotalPujado()));
        ((TextView) findViewById(R.id.tvDetalleTotalPagado)).setText(
                "TOTAL PAGADO\n" + FormatoPujas.moneda(
                        moneda, resumen == null ? 0.0 : resumen.getTotalPagado()));

        List<HistorialPujasClienteDTO.PujaDTO> pujas = detalle.getPujas();
        adapter.setItems(pujas, moneda);
        vacio.setVisibility(pujas == null || pujas.isEmpty()
                ? View.VISIBLE : View.GONE);
    }

    private void cargarDetalleSubasta() {
        api.obtenerDetalleSubasta(subastaId, token()).enqueue(new Callback<Subasta>() {
            @Override
            public void onResponse(Call<Subasta> call, Response<Subasta> response) {
                if (!response.isSuccessful() || response.body() == null
                        || response.body().getCatalogo() == null
                        || response.body().getCatalogo().getItems() == null) return;

                for (ItemCatalogo item : response.body().getCatalogo().getItems()) {
                    if (item.getId() != null && item.getId() == loteGanado) {
                        mostrarItemGanado(item);
                        return;
                    }
                }
            }

            @Override
            public void onFailure(Call<Subasta> call, Throwable t) {
                // El historial principal ya contiene los datos contractuales necesarios.
            }
        });
    }

    private void mostrarItemGanado(ItemCatalogo item) {
        String descripcion = "Ítem #" + item.getId();
        if (item.getProducto() != null && item.getProducto().getDescripcion() != null) {
            descripcion += " · " + item.getProducto().getDescripcion();
        }
        itemGanado.setText("ÍTEM GANADO\n" + descripcion);
        itemGanado.setVisibility(View.VISIBLE);
    }

    private void mostrarError(String mensaje) {
        vacio.setText(mensaje);
        vacio.setVisibility(View.VISIBLE);
    }

    private String token() {
        return "Bearer " + tokenManager.getToken();
    }
}
