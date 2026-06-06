package com.grupo6.subastar;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grupo6.subastar.adapter.ItemProductoAdapter;
import com.grupo6.subastar.model.Subasta;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CatalogoActivity extends AppCompatActivity {

    private TextView tvTitulo, tvCat, tvMoneda, tvEstado;
    private RecyclerView recyclerView;
    private Integer subastaId;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogo);

        tokenManager = new TokenManager(this);

        // Bindear vistas
        tvTitulo = findViewById(R.id.tvCatalogoTitulo);
        tvCat = findViewById(R.id.tvHeaderCat);
        tvMoneda = findViewById(R.id.tvHeaderMoneda);
        tvEstado = findViewById(R.id.tvHeaderEstado);
        recyclerView = findViewById(R.id.recyclerViewProductos);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Botón volver
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // Cierra esta pantalla y vuelve al Home

        // Capturar ID que mandó el HomeActivity
        subastaId = getIntent().getIntExtra("SUBASTA_ID", -1);

        if (subastaId != -1) {
            cargarDetalleSubasta(subastaId);
        } else {
            Toast.makeText(this, "Error: No se encontró la subasta", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void cargarDetalleSubasta(Integer id) {
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

        // Simulamos usuario no logueado pasando null en el token
        api.obtenerDetalleSubasta(id, tokenHeader).enqueue(new Callback<Subasta>() {
            @Override
            public void onResponse(Call<Subasta> call, Response<Subasta> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Subasta subasta = response.body();

                    // Llenar cabecera
                    tvTitulo.setText(subasta.getUbicacion());
                    tvCat.setText(subasta.getCategoria().toUpperCase());
                    tvMoneda.setText(subasta.getMoneda());
                    tvEstado.setText(subasta.getEstado().toUpperCase());

                    // Le pasamos los datos directamente al instanciar el adapter
                    if (subasta.getCatalogo() != null && subasta.getCatalogo().getItems() != null) {
                        ItemProductoAdapter adapter = new ItemProductoAdapter(
                                subasta.getCatalogo().getItems(),
                                subasta.getId(),
                                subasta.getEstado()
                        );
                        recyclerView.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<Subasta> call, Throwable t) {
                Toast.makeText(CatalogoActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}