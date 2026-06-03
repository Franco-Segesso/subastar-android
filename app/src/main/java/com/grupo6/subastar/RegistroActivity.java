package com.grupo6.subastar;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class RegistroActivity extends AppCompatActivity {

    private byte[] bytesFrente = null;
    private byte[] bytesDorso = null;
    private TextView tvEstadoFrente, tvEstadoDorso;
    private boolean isCargandoFrente = true;

    // AHORA SÍ DECLARAMOS EL SPINNER Y LA LISTA DE PAÍSES
    private Spinner spPais;
    private List<Pais> listaPaises = new ArrayList<>();

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
        EditText etDireccion = findViewById(R.id.etDireccion);
        EditText etFecha = findViewById(R.id.etFechaNacimiento);
        spPais = findViewById(R.id.spPais);

        android.widget.LinearLayout btnFotoFrente = findViewById(R.id.btnFotoFrente);
        android.widget.LinearLayout btnFotoDorso = findViewById(R.id.btnFotoDorso);
        tvEstadoFrente = findViewById(R.id.tvEstadoFrente);
        tvEstadoDorso = findViewById(R.id.tvEstadoDorso);
        Button btnRegistrar = findViewById(R.id.btnRegistrar);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SubastarApi api = retrofit.create(SubastarApi.class);

        // LLAMADA AL BACKEND PARA CARGAR LOS PAÍSES EN EL DESPLEGABLE
        api.getPaises().enqueue(new Callback<List<Pais>>() {
            @Override
            public void onResponse(Call<List<Pais>> call, Response<List<Pais>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaPaises = response.body();
                    List<String> nombresPaises = new ArrayList<>();
                    for (Pais p : listaPaises) {
                        nombresPaises.add(p.getNombre());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(RegistroActivity.this, android.R.layout.simple_spinner_dropdown_item, nombresPaises);
                    spPais.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Pais>> call, Throwable t) {
                Toast.makeText(RegistroActivity.this, "Error al cargar países", Toast.LENGTH_SHORT).show();
            }
        });

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
            if (listaPaises == null || listaPaises.isEmpty()) {
                Toast.makeText(this, "Esperando países del servidor...", Toast.LENGTH_SHORT).show();
                return;
            }

            int idPaisSeleccionado = listaPaises.get(spPais.getSelectedItemPosition()).getNumero();

            RequestBody reqNombre = RequestBody.create(MediaType.parse("text/plain"), etNombre.getText().toString());
            RequestBody reqApellido = RequestBody.create(MediaType.parse("text/plain"), etApellido.getText().toString());
            RequestBody reqEmail = RequestBody.create(MediaType.parse("text/plain"), etEmail.getText().toString());
            RequestBody reqDoc = RequestBody.create(MediaType.parse("text/plain"), etDoc.getText().toString());
            RequestBody reqDir = RequestBody.create(MediaType.parse("text/plain"), etDireccion.getText().toString());
            RequestBody reqPais = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(idPaisSeleccionado));
            // Abajo de donde creás reqDir y reqPais, agregá esto:
            RequestBody reqFecha = RequestBody.create(MediaType.parse("text/plain"), etFecha.getText().toString());



            RequestBody bodyFrente = RequestBody.create(MediaType.parse("image/*"), bytesFrente);
            MultipartBody.Part partFrente = MultipartBody.Part.createFormData("fotoDniFrente", "frente.jpg", bodyFrente);

            RequestBody bodyDorso = RequestBody.create(MediaType.parse("image/*"), bytesDorso);
            MultipartBody.Part partDorso = MultipartBody.Part.createFormData("fotoDniDorso", "dorso.jpg", bodyDorso);

            btnRegistrar.setEnabled(false);
            btnRegistrar.setText("Enviando...");

            // Disparamos la petición
            api.registrar(reqNombre, reqApellido, reqEmail, reqDoc, reqDir, reqFecha, reqPais, partFrente, partDorso)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(RegistroActivity.this, "¡Datos enviados! En espera de validación.", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                btnRegistrar.setEnabled(true);
                                btnRegistrar.setText("Registrarme");
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
                            btnRegistrar.setText("Registrarme");
                            Toast.makeText(RegistroActivity.this, "Falla de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

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