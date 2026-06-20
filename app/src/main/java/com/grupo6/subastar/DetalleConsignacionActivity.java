package com.grupo6.subastar;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.grupo6.subastar.dto.ConsignacionDTO;
import com.grupo6.subastar.dto.RespuestaConsignacionRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import okhttp3.ResponseBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetalleConsignacionActivity extends AppCompatActivity {

    private static final int REQ_DOCUMENTOS = 702;
    private Integer consignacionId;
    private TokenManager tokenManager;
    private SubastarApi api;
    private TextView tvTitulo, tvCondiciones, tvUbicacionTitulo, tvUbicacion;
    private TextView tvSeguroTitulo, tvSeguro;
    private TextView tvIconoEstado, tvMotivoRechazo;
    private TextView tvCostoDevolucion, tvInstruccionDevolucion;
    private Button btnContactarAseguradora;
    private LinearLayout layoutTimeline, layoutCondiciones, layoutAcciones;
    private LinearLayout layoutDocumentacion;
    private LinearLayout layoutRechazada;
    private TextView tvMotivoDocumentacion, tvCantidadDocumentos, tvDocumentosPresentados;
    private EditText etDescripcionDocumentacion;
    private Button btnSeleccionarDocumentos, btnEnviarDocumentacion;
    private final List<Uri> documentosSeleccionados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_consignacion);

        consignacionId = getIntent().getIntExtra("CONSIGNACION_ID", -1);
        tokenManager = new TokenManager(this);
        api = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubastarApi.class);

        ImageButton btnVolver = findViewById(R.id.btnVolver);
        Button btnAceptar = findViewById(R.id.btnAceptarCondiciones);
        Button btnRechazar = findViewById(R.id.btnRechazarCondiciones);
        tvTitulo = findViewById(R.id.tvDetalleTituloBien);
        tvIconoEstado = findViewById(R.id.tvDetalleIconoEstado);
        tvMotivoRechazo = findViewById(R.id.tvMotivoRechazoConsignacion);
        tvCostoDevolucion = findViewById(R.id.tvCostoDevolucionConsignacion);
        tvInstruccionDevolucion =
                findViewById(R.id.tvInstruccionDevolucionConsignacion);
        tvCondiciones = findViewById(R.id.tvCondicionesDetalle);
        tvUbicacionTitulo = findViewById(R.id.tvUbicacionTitulo);
        tvUbicacion = findViewById(R.id.tvUbicacionDetalle);
        tvSeguroTitulo = findViewById(R.id.tvSeguroTitulo);
        tvSeguro = findViewById(R.id.tvSeguroDetalle);
        btnContactarAseguradora = findViewById(R.id.btnContactarAseguradora);
        layoutDocumentacion = findViewById(R.id.layoutDocumentacionOrigen);
        layoutRechazada = findViewById(R.id.layoutConsignacionRechazada);
        tvMotivoDocumentacion = findViewById(R.id.tvMotivoDocumentacion);
        tvCantidadDocumentos = findViewById(R.id.tvCantidadDocumentos);
        tvDocumentosPresentados = findViewById(R.id.tvDocumentosPresentados);
        etDescripcionDocumentacion = findViewById(R.id.etDescripcionDocumentacion);
        btnSeleccionarDocumentos = findViewById(R.id.btnSeleccionarDocumentos);
        btnEnviarDocumentacion = findViewById(R.id.btnEnviarDocumentacion);
        layoutTimeline = findViewById(R.id.layoutTimeline);
        layoutCondiciones = findViewById(R.id.layoutCondiciones);
        layoutAcciones = findViewById(R.id.layoutAccionesCondiciones);

        btnVolver.setOnClickListener(v -> finish());
        btnAceptar.setOnClickListener(v -> responder(true));
        btnRechazar.setOnClickListener(v -> responder(false));
        btnSeleccionarDocumentos.setOnClickListener(v -> seleccionarDocumentos());
        btnEnviarDocumentacion.setOnClickListener(v -> enviarDocumentacion());

        cargarDetalle();
    }

    private void cargarDetalle() {
        String token = tokenManager.getToken();
        if (token == null || consignacionId == null || consignacionId <= 0) {
            finish();
            return;
        }
        api.obtenerDetalleConsignacion("Bearer " + token, consignacionId).enqueue(new Callback<ConsignacionDTO>() {
            @Override
            public void onResponse(Call<ConsignacionDTO> call, Response<ConsignacionDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    pintar(response.body());
                } else {
                    Toast.makeText(DetalleConsignacionActivity.this, "No se pudo cargar el detalle.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ConsignacionDTO> call, Throwable t) {
                Toast.makeText(DetalleConsignacionActivity.this, "Error de conexion.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void pintar(ConsignacionDTO c) {
        String nombre = c.getProducto() != null ? c.getProducto().getDescripcion() : "Bien consignado";
        tvTitulo.setText(nombre);
        boolean rechazada = "rechazado".equalsIgnoreCase(c.getEstado());
        if (rechazada) {
            tvIconoEstado.setText("X");
            tvIconoEstado.setTextColor(getResources().getColor(R.color.error));
            tvIconoEstado.setBackgroundResource(R.drawable.bg_circulo_error);
            layoutRechazada.setVisibility(View.VISIBLE);
            tvMotivoRechazo.setText(
                    "Motivo:\n" + texto(c.getMotivoRechazo()));
            tvCostoDevolucion.setText(
                    "Costo de devolucion: "
                            + importe(c.getMonedaDevolucion(), c.getCostoDevolucion()));
            tvInstruccionDevolucion.setText(
                    texto(c.getInstruccionDevolucion()));
        } else {
            // Dejamos el texto vacío para que no se superponga y se vea solo el tilde del fondo
            tvIconoEstado.setText("");
            tvIconoEstado.setBackgroundResource(R.drawable.bg_success_icon);
            layoutRechazada.setVisibility(View.GONE);
        }
        layoutTimeline.removeAllViews();
        if (c.getInstancias() != null) {
            for (ConsignacionDTO.InstanciaDTO instancia : c.getInstancias()) {
                TextView row = new TextView(this);
                String marca = Boolean.TRUE.equals(instancia.getCompletada()) ? "✓  " : "○  ";
                row.setText(marca + instancia.getTitulo() + "\n" + instancia.getFecha());
                row.setTextColor(getResources().getColor(R.color.texto_ppal));
                row.setTextSize(15);
                row.setPadding(12, 10, 12, 10);
                layoutTimeline.addView(row);
            }
        }

        boolean condicionesPendientes = "aceptado".equalsIgnoreCase(c.getEstado())
                && c.getSeguro() != null
                && c.getCondicionesEmpresa() != null
                && c.getCondicionesEmpresa().getPrecioBase() != null
                && c.getCondicionesEmpresa().getComisionEmpresa() != null
                && c.getCondicionesEmpresa().getSubastaAsignada() != null
                && !Boolean.TRUE.equals(c.getCondicionesAceptadas());
        if (c.getCondicionesEmpresa() != null) {
            layoutCondiciones.setVisibility(View.VISIBLE);
            ConsignacionDTO.CondicionesEmpresaDTO cond = c.getCondicionesEmpresa();
            tvCondiciones.setText(
                    "Precio base\n" + importe(cond.getMoneda(), cond.getPrecioBase()) + "\n\n" +
                    "Comision empresa\n" + porcentaje(cond.getComisionEmpresa()) + "\n\n" +
                    "Poliza\n" + texto(cond.getSeguroPoliza()) + "\n\n" +
                    "Compania aseguradora\n" + texto(cond.getContactoPoliza()) + "\n\n" +
                    "Subasta asignada\n" + texto(cond.getSubastaAsignada()));
        }
        configurarDocumentacion(c);
        if (c.getUbicacionDeposito() != null) {
            tvUbicacionTitulo.setVisibility(View.VISIBLE);
            tvUbicacion.setVisibility(View.VISIBLE);
            tvUbicacion.setText(c.getUbicacionDeposito().getNombre() + "\n" + c.getUbicacionDeposito().getDireccion());
        }
        if (c.getSeguro() != null) {
            ConsignacionDTO.SeguroDTO seguro = c.getSeguro();
            tvSeguroTitulo.setVisibility(View.VISIBLE);
            tvSeguro.setVisibility(View.VISIBLE);
            tvSeguro.setText(
                    "Nro. de poliza\n" + texto(seguro.getNroPoliza()) + "\n\n" +
                    "Compania\n" + texto(seguro.getCompania()) + "\n\n" +
                    "Valor asegurado\n" + importe(seguro.getMoneda(), seguro.getImporte()) + "\n\n" +
                    "Cobertura combinada\n" +
                    ("si".equalsIgnoreCase(seguro.getPolizaCombinada()) ? "Si" : "No"));
            if (seguro.getCompania() != null && !seguro.getCompania().isBlank()) {
                btnContactarAseguradora.setVisibility(View.VISIBLE);
                btnContactarAseguradora.setOnClickListener(v ->
                        abrirContactoAseguradora(seguro.getCompania()));
            }
        }
        layoutAcciones.setVisibility(condicionesPendientes ? View.VISIBLE : View.GONE);
    }

    private void configurarDocumentacion(ConsignacionDTO c) {
        boolean pendiente = "documentacion_pendiente".equalsIgnoreCase(c.getEstado());
        boolean presentada = "documentacion_presentada".equalsIgnoreCase(c.getEstado());
        if (!pendiente && !presentada) {
            layoutDocumentacion.setVisibility(View.GONE);
            return;
        }

        layoutDocumentacion.setVisibility(View.VISIBLE);
        tvMotivoDocumentacion.setText(
                pendiente
                        ? "La empresa necesita la siguiente acreditacion:\n"
                            + texto(c.getMotivoDocumentacion())
                        : "Documentacion enviada. La empresa esta revisando los archivos.");

        btnSeleccionarDocumentos.setVisibility(pendiente ? View.VISIBLE : View.GONE);
        tvCantidadDocumentos.setVisibility(pendiente ? View.VISIBLE : View.GONE);
        etDescripcionDocumentacion.setVisibility(pendiente ? View.VISIBLE : View.GONE);
        btnEnviarDocumentacion.setVisibility(pendiente ? View.VISIBLE : View.GONE);

        if (presentada) {
            tvDocumentosPresentados.setVisibility(View.VISIBLE);
            StringBuilder detalle = new StringBuilder("Archivos presentados:");
            if (c.getDocumentosOrigen() != null) {
                for (ConsignacionDTO.DocumentoDTO documento : c.getDocumentosOrigen()) {
                    detalle.append("\n- ").append(texto(documento.getNombreArchivo()));
                }
            }
            tvDocumentosPresentados.setText(detalle.toString());
        } else {
            tvDocumentosPresentados.setVisibility(View.GONE);
        }
    }

    private void seleccionarDocumentos() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES,
                new String[]{"application/pdf", "image/*"});
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(
                Intent.createChooser(intent, "Seleccionar documentacion"),
                REQ_DOCUMENTOS);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_DOCUMENTOS
                || resultCode != RESULT_OK
                || data == null) return;

        if (data.getClipData() != null) {
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                agregarDocumento(data.getClipData().getItemAt(i).getUri());
            }
        } else if (data.getData() != null) {
            agregarDocumento(data.getData());
        }
        tvCantidadDocumentos.setText(
                documentosSeleccionados.size() + " archivos seleccionados");
    }

    private void agregarDocumento(Uri uri) {
        if (uri == null || documentosSeleccionados.contains(uri)) return;
        try {
            getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
        }
        documentosSeleccionados.add(uri);
    }

    private void enviarDocumentacion() {
        if (documentosSeleccionados.isEmpty()) {
            Toast.makeText(this,
                    "Selecciona al menos un PDF o imagen.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        List<MultipartBody.Part> partes = new ArrayList<>();
        try {
            for (Uri uri : documentosSeleccionados) {
                String nombre = nombreArchivo(uri);
                String tipo = tipoArchivo(uri, nombre);
                RequestBody body = RequestBody.create(
                        MediaType.parse(tipo), leerBytes(uri));
                partes.add(MultipartBody.Part.createFormData(
                        "archivos", nombre, body));
            }
        } catch (Exception e) {
            Log.e("DOC_CONSIGNACION", "No se pudieron leer los archivos", e);
            Toast.makeText(this,
                    "No se pudieron leer todos los archivos.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        btnEnviarDocumentacion.setEnabled(false);
        RequestBody descripcion = RequestBody.create(
                MediaType.parse("text/plain"),
                etDescripcionDocumentacion.getText().toString().trim());
        api.registrarDocumentacionOrigen(
                "Bearer " + tokenManager.getToken(),
                consignacionId,
                partes,
                descripcion
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(
                    Call<ResponseBody> call,
                    Response<ResponseBody> response) {
                btnEnviarDocumentacion.setEnabled(true);
                if (response.isSuccessful()) {
                    documentosSeleccionados.clear();
                    Toast.makeText(
                            DetalleConsignacionActivity.this,
                            "Documentacion enviada correctamente.",
                            Toast.LENGTH_LONG).show();
                    cargarDetalle();
                } else {
                    Toast.makeText(
                            DetalleConsignacionActivity.this,
                            leerError(response),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnEnviarDocumentacion.setEnabled(true);
                Toast.makeText(
                        DetalleConsignacionActivity.this,
                        "Error de conexion al enviar la documentacion.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private byte[] leerBytes(Uri uri) throws Exception {
        try (InputStream input = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            if (input == null) throw new IllegalArgumentException("Archivo invalido");
            byte[] buffer = new byte[8192];
            int leidos;
            while ((leidos = input.read(buffer)) != -1) {
                salida.write(buffer, 0, leidos);
            }
            return salida.toByteArray();
        }
    }

    private String nombreArchivo(Uri uri) {
        try (Cursor cursor = getContentResolver().query(
                uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int indice = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (indice >= 0) {
                    String nombre = cursor.getString(indice);
                    if (nombre != null && !nombre.isBlank()) return nombre;
                }
            }
        }
        return "documento";
    }

    private String tipoArchivo(Uri uri, String nombre) {
        String tipo = getContentResolver().getType(uri);
        if (tipo != null) return tipo;
        String minuscula = nombre.toLowerCase(Locale.ROOT);
        if (minuscula.endsWith(".pdf")) return "application/pdf";
        if (minuscula.endsWith(".png")) return "image/png";
        return "image/jpeg";
    }

    private String leerError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string().replaceFirst("^\\d{3}:\\s*", "");
            }
        } catch (Exception ignored) {
        }
        return "No se pudo enviar la documentacion.";
    }

    private void responder(boolean acepta) {
        String token = "Bearer " + tokenManager.getToken();
        api.responderConsignacion(token, consignacionId, new RespuestaConsignacionRequest(acepta)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (acepta) {
                        Intent intent = new Intent(DetalleConsignacionActivity.this, CuentaDestinoConsignacionActivity.class);
                        intent.putExtra("CONSIGNACION_ID", consignacionId);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(DetalleConsignacionActivity.this, ResultadoConsignacionActivity.class);
                        intent.putExtra("RESULTADO", "rechazada");
                        startActivity(intent);
                    }
                    finish();
                } else {
                    Toast.makeText(DetalleConsignacionActivity.this, "No se pudo registrar la respuesta.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(DetalleConsignacionActivity.this, "Error de conexion.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String entero(Double valor) {
        if (valor == null) return "-";
        return String.valueOf(valor.intValue());
    }

    private String porcentaje(Double valor) {
        return valor == null ? "A confirmar" : entero(valor) + "%";
    }

    private String importe(String moneda, Double valor) {
        if (valor == null) return "A confirmar";
        NumberFormat formato = NumberFormat.getNumberInstance(new Locale("es", "AR"));
        formato.setMaximumFractionDigits(2);
        formato.setMinimumFractionDigits(0);
        String prefijo = moneda == null || moneda.isBlank() ? "" : moneda + " ";
        return prefijo + formato.format(valor);
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "A confirmar" : valor;
    }

    private void abrirContactoAseguradora(String compania) {
        Uri busqueda = Uri.parse(
                "https://www.google.com/search?q="
                        + Uri.encode("contacto oficial aseguradora " + compania));
        Intent intent = new Intent(Intent.ACTION_VIEW, busqueda);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this,
                    "No se pudo abrir el contacto de la aseguradora.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
