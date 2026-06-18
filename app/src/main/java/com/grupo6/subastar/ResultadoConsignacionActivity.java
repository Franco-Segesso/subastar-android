package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class ResultadoConsignacionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado_consignacion);

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        Button btnVolverMis = findViewById(R.id.btnVolverMisConsignaciones);
        btnVolver.setOnClickListener(v -> finish());
        btnVolverMis.setOnClickListener(v -> {
            Intent intent = new Intent(this, MisConsignacionesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
