package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.grupo6.subastar.dto.AgregarChequeRequest;
import com.grupo6.subastar.dto.AgregarCuentaRequest;
import com.grupo6.subastar.dto.AgregarTarjetaRequest;
import java.math.BigDecimal;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AgregarMedioPagoActivity extends AppCompatActivity {

    private LinearLayout formTarjeta, formCuenta, formCheque;
    private TextView tabTarjeta, tabCuenta, tabCheque;
    private String tipoSeleccionado = "tarjeta";
    private Integer clienteId;
    private SubastarApi api;
    private TokenManager tokenManager;

    // Campos tarjeta
    private EditText etTitular, etNumeroTarjeta, etCvv, etVencimiento;
    private Spinner spinnerExtranjera, spinnerPaisEmisor;
    private View labelPaisEmisor;

    // Campos cuenta
    private EditText etCbuIban, etAlias, etBancoCuenta;
    private Spinner spinnerMonedaCuenta, spinnerPaisBanco;

    private java.util.List<Pais> listaPaises = new java.util.ArrayList<>();

    // Campos cheque
    private EditText etNroCheque, etBancoCheque, etMontoGarantia, etFechaEntrega;
    private Spinner spinnerMonedaCheque;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_medio_pago);

        // 1. Recibimos la bandera para saber si lo forzamos o no
        boolean esObligatorio = getIntent().getBooleanExtra("esObligatorio", false);

        if (esObligatorio) {
            // Le explicamos al usuario por qué está acá
            Toast.makeText(this, "Para participar, es obligatorio registrar al menos un medio de pago.", Toast.LENGTH_LONG).show();

            // 2. BLOQUEAMOS EL BOTÓN FÍSICO "ATRÁS" DEL CELULAR ANDROID
            getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // En vez de salir de la pantalla, le repetimos el aviso
                    Toast.makeText(AgregarMedioPagoActivity.this, "No puedes salir sin registrar un medio de pago.", Toast.LENGTH_SHORT).show();
                }
            });
        }


        tokenManager = new TokenManager(this);
        clienteId = getIntent().getIntExtra("clienteId", -1);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(SubastarApi.class);

        // Vistas
        formTarjeta = findViewById(R.id.formTarjeta);
        formCuenta  = findViewById(R.id.formCuenta);
        formCheque  = findViewById(R.id.formCheque);
        tabTarjeta  = findViewById(R.id.tabTarjeta);
        tabCuenta   = findViewById(R.id.tabCuenta);
        tabCheque   = findViewById(R.id.tabCheque);

        // Campos tarjeta
        etTitular        = findViewById(R.id.etTitular);
        etNumeroTarjeta  = findViewById(R.id.etNumeroTarjeta);
        etCvv            = findViewById(R.id.etCvv);
        etVencimiento    = findViewById(R.id.etVencimiento);
        labelPaisEmisor  = findViewById(R.id.labelPaisEmisor);
        spinnerExtranjera = findViewById(R.id.spinnerExtranjera);
        spinnerPaisEmisor = findViewById(R.id.spinnerPaisEmisor);

        ArrayAdapter<String> adapterSiNo = new ArrayAdapter<>(this,
                R.layout.item_spinner, new String[]{"no", "si"});
        adapterSiNo.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerExtranjera.setAdapter(adapterSiNo);

        // Mostrar/ocultar campo País emisor según si es extranjera
        spinnerExtranjera.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                boolean esExtranjera = position == 1; // "si"
                labelPaisEmisor.setVisibility(esExtranjera ? View.VISIBLE : View.GONE);
                spinnerPaisEmisor.setVisibility(esExtranjera ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });


        // Campos cuenta
        etCbuIban           = findViewById(R.id.etCbuIban);
        etAlias             = findViewById(R.id.etAlias);
        etBancoCuenta       = findViewById(R.id.etBancoCuenta);
        spinnerPaisBanco    = findViewById(R.id.spinnerPaisBanco);
        spinnerMonedaCuenta = findViewById(R.id.spinnerMonedaCuenta);

        // Campos cheque
        etNroCheque         = findViewById(R.id.etNroCheque);
        etBancoCheque       = findViewById(R.id.etBancoCheque);
        etMontoGarantia     = findViewById(R.id.etMontoGarantia);
        etFechaEntrega      = findViewById(R.id.etFechaEntrega);
        spinnerMonedaCheque = findViewById(R.id.spinnerMonedaCheque);

        // Spinner monedas
        ArrayAdapter<String> adapterMoneda = new ArrayAdapter<>(this,
                R.layout.item_spinner, new String[]{"ARS", "USD"});
        adapterMoneda.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerMonedaCuenta.setAdapter(adapterMoneda);
        spinnerMonedaCheque.setAdapter(adapterMoneda);

        // Configuramos el calendario para la fecha del cheque
        etFechaEntrega.setOnClickListener(v -> {
            final java.util.Calendar c = java.util.Calendar.getInstance();
            int year = c.get(java.util.Calendar.YEAR);
            int month = c.get(java.util.Calendar.MONTH);
            int day = c.get(java.util.Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    AgregarMedioPagoActivity.this,
                    (view, yearSeleccionado, monthOfYear, dayOfMonth) -> {
                        // Le sumamos 1 al mes porque Enero es 0
                        int mesReal = monthOfYear + 1;

                        // Formateamos como YYYY-MM-DD
                        String fechaFormateada = String.format("%04d-%02d-%02d", yearSeleccionado, mesReal, dayOfMonth);
                        etFechaEntrega.setText(fechaFormateada);
                    },
                    year, month, day);

            // Bloqueamos las fechas del pasado
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

            datePickerDialog.show();
        });

        // Tabs
        tabTarjeta.setOnClickListener(v -> seleccionarTab("tarjeta"));
        tabCuenta.setOnClickListener(v -> seleccionarTab("cuenta"));
        tabCheque.setOnClickListener(v -> seleccionarTab("cheque"));

        // Volver
        ImageButton btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> {
            if (esObligatorio) {
                  Toast.makeText(AgregarMedioPagoActivity.this, "No puedes salir sin registrar un medio de pago.", Toast.LENGTH_SHORT).show();
            } else {
                    finish(); // Si no es obligatorio, sale normalmente
                }
             });

        // Guardar
        Button btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> guardar());

        cargarPaises();
    }

    private void cargarPaises() {
        api.getPaises().enqueue(new Callback<java.util.List<Pais>>() {
            @Override
            public void onResponse(Call<java.util.List<Pais>> call, Response<java.util.List<Pais>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaPaises = response.body();

                    // 1. Armamos la lista con TODOS los países para la Cuenta Bancaria
                    java.util.List<String> nombresTodos = new java.util.ArrayList<>();
                    for (Pais p : listaPaises) {
                        nombresTodos.add(p.getNombre());
                    }
                    ArrayAdapter<String> adapterBanco = new ArrayAdapter<>(AgregarMedioPagoActivity.this, R.layout.item_spinner, nombresTodos);
                    adapterBanco.setDropDownViewResource(R.layout.item_spinner_dropdown);
                    spinnerPaisBanco.setAdapter(adapterBanco);

                    // 2. Armamos la lista SIN ARGENTINA para la Tarjeta Extranjera
                    java.util.List<String> nombresExtranjeros = new java.util.ArrayList<>();
                    for (Pais p : listaPaises) {
                        if (!p.getNombre().equalsIgnoreCase("Argentina")) {
                            nombresExtranjeros.add(p.getNombre());
                        }
                    }
                    ArrayAdapter<String> adapterEmisor = new ArrayAdapter<>(AgregarMedioPagoActivity.this, R.layout.item_spinner, nombresExtranjeros);
                    adapterEmisor.setDropDownViewResource(R.layout.item_spinner_dropdown);
                    spinnerPaisEmisor.setAdapter(adapterEmisor);
                }
            }

            @Override
            public void onFailure(Call<java.util.List<Pais>> call, Throwable t) {
                Toast.makeText(AgregarMedioPagoActivity.this, "No se pudieron cargar los países", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seleccionarTab(String tipo) {
        tipoSeleccionado = tipo;

        // Reset todos los tabs
        tabTarjeta.setBackgroundResource(R.drawable.bg_chip_inactivo);
        tabTarjeta.setTextColor(0xFFAAAAAA);
        tabCuenta.setBackgroundResource(R.drawable.bg_chip_inactivo);
        tabCuenta.setTextColor(0xFFAAAAAA);
        tabCheque.setBackgroundResource(R.drawable.bg_chip_inactivo);
        tabCheque.setTextColor(0xFFAAAAAA);

        // Activar el seleccionado
        TextView tabActivo = tipo.equals("tarjeta") ? tabTarjeta : tipo.equals("cuenta") ? tabCuenta : tabCheque;
        tabActivo.setBackgroundResource(R.drawable.bg_chip_activo);
        tabActivo.setTextColor(0xFFC8A951);

        // Mostrar formulario correcto
        formTarjeta.setVisibility(tipo.equals("tarjeta") ? View.VISIBLE : View.GONE);
        formCuenta.setVisibility(tipo.equals("cuenta")   ? View.VISIBLE : View.GONE);
        formCheque.setVisibility(tipo.equals("cheque")   ? View.VISIBLE : View.GONE);
    }

    private void guardar() {
        String token = "Bearer " + tokenManager.getToken();

        if (tipoSeleccionado.equals("tarjeta")) {
            String titular  = etTitular.getText().toString().trim();
            String numeroCompleto = etNumeroTarjeta.getText().toString().trim();
            String cvv      = etCvv.getText().toString().trim();
            String vence    = etVencimiento.getText().toString().trim();
            String extran   = spinnerExtranjera.getSelectedItem().toString();

            // Leemos el país seleccionado desde el Spinner
            String pais = spinnerPaisEmisor.getSelectedItem() != null ? spinnerPaisEmisor.getSelectedItem().toString() : "";

            if (titular.isEmpty() || numeroCompleto.isEmpty() || cvv.isEmpty() || vence.isEmpty()) {
                Toast.makeText(this, "Completá todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // VALIDACIONES ESTRICTAS DE TARJETA
            if (numeroCompleto.length() < 15 || numeroCompleto.length() > 16) {
                etNumeroTarjeta.setError("Debe tener 15 o 16 dígitos");
                etNumeroTarjeta.requestFocus();
                return;
            }
            if (cvv.length() < 3) {
                etCvv.setError("CVV inválido");
                etCvv.requestFocus();
                return;
            }
            if (!vence.matches("^(0[1-9]|1[0-2])/?([0-9]{2})$")) {
                etVencimiento.setError("Formato inválido (MM/AA)");
                etVencimiento.requestFocus();
                return;
            }
            if (esTarjetaVencida(vence)) {
                etVencimiento.setError("La tarjeta está vencida");
                etVencimiento.requestFocus();
                return;
            }

            // Extraemos solo los últimos 4 dígitos para mandarlos al servidor y descartamos el CVV
            String digitosFinales = numeroCompleto.substring(numeroCompleto.length() - 4);

            AgregarTarjetaRequest req = new AgregarTarjetaRequest(digitosFinales, vence, titular, extran,
                    extran.equals("si") ? pais : null);

            api.agregarTarjeta(clienteId, token, req).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { manejarRespuesta(response); }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) { Toast.makeText(AgregarMedioPagoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show(); }
            });

        } else if (tipoSeleccionado.equals("cuenta")) {
            String cbu   = etCbuIban.getText().toString().trim();
            String banco = etBancoCuenta.getText().toString().trim();
            String moneda = spinnerMonedaCuenta.getSelectedItem().toString();

            // Leemos el país seleccionado desde el Spinner nuevo
            String pais = "";
            if (spinnerPaisBanco.getSelectedItem() != null) {
                pais = spinnerPaisBanco.getSelectedItem().toString();
            }

            if (cbu.isEmpty() || banco.isEmpty()) {
                Toast.makeText(this, "CBU/IBAN y Banco son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cbu.length() < 10) {
                etCbuIban.setError("El CBU/IBAN ingresado es muy corto");
                etCbuIban.requestFocus();
                return;
            }


            AgregarCuentaRequest req = new AgregarCuentaRequest(cbu,
                    etAlias.getText().toString().trim(), banco, pais, moneda);

            api.agregarCuenta(clienteId, token, req).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { manejarRespuesta(response); }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) { Toast.makeText(AgregarMedioPagoActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show(); }
            });

        } else {
            String nro   = etNroCheque.getText().toString().trim();
            String banco = etBancoCheque.getText().toString().trim();
            String monto = etMontoGarantia.getText().toString().trim();
            String fecha = etFechaEntrega.getText().toString().trim();
            String moneda = spinnerMonedaCheque.getSelectedItem().toString();

            if (nro.isEmpty() || banco.isEmpty() || monto.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Completá todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // VALIDACIONES DE CHEQUE
            try {
                double montoValidado = Double.parseDouble(monto);
                if (montoValidado <= 0) {
                    etMontoGarantia.setError("El monto debe ser mayor a 0");
                    etMontoGarantia.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etMontoGarantia.setError("Ingresá un número válido");
                etMontoGarantia.requestFocus();
                return;
            }

            if (!fecha.matches("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$")) {
                etFechaEntrega.setError("Formato inválido (YYYY-MM-DD)");
                etFechaEntrega.requestFocus();
                return;
            }

            AgregarChequeRequest req = new AgregarChequeRequest(nro, banco, moneda,
                    new BigDecimal(monto), fecha);

            api.agregarCheque(clienteId, token, req).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { manejarRespuesta(response); }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    mostrarDialogoError("No tienes conexión a internet o el servidor no responde.");
                }
            });
        }
    }

    private boolean esTarjetaVencida(String vencimiento) {
        try {
            String[] partes = vencimiento.split("/");
            if (partes.length != 2) return true;

            int mes = Integer.parseInt(partes[0]);
            int anio = Integer.parseInt(partes[1]) + 2000; // Asumimos que "24" es "2024"

            java.util.Calendar c = java.util.Calendar.getInstance();
            int mesActual = c.get(java.util.Calendar.MONTH) + 1; // En Java, Enero es 0
            int anioActual = c.get(java.util.Calendar.YEAR);

            if (anio < anioActual) {
                return true; // Año vencido
            }
            if (anio == anioActual && mes < mesActual) {
                return true; // Mismo año, pero mes viejo
            }

            return false; // Tarjeta válida
        } catch (Exception e) {
            return true; // Si falló el cálculo, asumimos inválido
        }
    }

    private void manejarRespuesta(Response<?> response) {
        if (response.isSuccessful()) {
            // Lanzamos la pantalla de éxito
            Intent intent = new Intent(AgregarMedioPagoActivity.this, MedioPagoExitosoActivity.class);
            startActivity(intent);
            finish();
        } else {
            String mensajeError = "Ocurrió un error al procesar la solicitud.";
            try {
                if (response.errorBody() != null) {
                    String errorJsonStr = response.errorBody().string();
                    org.json.JSONObject errorJson = new org.json.JSONObject(errorJsonStr);


                    if (errorJson.has("message")) {
                        mensajeError = errorJson.getString("message");
                    } else if (errorJson.has("error")) {
                        mensajeError = errorJson.getString("error");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            mostrarDialogoError(mensajeError);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Si el resultado viene de la pantalla de éxito (código 100)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            setResult(RESULT_OK); // Le avisamos al Perfil que todo salió bien
            finish(); // Cerramos el formulario
        }
    }

    private void mostrarDialogoError(String mensaje) {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_error);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        android.widget.TextView tvMensaje = dialog.findViewById(R.id.tvMensajeError);
        tvMensaje.setText(mensaje);

        android.widget.Button btnEntendido = dialog.findViewById(R.id.btnEntendidoError);
        btnEntendido.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}