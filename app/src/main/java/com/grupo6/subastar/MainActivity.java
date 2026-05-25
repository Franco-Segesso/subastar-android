package com.grupo6.subastar;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
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

        // 1. Instanciamos Retrofit apuntando a Spring Boot
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080") // La IP del emulador hacia tu PC local
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SubastarApi api = retrofit.create(SubastarApi.class);

        // 2. Ejecutamos la petición en segundo plano
        api.obtenerPaises().enqueue(new Callback<List<Pais>>() {
            @Override
            public void onResponse(Call<List<Pais>> call, Response<List<Pais>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Pais p : response.body()) {
                        // Imprime el resultado en la consola de Android Studio
                        Log.d("API_PRUEBA", "Llegó el país: " + p.getNombre());
                    }
                } else {
                    Log.e("API_PRUEBA", "Error en servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Pais>> call, Throwable t) {
                Log.e("API_PRUEBA", "Explotó la red: " + t.getMessage());
            }
        });
    }
}