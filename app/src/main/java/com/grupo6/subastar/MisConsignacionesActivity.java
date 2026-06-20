package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.grupo6.subastar.adapter.ConsignacionAdapter;
import com.grupo6.subastar.dto.ConsignacionDTO;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MisConsignacionesActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private SubastarApi api;
    private ConsignacionAdapter adapter;
    private List<ConsignacionDTO> todas = new ArrayList<>();
    private TextView tvActivasCount, tvVacio, chipTodas, chipActivas, chipSubasta, chipVendidas;
    private String filtro = "todas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_consignaciones);

        tokenManager = new TokenManager(this);
        api = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubastarApi.class);

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        tvActivasCount = findViewById(R.id.tvActivasCount);
        tvVacio = findViewById(R.id.tvMisConsignacionesVacio);
        chipTodas = findViewById(R.id.chipTodas);
        chipActivas = findViewById(R.id.chipActivas);
        chipSubasta = findViewById(R.id.chipSubasta);
        chipVendidas = findViewById(R.id.chipVendidas);
        RecyclerView recycler = findViewById(R.id.recyclerConsignaciones);

        btnVolver.setOnClickListener(v -> finish());
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConsignacionAdapter(item -> {
            Intent intent = new Intent(this, DetalleConsignacionActivity.class);
            intent.putExtra("CONSIGNACION_ID", item.getIdentificador());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        chipTodas.setOnClickListener(v -> aplicarFiltro("todas"));
        chipActivas.setOnClickListener(v -> aplicarFiltro("activas"));
        chipSubasta.setOnClickListener(v -> aplicarFiltro("subasta"));
        chipVendidas.setOnClickListener(v -> aplicarFiltro("vendidas"));
        actualizarChips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    private void cargar() {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Necesitas iniciar sesion.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        api.obtenerConsignaciones("Bearer " + token).enqueue(new Callback<List<ConsignacionDTO>>() {
            @Override
            public void onResponse(Call<List<ConsignacionDTO>> call, Response<List<ConsignacionDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    todas = response.body();
                    tvActivasCount.setText(contarActivas() + " activas");
                    aplicarFiltro(filtro);
                } else {
                    Toast.makeText(MisConsignacionesActivity.this, "No se pudieron cargar tus consignaciones.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<ConsignacionDTO>> call, Throwable t) {
                Toast.makeText(MisConsignacionesActivity.this, "Error de conexion.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void aplicarFiltro(String nuevoFiltro) {
        filtro = nuevoFiltro;
        List<ConsignacionDTO> filtradas = new ArrayList<>();
        for (ConsignacionDTO c : todas) {
            if (coincide(c)) filtradas.add(c);
        }
        adapter.setItems(filtradas);
        tvVacio.setVisibility(filtradas.isEmpty() ? View.VISIBLE : View.GONE);
        actualizarChips();
    }

    private boolean coincide(ConsignacionDTO c) {
        String estado = c.getEstado() == null ? "" : c.getEstado().toLowerCase();
        if ("activas".equals(filtro)) return esActiva(estado);
        if ("subasta".equals(filtro)) return "aceptado".equals(estado) && Boolean.TRUE.equals(c.getCondicionesAceptadas());
        if ("vendidas".equals(filtro)) return false;
        return true;
    }

    private int contarActivas() {
        int total = 0;
        for (ConsignacionDTO c : todas) {
            String estado = c.getEstado() == null ? "" : c.getEstado().toLowerCase();
            if (esActiva(estado)) total++;
        }
        return total;
    }

    private boolean esActiva(String estado) {
        return "pendiente".equals(estado)
                || "documentacion_pendiente".equals(estado)
                || "documentacion_presentada".equals(estado)
                || "aceptado".equals(estado);
    }

    private void actualizarChips() {
        pintarChip(chipTodas, "todas".equals(filtro));
        pintarChip(chipActivas, "activas".equals(filtro));
        pintarChip(chipSubasta, "subasta".equals(filtro));
        pintarChip(chipVendidas, "vendidas".equals(filtro));
    }

    private void pintarChip(TextView chip, boolean seleccionado) {
        chip.setBackground(ContextCompat.getDrawable(this, seleccionado ? R.drawable.bg_chip_activo : R.drawable.bg_chip_inactivo));
        chip.setTextColor(ContextCompat.getColor(this, seleccionado ? R.color.secundario : R.color.texto_ppal));
    }
}
