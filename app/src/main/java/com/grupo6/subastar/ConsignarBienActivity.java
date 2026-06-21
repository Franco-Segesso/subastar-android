package com.grupo6.subastar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.grupo6.subastar.dto.ConsignacionDTO;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ConsignarBienActivity extends AppCompatActivity {

    private static final int REQ_FOTOS = 501;
    private EditText etTipoBien, etDescripcion, etArtista, etFechaCreacion, etHistoria;
    private TextView tvCantidadFotos;
    private CheckBox chkPropiedad;
    private final List<Uri> fotosSeleccionadas = new ArrayList<>();
    private TokenManager tokenManager;
    private SubastarApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consignar_bien);

        tokenManager = new TokenManager(this);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();

        api = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubastarApi.class);

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        etTipoBien = findViewById(R.id.etTipoBien);
        etDescripcion = findViewById(R.id.etDescripcion);
        etArtista = findViewById(R.id.etArtista);
        etFechaCreacion = findViewById(R.id.etFechaCreacion);
        etHistoria = findViewById(R.id.etHistoria);
        tvCantidadFotos = findViewById(R.id.tvCantidadFotos);
        chkPropiedad = findViewById(R.id.chkPropiedad);
        Button btnSeleccionarFotos = findViewById(R.id.btnSeleccionarFotos);
        Button btnEnviar = findViewById(R.id.btnEnviarSolicitud);

        btnVolver.setOnClickListener(v -> finish());
        btnSeleccionarFotos.setOnClickListener(v -> abrirSelectorFotos());
        btnEnviar.setOnClickListener(v -> enviarSolicitud(btnEnviar));
    }

    private void abrirSelectorFotos() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar fotos del bien"), REQ_FOTOS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_FOTOS || resultCode != RESULT_OK || data == null) return;

        if (data.getClipData() != null) {
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                agregarFotoSiNoExiste(data.getClipData().getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            agregarFotoSiNoExiste(data.getData());
        }
        actualizarCantidadFotos();
    }

    private void agregarFotoSiNoExiste(Uri uri) {
        if (uri == null) return;
        for (Uri foto : fotosSeleccionadas) {
            if (foto.equals(uri)) return;
        }
        fotosSeleccionadas.add(uri);
    }

    private void actualizarCantidadFotos() {
        int cantidad = fotosSeleccionadas.size();
        tvCantidadFotos.setText(cantidad + " de 6 fotos cargadas");
    }

    private void enviarSolicitud(Button btnEnviar) {
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Necesitas iniciar sesion.", Toast.LENGTH_LONG).show();
            return;
        }
        if (texto(etTipoBien).isEmpty() || texto(etDescripcion).isEmpty()) {
            Toast.makeText(this, "Completa tipo de bien y descripcion.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!chkPropiedad.isChecked()) {
            Toast.makeText(this, "Debes declarar la propiedad del bien.", Toast.LENGTH_LONG).show();
            return;
        }
        if (fotosSeleccionadas.size() < 6) {
            Toast.makeText(this, "Debes cargar al menos 6 fotos.", Toast.LENGTH_LONG).show();
            return;
        }

        List<MultipartBody.Part> fotos = partesFotos();
        if (fotos.size() != fotosSeleccionadas.size()) {
            Toast.makeText(this, "No se pudieron leer todas las fotos. Volve a seleccionarlas.", Toast.LENGTH_LONG).show();
            return;
        }

        btnEnviar.setEnabled(false);
        api.crearConsignacion(
                "Bearer " + token,
                parteTexto(texto(etTipoBien)),
                parteTexto(texto(etDescripcion)),
                parteTexto(texto(etArtista)),
                parteTexto(texto(etFechaCreacion)),
                parteTexto(texto(etHistoria)),
                parteTexto("true"),
                fotos
        ).enqueue(new Callback<ConsignacionDTO>() {
            @Override
            public void onResponse(Call<ConsignacionDTO> call, Response<ConsignacionDTO> response) {
                btnEnviar.setEnabled(true);
                if (response.isSuccessful()) {
                    startActivity(new Intent(ConsignarBienActivity.this, ConsignacionEnviadaActivity.class));
                    finish();
                } else {
                    String detalle = leerError(response);
                    Log.e("CONSIGNACION", "HTTP " + response.code() + ": " + detalle);
                    Toast.makeText(
                            ConsignarBienActivity.this,
                            detalle.isEmpty()
                                    ? "No se pudo enviar la solicitud (" + response.code() + ")."
                                    : detalle,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<ConsignacionDTO> call, Throwable t) {
                btnEnviar.setEnabled(true);
                Log.e("CONSIGNACION", "Error enviando solicitud", t);
                Toast.makeText(ConsignarBienActivity.this, "Error de conexion.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<MultipartBody.Part> partesFotos() {
        List<MultipartBody.Part> partes = new ArrayList<>();
        for (int i = 0; i < fotosSeleccionadas.size(); i++) {
            try {
                byte[] bytes = comprimirImagen(fotosSeleccionadas.get(i));
                RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
                partes.add(MultipartBody.Part.createFormData("fotos", "foto_" + i + ".jpg", body));
            } catch (Exception e) {
                Log.e("CONSIGNACION", "No se pudo preparar la foto " + i, e);
            }
        }
        return partes;
    }

    private byte[] comprimirImagen(Uri uri) throws Exception {
        BitmapFactory.Options limites = new BitmapFactory.Options();
        limites.inJustDecodeBounds = true;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(input, null, limites);
        }

        if (limites.outWidth <= 0 || limites.outHeight <= 0) {
            throw new IllegalArgumentException("La imagen no es valida");
        }

        int muestra = 1;
        while (limites.outWidth / muestra > 1600 || limites.outHeight / muestra > 1600) {
            muestra *= 2;
        }

        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = muestra;
        Bitmap bitmap;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(input, null, opciones);
        }
        if (bitmap == null) {
            throw new IllegalArgumentException("No se pudo decodificar la imagen");
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 82, buffer)) {
                throw new IllegalStateException("No se pudo comprimir la imagen");
            }
            return buffer.toByteArray();
        } finally {
            bitmap.recycle();
            buffer.close();
        }
    }

    private RequestBody parteTexto(String valor) {
        return RequestBody.create(MediaType.parse("text/plain"), valor == null ? "" : valor);
    }

    private String texto(EditText editText) {
        return editText.getText().toString().trim();
    }

    private String leerError(Response<?> response) {
        if (response.errorBody() == null) return "";
        try {
            String mensaje = new String(response.errorBody().bytes(), StandardCharsets.UTF_8).trim();
            return mensaje.replaceFirst("^\\d{3}:\\s*", "");
        } catch (Exception e) {
            return "";
        }
    }
}
