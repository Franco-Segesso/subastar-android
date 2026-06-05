package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class RegistroExitosoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_exitoso);

        Button btnEntendido = findViewById(R.id.btnEntendido);

        btnEntendido.setOnClickListener(v -> {
            // Mandamos al usuario de vuelta a la pantalla de bienvenida
            Intent intent = new Intent(RegistroExitosoActivity.this, WelcomeActivity.class);
            // Limpiamos el historial para que no pueda volver a esta pantalla de éxito
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}