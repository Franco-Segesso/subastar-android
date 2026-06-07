package com.grupo6.subastar;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.grupo6.subastar.adapter.PujaHistorialAdapter;
import com.grupo6.subastar.dto.CierreSubastaDTO;
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

    // 1. Variables mapeadas exactamente al último XML
    private ImageView btnVolver, ivItemImagen;
    private TextView tvHeaderTitle, tvHeaderSubtitle, tvBase, tvOfertaActual, tvTemporizador, tvBannerEstado;
    private EditText etMontoPuja;
    private Button btnPujar;
    private RecyclerView rvHistorialPujas;

    private PujaHistorialAdapter adapter;
    private CountDownTimer countDownTimer;
    private StompClient mStompClient;
    private CompositeDisposable compositeDisposable;
    private Gson gson;

    private Integer subastaId;
    private Integer itemId;
    private Integer miClienteId;
    private String tokenJwt;
    private SubastarApi api;

    private boolean modalMostrado = false;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala_puja);

        tokenManager = new TokenManager(this);

        // Simulamos recuperar datos del Intent (ajusta según lo que envíes desde tu DetalleItemActivity)
        subastaId = getIntent().getIntExtra("SUBASTA_ID", 1);
        itemId = getIntent().getIntExtra("ITEM_ID", 1);
        miClienteId = 123; // TODO: Reemplazar por el ID real del usuario logueado

        initViews();
        cargarDatosUI();

        gson = new Gson();
        compositeDisposable = new CompositeDisposable();
        tokenJwt = "Bearer " + tokenManager.getToken();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SubastarApi.class);

        configurarRecyclerView();
        configurarBotones();

        // Iniciar el flujo de la sala
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

        tvBannerEstado.setVisibility(View.GONE); // Se oculta hasta que el usuario pase a ganar
    }

    private void cargarDatosUI() {
        String titulo = getIntent().getStringExtra("ITEM_TITULO");
        Double precioBase = getIntent().getDoubleExtra("ITEM_BASE", 0);
        String urlImagen = getIntent().getStringExtra("ITEM_IMAGEN");

        if (titulo != null) tvHeaderTitle.setText("Sala de puja - " + titulo);
        if (precioBase != 0) {
            String precioFormateado = String.format("%.2f", precioBase);
            tvBase.setText("Base: $" + precioBase);
            tvOfertaActual.setText("$" + precioBase);
        }

        // Si tuvieras una URL de imagen, Glide la carga acá:
        /*
        if (urlImagen != null && !urlImagen.isEmpty()) {
            Glide.with(this).load(urlImagen).centerCrop().into(ivItemImagen);
        }
        */
    }

    private void configurarRecyclerView() {
        adapter = new PujaHistorialAdapter(miClienteId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvHistorialPujas.setLayoutManager(layoutManager);
        rvHistorialPujas.setAdapter(adapter);
    }

    private void configurarBotones() {
        btnVolver.setOnClickListener(v -> salirYFinalizar());

        btnPujar.setOnClickListener(v -> {
            String montoStr = etMontoPuja.getText().toString().trim();
            if (montoStr.isEmpty()) {
                etMontoPuja.setError("Ingresa un monto");
                return;
            }
            realizarPujaBackend(Double.parseDouble(montoStr));
        });
    }

    private void ingresarSalaBackend() {
        api.ingresarSubasta(tokenJwt, subastaId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    conectarWebSocket();
                    iniciarTemporizador();
                } else {
                    // ¡MODIFICACIÓN AQUÍ PARA LEER EL ERROR DEL BACKEND!
                    String mensajeError = "Error " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            mensajeError += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.e("ERROR_INGRESO_SALA", mensajeError);
                    Toast.makeText(SalaPujaActivity.this, mensajeError, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SalaPujaActivity.this, "Fallo de conexión.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void realizarPujaBackend(Double monto) {
        btnPujar.setEnabled(false);
        PujaRequest request = new PujaRequest(itemId, monto);

        api.registrarPuja(tokenJwt, subastaId, request).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                btnPujar.setEnabled(true);
                if (response.isSuccessful()) {
                    etMontoPuja.setText("");
                } else if (response.code() == 400) {
                    Toast.makeText(SalaPujaActivity.this, "Oferta rechazada: No supera el mínimo", Toast.LENGTH_LONG).show();
                } else if (response.code() == 409) {
                    Toast.makeText(SalaPujaActivity.this, "Oferta rechazada: Supera el límite máximo", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                btnPujar.setEnabled(true);
                Toast.makeText(SalaPujaActivity.this, "Error de red al pujar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void conectarWebSocket() {
        String wsUrl = "ws://10.0.2.2:8080/v1/subastar-ws/websocket";
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl);

        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("Authorization", tokenJwt));

        // 1. Escuchar estados de conexión
        compositeDisposable.add(mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d("STOMP", "Conexión abierta, suscribiéndose...");
                            suscribirseATopicos(); // <--- Solo aquí nos suscribimos
                            break;
                        case ERROR:
                            Log.e("STOMP", "Error: ", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            Log.d("STOMP", "Conexión cerrada");
                            break;
                    }
                }));

        mStompClient.connect(headers);
    }

    private void suscribirseATopicos() {
        // Suscripción al tópico de pujas
        compositeDisposable.add(mStompClient.topic("/topic/subastas/" + subastaId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stompMessage -> {
                    PujaMensajeDTO nuevaPuja = gson.fromJson(stompMessage.getPayload(), PujaMensajeDTO.class);
                    actualizarSalaConNuevaPuja(nuevaPuja);
                }, error -> Log.e("STOMP", "Error en topic pujas", error)));

        // Suscripción al tópico de cierre
        compositeDisposable.add(mStompClient.topic("/topic/subastas/" + subastaId + "/cierre")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stompMessage -> {
                    CierreSubastaDTO cierre = gson.fromJson(stompMessage.getPayload(), CierreSubastaDTO.class);
                    mostrarModalResultado(cierre);
                }, error -> Log.e("STOMP", "Error en topic cierre", error)));
    }

    private void actualizarSalaConNuevaPuja(PujaMensajeDTO puja) {
        adapter.agregarPuja(puja);
        rvHistorialPujas.scrollToPosition(0);
        tvOfertaActual.setText(String.format("USD %.2f", puja.getImporte()));

        if (puja.getAsistente().getCliente().getIdentificador().equals(miClienteId)) {
            tvBannerEstado.setVisibility(View.VISIBLE);
        } else {
            tvBannerEstado.setVisibility(View.GONE);
        }

        reiniciarTemporizador();
    }

    private void iniciarTemporizador() {
        countDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                long segundos = millisUntilFinished / 1000;
                tvTemporizador.setText(String.format("00:%02d", segundos));
            }
            public void onFinish() {
                tvTemporizador.setText("00:00");
                cerrarSubastaEnBackend();
            }
        }.start();
    }

    private void reiniciarTemporizador() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            iniciarTemporizador();
        }
    }

    private void cerrarSubastaEnBackend() {
        btnPujar.setEnabled(false);
        api.cerrarSubasta(tokenJwt, subastaId, itemId).enqueue(new Callback<CierreSubastaDTO>() {
            @Override
            public void onResponse(Call<CierreSubastaDTO> call, Response<CierreSubastaDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mostrarModalResultado(response.body());
                }
            }
            @Override
            public void onFailure(Call<CierreSubastaDTO> call, Throwable t) {
                Toast.makeText(SalaPujaActivity.this, "Error al cerrar la subasta", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void mostrarModalResultado(CierreSubastaDTO cierre) {
        if (modalMostrado) return;
        modalMostrado = true;

        if (countDownTimer != null) countDownTimer.cancel();
        if (mStompClient != null && mStompClient.isConnected()) mStompClient.disconnect();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        if (!cierre.isHayGanador()) {
            builder.setTitle("Subasta Desierta").setMessage("Nadie pujó por este ítem.");
        } else if (cierre.getIdClienteGanador().equals(miClienteId)) {
            builder.setTitle("¡Ganaste la Subasta!")
                    .setMessage("Adjudicado por USD " + cierre.getImporteFinal() + "\n\nPor favor, procede a pagar para concretar la transacción.")
                    .setPositiveButton("Proceder al Pago", (dialog, which) -> {
                        finish();
                    });
            builder.show();
            return;
        } else {
            builder.setTitle("Subasta Finalizada")
                    .setMessage("El ítem fue vendido a otro postor por USD " + cierre.getImporteFinal());
        }

        builder.setPositiveButton("Volver", (dialog, which) -> finish());
        builder.show();
    }

    private void salirYFinalizar() {
        if (mStompClient != null && mStompClient.isConnected()) mStompClient.disconnect();
        if (countDownTimer != null) countDownTimer.cancel();

        api.salirSubasta(tokenJwt, subastaId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) { finish(); }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { finish(); }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        salirYFinalizar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null) compositeDisposable.dispose();
    }
}