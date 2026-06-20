package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grupo6.subastar.adapter.MultaAdapter;
import com.grupo6.subastar.dto.MultaDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MultasActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable actualizarContadores = new Runnable() {
        @Override
        public void run() {
            adapterPendientes.notifyDataSetChanged();
            handler.postDelayed(this, 1000);
        }
    };

    private MultaAdapter adapterPendientes;
    private MultaAdapter adapterHistorial;
    private TextView vacioPendientes;
    private TextView vacioHistorial;
    private SubastarApi api;
    private TokenManager tokenManager;
    private int multaDestacada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multas);

        tokenManager = new TokenManager(this);
        api = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubastarApi.class);
        multaDestacada = getIntent().getIntExtra("MULTA_ID", -1);

        findViewById(R.id.btnVolverMultas).setOnClickListener(v -> finish());
        vacioPendientes = findViewById(R.id.tvMultasPendientesVacio);
        vacioHistorial = findViewById(R.id.tvMultasHistorialVacio);

        RecyclerView pendientes = findViewById(R.id.recyclerMultasPendientes);
        RecyclerView historial = findViewById(R.id.recyclerMultasHistorial);
        pendientes.setLayoutManager(new LinearLayoutManager(this));
        historial.setLayoutManager(new LinearLayoutManager(this));
        pendientes.setNestedScrollingEnabled(false);
        historial.setNestedScrollingEnabled(false);

        adapterPendientes = new MultaAdapter(true, new MultaAdapter.OnAccionClick() {
            @Override
            public void onPagarMulta(MultaDTO multa) {
                abrirPagoMulta(multa);
            }

            @Override
            public void onPagarCompra(MultaDTO multa) {
                abrirPagoCompra(multa);
            }
        });
        adapterHistorial = new MultaAdapter(false, new MultaAdapter.OnAccionClick() {
            @Override
            public void onPagarMulta(MultaDTO multa) {
            }

            @Override
            public void onPagarCompra(MultaDTO multa) {
            }
        });
        pendientes.setAdapter(adapterPendientes);
        historial.setAdapter(adapterHistorial);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMultas();
        handler.post(actualizarContadores);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(actualizarContadores);
        super.onPause();
    }

    private void cargarMultas() {
        api.obtenerMultas("Bearer " + tokenManager.getToken())
                .enqueue(new Callback<List<MultaDTO>>() {
                    @Override
                    public void onResponse(
                            Call<List<MultaDTO>> call,
                            Response<List<MultaDTO>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(MultasActivity.this,
                                    "No se pudieron cargar las multas.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        List<MultaDTO> pendientes = new ArrayList<>();
                        List<MultaDTO> historial = new ArrayList<>();
                        for (MultaDTO multa : response.body()) {
                            boolean obligacionPendiente =
                                    "pendiente".equalsIgnoreCase(multa.getEstado())
                                    || "pendiente".equalsIgnoreCase(
                                    multa.getEstadoCompra());
                            if (obligacionPendiente) {
                                if (multa.getIdentificador() != null
                                        && multa.getIdentificador() == multaDestacada) {
                                    pendientes.add(0, multa);
                                } else {
                                    pendientes.add(multa);
                                }
                            } else {
                                historial.add(multa);
                            }
                        }
                        adapterPendientes.setItems(pendientes);
                        adapterHistorial.setItems(historial);
                        vacioPendientes.setVisibility(
                                pendientes.isEmpty() ? View.VISIBLE : View.GONE);
                        vacioHistorial.setVisibility(
                                historial.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onFailure(Call<List<MultaDTO>> call, Throwable t) {
                        Toast.makeText(MultasActivity.this,
                                "Error de conexion.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void abrirPagoMulta(MultaDTO multa) {
        Intent intent = new Intent(this, PagarMultaActivity.class);
        intent.putExtra("MULTA_ID", multa.getIdentificador());
        intent.putExtra("MULTA_IMPORTE", multa.getImporte());
        intent.putExtra("MULTA_SUBASTA", multa.getSubasta());
        startActivity(intent);
    }

    private void abrirPagoCompra(MultaDTO multa) {
        if (multa.getCompraId() == null) return;
        Intent intent = new Intent(this, FacturaCompraActivity.class);
        intent.putExtra("COMPRA_ID", multa.getCompraId());
        startActivity(intent);
    }
}
