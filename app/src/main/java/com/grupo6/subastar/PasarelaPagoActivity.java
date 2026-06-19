package com.grupo6.subastar;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PasarelaPagoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasarela_pago);

        // Atrapamos el ID del ítem que viene desde la Notificación
        int itemId = getIntent().getIntExtra("ITEM_ID", -1);

        TextView tvTitulo = findViewById(R.id.tvTituloPago);
        if (itemId != -1) {
            tvTitulo.setText("Pasarela de Pago para Ítem #" + itemId);
        }
    }
}