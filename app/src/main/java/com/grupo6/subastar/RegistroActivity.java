package com.grupo6.subastar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.util.Calendar;
import android.app.DatePickerDialog;



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
        ImageButton btnVolver = findViewById(R.id.btnVolver);
        findViewById(R.id.btnVolver).setOnClickListener(v -> finish());

        // Configuramos el evento de clic para el campo de fecha
        etFecha.setOnClickListener(v -> {
            // 1. Obtenemos la fecha actual para que el calendario arranque en el día de hoy
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // 2. Creamos el diálogo del calendario
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    RegistroActivity.this,
                    (view, yearSeleccionado, monthOfYear, dayOfMonth) -> {
                        // El mes en Java empieza en 0 (Enero es 0), así que le sumamos 1
                        int mesReal = monthOfYear + 1;

                        // Formateamos para que siempre tenga 2 dígitos (ej: "1" pasa a ser "01")
                        // Esto garantiza el formato YYYY-MM-DD para tu backend
                        String fechaFormateada = String.format("%04d-%02d-%02d", yearSeleccionado, mesReal, dayOfMonth);

                        // Ponemos la fecha lista en la pantalla
                        etFecha.setText(fechaFormateada);
                    },
                    year, month, day);

            // 3. Detalle profesional: Bloqueamos las fechas del futuro (nadie puede nacer mañana)
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

            // 4. Mostramos el calendario
            datePickerDialog.show();
        });
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
                                // En vez de mostrar un Toast, abrimos la nueva pantalla de éxito
                                Intent intent = new Intent(RegistroActivity.this, RegistroExitosoActivity.class);
                                startActivity(intent);
                                finish(); // Cerramos RegistroActivity
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