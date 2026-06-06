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
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PerfilActivity extends AppCompatActivity {

    // 1. Declaramos todas las vistas, incluyendo las nuevas de dirección, país y DNI
    private TextView tvNombreCompleto, tvEmail, tvCategoria, tvDireccion, tvPais, tvDocumento;
    private RecyclerView recyclerMediosPago;
    private MedioPagoAdapter adapter;
    private List<MedioPagoDTO> listaMedios = new ArrayList<>();
    private TokenManager tokenManager;
    private Integer clienteId;
    private SubastarApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        tokenManager = new TokenManager(this);

        // 2. Enlazamos las vistas con los IDs de tu XML
        tvNombreCompleto = findViewById(R.id.tvNombreCompleto);
        tvEmail          = findViewById(R.id.tvEmail);
        tvCategoria      = findViewById(R.id.tvCategoria);
        tvDireccion      = findViewById(R.id.tvDireccion);
        tvPais           = findViewById(R.id.tvPais);
        tvDocumento      = findViewById(R.id.tvDocumento);
        recyclerMediosPago = findViewById(R.id.recyclerMediosPago);

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
        ImageButton btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(v -> {
            tokenManager.clearToken();
            getSharedPreferences("SubastarPrefs", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(PerfilActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Construir Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(SubastarApi.class);

        // 3. Obtenemos TODOS los datos desde SharedPreferences (guardados en el Login)
        String nombre    = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_NAME", "");
        String email     = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_EMAIL", "");
        String categoria = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_CATEGORIA", "");
        String direccion = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_DIRECCION", "-");
        String pais      = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_PAIS", "-");
        String documento = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getString("USER_DOCUMENTO", "-");
        clienteId        = getSharedPreferences("SubastarPrefs", MODE_PRIVATE).getInt("USER_ID", -1);

        // 4. Se los inyectamos a los TextView de la pantalla
        tvNombreCompleto.setText(nombre);
        tvEmail.setText(email);
        tvCategoria.setText(categoria.isEmpty() ? "Sin categoría" : categoria);
        tvDireccion.setText(direccion);
        tvPais.setText(pais);
        tvDocumento.setText(documento);

        cargarMediosPago();
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

    private void confirmarEliminar(MedioPagoDTO item) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar método de pago")
                .setMessage("¿Estás seguro que querés eliminar este método de pago?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarMedioPago(item))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarMedioPago(MedioPagoDTO item) {
        String token = "Bearer " + tokenManager.getToken();

        api.darDeBajaMedioPago(item.getIdentificador(), token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PerfilActivity.this, "Método de pago eliminado", Toast.LENGTH_SHORT).show();
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
}