package com.grupo6.subastar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.grupo6.subastar.model.ItemCatalogo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.android.material.appbar.MaterialToolbar;

public class DetalleItemActivity extends AppCompatActivity {

    private ImageView ivImagen;
    private TextView tvTitulo, tvPrecio, tvDescripcion, tvCategoria, tvDuenio;
    private MaterialButton btnPujar;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_item);

        tokenManager = new TokenManager(this);

        ivImagen = findViewById(R.id.ivDetalleImagen);
        tvTitulo = findViewById(R.id.tvDetalleTitulo);
        tvPrecio = findViewById(R.id.tvDetallePrecio);
        tvDescripcion = findViewById(R.id.tvDetalleDescripcion);
        tvCategoria = findViewById(R.id.tvDetalleCategoria);
        tvDuenio = findViewById(R.id.tvDetalleDuenio);
        btnPujar = findViewById(R.id.btnPujar);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        int itemId = getIntent().getIntExtra("ITEM_ID", -1);
        int subastaId = getIntent().getIntExtra("SUBASTA_ID", -1);
        String estado = getIntent().getStringExtra("SUBASTA_ESTADO");
        String nombreItem = getIntent().getStringExtra("ITEM_TITULO");
        Double baseItem = getIntent().getDoubleExtra("ITEM_BASE", 0);

        // Regla: Solo mostramos el botón si la subasta está abierta
        if ("abierta".equalsIgnoreCase(estado)) {
            btnPujar.setVisibility(View.VISIBLE);

            btnPujar.setOnClickListener(v -> {
                // Creamos el "puente" hacia la nueva Activity
                android.content.Intent intent = new android.content.Intent(DetalleItemActivity.this, SalaPujaActivity.class);

                // Le pasamos los datos exactos que espera recibir el onCreate() de SalaPujaActivity
                intent.putExtra("SUBASTA_ID", subastaId);
                intent.putExtra("ITEM_ID", itemId);

                // Opcional: pasar textos para la cabecera (reemplazá con los datos reales de tu objeto ítem)
                intent.putExtra("ITEM_TITULO", nombreItem);
                intent.putExtra("ITEM_BASE", baseItem);

                startActivity(intent);
            });
        }

        cargarDatosBackend(subastaId, itemId);
    }


    private void cargarDatosBackend(int subastaId, int itemId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SubastarApi api = retrofit.create(SubastarApi.class);

        String tokenGuardado = tokenManager.getToken();
        String tokenHeader = null;

        // Si el usuario está logueado, armamos el header de autorización
        if (tokenGuardado != null) {
            tokenHeader = "Bearer " + tokenGuardado;
        }

        api.obtenerDetalleItem(subastaId, itemId, tokenHeader).enqueue(new Callback<ItemCatalogo>() {
            @Override
            public void onResponse(Call<ItemCatalogo> call, Response<ItemCatalogo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ItemCatalogo item = response.body();

                    tvTitulo.setText(item.getProducto().getTipo());
                    tvDescripcion.setText(item.getProducto().getHistoria());
                    tvCategoria.setText("ARTE - ÍTEM #" + item.getId());

                    if (item.getProducto() != null && item.getProducto().getNombreDuenioReal() != null) {
                        tvDuenio.setText(item.getProducto().getNombreDuenioReal());
                    } else {
                        tvDuenio.setText("Dueño Anónimo");
                    }

                    if (item.getPrecioBase() != null) {
                        tvPrecio.setText("USD " + item.getPrecioBase());
                        tvPrecio.setVisibility(View.VISIBLE);
                    } else {
                        tvPrecio.setText("Iniciá sesión");
                    }

                    if (item.getProducto().getFotos() != null && !item.getProducto().getFotos().isEmpty()) {
                        Glide.with(DetalleItemActivity.this)
                                .load(item.getProducto().getFotos().get(0).getUrlFoto())
                                .into(ivImagen);
                    }
                    if ("si".equalsIgnoreCase(item.getSubastado())) {
                        btnPujar.setEnabled(false);
                        btnPujar.setText("Subasta Finalizada");
                        btnPujar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY));
                    }else {
                        // Nos aseguramos de que el botón esté habilitado para los pendientes
                        btnPujar.setEnabled(true);
                        btnPujar.setText("Participar en la puja");
                    }
                }
            }

            @Override
            public void onFailure(Call<ItemCatalogo> call, Throwable t) {
                Toast.makeText(DetalleItemActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}