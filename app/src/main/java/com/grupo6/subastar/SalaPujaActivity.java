package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.grupo6.subastar.adapter.PujaHistorialAdapter;
import com.grupo6.subastar.dto.CierreSubastaDTO;
import com.grupo6.subastar.dto.EstadoPujaDTO;
import com.grupo6.subastar.dto.PujaMensajeDTO;
import com.grupo6.subastar.dto.PujaRequest;

import java.util.ArrayList;
import java.util.List;

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
import ua.naiksoftware.stomp.dto.StompHeader;

public class SalaPujaActivity extends AppCompatActivity {

    private ImageView btnVolver, ivItemImagen;
    private TextView tvHeaderTitle, tvHeaderSubtitle, tvBase, tvOfertaActual, tvTemporizador, tvBannerEstado;
    private EditText etMontoPuja;
    private Button btnPujar;
    private RecyclerView rvHistorialPujas;

    private PujaHistorialAdapter adapter;
    private StompClient mStompClient;
    private CompositeDisposable compositeDisposable;
    private Gson gson;

    private Integer subastaId;
    private Integer itemId;
    private Integer miClienteId;
    private String tokenJwt;
    private SubastarApi api;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private int segundosReferencia = 0;
    private long tiempoReferenciaMs = 0;
    private final Runnable tickerTimer = new Runnable() {
        @Override
        public void run() {
            actualizarTimerVisual();
            timerHandler.postDelayed(this, 250);
        }
    };

    private boolean modalResultadoMostrado = false;
    private boolean saliendo = false;
    private boolean itemActivo = false;
    private boolean soyMayorPostor = false;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala_puja);

        tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();
        miClienteId = tokenManager.getClienteId();

        if (token == null || miClienteId == null || miClienteId <= 0) {
            Toast.makeText(this, "Necesitas iniciar sesion para participar.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        subastaId = getIntent().getIntExtra("SUBASTA_ID", -1);
        itemId = getIntent().getIntExtra("ITEM_ID", -1);
        if (subastaId <= 0 || itemId <= 0) {
            Toast.makeText(this, "No se pudo abrir la sala de puja.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        cargarDatosUI();

        gson = new Gson();
        compositeDisposable = new CompositeDisposable();
        tokenJwt = "Bearer " + token;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SubastarApi.class);

        configurarRecyclerView();
        configurarBotones();
        configurarNavegacionAtras();
        ingresarSalaBackend();
    }

    private void initViews() {
        btnVolver = findViewById(R.id.btnVolver);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderSubtitle = findViewById(R.id.tvHeaderSubtitle);
        ivItemImagen = findViewById(R.id.ivItemImagen);
        tvBase = findViewById(R.id.tvBase);
        tvOfertaActual = findViewById(R.id.tvOfertaActual);
        tvTemporizador = findViewById(R.id.tvTemporizador);
        tvBannerEstado = findViewById(R.id.tvBannerEstado);
        etMontoPuja = findViewById(R.id.etMontoPuja);
        btnPujar = findViewById(R.id.btnPujar);
        rvHistorialPujas = findViewById(R.id.rvHistorialPujas);

        tvBannerEstado.setVisibility(View.GONE);
        habilitarPuja(false);
    }

    private void cargarDatosUI() {
        String titulo = getIntent().getStringExtra("ITEM_TITULO");
        double precioBase = getIntent().getDoubleExtra("ITEM_BASE", 0);
        String urlImagen = getIntent().getStringExtra("ITEM_IMAGEN");

        if (titulo != null) tvHeaderTitle.setText("Sala de puja - " + titulo);
        if (precioBase > 0) {
            tvBase.setText(String.format("Base: USD %.2f", precioBase));
            tvOfertaActual.setText(String.format("USD %.2f", precioBase));
        }
        if (urlImagen != null && !urlImagen.isEmpty()) {
            Glide.with(this).load(urlImagen).centerCrop().into(ivItemImagen);
        }
    }

    private void configurarRecyclerView() {
        adapter = new PujaHistorialAdapter(miClienteId);
        rvHistorialPujas.setLayoutManager(new LinearLayoutManager(this));
        rvHistorialPujas.setAdapter(adapter);
    }

    private void configurarBotones() {
        btnVolver.setOnClickListener(v -> salirYFinalizar());

        btnPujar.setOnClickListener(v -> {
            if (!itemActivo) {
                Toast.makeText(this, "Este item no esta activo para pujar.", Toast.LENGTH_SHORT).show();
                return;
            }

            String montoStr = etMontoPuja.getText().toString().trim();
            if (montoStr.isEmpty()) {
                etMontoPuja.setError("Ingresa un monto");
                return;
            }

            try {
                realizarPujaBackend(Double.parseDouble(montoStr));
            } catch (NumberFormatException e) {
                etMontoPuja.setError("Monto invalido");
            }
        });
    }

    private void ingresarSalaBackend() {
        api.ingresarSubasta(tokenJwt, subastaId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cargarHistorialPujas();
                    conectarWebSocket();
                } else {
                    mostrarErrorIngreso(response.code(), leerError(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mostrarErrorIngreso(0, "Fallo de conexion.");
            }
        });
    }

    private void cargarHistorialPujas() {
        api.obtenerHistorialPujas(tokenJwt, subastaId, itemId).enqueue(new Callback<List<PujaMensajeDTO>>() {
            @Override
            public void onResponse(Call<List<PujaMensajeDTO>> call, Response<List<PujaMensajeDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setPujas(response.body());
                    if (!response.body().isEmpty()) {
                        actualizarResumenConPuja(response.body().get(0));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PujaMensajeDTO>> call, Throwable t) {
                Log.e("HISTORIAL_PUJAS", "No se pudo cargar historial", t);
            }
        });
    }

    private void realizarPujaBackend(Double monto) {
        habilitarPuja(false);
        PujaRequest request = new PujaRequest(itemId, monto);

        api.registrarPuja(tokenJwt, subastaId, request).enqueue(new Callback<PujaMensajeDTO>() {
            @Override
            public void onResponse(Call<PujaMensajeDTO> call, Response<PujaMensajeDTO> response) {
                habilitarPuja(itemActivo);
                if (response.isSuccessful()) {
                    etMontoPuja.setText("");
                    mostrarModalPujaExitosa();
                } else if (response.code() == 400) {
                    Toast.makeText(SalaPujaActivity.this, "Oferta rechazada: no supera el minimo", Toast.LENGTH_LONG).show();
                } else if (response.code() == 409) {
                    Toast.makeText(SalaPujaActivity.this, "Oferta rechazada: supera el limite maximo", Toast.LENGTH_LONG).show();
                } else if (response.code() == 422) {
                    Toast.makeText(SalaPujaActivity.this, "El item ya fue subastado", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SalaPujaActivity.this, leerError(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PujaMensajeDTO> call, Throwable t) {
                habilitarPuja(itemActivo);
                Toast.makeText(SalaPujaActivity.this, "Error de red al pujar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void conectarWebSocket() {
        String wsUrl = "ws://10.0.2.2:8080/v1/subastar-ws/websocket";
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl);

        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("Authorization", tokenJwt));

        compositeDisposable.add(mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            suscribirseATopicos();
                            break;
                        case ERROR:
                            Log.e("STOMP", "Error", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.d("STOMP", "Conexion cerrada");
                            break;
                    }
                }));

        mStompClient.connect(headers);
    }

    private void suscribirseATopicos() {
        compositeDisposable.add(mStompClient.topic("/topic/subastas/" + subastaId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stompMessage -> {
                    PujaMensajeDTO nuevaPuja = gson.fromJson(stompMessage.getPayload(), PujaMensajeDTO.class);
                    actualizarSalaConNuevaPuja(nuevaPuja);
                }, error -> Log.e("STOMP", "Error en topic pujas", error)));

        compositeDisposable.add(mStompClient.topic("/topic/subastas/" + subastaId + "/cierre")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stompMessage -> {
                    CierreSubastaDTO cierre = gson.fromJson(stompMessage.getPayload(), CierreSubastaDTO.class);
                    mostrarModalResultado(cierre);
                }, error -> Log.e("STOMP", "Error en topic cierre", error)));

        compositeDisposable.add(mStompClient.topic("/topic/subastas/" + subastaId + "/estado")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stompMessage -> {
                    EstadoPujaDTO estado = gson.fromJson(stompMessage.getPayload(), EstadoPujaDTO.class);
                    actualizarEstadoPuja(estado);
                }, error -> Log.e("STOMP", "Error en topic estado", error)));
    }

    private void actualizarSalaConNuevaPuja(PujaMensajeDTO puja) {
        if (puja == null || puja.getItemId() == null || !puja.getItemId().equals(itemId)) return;

        adapter.agregarPuja(puja);
        rvHistorialPujas.scrollToPosition(0);
        actualizarResumenConPuja(puja);
    }

    private void actualizarEstadoPuja(EstadoPujaDTO estado) {
        if (estado == null || estado.getItemId() == null) return;

        boolean esEsteItem = estado.getItemId().equals(itemId);
        itemActivo = esEsteItem && !estado.isCerrado();

        if (estado.getImporteActual() != null && esEsteItem) {
            tvOfertaActual.setText(String.format("USD %.2f", estado.getImporteActual()));
        }

        int segundos = estado.getTiempoRestanteSegundos() != null ? estado.getTiempoRestanteSegundos() : 0;
        iniciarTickerVisual(Math.max(0, segundos));

        if (!esEsteItem) {
            detenerTickerVisual();
            habilitarPuja(false);
            soyMayorPostor = false;
            tvBannerEstado.setText("Este item no es el item activo de la subasta.");
            tvBannerEstado.setVisibility(View.VISIBLE);
        } else if (estado.isCerrado()) {
            detenerTickerVisual();
            tvTemporizador.setText("00:00");
            habilitarPuja(false);
            soyMayorPostor = false;
            tvBannerEstado.setText("Item cerrado. Esperando resultado...");
            tvBannerEstado.setVisibility(View.VISIBLE);
        } else {
            habilitarPuja(true);
        }
    }

    private void actualizarResumenConPuja(PujaMensajeDTO puja) {
        if (puja == null) return;
        tvOfertaActual.setText(String.format("USD %.2f", puja.getImporte()));
        if (esMiPuja(puja)) {
            soyMayorPostor = true;
            tvBannerEstado.setText("Actualmente eres el mayor postor de esta subasta!");
            tvBannerEstado.setVisibility(View.VISIBLE);
        } else {
            soyMayorPostor = false;
            tvBannerEstado.setVisibility(View.GONE);
        }
    }

    private boolean esMiPuja(PujaMensajeDTO puja) {
        if (puja == null || puja.getAsistente() == null || puja.getAsistente().getCliente() == null) return false;
        Integer clienteId = puja.getAsistente().getCliente().getIdentificador();
        return clienteId != null && clienteId.equals(miClienteId);
    }

    private void iniciarTickerVisual(int segundosBackend) {
        segundosReferencia = segundosBackend;
        tiempoReferenciaMs = SystemClock.elapsedRealtime();
        timerHandler.removeCallbacks(tickerTimer);
        actualizarTimerVisual();
        if (segundosBackend > 0) {
            timerHandler.postDelayed(tickerTimer, 250);
        }
    }

    private void actualizarTimerVisual() {
        long transcurridoMs = SystemClock.elapsedRealtime() - tiempoReferenciaMs;
        long restanteMs = Math.max(0, (segundosReferencia * 1000L) - transcurridoMs);
        int segundosMostrados = (int) ((restanteMs + 999) / 1000);
        tvTemporizador.setText(String.format("00:%02d", segundosMostrados));

        if (segundosMostrados <= 0 && itemActivo) {
            itemActivo = false;
            habilitarPuja(false);
            tvBannerEstado.setText("Item cerrado. Esperando resultado...");
            tvBannerEstado.setVisibility(View.VISIBLE);
        }
    }

    private void detenerTickerVisual() {
        timerHandler.removeCallbacks(tickerTimer);
    }

    private void mostrarModalPujaExitosa() {
        new AlertDialog.Builder(this)
                .setTitle("PUJA EXITOSA!")
                .setMessage("Actualmente eres el mayor postor por este item.")
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void mostrarModalResultado(CierreSubastaDTO cierre) {
        if (modalResultadoMostrado || cierre == null) return;
        if (cierre.getItemId() != null && !cierre.getItemId().equals(itemId)) return;

        modalResultadoMostrado = true;
        habilitarPuja(false);
        soyMayorPostor = false;
        detenerTickerVisual();

        if (mStompClient != null && mStompClient.isConnected()) mStompClient.disconnect();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        if (!cierre.isHayGanador()) {
            builder.setTitle("Subasta Desierta")
                    .setMessage("Nadie pujo por este item.")
                    .setPositiveButton("Volver", (dialog, which) -> salirYNavegarAlCatalogo());
        } else if (miClienteId.equals(cierre.getIdClienteGanador())) {
            builder.setTitle("FELICIDADES\nGANASTE LA SUBASTA")
                    .setMessage("Se registro tu compra privada con el importe que debes pagar, comisiones y costo de envio.")
                    .setNegativeButton("Volver a subastas", (dialog, which) -> salirYNavegarAlCatalogo())
                    .setPositiveButton("Ir al pago", (dialog, which) -> salirYNavegarAlCatalogo());
        } else {
            builder.setTitle("Subasta Finalizada")
                    .setMessage("El item fue vendido a otro postor por USD " + cierre.getImporteFinal())
                    .setPositiveButton("Volver", (dialog, which) -> salirYNavegarAlCatalogo());
        }

        builder.show();
    }

    private void mostrarErrorIngreso(int codigo, String detalle) {
        String titulo = codigo == 403 ? "Tu categoria no te permite participar en esta subasta" : "No se pudo ingresar";
        String mensaje = detalle != null && !detalle.isEmpty() ? detalle : "Intenta nuevamente mas tarde.";

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setCancelable(false)
                .setPositiveButton("Aceptar", (dialog, which) -> finish())
                .show();
    }

    private void habilitarPuja(boolean habilitada) {
        etMontoPuja.setEnabled(habilitada);
        btnPujar.setEnabled(habilitada);
    }

    private String leerError(Response<?> response) {
        if (response == null) return "Error desconocido";
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (Exception e) {
            Log.e("API_ERROR", "No se pudo leer error", e);
        }
        return "Error " + response.code();
    }

    private void salirYFinalizar() {
        if (soyMayorPostor) {
            mostrarModalMayorPostor();
            return;
        }
        salirDeSala(false);
    }

    private void salirYNavegarAlCatalogo() {
        salirDeSala(true);
    }

    private void salirDeSala(boolean navegarAlCatalogo) {
        if (saliendo) return;
        saliendo = true;

        if (api == null || tokenJwt == null || subastaId == null) {
            if (mStompClient != null && mStompClient.isConnected()) mStompClient.disconnect();
            detenerTickerVisual();
            finalizarSalida(navegarAlCatalogo);
            return;
        }

        api.salirSubasta(tokenJwt, subastaId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (mStompClient != null && mStompClient.isConnected()) mStompClient.disconnect();
                    detenerTickerVisual();
                    finalizarSalida(navegarAlCatalogo);
                } else if (response.code() == 409) {
                    saliendo = false;
                    mostrarModalMayorPostor();
                } else {
                    if (mStompClient != null && mStompClient.isConnected()) mStompClient.disconnect();
                    detenerTickerVisual();
                    finalizarSalida(navegarAlCatalogo);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                saliendo = false;
                Toast.makeText(SalaPujaActivity.this, "No se pudo salir de la sala. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarNavegacionAtras() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                salirYFinalizar();
            }
        });
    }

    private void mostrarModalMayorPostor() {
        new AlertDialog.Builder(this)
                .setTitle("No podes salir todavia")
                .setMessage("Actualmente sos el mayor postor de este item. Para mantener la puja activa, tenes que esperar a que alguien te supere o a que finalice la subasta del item.")
                .setPositiveButton("Entendido", null)
                .show();
    }

    private void finalizarSalida(boolean navegarAlCatalogo) {
        if (navegarAlCatalogo && subastaId != null && subastaId > 0) {
            Intent intent = new Intent(SalaPujaActivity.this, CatalogoActivity.class);
            intent.putExtra("SUBASTA_ID", subastaId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        salirYFinalizar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detenerTickerVisual();
        if (compositeDisposable != null) compositeDisposable.dispose();
    }
}
