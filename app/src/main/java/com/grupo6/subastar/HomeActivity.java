package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grupo6.subastar.adapter.SubastaAdapter;
import com.grupo6.subastar.dto.NotificacionDTO;
import com.grupo6.subastar.model.Subasta;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SubastaAdapter adapter;
    private List<TextView> listaChips = new ArrayList<>();
    private LocalDate fechaVisualizada = LocalDate.now();
    private TextView tvFechaActual;
    private ImageButton btnDiaAnterior, btnDiaSiguiente;
    private TokenManager tokenManager;
    private TextView tvMensajeVacio;
    private TextView tvProximas;
    private StompClient stompClient;
    private CompositeDisposable compositeDisposable;
    private EditText etBuscador;
    private List<Subasta> subastasDelDia = new ArrayList<>();

    // Componente visual para el punto rojo
    private View badgeNotificacion;

    // Variables para mantener los filtros activos
    private String estadoActual = null;
    private String categoriaActual = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        tokenManager = new TokenManager(this);

        TextView tvNombreUsuario = findViewById(R.id.tvNombreUsuario);

        String nombreGuardado = getSharedPreferences("SubastarPrefs", MODE_PRIVATE)
                .getString("USER_NAME", "Invitado");

        tvNombreUsuario.setText(nombreGuardado);

        ImageView btnPerfil = findViewById(R.id.btnPerfil);
        ImageView btnNotificaciones = findViewById(R.id.btnNotificaciones);
        TextView navMisPujas = findViewById(R.id.navMisPujas);
        TextView navConsignacion = findViewById(R.id.navConsignacion);

        // Inicializamos el badge y lo ocultamos por defecto
        badgeNotificacion = findViewById(R.id.badgeNotificacion);

        btnPerfil.setOnClickListener(v -> {
            String tokenGuardado = tokenManager.getToken();

            if (tokenGuardado == null) {
                Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(HomeActivity.this, PerfilActivity.class);
                startActivity(intent);
            }
        });

        // --- LÓGICA DE NOTIFICACIONES ---
        // Pedir permiso para notificaciones
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        btnNotificaciones.setOnClickListener(v -> {
            if (tokenManager.getToken() == null) {
                Toast.makeText(HomeActivity.this, "Debes iniciar sesión para ver tus notificaciones", Toast.LENGTH_SHORT).show();
            } else {
                // Redirigimos al usuario a la pantalla de notificaciones
                Intent intent = new Intent(HomeActivity.this, NotificacionesActivity.class);
                startActivity(intent);
            }
        });

        // --- LÓGICA DE NAVEGACIÓN INFERIOR ---
        navMisPujas.setOnClickListener(v -> {
            if (tokenManager.getToken() == null) {
                startActivity(new Intent(HomeActivity.this, WelcomeActivity.class));
            } else {
                startActivity(new Intent(HomeActivity.this, MisPujasActivity.class));
            }
        });

        navConsignacion.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Esa funcionalidad todavía no está disponible", Toast.LENGTH_SHORT).show();
        });

        navConsignacion.setOnClickListener(v -> {
            if (tokenManager.getToken() == null) {
                startActivity(new Intent(HomeActivity.this, WelcomeActivity.class));
            } else {
                startActivity(new Intent(HomeActivity.this, ConsignarBienActivity.class));
            }
        });

        recyclerView = findViewById(R.id.recyclerViewSubastas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvFechaActual = findViewById(R.id.tvFechaActual);
        tvProximas = findViewById(R.id.tvProximas);
        btnDiaAnterior = findViewById(R.id.btnDiaAnterior);
        btnDiaSiguiente = findViewById(R.id.btnDiaSiguiente);
        tvMensajeVacio = findViewById(R.id.tvMensajeVacio);
        etBuscador = findViewById(R.id.etBuscador);

        etBuscador.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarBuscador(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        etBuscador.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                v.clearFocus();
                return true;
            }
            return false;
        });

        btnDiaAnterior.setOnClickListener(v -> cambiarDia(-1));
        btnDiaSiguiente.setOnClickListener(v -> cambiarDia(1));

        actualizarTextoFecha();
        configurarChips();

        ejecutarConsultaBackend(null, null);
        conectarWebSocketHome();
    }

    private void conectarWebSocketHome() {
        if (stompClient != null && stompClient.isConnected()) return;

        compositeDisposable = new CompositeDisposable();
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/v1/subastar-ws/websocket");

        compositeDisposable.add(stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            suscribirseAEstadosSubastas();
                            break;
                        case ERROR:
                            Log.e("HOME_STOMP", "Error WebSocket", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.d("HOME_STOMP", "Conexion cerrada");
                            break;
                    }
                }));

        stompClient.connect();
    }

    private void suscribirseAEstadosSubastas() {
        compositeDisposable.add(stompClient.topic("/topic/subastas/estado-general")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stompMessage -> ejecutarConsultaBackend(estadoActual, categoriaActual, true),
                        error -> Log.e("HOME_STOMP", "Error en topic estado-general", error)));
    }

    private void cambiarDia(int dias) {
        fechaVisualizada = fechaVisualizada.plusDays(dias);
        actualizarTextoFecha();
        ejecutarConsultaBackend(estadoActual, categoriaActual);
    }

    private void actualizarTextoFecha() {
        if (etBuscador != null) {
            etBuscador.setText("");
        }

        if (fechaVisualizada.isEqual(LocalDate.now())) {
            tvFechaActual.setText("HOY");

            if (tvProximas != null){
                tvProximas.setText("SUBASTAS DEL DÍA");
            }

        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd", new Locale("es", "AR"));
            String textoFecha = fechaVisualizada.format(formatter).toUpperCase();

            tvFechaActual.setText(textoFecha.replace(".", ""));

            if (tvProximas != null) {
                if (fechaVisualizada.isBefore(LocalDate.now())) {
                    tvProximas.setText("SUBASTAS PASADAS");
                } else {
                    tvProximas.setText("PRÓXIMAS SUBASTAS");
                }
            }
        }
        btnDiaSiguiente.setVisibility(View.VISIBLE);
    }

    private void configurarChips() {
        TextView chipTodas = findViewById(R.id.chipTodas);
        TextView chipEnVivo = findViewById(R.id.chipEnVivo);

        boolean isInvitado = tokenManager.getToken() == null;

        if (isInvitado){
            chipEnVivo.setVisibility(View.GONE);
        }

        TextView chipComun = findViewById(R.id.chipComun);
        TextView chipEspecial = findViewById(R.id.chipEspecial);
        TextView chipPlata = findViewById(R.id.chipPlata);
        TextView chipOro = findViewById(R.id.chipOro);
        TextView chipPlatino = findViewById(R.id.chipPlatino);

        listaChips.add(chipTodas);
        listaChips.add(chipEnVivo);
        listaChips.add(chipComun);
        listaChips.add(chipEspecial);
        listaChips.add(chipPlata);
        listaChips.add(chipOro);
        listaChips.add(chipPlatino);

        chipTodas.setOnClickListener(v -> actualizarVistaChip(chipTodas, null, null));
        chipEnVivo.setOnClickListener(v -> actualizarVistaChip(chipEnVivo, "abierta", null));
        chipComun.setOnClickListener(v -> actualizarVistaChip(chipComun, null, "comun"));
        chipEspecial.setOnClickListener(v -> actualizarVistaChip(chipEspecial, null, "especial"));
        chipPlata.setOnClickListener(v -> actualizarVistaChip(chipPlata, null, "plata"));
        chipOro.setOnClickListener(v -> actualizarVistaChip(chipOro, null, "oro"));
        chipPlatino.setOnClickListener(v -> actualizarVistaChip(chipPlatino, null, "platino"));
    }

    private void actualizarVistaChip(TextView chipSeleccionado, String estado, String categoria) {

        this.estadoActual = estado;
        this.categoriaActual = categoria;

        for (TextView chip : listaChips) {
            chip.setBackgroundResource(R.drawable.bg_chip_inactivo);
            chip.setTextColor(ContextCompat.getColor(this, R.color.texto_sec));
            chip.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        chipSeleccionado.setBackgroundResource(R.drawable.bg_chip_activo);
        chipSeleccionado.setTextColor(ContextCompat.getColor(this, R.color.secundario));
        chipSeleccionado.setTypeface(null, android.graphics.Typeface.BOLD);

        ejecutarConsultaBackend(estado, categoria);
    }

    private void ejecutarConsultaBackend(String estado, String categoria) {
        ejecutarConsultaBackend(estado, categoria, false);
    }

    private void ejecutarConsultaBackend(String estado, String categoria, boolean preservarScroll) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SubastarApi api = retrofit.create(SubastarApi.class);

        String fechaBackend = fechaVisualizada.format(DateTimeFormatter.ISO_LOCAL_DATE);

        String tokenGuardado = tokenManager.getToken();
        String tokenHeader = null;

        if (tokenGuardado != null) {
            tokenHeader = "Bearer " + tokenGuardado;
        }

        api.obtenerSubastas(tokenHeader, estado, categoria, null, fechaBackend).enqueue(new Callback<List<Subasta>>() {
            @Override
            public void onResponse(Call<List<Subasta>> call, Response<List<Subasta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Subasta> listaOriginal = response.body();

                    boolean isInvitado = tokenManager.getToken() == null;

                    if (isInvitado) {
                        List<Subasta> listaFiltrada = new ArrayList<>();
                        for (Subasta subasta : listaOriginal) {
                            if (!"abierta".equalsIgnoreCase(subasta.getEstado())) {
                                listaFiltrada.add(subasta);
                            }
                        }
                        listaOriginal = listaFiltrada;
                    }

                    subastasDelDia.clear();
                    subastasDelDia.addAll(listaOriginal);

                    if (subastasDelDia.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        tvMensajeVacio.setText("No hay subastas programadas para este día.");
                        tvMensajeVacio.setVisibility(View.VISIBLE);
                    } else {
                        if (etBuscador != null) {
                            filtrarBuscador(etBuscador.getText().toString());
                        }
                    }

                    subastasDelDia.clear();
                    subastasDelDia.addAll(listaOriginal);

                    if (listaOriginal.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        tvMensajeVacio.setText("No hay subastas programadas para este día.");
                        tvMensajeVacio.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        tvMensajeVacio.setVisibility(View.GONE);

                        int posicionScroll = RecyclerView.NO_POSITION;
                        int offsetScroll = 0;
                        if (preservarScroll && recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                            posicionScroll = layoutManager.findFirstVisibleItemPosition();
                            View itemVisible = layoutManager.findViewByPosition(posicionScroll);
                            if (itemVisible != null) {
                                offsetScroll = itemVisible.getTop() - recyclerView.getPaddingTop();
                            }
                        }

                        adapter = new SubastaAdapter(listaOriginal);
                        recyclerView.setAdapter(adapter);

                        if (posicionScroll != RecyclerView.NO_POSITION && recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                            int posicionFinal = Math.min(posicionScroll, adapter.getItemCount() - 1);
                            int offsetFinal = offsetScroll;
                            recyclerView.post(() -> ((LinearLayoutManager) recyclerView.getLayoutManager())
                                    .scrollToPositionWithOffset(posicionFinal, offsetFinal));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Subasta>> call, Throwable t) {
                Log.e("HOME_SERVER_ERROR", t.getMessage());

                recyclerView.setVisibility(View.GONE);
                tvMensajeVacio.setText("No tienes conexión a internet, prueba abriendo de nuevo la app.");
                tvMensajeVacio.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tokenManager != null && tokenManager.getToken() != null) {
            verificarMediosPagoObligatorio();
            chequearNotificacionesPendientes();
            suscribirAFirebase();
        }
    }

    // Nuevo método armado exactamente con tu estructura de Retrofit y Token
    private void chequearNotificacionesPendientes() {
        String tokenGuardado = tokenManager.getToken();
        if (tokenGuardado == null) return;

        String tokenHeader = "Bearer " + tokenGuardado;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SubastarApi api = retrofit.create(SubastarApi.class);

        api.getNotificaciones(tokenHeader, false).enqueue(new Callback<List<NotificacionDTO>>() {
            @Override
            public void onResponse(Call<List<NotificacionDTO>> call, Response<List<NotificacionDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!response.body().isEmpty()) {
                        badgeNotificacion.setVisibility(View.VISIBLE);
                    } else {
                        badgeNotificacion.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<NotificacionDTO>> call, Throwable t) {
                Log.e("HOME_NOTIF_ERROR", "Error al traer notificaciones", t);
            }
        });
    }

    private void verificarMediosPagoObligatorio() {
        int idCliente = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getInt("USER_ID", -1);
        String token = "Bearer " + tokenManager.getToken();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build();
        SubastarApi api = retrofit.create(SubastarApi.class);

        api.obtenerMediosPago(idCliente, token).enqueue(new retrofit2.Callback<java.util.List<com.grupo6.subastar.dto.MedioPagoDTO>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.grupo6.subastar.dto.MedioPagoDTO>> call, retrofit2.Response<java.util.List<com.grupo6.subastar.dto.MedioPagoDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        android.content.Intent intent = new android.content.Intent(HomeActivity.this, AgregarMedioPagoActivity.class);
                        intent.putExtra("clienteId", idCliente);
                        intent.putExtra("esObligatorio", true);
                        startActivity(intent);
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.grupo6.subastar.dto.MedioPagoDTO>> call, Throwable t) {}
        });
    }

    private void suscribirAFirebase() {
        // Recuperamos el ID del cliente de tus SharedPreferences
        int idCliente = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getInt("USER_ID", -1);

        if (idCliente != -1) {
            com.google.firebase.messaging.FirebaseMessaging.getInstance()
                    .subscribeToTopic("cliente_" + idCliente)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("FIREBASE", "Suscrito exitosamente al canal: cliente_" + idCliente);
                        } else {
                            Log.e("FIREBASE", "Falló la suscripción a Firebase");
                        }
                    });
        }
    }

    private void filtrarBuscador(String textoBusqueda) {
        List<Subasta> listaFiltrada = new ArrayList<>();

        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            listaFiltrada.addAll(subastasDelDia);
        } else {
            String texto = textoBusqueda.toLowerCase().trim();

            for (Subasta s : subastasDelDia) {
                boolean coincideTitulo = s.getCatalogo() != null
                        && s.getCatalogo().getDescripcion() != null
                        && s.getCatalogo().getDescripcion().toLowerCase().contains(texto);

                boolean coincideCategoria = s.getCategoria() != null
                        && s.getCategoria().toLowerCase().contains(texto);

                if (coincideTitulo || coincideCategoria) {
                    listaFiltrada.add(s);
                }
            }
        }

        if (listaFiltrada.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvMensajeVacio.setText("No se encontraron resultados para tu búsqueda.");
            tvMensajeVacio.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvMensajeVacio.setVisibility(View.GONE);

            if (adapter != null) {
                adapter.actualizarLista(listaFiltrada);
            } else {
                adapter = new SubastaAdapter(listaFiltrada);
                recyclerView.setAdapter(adapter);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) compositeDisposable.dispose();
        if (stompClient != null && stompClient.isConnected()) stompClient.disconnect();
    }
}