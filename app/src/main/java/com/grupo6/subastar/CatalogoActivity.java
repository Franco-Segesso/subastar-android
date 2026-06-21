package com.grupo6.subastar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.grupo6.subastar.adapter.ItemProductoAdapter;
import com.grupo6.subastar.dto.EstadoPujaDTO;
import com.grupo6.subastar.model.Subasta;
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

public class CatalogoActivity extends AppCompatActivity {

    private TextView tvTitulo, tvCat, tvMoneda, tvEstado, tvFecha;
    private View layoutTransmisionVivo;
    private RecyclerView recyclerView;
    private Integer subastaId;
    private TokenManager tokenManager;
    private ItemProductoAdapter adapter;
    private StompClient stompClient;
    private CompositeDisposable compositeDisposable;
    private Gson gson;
    private LinearLayout layoutEnVivoCatalogo;
    private boolean refrescandoPorCierre = false;
    private final Handler websocketHandler = new Handler(Looper.getMainLooper());
    private boolean conectandoWebSocket = false;
    private boolean pantallaDestruida = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo);

        tokenManager = new TokenManager(this);
        gson = new Gson();


        tvTitulo = findViewById(R.id.tvCatalogoTitulo);
        tvCat = findViewById(R.id.tvHeaderCat);
        tvMoneda = findViewById(R.id.tvHeaderMoneda);
        tvEstado = findViewById(R.id.tvHeaderEstado);
        layoutTransmisionVivo = findViewById(R.id.layoutTransmisionVivo);
        recyclerView = findViewById(R.id.recyclerViewProductos);
        tvFecha = findViewById(R.id.tvCatalogoFecha);
        layoutEnVivoCatalogo = findViewById(R.id.layoutEnVivoCatalogo);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Botón volver
        ImageButton btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish()); // Cierra esta pantalla y vuelve al Home

        // Capturar ID que mandó el HomeActivity
        subastaId = getIntent().getIntExtra("SUBASTA_ID", -1);

        if (subastaId == -1) {
            Toast.makeText(this, "Error: No se encontró la subasta", Toast.LENGTH_SHORT).show();
            finish();
        }

        conectarWebSocketCatalogo();


    }

    @Override
    protected void onResume() {
        super.onResume();
        conectarWebSocketCatalogo();
        // Cada vez que el usuario vuelve a esta pantalla (por ejemplo, saliendo de la sala de puja)
        // recargamos los datos desde el backend usando el ID capturado.
        if (subastaId != null && subastaId != -1) {
            cargarDetalleSubasta(subastaId);
        }
    }

    private void conectarWebSocketCatalogo() {
        if (subastaId == null || subastaId <= 0) return;
        if (pantallaDestruida || conectandoWebSocket
                || (stompClient != null && stompClient.isConnected())) return;

        conectandoWebSocket = true;
        if (compositeDisposable != null) compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, BuildConfig.WS_URL);

        compositeDisposable.add(stompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            conectandoWebSocket = false;
                            suscribirseATopicosCatalogo();
                            break;
                        case ERROR:
                            conectandoWebSocket = false;
                            Log.e("CATALOGO_STOMP", "Error WebSocket", lifecycleEvent.getException());
                            programarReconexionWebSocket();
                            break;
                        case CLOSED:
                            conectandoWebSocket = false;
                            Log.d("CATALOGO_STOMP", "Conexion cerrada");
                            programarReconexionWebSocket();
                            break;
                    }
                }));

        stompClient.connect();
    }

    private void programarReconexionWebSocket() {
        websocketHandler.removeCallbacksAndMessages(null);
        if (!pantallaDestruida) {
            websocketHandler.postDelayed(this::conectarWebSocketCatalogo, 2000);
        }
    }

    private void suscribirseATopicosCatalogo() {
        compositeDisposable.add(stompClient.topic("/topic/subastas/estado-general")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stompMessage -> {
                    JsonObject evento = gson.fromJson(
                            stompMessage.getPayload(), JsonObject.class);
                    if (evento == null
                            || !evento.has("subastaId")
                            || evento.get("subastaId").getAsInt() != subastaId) {
                        return;
                    }
                    String estado = evento.has("estado")
                            ? evento.get("estado").getAsString() : "";
                    tvEstado.setText(estado.toUpperCase());
                    boolean abierta = "abierta".equalsIgnoreCase(estado);
                    layoutTransmisionVivo.setVisibility(
                            abierta ? View.VISIBLE : View.GONE);
                    layoutEnVivoCatalogo.setVisibility(
                            abierta ? View.VISIBLE : View.GONE);
                }, error -> Log.e(
                        "CATALOGO_STOMP", "Error en topic estado-general", error)));

        compositeDisposable.add(stompClient.topic("/topic/subastas/" + subastaId + "/estado")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stompMessage -> {
                    if (refrescandoPorCierre) return;
                    EstadoPujaDTO estado = gson.fromJson(stompMessage.getPayload(), EstadoPujaDTO.class);
                    if (estado != null && estado.getItemId() != null && adapter != null && !estado.isCerrado()) {
                        layoutTransmisionVivo.setVisibility(View.VISIBLE);
                        layoutEnVivoCatalogo.setVisibility(View.VISIBLE);
                        tvEstado.setText("ABIERTA");
                        adapter.actualizarItemActivo(estado.getItemId());
                    }
                }, error -> Log.e("CATALOGO_STOMP", "Error en topic estado", error)));

        compositeDisposable.add(stompClient.topic("/topic/subastas/" + subastaId + "/cierre")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stompMessage -> {
                            refrescandoPorCierre = true;
                            recyclerView.postDelayed(() -> cargarDetalleSubasta(subastaId), 800);
                        },
                        error -> Log.e("CATALOGO_STOMP", "Error en topic cierre", error)));
    }

    private void cargarDetalleSubasta(Integer id) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SubastarApi api = retrofit.create(SubastarApi.class);

        String tokenGuardado = tokenManager.getToken();
        String tokenHeader = null;

        // Si el usuario está logueado, armamos el header de autorización
        if (tokenGuardado != null) {
            tokenHeader = "Bearer " + tokenGuardado;
        }

        api.obtenerDetalleSubasta(id, tokenHeader).enqueue(new Callback<Subasta>() {
            @Override
            public void onResponse(Call<Subasta> call, Response<Subasta> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Subasta subasta = response.body();


                    if (subasta.getCatalogo() != null && subasta.getCatalogo().getDescripcion() != null) {
                        tvTitulo.setText(subasta.getCatalogo().getDescripcion());
                    } else {
                        tvTitulo.setText("Subasta sin título");
                    }
                    tvCat.setText(subasta.getCategoria().toUpperCase());
                    tvMoneda.setText(subasta.getMoneda());
                    tvEstado.setText(subasta.getEstado().toUpperCase());
                    layoutTransmisionVivo.setVisibility(
                            "abierta".equalsIgnoreCase(subasta.getEstado()) ? View.VISIBLE : View.GONE
                    );

                    String fecha = subasta.getFecha() != null ? subasta.getFecha() : "Fecha a confirmar";
                    String hora = subasta.getHora() != null ? subasta.getHora() : "";
                    String fechaFormateada = fecha + " - " + hora + " hs";
                    // Formateamos para que se vea prolijo (Ej: 15/08/2026 - 18:30 hs)
                    tvFecha.setText(fechaFormateada);

                    if ("abierta".equalsIgnoreCase(subasta.getEstado())) {
                        layoutEnVivoCatalogo.setVisibility(View.VISIBLE);
                    } else {
                        layoutEnVivoCatalogo.setVisibility(View.GONE);
                    }

                    // Le pasamos los datos directamente al instanciar el adapter
                    if (subasta.getCatalogo() != null && subasta.getCatalogo().getItems() != null) {
                        adapter = new ItemProductoAdapter(
                                subasta.getCatalogo().getItems(),
                                CatalogoActivity.this,
                                subasta.getId(),
                                subasta.getEstado(),
                                fechaFormateada,
                                subasta.getMoneda()

                        );
                        // Aseguramos que la lista se repinte por completo al volver de la sala
                        recyclerView.setAdapter(adapter);
                    }
                    refrescandoPorCierre = false;
                }
            }

            @Override
            public void onFailure(Call<Subasta> call, Throwable t) {
                refrescandoPorCierre = false;
                Toast.makeText(CatalogoActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pantallaDestruida = true;
        websocketHandler.removeCallbacksAndMessages(null);
        if (compositeDisposable != null) compositeDisposable.dispose();
        if (stompClient != null && stompClient.isConnected()) stompClient.disconnect();
    }
}
