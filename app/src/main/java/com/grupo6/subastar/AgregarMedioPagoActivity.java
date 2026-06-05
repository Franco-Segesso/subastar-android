package com.grupo6.subastar;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.grupo6.subastar.dto.AgregarChequeRequest;
import com.grupo6.subastar.dto.AgregarCuentaRequest;
import com.grupo6.subastar.dto.AgregarTarjetaRequest;
import java.math.BigDecimal;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AgregarMedioPagoActivity extends AppCompatActivity {

    private LinearLayout formTarjeta, formCuenta, formCheque;
    private TextView tabTarjeta, tabCuenta, tabCheque;
    private String tipoSeleccionado = "tarjeta";
    private Integer clienteId;
    private SubastarApi api;
    private TokenManager tokenManager;

    // Campos tarjeta
    private EditText etTitular, etUltimosDigitos, etVencimiento, etPaisEmisor;
    private Spinner spinnerExtranjera;
    private View labelPaisEmisor;

    // Campos cuenta
    private EditText etCbuIban, etAlias, etBancoCuenta, etPaisBanco;
    private Spinner spinnerMonedaCuenta;

    // Campos cheque
    private EditText etNroCheque, etBancoCheque, etMontoGarantia, etFechaEntrega;
    private Spinner spinnerMonedaCheque;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_medio_pago);

        tokenManager = new TokenManager(this);
        clienteId = getIntent().getIntExtra("clienteId", -1);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(SubastarApi.class);

        // Vistas
        formTarjeta = findViewById(R.id.formTarjeta);
        formCuenta  = findViewById(R.id.formCuenta);
        formCheque  = findViewById(R.id.formCheque);
        tabTarjeta  = findViewById(R.id.tabTarjeta);
        tabCuenta   = findViewById(R.id.tabCuenta);
        tabCheque   = findViewById(R.id.tabCheque);

        // Campos tarjeta
        etTitular        = findViewById(R.id.etTitular);
        etUltimosDigitos = findViewById(R.id.etUltimosDigitos);
        etVencimiento    = findViewById(R.id.etVencimiento);
        etPaisEmisor     = findViewById(R.id.etPaisEmisor);
        labelPaisEmisor  = findViewById(R.id.labelPaisEmisor);
        spinnerExtranjera = findViewById(R.id.spinnerExtranjera);

        ArrayAdapter<String> adapterSiNo = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"no", "si"});
        adapterSiNo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExtranjera.setAdapter(adapterSiNo);

        // Mostrar/ocultar campo País emisor según si es extranjera
        spinnerExtranjera.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                boolean esExtranjera = position == 1; // "si" está en posición 1
                labelPaisEmisor.setVisibility(esExtranjera ? View.VISIBLE : View.GONE);
                etPaisEmisor.setVisibility(esExtranjera ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Campos cuenta
        etCbuIban         = findViewById(R.id.etCbuIban);
        etAlias           = findViewById(R.id.etAlias);
        etBancoCuenta     = findViewById(R.id.etBancoCuenta);
        etPaisBanco       = findViewById(R.id.etPaisBanco);
        spinnerMonedaCuenta = findViewById(R.id.spinnerMonedaCuenta);

        // Campos cheque
        etNroCheque       = findViewById(R.id.etNroCheque);
        etBancoCheque     = findViewById(R.id.etBancoCheque);
        etMontoGarantia   = findViewById(R.id.etMontoGarantia);
        etFechaEntrega    = findViewById(R.id.etFechaEntrega);
        spinnerMonedaCheque = findViewById(R.id.spinnerMonedaCheque);

        // Spinner monedas
        ArrayAdapter<String> adapterMoneda = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"ARS", "USD", "EUR"});
        adapterMoneda.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonedaCuenta.setAdapter(adapterMoneda);
        spinnerMonedaCheque.setAdapter(adapterMoneda);

        // Tabs
        tabTarjeta.setOnClickListener(v -> seleccionarTab("tarjeta"));
        tabCuenta.setOnClickListener(v -> seleccionarTab("cuenta"));
        tabCheque.setOnClickListener(v -> seleccionarTab("cheque"));

        // Volver
        ImageButton btnVolver = findViewById(R.id.btnVolverAgregar);
        btnVolver.setOnClickListener(v -> finish());

        // Guardar
        Button btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> guardar());
    }

    private void seleccionarTab(String tipo) {
        tipoSeleccionado = tipo;

        // Reset todos los tabs
        tabTarjeta.setBackgroundResource(R.drawable.bg_chip_inactivo);
        tabTarjeta.setTextColor(0xFFAAAAAA);
        tabCuenta.setBackgroundResource(R.drawable.bg_chip_inactivo);
        tabCuenta.setTextColor(0xFFAAAAAA);
        tabCheque.setBackgroundResource(R.drawable.bg_chip_inactivo);
        tabCheque.setTextColor(0xFFAAAAAA);

        // Activar el seleccionado
        TextView tabActivo = tipo.equals("tarjeta") ? tabTarjeta : tipo.equals("cuenta") ? tabCuenta : tabCheque;
        tabActivo.setBackgroundResource(R.drawable.bg_chip_activo);
        tabActivo.setTextColor(0xFFC8A951);

        // Mostrar formulario correcto
        formTarjeta.setVisibility(tipo.equals("tarjeta") ? View.VISIBLE : View.GONE);
        formCuenta.setVisibility(tipo.equals("cuenta")   ? View.VISIBLE : View.GONE);
        formCheque.setVisibility(tipo.equals("cheque")   ? View.VISIBLE : View.GONE);
    }

    private void guardar() {
        String token = "Bearer " + tokenManager.getToken();

        if (tipoSeleccionado.equals("tarjeta")) {
            String titular  = etTitular.getText().toString().trim();
            String digitos  = etUltimosDigitos.getText().toString().trim();
            String vence    = etVencimiento.getText().toString().trim();
            String extran   = spinnerExtranjera.getSelectedItem().toString();
            String pais     = etPaisEmisor.getText().toString().trim();

            if (titular.isEmpty() || digitos.isEmpty() || vence.isEmpty()) {
                Toast.makeText(this, "Completá todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            AgregarTarjetaRequest req = new AgregarTarjetaRequest(digitos, vence, titular, extran,
                    extran.equals("si") ? pais : null);

            api.agregarTarjeta(clienteId, token, req).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    manejarRespuesta(response);
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(AgregarMedioPagoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });

        } else if (tipoSeleccionado.equals("cuenta")) {
            String cbu   = etCbuIban.getText().toString().trim();
            String banco = etBancoCuenta.getText().toString().trim();
            String moneda = spinnerMonedaCuenta.getSelectedItem().toString();

            if (cbu.isEmpty() || banco.isEmpty()) {
                Toast.makeText(this, "CBU/IBAN y Banco son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            AgregarCuentaRequest req = new AgregarCuentaRequest(cbu,
                    etAlias.getText().toString().trim(), banco,
                    etPaisBanco.getText().toString().trim(), moneda);

            api.agregarCuenta(clienteId, token, req).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    manejarRespuesta(response);
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(AgregarMedioPagoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            String nro   = etNroCheque.getText().toString().trim();
            String banco = etBancoCheque.getText().toString().trim();
            String monto = etMontoGarantia.getText().toString().trim();
            String fecha = etFechaEntrega.getText().toString().trim();
            String moneda = spinnerMonedaCheque.getSelectedItem().toString();

            if (nro.isEmpty() || banco.isEmpty() || monto.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Completá todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            AgregarChequeRequest req = new AgregarChequeRequest(nro, banco, moneda,
                    new BigDecimal(monto), fecha);

            api.agregarCheque(clienteId, token, req).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    manejarRespuesta(response);
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(AgregarMedioPagoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void manejarRespuesta(Response<?> response) {
        if (response.isSuccessful()) {
            Toast.makeText(this, "Método de pago agregado correctamente", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Error al guardar. Revisá los datos.", Toast.LENGTH_SHORT).show();
        }
    }
}