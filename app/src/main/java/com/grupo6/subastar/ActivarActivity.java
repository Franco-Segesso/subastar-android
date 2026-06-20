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
                com.grupo6.subastar.util.DialogUtils.mostrarError(this, "Complete los campos obligatorios");
                return;
            }

            // 2. Regla: Mínimo 8 caracteres, 1 número, 1 minúscula, 1 mayúscula, 1 símbolo.
            String patronPassword = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*._-]).{8,}$";

            if (!clave.matches(patronPassword)) {
                com.grupo6.subastar.util.DialogUtils.mostrarError(this, "La clave debe tener mín. 8 caracteres, una mayúscula, una minúscula, un número y un símbolo especial (!@#$...).");
                return;
            }

            // 3. Validar que coincidan
            if (!clave.equals(confirmar)) {
                com.grupo6.subastar.util.DialogUtils.mostrarError(this, "Las contraseñas no coinciden");
                return;
            }

            btnActivar.setEnabled(false);
            btnActivar.setText("Procesando...");

            ActivarRequest request = new ActivarRequest(email, clave, confirmar);

            api.activarCuenta(request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        // MODAL DE ÉXITO (al cerrarlo ejecuta el finish() para volver al Login)
                        com.grupo6.subastar.util.DialogUtils.mostrarExito(ActivarActivity.this, "¡Cuenta activada! Ya podés iniciar sesión.", () -> finish());
                    } else {
                        btnActivar.setEnabled(true);
                        btnActivar.setText("Generar Clave y Activar");
                        try {
                            com.grupo6.subastar.util.DialogUtils.mostrarError(ActivarActivity.this, "Error: " + response.errorBody().string());
                        } catch (Exception e) {}
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    btnActivar.setEnabled(true);
                    btnActivar.setText("Generar Clave y Activar");
                    com.grupo6.subastar.util.DialogUtils.mostrarError(ActivarActivity.this, "Falla de red. Verifica tu conexión a internet.");
                }
            });
        });
    }
}