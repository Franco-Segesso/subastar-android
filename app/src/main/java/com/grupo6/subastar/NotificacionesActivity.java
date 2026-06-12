package com.grupo6.subastar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.grupo6.subastar.adapter.NotificacionAdapter;
import com.grupo6.subastar.dto.NotificacionDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificacionesActivity extends AppCompatActivity {

    private RecyclerView rvNotificaciones;
    private NotificacionAdapter adapter;
    private TextView tvContadorNoLeidas, btnMarcarTodas;
    private MaterialButtonToggleGroup toggleFiltro;
    private ImageView btnVolver;
    private SubastarApi api;
    private String token;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);

        tokenManager = new TokenManager(this);

        // Inicializar vistas
        rvNotificaciones = findViewById(R.id.rvNotificaciones);
        tvContadorNoLeidas = findViewById(R.id.tvContadorNoLeidas);
        btnMarcarTodas = findViewById(R.id.btnMarcarTodas);
        toggleFiltro = findViewById(R.id.toggleFiltroNotif);
        btnVolver = findViewById(R.id.btnVolverNotif);

        // Configurar Retrofit y Token
        token = "Bearer " + tokenManager.getToken();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SubastarApi.class);

        // Configurar RecyclerView
        rvNotificaciones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificacionAdapter(new ArrayList<>(), this, this::procesarClicNotificacion);
        rvNotificaciones.setAdapter(adapter);

        // Eventos
        btnVolver.setOnClickListener(v -> finish());

        btnMarcarTodas.setOnClickListener(v -> marcarTodasComoLeidas());

        toggleFiltro.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnFiltroTodas) {
                    cargarNotificaciones(null); // Trae todas
                } else if (checkedId == R.id.btnFiltroNoLeidas) {
                    cargarNotificaciones(false); // Trae solo las no leídas
                }
            }
        });

        // Carga inicial
        cargarNotificaciones(null);
    }

    private void cargarNotificaciones(Boolean soloNoLeidas) {
        api.getNotificaciones(token, soloNoLeidas).enqueue(new Callback<List<NotificacionDTO>>() {
            @Override
            public void onResponse(Call<List<NotificacionDTO>> call, Response<List<NotificacionDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NotificacionDTO> lista = response.body();
                    adapter.setNotificaciones(lista);
                    actualizarContador(lista);
                } else {
                    mostrarDialogoError("Error al cargar las notificaciones. Intente más tarde.");
                }
            }

            @Override
            public void onFailure(Call<List<NotificacionDTO>> call, Throwable t) {
                mostrarDialogoError("Fallo de conexión. Revise su acceso a internet e intente nuevamente.");
            }
        });
    }

    private void actualizarContador(List<NotificacionDTO> lista) {
        long noLeidas = lista.stream().filter(n -> !n.getLeido()).count();
        tvContadorNoLeidas.setText(String.valueOf(noLeidas));
        tvContadorNoLeidas.setVisibility(noLeidas > 0 ? View.VISIBLE : View.GONE);
    }

    private void marcarTodasComoLeidas() {
        api.marcarTodasComoLeidas(token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    cargarNotificaciones(toggleFiltro.getCheckedButtonId() == R.id.btnFiltroNoLeidas ? false : null);
                    mostrarDialogoExito("Todas las notificaciones fueron marcadas como leídas exitosamente.");
                } else {
                    mostrarDialogoError("No se pudieron actualizar las notificaciones.");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                mostrarDialogoError("Ocurrió un error de conexión al intentar actualizar.");
            }
        });
    }

    private void procesarClicNotificacion(NotificacionDTO notificacion) {
        if (!notificacion.getLeido()) {
            api.marcarComoLeida(token, notificacion.getIdentificador()).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {}
                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {}
            });
        }

        Intent intent;
        switch (notificacion.getTipo()) {
            case "GANADA":
                intent = new Intent(this, PasarelaPagoActivity.class);
                intent.putExtra("ITEM_ID", notificacion.getReferenciaId());
                startActivity(intent);
                break;

            case "MULTA":
                intent = new Intent(this, MultasActivity.class);
                intent.putExtra("MULTA_ID", notificacion.getReferenciaId());
                startActivity(intent);
                break;

            case "CONSIGNACION":
                intent = new Intent(this, MisConsignacionesActivity.class);
                startActivity(intent);
                break;

            default:
                cargarNotificaciones(toggleFiltro.getCheckedButtonId() == R.id.btnFiltroNoLeidas ? false : null);
                break;
        }
    }

    // --- MÉTODOS PARA MOSTRAR LOS DIÁLOGOS PERSONALIZADOS ---

    private void mostrarDialogoExito(String mensaje) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_exito);
        // Hacemos el fondo transparente para que se vean las esquinas redondeadas del XML
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Buscamos los elementos dentro de dialog_exito.xml
        // NOTA: Ajustá los IDs (R.id.tvMensajeExito, R.id.btnAceptarExito) a los que realmente tengas en tu XML
        TextView tvMensaje = dialog.findViewById(R.id.tvMensajeExito);
        if (tvMensaje != null) {
            tvMensaje.setText(mensaje);
        }

        View btnAceptar = dialog.findViewById(R.id.btnAceptarExito);
        if (btnAceptar != null) {
            btnAceptar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void mostrarDialogoError(String mensaje) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_error);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Buscamos los elementos dentro de dialog_error.xml
        // NOTA: Ajustá los IDs a los que realmente tengas en tu XML
        TextView tvMensaje = dialog.findViewById(R.id.tvMensajeError);
        if (tvMensaje != null) {
            tvMensaje.setText(mensaje);
        }

        View btnCerrar = dialog.findViewById(R.id.btnEntendidoError);
        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
}