package com.grupo6.subastar;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.grupo6.subastar.adapter.SubastaAdapter;
import com.grupo6.subastar.model.Subasta;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SubastaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Apunta al layout renombrado

        recyclerView = findViewById(R.id.recyclerViewSubastas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ejecutarConsultaBackend();
    }

    private void ejecutarConsultaBackend() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SubastarApi api = retrofit.create(SubastarApi.class);

        // Pasamos parámetros nulos para traer el catálogo abierto general sin filtros iniciales
        api.obtenerSubastas(null, "abierta", null, null).enqueue(new Callback<List<Subasta>>() {
            @Override
            public void onResponse(Call<List<Subasta>> call, Response<List<Subasta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Subasta> subastas = response.body();

                    // Seteamos el adapter con los datos reales de la BD
                    adapter = new SubastaAdapter(subastas);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(HomeActivity.this, "Error de respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Subasta>> call, Throwable t) {
                Log.e("HOME_SERVER_ERROR", t.getMessage());
                Toast.makeText(HomeActivity.this, "Fallo en la conexión de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}