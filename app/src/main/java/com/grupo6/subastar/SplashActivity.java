package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen; // <-- Librería nueva

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Instalamos y capturamos la pantalla nativa ANTES del super.onCreate
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // 2. Le decimos a Android que MANTENGA el logo en pantalla y no lo borre
        splashScreen.setKeepOnScreenCondition(() -> true);

        // Ya NO usamos setContentView(R.layout.activity_splash); porque no necesitamos el XML

        // 3. Mantenemos tu temporizador de 2.5 segundos
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            TokenManager tokenManager = new TokenManager(this);
            Intent intent;

            if (tokenManager.getToken() != null && !tokenManager.getToken().isEmpty()) {
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, WelcomeActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(0, 0); // Evita pestañeos al cambiar de Activity
            finish();

        }, 2500);
    }
}