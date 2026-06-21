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
import com.grupo6.subastar.dto.MedioPagoDTO;

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
    private TextView btnSeleccionarMedioPuja;
    private View layoutEnVivo;

    private PujaHistorialAdapter adapter;
    private StompClient mStompClient;
    private CompositeDisposable compositeDisposable;
    private Gson gson;

    private Integer subastaId;
    private Integer itemId;
    private Integer miClienteId;
    private String monedaSubasta;
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
    private final List<MedioPagoDTO> mediosPago = new ArrayList<>();
    private MedioPagoDTO medioPagoSeleccionado;
    private boolean itemVendidoExtra = false;

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
        monedaSubasta = getIntent().getStringExtra("SUBASTA_MONEDA");
        if (monedaSubasta == null || monedaSubasta.isBlank()) {
            monedaSubasta = "ARS";
        }
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
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SubastarApi.class);

        itemVendidoExtra = getIntent().getBooleanExtra("ITEM_VENDIDO", false);

        configurarRecyclerView();
        configurarBotones();
        configurarNavegacionAtras();
        cargarMediosPago();
        if (itemVendidoExtra) {
            prepararSalaModoLectura();
            cargarHistorialPujas();
        } else {
            ingresarSalaBackend();
        }
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
        btnSeleccionarMedioPuja = findViewById(R.id.btnSeleccionarMedioPuja);
        layoutEnVivo = findViewById(R.id.layoutEnVivo);

        tvBannerEstado.setVisibility(View.GONE);
        habilitarPuja(false);
    }

    private void cargarDatosUI() {
        String titulo = getIntent().getStringExtra("ITEM_TITULO");
        double precioBase = getIntent().getDoubleExtra("ITEM_BASE", 0);
        String urlImagen = getIntent().getStringExtra("ITEM_IMAGEN");
        String fechaSubasta = getIntent().getStringExtra("SUBASTA_FECHA");

        if (titulo != null) tvHeaderTitle.setText("Sala de puja - " + titulo);
        if (precioBase > 0) {
            tvBase.setText(String.format("Base: %s %.2f", monedaSubasta, precioBase));
            tvOfertaActual.setText(String.format("%s %.2f", monedaSubasta, precioBase));
        }
        if (urlImagen != null && !urlImagen.isEmpty()) {
            Glide.with(this).load(urlImagen).centerCrop().into(ivItemImagen);
        }

        if (fechaSubasta != null && !fechaSubasta.isEmpty()) {
            tvHeaderSubtitle.setText("Subasta - " + fechaSubasta);
        } else {
            tvHeaderSubtitle.setText("Subasta - Fecha a confirmar");
        }
    }

    private void configurarRecyclerView() {
        adapter = new PujaHistorialAdapter(miClienteId, monedaSubasta, itemVendidoExtra);
        rvHistorialPujas.setLayoutManager(new LinearLayoutManager(this));
        rvHistorialPujas.setAdapter(adapter);
    }

    private void configurarBotones() {
        btnVolver.setOnClickListener(v -> salirYFinalizar());
        btnSeleccionarMedioPuja.setOnClickListener(v -> mostrarSelectorMediosPago());

        btnPujar.setOnClickListener(v -> {
            if (!itemActivo) {
                mostrarDialogoError("Este ítem no está activo para pujar.");
                return;
            }

            if (medioPagoSeleccionado == null) {
                mostrarDialogoError("Selecciona un medio de pago antes de pujar.");
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
        PujaRequest request = new PujaRequest(
                itemId,
                monto,
                medioPagoSeleccionado.getIdentificador());

        api.registrarPuja(tokenJwt, subastaId, request).enqueue(new Callback<PujaMensajeDTO>() {
            @Override
            public void onResponse(Call<PujaMensajeDTO> call, Response<PujaMensajeDTO> response) {
                habilitarPuja(itemActivo);
                if (response.isSuccessful()) {
                    etMontoPuja.setText("");
                    mostrarModalPujaExitosa();
                } else if (response.code() == 400) {
                    mostrarDialogoError("Oferta rechazada: el monto no supera a la oferta actual.");
                } else if (response.code() == 409) {
                    mostrarDialogoError("Oferta rechazada: supera el límite máximo permitido (20% de la base).");
                } else if (response.code() == 422) {
                    mostrarDialogoError("El ítem ya fue subastado.");
                } else {
                    mostrarDialogoError(leerError(response));
                }
            }

            @Override
            public void onFailure(Call<PujaMensajeDTO> call, Throwable t) {
                habilitarPuja(itemActivo);
                mostrarDialogoError("Error de red al intentar enviar tu puja.");
            }
        });
    }

    private void cargarMediosPago() {
        api.obtenerMediosPago(miClienteId, tokenJwt)
                .enqueue(new Callback<List<MedioPagoDTO>>() {
                    @Override
                    public void onResponse(
                            Call<List<MedioPagoDTO>> call,
                            Response<List<MedioPagoDTO>> response) {
                        mediosPago.clear();
                        if (response.isSuccessful() && response.body() != null) {
                            for (MedioPagoDTO medio : response.body()) {
                                if ("si".equalsIgnoreCase(medio.getActivo())) {
                                    mediosPago.add(medio);
                                }
                            }
                            if (mediosPago.size() == 1) {
                                seleccionarMedio(mediosPago.get(0));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<MedioPagoDTO>> call, Throwable t) {
                        Log.e("MEDIOS_PUJA", "No se pudieron cargar los medios", t);
                    }
                });
    }

    private void mostrarSelectorMediosPago() {
        if (mediosPago.isEmpty()) {
            mostrarDialogoError("No tenes medios de pago activos disponibles.");
            return;
        }
        String[] opciones = new String[mediosPago.size()];
        for (int i = 0; i < mediosPago.size(); i++) {
            opciones[i] = descripcionMedio(mediosPago.get(i));
        }
        new AlertDialog.Builder(this)
                .setTitle("Seleccionar medio de pago")
                .setItems(opciones, (dialog, posicion) ->
                        seleccionarMedio(mediosPago.get(posicion)))
                .show();
    }

    private void seleccionarMedio(MedioPagoDTO medio) {
        medioPagoSeleccionado = medio;
        btnSeleccionarMedioPuja.setText(descripcionMedio(medio));
        btnSeleccionarMedioPuja.setTextColor(
                androidx.core.content.ContextCompat.getColor(this, R.color.secundario));
        btnSeleccionarMedioPuja.setBackgroundResource(R.drawable.bg_chip_activo);
    }

    private String descripcionMedio(MedioPagoDTO medio) {
        if (medio == null || medio.getTipo() == null) return "Medio de pago";
        if ("tarjeta".equalsIgnoreCase(medio.getTipo())) {
            return "Tarjeta terminada en " + valorTexto(medio.getUltimosDigitos());
        }
        if ("cuenta".equalsIgnoreCase(medio.getTipo())) {
            return "Cuenta " + valorTexto(medio.getBanco())
                    + " - " + valorTexto(medio.getMoneda())
                    + " " + valorNumero(medio.getFondosReservados());
        }
        return "Cheque " + valorTexto(medio.getNroCheque())
                + " - " + valorTexto(medio.getMoneda())
                + " " + valorNumero(medio.getMontoGarantia());
    }

    private String valorTexto(String valor) {
        return valor == null || valor.isBlank() ? "--" : valor;
    }

    private String valorNumero(Double valor) {
        return String.format(java.util.Locale.US, "%.2f", valor == null ? 0.0 : valor);
    }

    private void conectarWebSocket() {
        String wsUrl = BuildConfig.WS_URL;
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
            tvOfertaActual.setText(String.format("%s %.2f", monedaSubasta, estado.getImporteActual()));
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
        tvOfertaActual.setText(String.format("%s %.2f", monedaSubasta, puja.getImporte()));
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
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_exito);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        android.widget.TextView tvMensaje = dialog.findViewById(R.id.tvMensajeExito);
        tvMensaje.setText("Actualmente eres el mayor postor por este ítem.");

        android.widget.Button btnAceptar = dialog.findViewById(R.id.btnAceptarExito);
        btnAceptar.setText("Cerrar");
        btnAceptar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void mostrarModalResultado(com.grupo6.subastar.dto.CierreSubastaDTO cierre) {
        if (modalResultadoMostrado || cierre == null) return;
        if (cierre.getItemId() != null && !cierre.getItemId().equals(itemId)) return;

        modalResultadoMostrado = true;
        habilitarPuja(false);
        soyMayorPostor = false;
        detenerTickerVisual();

        if (mStompClient != null && mStompClient.isConnected()) mStompClient.disconnect();

        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_resultado); // <-- El XML nuevo
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false); // Obliga a tocar un botón para salir

        android.widget.ImageView ivIcono = dialog.findViewById(R.id.ivResultadoIcono);
        android.widget.TextView tvTitulo = dialog.findViewById(R.id.tvResultadoTitulo);
        android.widget.TextView tvMensaje = dialog.findViewById(R.id.tvResultadoMensaje);
        android.widget.Button btnSecundario = dialog.findViewById(R.id.btnResultadoSecundario);
        android.widget.Button btnPrincipal = dialog.findViewById(R.id.btnResultadoPrincipal);

        if (!cierre.isHayGanador()) {
            ivIcono.setImageResource(android.R.drawable.ic_dialog_info);
            tvTitulo.setText("Subasta Desierta");
            tvMensaje.setText("Nadie pujó por este ítem. El mismo será devuelto a su dueño.");
            btnPrincipal.setText("Volver al Catálogo");
            btnPrincipal.setOnClickListener(v -> { dialog.dismiss(); salirYNavegarAlCatalogo(); });

        } else if (miClienteId.equals(cierre.getIdClienteGanador())) {
            ivIcono.setImageResource(android.R.drawable.btn_star_big_on);
            tvTitulo.setText("¡FELICIDADES!\nGANASTE LA SUBASTA");
            tvMensaje.setText("Se registró tu compra por " + monedaSubasta + " " + cierre.getImporteFinal() + ".");

            btnSecundario.setVisibility(android.view.View.VISIBLE);
            btnSecundario.setText("Ver más ítems");
            btnSecundario.setOnClickListener(v -> { dialog.dismiss(); salirYNavegarAlCatalogo(); });

            if (cierre.getCompraId() != null) {
                btnPrincipal.setText("Ir al pago");
                btnPrincipal.setOnClickListener(v -> {
                    dialog.dismiss();
                    salirYNavegarAlPago(cierre.getCompraId());
                });
            } else {
                btnPrincipal.setText("Pagar luego");
                btnPrincipal.setOnClickListener(v -> {
                    dialog.dismiss();
                    salirYNavegarAlCatalogo();
                });
            }

        } else {
            ivIcono.setImageResource(android.R.drawable.ic_menu_recent_history);
            tvTitulo.setText("Subasta Finalizada");
            tvMensaje.setText("El ítem fue vendido a otro postor por " + monedaSubasta + " " + cierre.getImporteFinal());
            btnPrincipal.setText("Volver al Catálogo");
            btnPrincipal.setOnClickListener(v -> { dialog.dismiss(); salirYNavegarAlCatalogo(); });
        }

        dialog.show();
    }

    private void mostrarErrorIngreso(int codigo, String detalle) {
        String titulo = codigo == 403 ? "Categoría insuficiente\n" : "No se pudo ingresar\n";
        String mensaje = detalle != null && !detalle.isEmpty() ? detalle : "Intenta nuevamente más tarde.";

        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_error);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        android.widget.TextView tvMensaje = dialog.findViewById(R.id.tvMensajeError);
        tvMensaje.setText(titulo + mensaje);

        android.widget.Button btnEntendido = dialog.findViewById(R.id.btnEntendidoError);
        btnEntendido.setText("Salir de la sala");
        btnEntendido.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
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
        if (soyMayorPostor && !itemVendidoExtra) {
            mostrarModalMayorPostor();
            return;
        }
        salirDeSala(false);
    }

    private void salirYNavegarAlCatalogo() {
        salirDeSala(true);
    }

    private void salirYNavegarAlPago(Integer compraId) {
        if (saliendo) return;
        saliendo = true;
        api.salirSubasta(tokenJwt, subastaId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Intent intent = new Intent(
                        SalaPujaActivity.this,
                        FacturaCompraActivity.class);
                intent.putExtra("COMPRA_ID", compraId);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                saliendo = false;
                mostrarDialogoError(
                        "No se pudo salir de la sala para continuar al pago.");
            }
        });
    }

    private void salirDeSala(boolean navegarAlCatalogo) {
        if (saliendo) return;
        saliendo = true;

        if (itemVendidoExtra) {
            finalizarSalida(navegarAlCatalogo);
            return;
        }

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
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_error);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        android.widget.TextView tvMensaje = dialog.findViewById(R.id.tvMensajeError);

        tvMensaje.setText("No podés salir todavía.\n\nActualmente sos el mayor postor de este ítem. Para mantener la puja activa, tenés que esperar a que alguien te supere o a que finalice la subasta del ítem.");

        android.widget.Button btnEntendido = dialog.findViewById(R.id.btnEntendidoError);
        btnEntendido.setText("Entendido");
        btnEntendido.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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

    private void prepararSalaModoLectura() {
        etMontoPuja.setVisibility(View.GONE);
        btnPujar.setVisibility(View.GONE);
        btnSeleccionarMedioPuja.setVisibility(View.GONE);
        tvTemporizador.setVisibility(View.GONE);
        layoutEnVivo.setVisibility(View.GONE);

        String titulo = getIntent().getStringExtra("ITEM_TITULO");
        tvHeaderTitle.setText("Historial - " + titulo);

        tvBannerEstado.setText("Subasta Finalizada - Historial de Pujas");
        tvBannerEstado.setVisibility(View.VISIBLE);
    }
}
