package com.grupo6.subastar;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.grupo6.subastar.dto.RegistroRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        EditText etNombre = findViewById(R.id.etNombre);
        EditText etApellido = findViewById(R.id.etApellido);
        EditText etDoc = findViewById(R.id.etDocumento);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etClave = findViewById(R.id.etClave);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);

        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://10.0.2.2:8080/").addConverterFactory(GsonConverterFactory.create()).build();
        SubastarApi api = retrofit.create(SubastarApi.class);

        btnRegistrar.setOnClickListener(v -> {
            RegistroRequest req = new RegistroRequest(
                    etNombre.getText().toString(), etApellido.getText().toString(),
                    etEmail.getText().toString(), etClave.getText().toString(), etDoc.getText().toString()
            );

            api.registrar(req).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(RegistroActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                        finish(); // Cierra el registro y vuelve al login
                    } else {
                        // Esto va a mostrar el mensaje real que lanza tu AuthService (ej: "El email ya está registrado")
                        try {
                            String errorMsg = response.errorBody().string();
                            Toast.makeText(RegistroActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(RegistroActivity.this, "Error desconocido", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(RegistroActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}