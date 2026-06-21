package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.grupo6.subastar.adapter.MedioPagoAdapter;
import com.grupo6.subastar.dto.MedioPagoDTO;
import com.grupo6.subastar.dto.MultaDTO;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.net.Uri;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PerfilActivity extends AppCompatActivity {


    private TextView tvNombreCompleto, tvEmail, tvCategoria, tvDireccion, tvPais, tvDocumento;
    private TextView tvMultasPendientes, tvBadgeMultas;
    private RecyclerView recyclerMediosPago;
    private MedioPagoAdapter adapter;
    private List<MedioPagoDTO> listaMedios = new ArrayList<>();
    private TokenManager tokenManager;
    private Integer clienteId;
    private SubastarApi api;

    private ImageView ivFotoPerfil;
    private ActivityResultLauncher<String> seleccionarFotoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        tokenManager = new TokenManager(this);

        seleccionarFotoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        subirFotoPerfil(uri);
                    }
                }
        );


        tvNombreCompleto = findViewById(R.id.tvNombreCompleto);
        tvEmail          = findViewById(R.id.tvEmail);
        tvCategoria      = findViewById(R.id.tvCategoria);
        tvDireccion      = findViewById(R.id.tvDireccion);
        tvPais           = findViewById(R.id.tvPais);
        tvDocumento      = findViewById(R.id.tvDocumento);
        tvMultasPendientes = findViewById(R.id.tvMultasPendientesPerfil);
        tvBadgeMultas = findViewById(R.id.tvBadgeMultasPerfil);
        recyclerMediosPago = findViewById(R.id.recyclerMediosPago);
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);

        recyclerMediosPago.setLayoutManager(new LinearLayoutManager(this));

        // Botón volver
        ImageButton btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> finish());

        // Botón agregar medio de pago
        findViewById(R.id.btnAgregarMedioPago).setOnClickListener(v -> {
            Intent intent = new Intent(this, AgregarMedioPagoActivity.class);
            intent.putExtra("clienteId", clienteId);
            startActivityForResult(intent, 100);
        });

        // Botón Cerrar Sesión
        findViewById(R.id.btnMisConsignaciones).setOnClickListener(v -> {
            Intent intent = new Intent(this, MisConsignacionesActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.btnMultasPerfil).setOnClickListener(v ->
                startActivity(new Intent(this, MultasActivity.class)));

        ImageButton btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(v -> {
            int firebaseClienteId = getSharedPreferences(
                    "SubastarPrefs", MODE_PRIVATE)
                    .getInt("FIREBASE_CLIENT_ID", -1);
            if (firebaseClienteId > 0) {
                com.google.firebase.messaging.FirebaseMessaging.getInstance()
                        .unsubscribeFromTopic(
                                "cliente_" + firebaseClienteId);
            }
            tokenManager.clearToken();
            getSharedPreferences("SubastarPrefs", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(PerfilActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Construir Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(SubastarApi.class);


        String nombre    = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_NAME", "");
        String email     = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_EMAIL", "");
        String categoria = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_CATEGORIA", "");
        String direccion = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_DIRECCION", "-");
        String pais      = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_PAIS", "-");
        String documento = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_DOCUMENTO", "-");
        String fotoPerfil = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_FOTO", null);
        clienteId        = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getInt("USER_ID", -1);


        tvNombreCompleto.setText(nombre);
        tvEmail.setText(email);
        tvCategoria.setText(categoria.isEmpty() ? "Sin categoría" : categoria);
        tvDireccion.setText(direccion);
        tvPais.setText(pais);
        tvDocumento.setText(documento);

        mostrarFotoPerfil(fotoPerfil);

        ivFotoPerfil.setOnClickListener(v -> seleccionarFotoLauncher.launch("image/*"));

        cargarMediosPago();
        cargarMultasPendientes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (api != null) cargarMultasPendientes();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            cargarMediosPago();
        }
    }

    private void cargarMediosPago() {
        String token = "Bearer " + tokenManager.getToken();

        api.obtenerMediosPago(clienteId, token).enqueue(new Callback<List<MedioPagoDTO>>() {
            @Override
            public void onResponse(Call<List<MedioPagoDTO>> call, Response<List<MedioPagoDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaMedios = response.body();
                    adapter = new MedioPagoAdapter(listaMedios, item -> confirmarEliminar(item));
                    recyclerMediosPago.setAdapter(adapter);
                } else {
                    Toast.makeText(PerfilActivity.this, "No se pudieron cargar los métodos de pago", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MedioPagoDTO>> call, Throwable t) {
                Toast.makeText(PerfilActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarMultasPendientes() {
        api.obtenerMultas("Bearer " + tokenManager.getToken())
                .enqueue(new Callback<List<MultaDTO>>() {
                    @Override
                    public void onResponse(
                            Call<List<MultaDTO>> call,
                            Response<List<MultaDTO>> response) {
                        if (!response.isSuccessful() || response.body() == null) return;
                        int pendientes = 0;
                        for (MultaDTO multa : response.body()) {
                            if ("pendiente".equalsIgnoreCase(multa.getEstado())) {
                                pendientes++;
                            }
                        }
                        tvMultasPendientes.setText(pendientes == 0
                                ? "Sin multas pendientes"
                                : pendientes + (pendientes == 1
                                        ? " multa pendiente de pago"
                                        : " multas pendientes de pago"));
                        tvBadgeMultas.setText(String.valueOf(pendientes));
                        tvBadgeMultas.setVisibility(
                                pendientes > 0 ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onFailure(Call<List<MultaDTO>> call, Throwable t) {
                    }
                });
    }

    private void mostrarFotoPerfil(String fotoUrl) {
        if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {
            ivFotoPerfil.setPadding(0, 0, 0, 0);

            Glide.with(this)
                    .load(fotoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivFotoPerfil);
        } else {
            ivFotoPerfil.setPadding(16, 16, 16, 16);
            ivFotoPerfil.setImageResource(R.drawable.ic_person);
        }
    }

    private void subirFotoPerfil(Uri uri) {
        try {
            File archivo = crearArchivoTemporalDesdeUri(uri);

            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) {
                mimeType = "image/jpeg";
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), archivo);
            MultipartBody.Part fotoPart = MultipartBody.Part.createFormData(
                    "foto",
                    archivo.getName(),
                    requestFile
            );

            String token = "Bearer " + tokenManager.getToken();

            api.actualizarFotoPerfil(token, fotoPart).enqueue(new Callback<com.grupo6.subastar.dto.ClienteDTO>() {
                @Override
                public void onResponse(Call<com.grupo6.subastar.dto.ClienteDTO> call, Response<com.grupo6.subastar.dto.ClienteDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String nuevaFoto = response.body().getFoto();

                        getSharedPreferences("SubastarPrefs", MODE_PRIVATE)
                                .edit()
                                .putString("USER_FOTO", nuevaFoto)
                                .apply();

                        mostrarFotoPerfil(nuevaFoto);
                        Toast.makeText(PerfilActivity.this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PerfilActivity.this, "No se pudo actualizar la foto", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.grupo6.subastar.dto.ClienteDTO> call, Throwable t) {
                    Toast.makeText(PerfilActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "No se pudo leer la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private File crearArchivoTemporalDesdeUri(Uri uri) throws Exception {
        String mimeType = getContentResolver().getType(uri);
        String extension = ".jpg";

        if ("image/png".equalsIgnoreCase(mimeType)) {
            extension = ".png";
        } else if ("image/webp".equalsIgnoreCase(mimeType)) {
            extension = ".webp";
        }

        File archivoTemporal = File.createTempFile("foto_perfil_", extension, getCacheDir());

        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new Exception("No se pudo abrir la imagen.");
        }

        OutputStream outputStream = new FileOutputStream(archivoTemporal);

        byte[] buffer = new byte[4096];
        int bytesLeidos;

        while ((bytesLeidos = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesLeidos);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return archivoTemporal;
    }

    private void confirmarEliminar(MedioPagoDTO item) {
        // Creamos el Dialog vacío
        final android.app.Dialog dialog = new android.app.Dialog(this);

        dialog.setContentView(R.layout.dialog_eliminar_medio);


        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        android.widget.Button btnCancelar = dialog.findViewById(R.id.btnCancelarEliminar);
        android.widget.Button btnConfirmar = dialog.findViewById(R.id.btnConfirmarEliminar);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            eliminarMedioPago(item); // Ejecuta el POST a la API
            dialog.dismiss();
        });

        dialog.show();
    }

    private void eliminarMedioPago(MedioPagoDTO item) {
        String token = "Bearer " + tokenManager.getToken();

        api.darDeBajaMedioPago(item.getIdentificador(), token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    mostrarDialogoExito("El medio de pago fue eliminado correctamente.");
                    cargarMediosPago();
                } else {
                    Toast.makeText(PerfilActivity.this, "No se pudo eliminar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(PerfilActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoExito(String mensaje) {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_exito);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        android.widget.TextView tvMensaje = dialog.findViewById(R.id.tvMensajeExito);
        tvMensaje.setText(mensaje);

        android.widget.Button btnAceptar = dialog.findViewById(R.id.btnAceptarExito);
        btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            // Refrescamos la lista DESPUÉS de que el usuario clickea "Aceptar"

        });

        dialog.show();
    }
}
