package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

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
            // Lógica a futuro para invitado
        });
    }
}