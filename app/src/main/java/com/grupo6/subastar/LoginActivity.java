package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.grupo6.subastar.dto.LoginRequest;
import com.grupo6.subastar.dto.LoginResponse;
import com.grupo6.subastar.util.DialogUtils;

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


        etEmail = findViewById(R.id.etEmail);
        etClave = findViewById(R.id.etClave);
        btnLogin = findViewById(R.id.btnLogin);
        tvActivarCuenta = findViewById(R.id.tvActivarCuenta); // Enlazamos el texto nuevo
        TextView tvIrARegistro = findViewById(R.id.tvIrARegistro);
        ImageButton btnVolver = findViewById(R.id.btnVolver);

        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());


        tokenManager = new TokenManager(this);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SubastarApi.class);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realizarLogin();
            }
        });


        tvIrARegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });


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
            DialogUtils.mostrarError(this, "Por favor, completá todos los campos.");
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
                    if (response.body().getCliente() == null) {
                        DialogUtils.mostrarError(LoginActivity.this, "Login incompleto: falta información del cliente.");
                        return;
                    }

                    // 1. Obtenemos el objeto cliente completo desde la respuesta
                    com.grupo6.subastar.dto.ClienteDTO clienteLogueado = response.body().getCliente();
                    tokenManager.saveClienteId(clienteLogueado.getIdentificador());

                    // Armamos el nombre y apellido juntos
                    String nombreCompleto = clienteLogueado.getNombre() + " " + clienteLogueado.getApellido();

                    // 2. Guardamos TODOS los datos en la memoria del celular
                    getSharedPreferences("SubastarPrefs", MODE_PRIVATE)
                            .edit()
                            .putInt("USER_ID", clienteLogueado.getIdentificador())
                            .putString("USER_NAME", nombreCompleto)
                            .putString("USER_EMAIL", clienteLogueado.getEmail())
                            .putString("USER_CATEGORIA", clienteLogueado.getCategoria())
                            .putString("USER_DOCUMENTO", clienteLogueado.getDocumento())
                            .putString("USER_DIRECCION", clienteLogueado.getDireccion())
                            .putString("USER_PAIS", clienteLogueado.getPais())
                            .putString("USER_FOTO", clienteLogueado.getFoto())
                            .apply();



                    //viajamos al home
                    com.grupo6.subastar.util.DialogUtils.mostrarExito(LoginActivity.this, "¡Éxito! Bienvenido, " + clienteLogueado.getNombre(), () -> {
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });

                } else {
                    DialogUtils.mostrarError(LoginActivity.this, "Credenciales incorrectas o usuario bloqueado.");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Iniciar Sesión");
                DialogUtils.mostrarError(LoginActivity.this, "Error de conexión: " + t.getMessage());
            }
        });
    }
}
