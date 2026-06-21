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

public class CuentaDestinoConsignacionActivity extends AppCompatActivity {

    private Integer consignacionId;
    private TokenManager tokenManager;
    private SubastarApi api;
    private EditText etCbu;
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
        etCbu = findViewById(R.id.etCbu);
        btnAgregar = findViewById(R.id.btnAgregarCbu);
        btnVolverMis = findViewById(R.id.btnVolverMisConsignaciones);

        btnVolver.setOnClickListener(v -> finish());
        btnAgregar.setOnClickListener(v -> agregarCbu());
        btnVolverMis.setOnClickListener(v -> volverMisConsignaciones());
    }

    private void agregarCbu() {
        String cbu = etCbu.getText().toString().trim();
        if (cbu.length() < 8) {
            etCbu.setError("Ingresa un CBU valido");
            return;
        }
        btnAgregar.setEnabled(false);
        CuentaDestinoRequest request = new CuentaDestinoRequest("Banco informado", cbu, "Argentina", "ARS");
        api.registrarCuentaDestino("Bearer " + tokenManager.getToken(), consignacionId, request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnAgregar.setEnabled(true);
                if (response.isSuccessful()) {
                    btnAgregar.setText("CBU agregado ✓");
                    btnVolverMis.setVisibility(android.view.View.VISIBLE);
                } else {
                    Toast.makeText(CuentaDestinoConsignacionActivity.this, "No se pudo agregar el CBU.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnAgregar.setEnabled(true);
                Toast.makeText(CuentaDestinoConsignacionActivity.this, "Error de conexion.", Toast.LENGTH_LONG).show();
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
