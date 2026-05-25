package com.grupo6.subastar;
import okhttp3.ResponseBody;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistroActivity extends AppCompatActivity {

    private byte[] bytesFrente = null;
    private byte[] bytesDorso = null;
    private TextView tvEstadoFrente, tvEstadoDorso;
    private boolean isCargandoFrente = true; // Para saber qué botón se apretó

    // Lanzador para abrir la galería de imágenes
    private final ActivityResultLauncher<String> selectorDeImagen = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    procesarImagenSeleccionada(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        EditText etNombre = findViewById(R.id.etNombre);
        EditText etApellido = findViewById(R.id.etApellido);
        EditText etDoc = findViewById(R.id.etDocumento);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etClave = findViewById(R.id.etClave);

        Button btnFotoFrente = findViewById(R.id.btnFotoFrente);
        Button btnFotoDorso = findViewById(R.id.btnFotoDorso);
        tvEstadoFrente = findViewById(R.id.tvEstadoFrente);
        tvEstadoDorso = findViewById(R.id.tvEstadoDorso);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SubastarApi api = retrofit.create(SubastarApi.class);

        // Eventos para abrir la galería
        btnFotoFrente.setOnClickListener(v -> {
            isCargandoFrente = true;
            selectorDeImagen.launch("image/*");
        });

        btnFotoDorso.setOnClickListener(v -> {
            isCargandoFrente = false;
            selectorDeImagen.launch("image/*");
        });

        btnRegistrar.setOnClickListener(v -> {
            // Validaciones básicas
            if (bytesFrente == null || bytesDorso == null) {
                Toast.makeText(this, "Debe cargar foto del frente y dorso del DNI", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convertimos los textos simples en RequestBody (Lo que pide el form-data)
            RequestBody reqNombre = RequestBody.create(MediaType.parse("text/plain"), etNombre.getText().toString());
            RequestBody reqApellido = RequestBody.create(MediaType.parse("text/plain"), etApellido.getText().toString());
            RequestBody reqEmail = RequestBody.create(MediaType.parse("text/plain"), etEmail.getText().toString());
            RequestBody reqClave = RequestBody.create(MediaType.parse("text/plain"), etClave.getText().toString());
            RequestBody reqDoc = RequestBody.create(MediaType.parse("text/plain"), etDoc.getText().toString());

            // Datos fijos para la prueba (Luego los podés pedir en la pantalla)
            RequestBody reqDir = RequestBody.create(MediaType.parse("text/plain"), "Direccion desde App");
            RequestBody reqFecha = RequestBody.create(MediaType.parse("text/plain"), "2000-01-01");
            RequestBody reqPais = RequestBody.create(MediaType.parse("text/plain"), "1");

            // Empaquetamos las fotos
            RequestBody bodyFrente = RequestBody.create(MediaType.parse("image/*"), bytesFrente);
            MultipartBody.Part partFrente = MultipartBody.Part.createFormData("fotoFrente", "frente.jpg", bodyFrente);

            RequestBody bodyDorso = RequestBody.create(MediaType.parse("image/*"), bytesDorso);
            MultipartBody.Part partDorso = MultipartBody.Part.createFormData("fotoDorso", "dorso.jpg", bodyDorso);

            // Deshabilitamos el botón para que no clickeen dos veces
            btnRegistrar.setEnabled(false);
            btnRegistrar.setText("Enviando...");

            // Disparamos la petición
            // Disparamos la petición
            api.registrar(reqNombre, reqApellido, reqEmail, reqClave, reqDoc, reqDir, reqFecha, reqPais, partFrente, partDorso)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(RegistroActivity.this, "¡Registro Exitoso!", Toast.LENGTH_SHORT).show();
                                finish(); // Cierra la pantalla y vuelve al Login
                            } else {
                                btnRegistrar.setEnabled(true);
                                btnRegistrar.setText("Registrarse");
                                try {
                                    Toast.makeText(RegistroActivity.this, "Error: " + response.errorBody().string(), Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            btnRegistrar.setEnabled(true);
                            btnRegistrar.setText("Registrarse");
                            Toast.makeText(RegistroActivity.this, "Falla de red real: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    // Método mágico que convierte la URI de Android en un arreglo de bytes listos para enviar
    private void procesarImagenSeleccionada(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            if (isCargandoFrente) {
                bytesFrente = buffer.toByteArray();
                tvEstadoFrente.setText("¡Frente OK!");
                tvEstadoFrente.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                bytesDorso = buffer.toByteArray();
                tvEstadoDorso.setText("¡Dorso OK!");
                tvEstadoDorso.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al leer la imagen", Toast.LENGTH_SHORT).show();
        }
    }
}