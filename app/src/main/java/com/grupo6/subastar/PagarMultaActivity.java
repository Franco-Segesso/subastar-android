package com.grupo6.subastar;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.grupo6.subastar.dto.MedioPagoDTO;
import com.grupo6.subastar.dto.PagarMultaRequest;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PagarMultaActivity extends AppCompatActivity {

    private final List<RadioButton> opciones = new ArrayList<>();
    private TokenManager tokenManager;
    private SubastarApi api;
    private LinearLayout contenedorMedios;
    private Button btnPagar;
    private MedioPagoDTO medioSeleccionado;
    private int multaId;
    private double importe;
    private String moneda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagar_multa);

        tokenManager = new TokenManager(this);
        api = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubastarApi.class);

        multaId = getIntent().getIntExtra("MULTA_ID", -1);
        importe = getIntent().getDoubleExtra("MULTA_IMPORTE", 0);
        String subasta = getIntent().getStringExtra("MULTA_SUBASTA");
        moneda = subasta != null && subasta.endsWith("(USD)") ? "USD" : "ARS";

        findViewById(R.id.btnVolverPagarMulta).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvPagoMultaSubasta))
                .setText(limpiarSubasta(subasta));
        ((TextView) findViewById(R.id.tvPagoMultaImporte))
                .setText(formatear(importe));
        ((TextView) findViewById(R.id.tvPagoMultaResumen))
                .setText("Multa                         " + formatear(importe)
                        + "\nComision                    " + formatear(0)
                        + "\n\nTOTAL                        " + formatear(importe));

        contenedorMedios = findViewById(R.id.contenedorMediosMulta);
        btnPagar = findViewById(R.id.btnConfirmarPagoMulta);
        btnPagar.setText("PAGAR MULTA - " + formatear(importe));
        btnPagar.setOnClickListener(v -> pagar());

        cargarMedios();
    }

    private void cargarMedios() {
        api.obtenerMediosPagoAutenticado("Bearer " + tokenManager.getToken())
                .enqueue(new Callback<List<MedioPagoDTO>>() {
                    @Override
                    public void onResponse(
                            Call<List<MedioPagoDTO>> call,
                            Response<List<MedioPagoDTO>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(PagarMultaActivity.this,
                                    "No se pudieron cargar los medios de pago.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        for (MedioPagoDTO medio : response.body()) {
                            if (esCompatible(medio)) agregarOpcion(medio);
                        }
                        if (opciones.isEmpty()) {
                            findViewById(R.id.tvSinMediosMulta)
                                    .setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<MedioPagoDTO>> call, Throwable t) {
                        Toast.makeText(PagarMultaActivity.this,
                                "Error de conexion.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void agregarOpcion(MedioPagoDTO medio) {
        RadioButton radio = new RadioButton(this);
        radio.setText(descripcion(medio));
        radio.setTextColor(getColor(R.color.texto_ppal));
        radio.setTextSize(15);
        radio.setButtonTintList(getColorStateList(R.color.secundario));
        radio.setPadding(dp(12), 0, dp(12), 0);

        GradientDrawable fondo = new GradientDrawable();
        fondo.setColor(getColor(R.color.fondo_sec_b));
        fondo.setCornerRadius(dp(8));
        fondo.setStroke(dp(1), getColor(R.color.bordes));
        radio.setBackground(fondo);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58));
        params.setMargins(0, 0, 0, dp(12));
        radio.setLayoutParams(params);
        radio.setOnClickListener(v -> seleccionar(radio, medio));
        opciones.add(radio);
        contenedorMedios.addView(radio);
    }

    private void seleccionar(RadioButton seleccionado, MedioPagoDTO medio) {
        medioSeleccionado = medio;
        for (RadioButton opcion : opciones) {
            opcion.setChecked(opcion == seleccionado);
        }
    }

    private void pagar() {
        if (medioSeleccionado == null) {
            Toast.makeText(this,
                    "Selecciona un medio de pago.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        btnPagar.setEnabled(false);
        api.pagarMulta(
                "Bearer " + tokenManager.getToken(),
                multaId,
                new PagarMultaRequest(medioSeleccionado.getIdentificador()))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response) {
                        btnPagar.setEnabled(true);
                        if (response.isSuccessful()) {
                            mostrarExito();
                        } else {
                            Toast.makeText(PagarMultaActivity.this,
                                    response.code() == 400
                                            ? "El medio seleccionado no tiene fondos suficientes."
                                            : "No se pudo pagar la multa.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        btnPagar.setEnabled(true);
                        Toast.makeText(PagarMultaActivity.this,
                                "Error de conexion.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarExito() {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_exito);
        dialog.getWindow().setBackgroundDrawable(
                new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        TextView mensaje = dialog.findViewById(R.id.tvMensajeExito);
        mensaje.setText(
                "La multa fue pagada correctamente.\n\n"
                        + "La compra original continua pendiente. "
                        + "El reloj de 72 horas no se reinicio. Debes abonarla "
                        + "antes del mismo vencimiento para evitar "
                        + "que el caso sea derivado a instancia judicial.\n\n"
                        + "En Mis Multas encontraras el boton para ir directamente al pago.");
        dialog.findViewById(R.id.btnAceptarExito).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        dialog.show();
    }

    private boolean esCompatible(MedioPagoDTO medio) {
        if (medio == null || !"si".equalsIgnoreCase(medio.getActivo())) return false;
        if ("cuenta".equalsIgnoreCase(medio.getTipo())
                || "cheque".equalsIgnoreCase(medio.getTipo())) {
            return moneda.equalsIgnoreCase(medio.getMoneda());
        }
        return !"USD".equals(moneda)
                || "si".equalsIgnoreCase(medio.getEsExtranjera());
    }

    private String descripcion(MedioPagoDTO medio) {
        if ("tarjeta".equalsIgnoreCase(medio.getTipo())) {
            return "Tarjeta ****" + medio.getUltimosDigitos();
        }
        if ("cuenta".equalsIgnoreCase(medio.getTipo())) {
            return "Cuenta bancaria " + valor(medio.getBanco())
                    + " (" + valor(medio.getMoneda()) + ")";
        }
        return "Cheque certificado (" + valor(medio.getMoneda()) + ")";
    }

    private String valor(String texto) {
        return texto == null ? "" : texto;
    }

    private String limpiarSubasta(String subasta) {
        if (subasta == null) return "";
        return "Subasta: " + subasta.replace(" (USD)", "").replace(" (ARS)", "");
    }

    private String formatear(double valor) {
        NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "AR"));
        return moneda + " " + formato.format(valor);
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
