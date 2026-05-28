package com.grupo6.subastar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private TokenManager tokenManager;
    private SubastarApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ACA ESTÁ EL CAMBIO: Apuntamos al XML que renombramos
        setContentView(R.layout.activity_login);

        // 1. Enlazamos las vistas del XML con Java
        etEmail = findViewById(R.id.etEmail);
        etClave = findViewById(R.id.etClave);
        btnLogin = findViewById(R.id.btnLogin);

        // BORRAMOS EL BOTÓN DE REGISTRO PORQUE AHORA ESTÁ EN EL WELCOME

        // 2. Inicializamos nuestra "bóveda" de seguridad
        tokenManager = new TokenManager(this);

        // 3. Configuramos Retrofit. Ojo a la IP: 10.0.2.2 es el localhost de tu PC visto desde el emulador
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SubastarApi.class);

        // 4. Capturamos el clic del botón
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarLogin();
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

        // Deshabilitamos el botón mientras hace la llamada a la red
        btnLogin.setEnabled(false);
        btnLogin.setText("Conectando...");

        LoginRequest request = new LoginRequest(email, clave);

        api.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar Sesión");

                if (response.isSuccessful() && response.body() != null) {
                    // ¡LOGIN EXITOSO (HTTP 200)!
                    String token = response.body().getToken();

                    // Guardamos el token en la bóveda encriptada
                    tokenManager.saveToken(token);

                    // Saludamos al usuario con su nombre
                    String nombre = response.body().getCliente().getNombre();
                    Toast.makeText(LoginActivity.this, "¡Éxito! Bienvenido, " + nombre, Toast.LENGTH_LONG).show();

                    // Nota: A futuro, acá harías un Intent para saltar a la pantalla de Subastas
                } else {
                    // ERROR HTTP 401 o 403
                    Toast.makeText(LoginActivity.this, "Credenciales incorrectas o usuario bloqueado", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar Sesión");

                // Si llegamos acá, el servidor está apagado o no hay internet en el emulador
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}