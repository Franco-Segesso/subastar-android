package com.grupo6.subastar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.grupo6.subastar.dto.ActivarRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activar);

        EditText etEmail = findViewById(R.id.etEmailActivar);
        EditText etClaveNueva = findViewById(R.id.etClaveNueva);
        EditText etClaveConfirmar = findViewById(R.id.etClaveConfirmar);
        Button btnActivar = findViewById(R.id.btnActivarCuenta);
        ImageButton btnVolver = findViewById(R.id.btnVolver);

        // Enlazar flecha de retroceso
        btnVolver.setOnClickListener(v -> finish());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SubastarApi api = retrofit.create(SubastarApi.class);

        btnActivar.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String clave = etClaveNueva.getText().toString();
            String confirmar = etClaveConfirmar.getText().toString();

            // 1. Validar que no estén vacíos
            if (email.isEmpty() || clave.isEmpty()) {
                Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. VALIDACIÓN DE SEGURIDAD (REGEX)
            // Regla: Mínimo 8 caracteres, 1 número, 1 minúscula, 1 mayúscula, 1 símbolo.
            String patronPassword = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*._-]).{8,}$";

            if (!clave.matches(patronPassword)) {
                Toast.makeText(this, "La clave debe tener mín. 8 caracteres, una mayúscula, una minúscula, un número y un símbolo especial (!@#$...).", Toast.LENGTH_LONG).show();
                return;
            }

            // 3. Validar que coincidan
            if (!clave.equals(confirmar)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            btnActivar.setEnabled(false);
            btnActivar.setText("Procesando...");

            ActivarRequest request = new ActivarRequest(email, clave, confirmar);

            api.activarCuenta(request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ActivarActivity.this, "¡Cuenta activada! Ya podés iniciar sesión.", Toast.LENGTH_LONG).show();
                        finish(); // Cierra y vuelve al Login
                    } else {
                        btnActivar.setEnabled(true);
                        btnActivar.setText("Generar Clave y Activar");
                        try {
                            Toast.makeText(ActivarActivity.this, "Error: " + response.errorBody().string(), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {}
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    btnActivar.setEnabled(true);
                    btnActivar.setText("Generar Clave y Activar");
                    Toast.makeText(ActivarActivity.this, "Falla de red", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}