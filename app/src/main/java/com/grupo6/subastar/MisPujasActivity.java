package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.grupo6.subastar.adapter.MisPujasAdapter;
import com.grupo6.subastar.dto.MetricasClienteDTO;
import com.grupo6.subastar.dto.SubastaParticipacionDTO;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MisPujasActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private SubastarApi api;
    private MisPujasAdapter adapter;
    private TextView vacio;
    private TextView chipTodas;
    private TextView chipGanadas;
    private TextView chipPerdidas;
    private String filtroActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_pujas);

        tokenManager = new TokenManager(this);
        if (tokenManager.getToken() == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        api = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubastarApi.class);

        vacio = findViewById(R.id.tvMisPujasVacio);
        chipTodas = findViewById(R.id.chipPujasTodas);
        chipGanadas = findViewById(R.id.chipPujasGanadas);
        chipPerdidas = findViewById(R.id.chipPujasPerdidas);
        RecyclerView recycler = findViewById(R.id.recyclerMisPujas);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MisPujasAdapter(this::abrirDetalle);
        recycler.setAdapter(adapter);

        findViewById(R.id.btnVolverMisPujas).setOnClickListener(v -> finish());
        findViewById(R.id.btnMisMetricas).setOnClickListener(v ->
                startActivity(new Intent(this, MisMetricasActivity.class)));
        findViewById(R.id.navPujasSubastas).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
        findViewById(R.id.navPujasConsignacion).setOnClickListener(v ->
                startActivity(new Intent(this, ConsignarBienActivity.class)));

        chipTodas.setOnClickListener(v -> cambiarFiltro(null));
        chipGanadas.setOnClickListener(v -> cambiarFiltro("ganada"));
        chipPerdidas.setOnClickListener(v -> cambiarFiltro("perdida"));
        actualizarChips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarListado();
        cargarMetricas();
    }

    private void cambiarFiltro(String filtro) {
        filtroActual = filtro;
        actualizarChips();
        cargarListado();
    }

    private void cargarListado() {
        api.obtenerMisPujas(token(), filtroActual)
                .enqueue(new Callback<List<SubastaParticipacionDTO>>() {
                    @Override
                    public void onResponse(
                            Call<List<SubastaParticipacionDTO>> call,
                            Response<List<SubastaParticipacionDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setItems(response.body());
                            vacio.setText(mensajeVacio());
                            vacio.setVisibility(
                                    response.body().isEmpty() ? View.VISIBLE : View.GONE);
                        } else {
                            mostrarErrorListado(response.code() == 401
                                    ? "Tu sesión venció. Volvé a iniciar sesión."
                                    : "No se pudieron cargar tus pujas.");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<SubastaParticipacionDTO>> call,
                            Throwable t) {
                        mostrarErrorListado("No se pudo conectar con el servidor.");
                    }
                });
    }

    private void cargarMetricas() {
        api.obtenerMisMetricas(token()).enqueue(new Callback<MetricasClienteDTO>() {
            @Override
            public void onResponse(
                    Call<MetricasClienteDTO> call,
                    Response<MetricasClienteDTO> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                MetricasClienteDTO metricas = response.body();
                ((TextView) findViewById(R.id.tvTotalSubastas)).setText(
                        numero(metricas.getTotalSubastas()) + "\nSUBASTAS");
                ((TextView) findViewById(R.id.tvSubastasGanadas)).setText(
                        numero(metricas.getSubastaGanadas()) + "\nGANADAS");
                int porcentaje = (int) Math.round(
                        metricas.getPorcentajeExito() == null
                                ? 0.0 : metricas.getPorcentajeExito());
                ((TextView) findViewById(R.id.tvPorcentajeExito)).setText(
                        porcentaje + "%\nÉXITO");
            }

            @Override
            public void onFailure(Call<MetricasClienteDTO> call, Throwable t) {
                // El listado sigue siendo util aunque el resumen no esté disponible.
            }
        });
    }

    private void abrirDetalle(SubastaParticipacionDTO item) {
        if (item.getSubasta() == null) return;
        Intent intent = new Intent(this, DetalleMisPujasActivity.class);
        intent.putExtra("SUBASTA_ID", item.getSubasta().getIdentificador());
        intent.putExtra("SUBASTA_FECHA", item.getSubasta().getFecha());
        intent.putExtra("SUBASTA_HORA", item.getSubasta().getHora());
        intent.putExtra("SUBASTA_CATEGORIA", item.getSubasta().getCategoria());
        if (item.getLoteGanado() != null) {
            intent.putExtra("LOTE_GANADO", item.getLoteGanado());
        }
        if (item.getCompraId() != null) {
            intent.putExtra("COMPRA_ID", item.getCompraId());
        }
        startActivity(intent);
    }

    private void actualizarChips() {
        pintarChip(chipTodas, filtroActual == null);
        pintarChip(chipGanadas, "ganada".equals(filtroActual));
        pintarChip(chipPerdidas, "perdida".equals(filtroActual));
    }

    private void pintarChip(TextView chip, boolean seleccionado) {
        chip.setBackgroundResource(
                seleccionado ? R.drawable.bg_chip_activo : R.drawable.bg_chip_inactivo);
        chip.setTextColor(ContextCompat.getColor(
                this, seleccionado ? R.color.secundario : R.color.texto_sec));
        chip.setTypeface(null, seleccionado
                ? android.graphics.Typeface.BOLD
                : android.graphics.Typeface.NORMAL);
    }

    private void mostrarErrorListado(String mensaje) {
        adapter.setItems(null);
        vacio.setText(mensaje);
        vacio.setVisibility(View.VISIBLE);
    }

    private String mensajeVacio() {
        if ("ganada".equals(filtroActual)) return "Todavía no ganaste ninguna subasta.";
        if ("perdida".equals(filtroActual)) return "No tenés subastas perdidas.";
        return "Todavía no participaste en subastas.";
    }

    private String token() {
        return "Bearer " + tokenManager.getToken();
    }

    private int numero(Integer valor) {
        return valor == null ? 0 : valor;
    }
}
