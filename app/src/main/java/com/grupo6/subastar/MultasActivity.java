package com.grupo6.subastar;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MultasActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multas);

        // Atrapamos el ID de la multa que viene desde la Notificación
        int multaId = getIntent().getIntExtra("MULTA_ID", -1);

        TextView tvTitulo = findViewById(R.id.tvTituloMultas);
        if (multaId != -1) {
            tvTitulo.setText("Detalle de Multa #" + multaId);
        }
    }
}