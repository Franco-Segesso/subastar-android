package com.grupo6.subastar;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.grupo6.subastar.model.Subasta;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080") // La IP mágica hacia tu backend local
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SubastarApi api = retrofit.create(SubastarApi.class);

        // Llamamos al endpoint pasándole null a todo para simular un usuario no logueado
        api.obtenerSubastas(null, null, null, null).enqueue(new Callback<List<Subasta>>() {
            @Override
            public void onResponse(Call<List<Subasta>> call, Response<List<Subasta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Subasta> lista = response.body();
                    Log.d("API_SUBASTAS", "Se encontraron " + lista.size() + " subastas.");

                    for (Subasta s : lista) {
                        Log.d("API_SUBASTAS", "Subasta ID: " + s.getId() +
                                " | Estado: " + s.getEstado() +
                                " | Moneda: " + s.getMoneda());
                    }
                } else {
                    Log.e("API_SUBASTAS", "Error en servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Subasta>> call, Throwable t) {
                Log.e("API_SUBASTAS", "Explotó la red: " + t.getMessage());
            }
        });
    }
}