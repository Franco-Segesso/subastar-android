package com.grupo6.subastar;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MedioPagoExitosoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medio_pago_exitoso);

        Button btnVolverPerfil = findViewById(R.id.btnVolverPerfil);

        btnVolverPerfil.setOnClickListener(v -> {
            // Le avisa a la pantalla anterior (Perfil) que todo salió bien para que recargue la lista
            setResult(RESULT_OK);
            // Cierra esta pantalla de éxito y vuelve al Perfil
            finish();
        });
    }
}