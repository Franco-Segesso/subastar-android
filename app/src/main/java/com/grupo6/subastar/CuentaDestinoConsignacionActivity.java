package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.grupo6.subastar.dto.CuentaDestinoRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;
public class CuentaDestinoConsignacionActivity extends AppCompatActivity {

    private Integer consignacionId;
    private TokenManager tokenManager;
    private SubastarApi api;
    private EditText etBanco, etCbu;
    private Spinner spinnerPais, spinnerMoneda;
    private List<Pais> paises = new ArrayList<>();
    private Button btnAgregar, btnVolverMis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta_destino_consignacion);

        consignacionId = getIntent().getIntExtra("CONSIGNACION_ID", -1);
        tokenManager = new TokenManager(this);
        api = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubastarApi.class);

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        etBanco = findViewById(R.id.etBanco);
        etCbu = findViewById(R.id.etCbu);
        spinnerPais = findViewById(R.id.spinnerPais);
        spinnerMoneda = findViewById(R.id.spinnerMoneda);
        btnAgregar = findViewById(R.id.btnAgregarCbu);
        btnVolverMis = findViewById(R.id.btnVolverMisConsignaciones);

        ArrayAdapter<String> adapterMoneda = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"ARS", "USD"}
        );
        adapterMoneda.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMoneda.setAdapter(adapterMoneda);
        cargarPaises();

        btnVolver.setOnClickListener(v -> finish());
        btnAgregar.setOnClickListener(v -> agregarCbu());
        btnVolverMis.setOnClickListener(v -> volverMisConsignaciones());
    }

    private void cargarPaises() {
        api.getPaises().enqueue(new Callback<List<Pais>>() {
            @Override
            public void onResponse(Call<List<Pais>> call, Response<List<Pais>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    paises.clear();
                    paises.addAll(response.body());

                    List<String> nombresPaises = new ArrayList<>();
                    for (Pais pais : paises) {
                        nombresPaises.add(pais.getNombre());
                    }

                    ArrayAdapter<String> adapterPais = new ArrayAdapter<>(
                            CuentaDestinoConsignacionActivity.this,
                            android.R.layout.simple_spinner_item,
                            nombresPaises
                    );
                    adapterPais.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPais.setAdapter(adapterPais);
                } else {
                    Toast.makeText(
                            CuentaDestinoConsignacionActivity.this,
                            "No se pudieron cargar los países",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<List<Pais>> call, Throwable t) {
                Toast.makeText(
                        CuentaDestinoConsignacionActivity.this,
                        "Error cargando países",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void agregarCbu() {
        String banco = etBanco.getText().toString().trim();
        String cbu = etCbu.getText().toString().trim();
        String moneda = spinnerMoneda.getSelectedItem().toString();

        if (banco.isEmpty()) {
            etBanco.setError("Ingresá el banco");
            return;
        }

        if (cbu.length() < 8) {
            etCbu.setError("Ingresá un CBU o IBAN válido");
            return;
        }

        if (paises.isEmpty() || spinnerPais.getSelectedItem() == null) {
            Toast.makeText(this, "Seleccioná un país", Toast.LENGTH_SHORT).show();
            return;
        }

        String pais = spinnerPais.getSelectedItem().toString();

        if (!"ARS".equals(moneda) && !"USD".equals(moneda)) {
            Toast.makeText(this, "La moneda debe ser ARS o USD", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAgregar.setEnabled(false);

        CuentaDestinoRequest request = new CuentaDestinoRequest(banco, cbu, pais, moneda);

        api.registrarCuentaDestino("Bearer " + tokenManager.getToken(), consignacionId, request)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        btnAgregar.setEnabled(true);

                        if (response.isSuccessful()) {
                            btnAgregar.setText("Cuenta guardada ✓");
                            btnAgregar.setEnabled(false);
                            btnVolverMis.setVisibility(android.view.View.VISIBLE);

                            com.grupo6.subastar.util.DialogUtils.mostrarExito(
                                    CuentaDestinoConsignacionActivity.this,
                                    "Cuenta destino guardada exitosamente.",
                                    null
                            );
                        } else if (response.code() == 409) {
                            com.grupo6.subastar.util.DialogUtils.mostrarError(
                                    CuentaDestinoConsignacionActivity.this,
                                    "No se puede registrar la cuenta. Puede que ya exista o que la subasta ya haya iniciado."
                            );
                        } else if (response.code() == 403) {
                            com.grupo6.subastar.util.DialogUtils.mostrarError(
                                    CuentaDestinoConsignacionActivity.this,
                                    "La consignación no pertenece a tu usuario."
                            );
                        } else {
                            com.grupo6.subastar.util.DialogUtils.mostrarError(
                                    CuentaDestinoConsignacionActivity.this,
                                    "No se pudo agregar la cuenta destino."
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        btnAgregar.setEnabled(true);
                        com.grupo6.subastar.util.DialogUtils.mostrarError(
                                CuentaDestinoConsignacionActivity.this,
                                "Error de conexión. Revisa tu internet."
                        );
                    }
                });
    }

    private void volverMisConsignaciones() {
        Intent intent = new Intent(this, MisConsignacionesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
