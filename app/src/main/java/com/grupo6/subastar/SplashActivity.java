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

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);


        splashScreen.setKeepOnScreenCondition(() -> true);




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