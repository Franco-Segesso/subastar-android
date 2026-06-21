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
                .baseUrl(BuildConfig.BASE_URL)
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
        configurarBarraDeProgreso(metricas.getCategoriaActual(), metricas.getSubastaGanadas());
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

    private void configurarBarraDeProgreso(String categoriaActual, Integer subastasGanadas) {
        int compras = (subastasGanadas == null) ? 0 : subastasGanadas;
        String cat = (categoriaActual == null) ? "comun" : categoriaActual.toLowerCase(Locale.ROOT);

        int progreso = 0;
        String mensaje = "";

        switch (cat) {
            case "comun":
                progreso = 15; // Un mínimo visual para que la barra no se vea rota
                mensaje = "Agregá 1 medio de pago para subir a ESPECIAL";
                break;
            case "especial":
                // Necesita 2 compras para ser PLATA
                progreso = (int) ((compras / 2.0) * 100);
                int faltanPlata = 2 - compras;
                mensaje = faltanPlata > 0 ? "Te faltan " + faltanPlata + " compras para ser PLATA" : "¡Evaluando tu ascenso a PLATA!";
                break;
            case "plata":
                // Necesita 5 compras para ser ORO
                progreso = (int) ((compras / 5.0) * 100);
                int faltanOro = 5 - compras;
                mensaje = faltanOro > 0 ? "Te faltan " + faltanOro + " compras para ser ORO" : "¡Evaluando tu ascenso a ORO!";
                break;
            case "oro":
                // Necesita 10 compras para ser PLATINO
                progreso = (int) ((compras / 10.0) * 100);
                int faltanPlatino = 10 - compras;
                mensaje = faltanPlatino > 0 ? "Te faltan " + faltanPlatino + " compras para ser PLATINO" : "¡Evaluando tu ascenso a PLATINO!";
                break;
            case "platino":
                progreso = 100;
                mensaje = "¡Alcanzaste la categoría máxima del sistema!";
                break;
        }

        // Evitamos que la barra se pase de largo visualmente
        if (progreso > 100) progreso = 100;

        ((ProgressBar) findViewById(R.id.progresoCategoria)).setProgress(progreso);
        ((TextView) findViewById(R.id.tvMetricaProgreso)).setText(mensaje);
    }

    private int numero(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private void mostrarError(String mensaje) {
        error.setText(mensaje);
        error.setVisibility(View.VISIBLE);
    }
}
