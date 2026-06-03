package com.grupo6.subastar;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_item);

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

        // Regla: Solo mostramos el botón si la subasta está abierta
        if ("abierta".equalsIgnoreCase(estado)) {
            btnPujar.setVisibility(View.VISIBLE);
        }

        cargarDatosBackend(subastaId, itemId);
    }

    private void cargarDatosBackend(int subastaId, int itemId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SubastarApi api = retrofit.create(SubastarApi.class);

        api.obtenerDetalleItem(subastaId, itemId, null).enqueue(new Callback<ItemCatalogo>() {
            @Override
            public void onResponse(Call<ItemCatalogo> call, Response<ItemCatalogo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ItemCatalogo item = response.body();

                    tvTitulo.setText(item.getProducto().getTipo());
                    tvDescripcion.setText(item.getProducto().getHistoria());
                    tvCategoria.setText("ARTE - ÍTEM #" + item.getId());

                    if (item.getPrecioBase() != null) {
                        tvPrecio.setText("USD " + item.getPrecioBase());
                    } else {
                        tvPrecio.setText("Iniciá sesión");
                    }

                    if (item.getProducto().getFotos() != null && !item.getProducto().getFotos().isEmpty()) {
                        Glide.with(DetalleItemActivity.this)
                                .load(item.getProducto().getFotos().get(0).getUrlFoto())
                                .into(ivImagen);
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