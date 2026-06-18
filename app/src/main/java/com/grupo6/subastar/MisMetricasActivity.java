package com.grupo6.subastar;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.grupo6.subastar.dto.MetricasClienteDTO;
import com.grupo6.subastar.util.FormatoPujas;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MisMetricasActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private SubastarApi api;
    private TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_metricas);

        tokenManager = new TokenManager(this);
        api = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubastarApi.class);
        error = findViewById(R.id.tvMetricasError);

        findViewById(R.id.btnVolverMetricas).setOnClickListener(v -> finish());
        cargarMetricas();
    }

    private void cargarMetricas() {
        api.obtenerMisMetricas("Bearer " + tokenManager.getToken())
                .enqueue(new Callback<MetricasClienteDTO>() {
                    @Override
                    public void onResponse(
                            Call<MetricasClienteDTO> call,
                            Response<MetricasClienteDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            error.setVisibility(View.GONE);
                            mostrarMetricas(response.body());
                        } else {
                            mostrarError("No se pudieron cargar tus métricas.");
                        }
                    }

                    @Override
                    public void onFailure(Call<MetricasClienteDTO> call, Throwable t) {
                        mostrarError("No se pudo conectar con el servidor.");
                    }
                });
    }

    private void mostrarMetricas(MetricasClienteDTO metricas) {
        ((TextView) findViewById(R.id.tvMetricaTotalPujado)).setText(
                "TOTAL PUJADO\n" + FormatoPujas.moneda("", metricas.getTotalPujado()));
        ((TextView) findViewById(R.id.tvMetricaTotalPagado)).setText(
                "TOTAL PAGADO\n" + FormatoPujas.moneda("", metricas.getTotalPagado()));
        ((TextView) findViewById(R.id.tvMetricaPujasRealizadas)).setText(
                "PUJAS REALIZADAS\n" + numero(metricas.getTotalPujas()));
        ((TextView) findViewById(R.id.tvMetricaCategoriaMasPujada)).setText(
                "CATEGORÍA MÁS PUJADA\n" + categoriaMasPujada(
                        metricas.getActividadPorCategoria()));
        ((TextView) findViewById(R.id.tvMetricaPujaMaxima)).setText(
                "PUJA MÁS ALTA\n" + FormatoPujas.moneda("", metricas.getPujaMaxima()));
        ((TextView) findViewById(R.id.tvMetricaConsignaciones)).setText(
                "CONSIGNACIONES\n" + numero(metricas.getConsignaciones()));

        String categoria = metricas.getCategoriaActual() == null
                ? "COMÚN" : metricas.getCategoriaActual().toUpperCase(Locale.ROOT);
        ((TextView) findViewById(R.id.tvMetricaCategoriaActual)).setText(
                "Categoría actual\n" + categoria);
        int progreso = progresoCategoria(metricas.getCategoriaActual());
        ((ProgressBar) findViewById(R.id.progresoCategoria)).setProgress(progreso);
        ((TextView) findViewById(R.id.tvMetricaProgreso)).setText(
                progreso + "% del recorrido de categorías completado");
    }

    private String categoriaMasPujada(
            List<MetricasClienteDTO.ActividadCategoriaDTO> actividad) {
        if (actividad == null || actividad.isEmpty()) return "SIN ACTIVIDAD";
        return actividad.stream()
                .max(Comparator.comparingInt(item ->
                        numero(item.getCantidadPujas())))
                .map(item -> item.getCategoria().toUpperCase(Locale.ROOT))
                .orElse("SIN ACTIVIDAD");
    }

    private int progresoCategoria(String categoria) {
        if (categoria == null) return 20;
        switch (categoria.toLowerCase(Locale.ROOT)) {
            case "platino": return 100;
            case "oro": return 80;
            case "plata": return 60;
            case "especial": return 40;
            default: return 20;
        }
    }

    private int numero(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private void mostrarError(String mensaje) {
        error.setText(mensaje);
        error.setVisibility(View.VISIBLE);
    }
}
