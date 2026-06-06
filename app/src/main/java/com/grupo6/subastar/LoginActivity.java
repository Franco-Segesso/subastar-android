package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.grupo6.subastar.dto.LoginRequest;
import com.grupo6.subastar.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etClave;
    private Button btnLogin;
    private TextView tvActivarCuenta; // Agregamos la variable
    private TokenManager tokenManager;
    private SubastarApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Enlazamos las vistas del XML con Java
        etEmail = findViewById(R.id.etEmail);
        etClave = findViewById(R.id.etClave);
        btnLogin = findViewById(R.id.btnLogin);
        tvActivarCuenta = findViewById(R.id.tvActivarCuenta); // Enlazamos el texto nuevo

        // 2. Inicializamos nuestra "bóveda" de seguridad
        tokenManager = new TokenManager(this);

        // 3. Configuramos Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SubastarApi.class);

        // 4. Capturamos el clic del botón de login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarLogin();
            }
        });

        // 5. NUEVO: Capturamos el clic para ir a Activar Cuenta
        tvActivarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ActivarActivity.class);
                startActivity(intent);
            }
        });
    }

    private void realizarLogin() {
        String email = etEmail.getText().toString().trim();
        String clave = etClave.getText().toString().trim();

        if (email.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Conectando...");

        LoginRequest request = new LoginRequest(email, clave);

        api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar Sesión");

                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    tokenManager.saveToken(token);

                    // 1. Obtenemos el objeto cliente completo desde la respuesta
                    com.grupo6.subastar.dto.ClienteDTO clienteLogueado = response.body().getCliente();

                    // Armamos el nombre y apellido juntos
                    String nombreCompleto = clienteLogueado.getNombre() + " " + clienteLogueado.getApellido();

                    // 2. Guardamos TODOS los datos en la memoria del celular
                    getSharedPreferences("SubastarPrefs", MODE_PRIVATE)
                            .edit()
                            .putInt("USER_ID", clienteLogueado.getIdentificador())
                            .putString("USER_NAME", nombreCompleto)
                            .putString("USER_EMAIL", clienteLogueado.getEmail())
                            .putString("USER_CATEGORIA", clienteLogueado.getCategoria())
                            .apply();

                    Toast.makeText(LoginActivity.this, "¡Éxito! Bienvenido, " + clienteLogueado.getNombre(), Toast.LENGTH_LONG).show();

                    //viajamos al home
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(LoginActivity.this, "Credenciales incorrectas o usuario bloqueado", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar Sesión");
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}