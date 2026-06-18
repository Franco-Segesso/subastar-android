package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class ConsignacionEnviadaActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consignacion_enviada);

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        Button btnIr = findViewById(R.id.btnIrMisConsignaciones);

        btnVolver.setOnClickListener(v -> finish());
        btnIr.setOnClickListener(v -> {
            Intent intent = new Intent(this, MisConsignacionesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
