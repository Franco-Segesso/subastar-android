package com.grupo6.subastar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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
    private TextView tvContadorNoLeidas, btnMarcarTodas, tvEmptyState;
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

        rvNotificaciones = findViewById(R.id.rvNotificaciones);
        tvContadorNoLeidas = findViewById(R.id.tvContadorNoLeidas);
        btnMarcarTodas = findViewById(R.id.btnMarcarTodas);
        toggleFiltro = findViewById(R.id.toggleFiltroNotif);
        btnVolver = findViewById(R.id.btnVolverNotif);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        String tokenGuardado = tokenManager.getToken();

        if (tokenGuardado != null) {
            token = "Bearer " + tokenGuardado;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(SubastarApi.class);

        rvNotificaciones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificacionAdapter(new ArrayList<>(), this, this::procesarClicNotificacion);
        rvNotificaciones.setAdapter(adapter);

        btnVolver.setOnClickListener(v -> finish());
        btnMarcarTodas.setOnClickListener(v -> marcarTodasComoLeidas());

        MaterialButton btnTodas = findViewById(R.id.btnFiltroTodas);
        btnTodas.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.secundario)));

        toggleFiltro.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            MaterialButton btn = findViewById(checkedId);
            if (isChecked) {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.secundario)));
                if (checkedId == R.id.btnFiltroTodas) {
                    cargarNotificaciones(null);
                } else if (checkedId == R.id.btnFiltroNoLeidas) {
                    cargarNotificaciones(false);
                }
            } else {
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
            }
        });

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

                    if (lista.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        // Validación segura para evitar el NullPointerException en Java
                        if (Boolean.FALSE.equals(soloNoLeidas)) {
                            tvEmptyState.setText("No tienes notificaciones sin ver.");
                        } else {
                            tvEmptyState.setText("No tienes notificaciones.");
                        }
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                    }
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
        if (lista == null) return;
        long noLeidas = 0;
        for (NotificacionDTO n : lista) {
            if (n.getLeido() != null && !n.getLeido()) {
                noLeidas++;
            }
        }
        tvContadorNoLeidas.setText(String.valueOf(noLeidas));
        tvContadorNoLeidas.setVisibility(noLeidas > 0 ? View.VISIBLE : View.GONE);
    }

    private void marcarTodasComoLeidas() {
        api.marcarTodasComoLeidas(token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    cargarNotificaciones(toggleFiltro.getCheckedButtonId() == R.id.btnFiltroNoLeidas ? false : null);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                mostrarDialogoError("Ocurrió un error de conexión al intentar actualizar.");
            }
        });
    }

    private void procesarClicNotificacion(NotificacionDTO notificacion) {
        if (notificacion.getLeido() != null && !notificacion.getLeido()) {
            notificacion.setLeido(true);
            adapter.notifyDataSetChanged();
            actualizarContador(adapter.getNotificaciones());

            api.marcarComoLeida(token, notificacion.getIdentificador()).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {}
                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {}
            });
        }

        if (notificacion.getTipo() == null) return;
        startActivity(NotificacionDestino.crearIntent(
                this,
                notificacion.getTipo(),
                notificacion.getReferenciaId()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (toggleFiltro != null) {
            boolean soloNoLeidas = toggleFiltro.getCheckedButtonId() == R.id.btnFiltroNoLeidas;
            cargarNotificaciones(soloNoLeidas ? false : null);
        }
    }

    private void mostrarDialogoExito(String mensaje) {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_exito);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        android.widget.TextView tvMensaje = dialog.findViewById(R.id.tvMensajeExito);
        tvMensaje.setText(mensaje);

        android.widget.Button btnAceptar = dialog.findViewById(R.id.btnAceptarExito);
        btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            // Refrescamos la lista DESPUÉS de que el usuario clickea "Aceptar"

        });

        dialog.show();
    }

    private void mostrarDialogoError(String mensaje) {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_error);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        android.widget.TextView tvMensaje = dialog.findViewById(R.id.tvMensajeError);
        tvMensaje.setText(mensaje);

        android.widget.Button btnEntendido = dialog.findViewById(R.id.btnEntendidoError);
        btnEntendido.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
