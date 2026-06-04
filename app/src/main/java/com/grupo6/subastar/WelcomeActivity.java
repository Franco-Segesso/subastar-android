package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        tokenManager = new TokenManager(this);

        MaterialButton btnGoLogin = findViewById(R.id.btnGoLogin);
        MaterialButton btnGoRegistro = findViewById(R.id.btnGoRegistro);
        TextView tvInvitado = findViewById(R.id.tvInvitado);

        btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        });

        btnGoRegistro.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, RegistroActivity.class));
        });

        tvInvitado.setOnClickListener(v -> {
            // 1. Limpiamos SharedPreferences para asegurarnos de que no haya un "USER_NAME" o Token viejo
            getSharedPreferences("SubastarPrefs", MODE_PRIVATE).edit().clear().apply();

            // (Opcional) Si Franco creó un metodo específico para borrar el token en su TokenManager, sumalo acá:
            tokenManager.clearToken();

            // 2. Creamos el puente hacia el Catálogo
            Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);

            // 3. Estas banderas evitan que el usuario pueda volver a esta pantalla tocando "Atrás"
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish(); // Destruimos el WelcomeActivity
        });
    }
}