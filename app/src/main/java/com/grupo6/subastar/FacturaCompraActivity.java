package com.grupo6.subastar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.grupo6.subastar.dto.CompraDTO;
import com.grupo6.subastar.dto.MedioPagoDTO;
import com.grupo6.subastar.dto.ModalidadEntregaRequest;
import com.grupo6.subastar.dto.PagarCompraRequest;
import com.grupo6.subastar.util.FormatoPujas;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FacturaCompraActivity extends AppCompatActivity {

    private static final double COSTO_ENVIO_FIJO = 5000.0;
    private SubastarApi api;
    private TokenManager tokenManager;
    private int compraId;
    private TextView error;
    private TextView selectorMedio;
    private TextView btnEnvio;
    private TextView btnRetiro;
    private TextView btnFinalizar;
    private TextView tvSeguro;
    private LinearLayout contenedorModalidad;
    private CompraDTO compraActual;
    private final List<MedioPagoDTO> medios = new ArrayList<>();
    private MedioPagoDTO medioSeleccionado;
    private String modalidadSeleccionada;
    private boolean procesando;
    private boolean entregaRegistrada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factura_compra);

        compraId = getIntent().getIntExtra("COMPRA_ID", -1);
        if (compraId < 0) {
            finish();
            return;
        }

        tokenManager = new TokenManager(this);
        api = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SubastarApi.class);

        error = findViewById(R.id.tvFacturaError);
        selectorMedio = findViewById(R.id.btnSeleccionarMedioCompra);
        btnEnvio = findViewById(R.id.btnElegirEnvio);
        btnRetiro = findViewById(R.id.btnElegirRetiro);
        btnFinalizar = findViewById(R.id.btnFinalizarCompra);
        tvSeguro = findViewById(R.id.tvFacturaSeguro);
        contenedorModalidad = findViewById(R.id.contenedorModalidad);

        findViewById(R.id.btnVolverFactura).setOnClickListener(v -> finish());
        selectorMedio.setOnClickListener(v -> mostrarSelectorMedios());
        btnEnvio.setOnClickListener(v -> seleccionarModalidad("envio"));
        btnRetiro.setOnClickListener(v -> seleccionarModalidad("retiro"));

        cargarMedios();
        cargarCompra();
    }

    private void cargarCompra() {
        api.obtenerCompra(token(), compraId).enqueue(new Callback<CompraDTO>() {
            @Override
            public void onResponse(Call<CompraDTO> call, Response<CompraDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    error.setVisibility(View.GONE);
                    compraActual = response.body();
                    mostrarCompra(compraActual);
                    preseleccionarMedio();
                } else {
                    mostrarError(response.code() == 404
                            ? "La compra todavía no está disponible."
                            : "No se pudo cargar el detalle del pago.");
                }
            }

            @Override
            public void onFailure(Call<CompraDTO> call, Throwable t) {
                mostrarError("No se pudo conectar con el servidor.");
            }
        });
    }

    private void cargarMedios() {
        Integer clienteId = tokenManager.getClienteId();
        if (clienteId == null || clienteId <= 0) return;
        api.obtenerMediosPago(clienteId, token())
                .enqueue(new Callback<List<MedioPagoDTO>>() {
                    @Override
                    public void onResponse(
                            Call<List<MedioPagoDTO>> call,
                            Response<List<MedioPagoDTO>> response) {
                        medios.clear();
                        if (response.isSuccessful() && response.body() != null) {
                            for (MedioPagoDTO medio : response.body()) {
                                if ("si".equalsIgnoreCase(medio.getActivo())) {
                                    medios.add(medio);
                                }
                            }
                            if (medios.size() == 1) seleccionarMedio(medios.get(0));
                            preseleccionarMedio();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<MedioPagoDTO>> call, Throwable t) {
                        mostrarError("No se pudieron cargar tus medios de pago.");
                    }
                });
    }

    private void mostrarCompra(CompraDTO compra) {
        String moneda = compra.getSubasta() == null ? "" : compra.getSubasta().getMoneda();
        String nombreSubasta = compra.getSubasta() == null ? "Subasta" : compra.getSubasta().getNombre();
        String item = "Ítem";
        if (compra.getItem() != null) {
            item = "Ítem #" + compra.getItem().getNumeroPieza() + " · " + compra.getItem().getDescripcionCatalogo();
        }

        ((TextView) findViewById(R.id.tvFacturaSubasta)).setText(nombreSubasta);
        ((TextView) findViewById(R.id.tvFacturaItem)).setText(item);
        ((TextView) findViewById(R.id.tvFacturaImporte)).setText("Precio pujado\n" + FormatoPujas.moneda(moneda, compra.getImportePujado()));
        ((TextView) findViewById(R.id.tvFacturaComision)).setText("Comisión\n" + FormatoPujas.moneda(moneda, compra.getComision()));
        ((TextView) findViewById(R.id.tvFacturaEnvio)).setText(compra.getCostoEnvio() == null
                ? "Costo de envío\nSe define según la modalidad"
                : "Costo de envío\n" + FormatoPujas.moneda(moneda, compra.getCostoEnvio()));
        ((TextView) findViewById(R.id.tvFacturaTotal)).setText("TOTAL A PAGAR\n" + FormatoPujas.moneda(moneda, compra.getTotal()));
        tvSeguro.setText(compra.getAvisoSeguro() == null
                ? "El bien permanece asegurado mientras esta bajo custodia de la empresa."
                : compra.getAvisoSeguro());

        String modalidad = compra.getModalidadEntrega() == null ? "pendiente" : compra.getModalidadEntrega();
        modalidadSeleccionada = "pendiente".equalsIgnoreCase(modalidad) ? null : modalidad.toLowerCase(Locale.ROOT);
        entregaRegistrada = modalidadSeleccionada != null;
        actualizarModalidadVisual();

        TextView direccion = findViewById(R.id.tvFacturaDireccion);
        if (compra.getDireccionEnvio() == null || compra.getDireccionEnvio().isBlank()) {
            direccion.setVisibility(View.GONE);
        } else {
            direccion.setText("Dirección de envío: " + compra.getDireccionEnvio());
            direccion.setVisibility(View.VISIBLE);
        }

        boolean pagada = "pagada".equalsIgnoreCase(compra.getEstadoPago());

        // --- LOGICA MODIFICADA PARA EL PDF ---
        if (pagada) {
            btnFinalizar.setText("DESCARGAR FACTURA (PDF)");
            btnFinalizar.setEnabled(true);
            btnFinalizar.setOnClickListener(v -> generarYDescargarPDF());
            selectorMedio.setEnabled(false);
            contenedorModalidad.setVisibility(View.GONE);
        } else {
            btnFinalizar.setText("FINALIZAR COMPRA");
            btnFinalizar.setEnabled(true);
            btnFinalizar.setOnClickListener(v -> finalizarCompra());
            selectorMedio.setEnabled(true);
            contenedorModalidad.setVisibility(View.VISIBLE);
        }
    }

    // =================================================================================
    // GENERACIÓN NATIVA DE PDF EN ANDROID
    // =================================================================================
    private void generarYDescargarPDF() {
        if (compraActual == null) return;

        android.graphics.pdf.PdfDocument pdf = new android.graphics.pdf.PdfDocument();
        android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create(); // Tamaño A4
        android.graphics.pdf.PdfDocument.Page page = pdf.startPage(pageInfo);
        android.graphics.Canvas canvas = page.getCanvas();
        android.graphics.Paint paint = new android.graphics.Paint();

        // Título principal
        paint.setTextSize(22f);
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        canvas.drawText("FACTURA DE COMPRA - subastAR", 50, 80, paint);

        // Detalles
        paint.setTextSize(16f);
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL));
        int y = 140;

        String moneda = compraActual.getSubasta() != null ? compraActual.getSubasta().getMoneda() : "$";
        String nombreSubasta = compraActual.getSubasta() != null ? compraActual.getSubasta().getNombre() : "";
        String articulo = compraActual.getItem() != null ? compraActual.getItem().getDescripcionCatalogo() : "";

        canvas.drawText("Nro. de Operación: #" + compraActual.getIdentificador(), 50, y, paint); y += 40;
        canvas.drawText("Subasta: " + nombreSubasta, 50, y, paint); y += 40;
        canvas.drawText("Artículo: " + articulo, 50, y, paint); y += 60;

        // Desglose económico
        canvas.drawText("Precio pujado: " + FormatoPujas.moneda(moneda, compraActual.getImportePujado()), 50, y, paint); y += 40;
        canvas.drawText("Comisión de la casa: " + FormatoPujas.moneda(moneda, compraActual.getComision()), 50, y, paint); y += 40;
        canvas.drawText("Costo de envío/retiro: " + FormatoPujas.moneda(moneda, compraActual.getCostoEnvio()), 50, y, paint); y += 60;

        // Total
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        paint.setTextSize(20f);
        canvas.drawText("TOTAL ABONADO: " + FormatoPujas.moneda(moneda, compraActual.getTotal()), 50, y, paint);

        pdf.finishPage(page);
        guardarPDF(pdf);
    }

    private void guardarPDF(android.graphics.pdf.PdfDocument pdf) {
        String fileName = "FacturaSubastAR_" + compraId + ".pdf";
        try {
            java.io.OutputStream fos;

            // Lógica para Android 10+ (Evita pedir permisos especiales de almacenamiento)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.content.ContentValues values = new android.content.ContentValues();
                values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS);
                android.net.Uri uri = getContentResolver().insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                fos = getContentResolver().openOutputStream(uri);
            } else {
                // Lógica para Android 9 o inferior
                java.io.File dir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
                java.io.File file = new java.io.File(dir, fileName);
                fos = new java.io.FileOutputStream(file);
            }

            pdf.writeTo(fos);
            pdf.close();
            if (fos != null) fos.close();
            Toast.makeText(this, "Factura descargada en la carpeta de Descargas", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarModalError("Error al guardar PDF: " + e.getMessage());
        }
    }
    // =================================================================================

    private void mostrarSelectorMedios() {
        if (medios.isEmpty()) {
            mostrarError("No tenés medios de pago activos.");
            return;
        }
        String[] opciones = new String[medios.size()];
        for (int i = 0; i < medios.size(); i++) {
            opciones[i] = descripcionMedio(medios.get(i));
        }
        new AlertDialog.Builder(this)
                .setTitle("Seleccionar medio de pago")
                .setItems(opciones, (dialog, posicion) ->
                        seleccionarMedio(medios.get(posicion)))
                .show();
    }

    private void seleccionarMedio(MedioPagoDTO medio) {
        medioSeleccionado = medio;
        selectorMedio.setText(descripcionMedio(medio));
        selectorMedio.setTextColor(ContextCompat.getColor(this, R.color.secundario));
        selectorMedio.setBackgroundResource(R.drawable.bg_chip_activo);
    }

    private void preseleccionarMedio() {
        if (compraActual == null || compraActual.getMedioPagoId() == null
                || medioSeleccionado != null) return;
        for (MedioPagoDTO medio : medios) {
            if (compraActual.getMedioPagoId().equals(medio.getIdentificador())) {
                seleccionarMedio(medio);
                return;
            }
        }
    }

    private void seleccionarModalidad(String modalidad) {
        if (compraActual != null && "pagada".equalsIgnoreCase(compraActual.getEstadoPago())) {
            return;
        }
        modalidadSeleccionada = modalidad;
        actualizarImportesSegunModalidad();
        actualizarModalidadVisual();
    }

    private void actualizarModalidadVisual() {
        boolean envio = "envio".equals(modalidadSeleccionada);
        boolean retiro = "retiro".equals(modalidadSeleccionada);
        pintarOpcion(btnEnvio, envio);
        pintarOpcion(btnRetiro, retiro);
        ((TextView) findViewById(R.id.tvFacturaModalidad)).setText(
                modalidadSeleccionada == null
                        ? "Elegí cómo querés recibir el artículo"
                        : "Modalidad: " + modalidadSeleccionada.toUpperCase(Locale.ROOT));
        actualizarAvisoSeguro();
    }

    private void actualizarImportesSegunModalidad() {
        if (compraActual == null) return;
        double costoEnvio = "envio".equals(modalidadSeleccionada) ? COSTO_ENVIO_FIJO : 0.0;
        double importe = compraActual.getImportePujado() == null ? 0.0 : compraActual.getImportePujado();
        double comision = compraActual.getComision() == null ? 0.0 : compraActual.getComision();
        compraActual.setCostoEnvio(costoEnvio);
        compraActual.setTotal(importe + comision + costoEnvio);
        String moneda = compraActual.getSubasta() == null ? "" : compraActual.getSubasta().getMoneda();
        ((TextView) findViewById(R.id.tvFacturaEnvio)).setText("Costo de envio\n" + FormatoPujas.moneda(moneda, costoEnvio));
        ((TextView) findViewById(R.id.tvFacturaTotal)).setText("TOTAL A PAGAR\n" + FormatoPujas.moneda(moneda, compraActual.getTotal()));
    }

    private void pintarOpcion(TextView opcion, boolean seleccionada) {
        opcion.setBackgroundResource(seleccionada ? R.drawable.bg_chip_activo : R.drawable.bg_chip_inactivo);
        opcion.setTextColor(ContextCompat.getColor(this, seleccionada ? R.color.secundario : R.color.texto_ppal));
    }

    private void actualizarAvisoSeguro() {
        if (tvSeguro == null) return;
        if ("retiro".equals(modalidadSeleccionada)) {
            tvSeguro.setText("COBERTURA VIGENTE HASTA EL RETIRO\nFinaliza cuando la empresa te entrega el bien.");
        } else if ("envio".equals(modalidadSeleccionada)) {
            tvSeguro.setText("COBERTURA VIGENTE DURANTE EL TRASLADO\nFinaliza cuando el bien se entrega en tu domicilio.");
        } else if (compraActual != null && compraActual.getAvisoSeguro() != null) {
            tvSeguro.setText(compraActual.getAvisoSeguro());
        }
    }

    private void finalizarCompra() {
        if (procesando) return;
        if (medioSeleccionado == null) {
            mostrarError("Seleccioná un medio de pago.");
            return;
        }
        if (modalidadSeleccionada == null) {
            mostrarError("Seleccioná envío a domicilio o retiro en persona.");
            return;
        }

        String aviso = "retiro".equals(modalidadSeleccionada)
                ? "La cobertura permanecerá vigente hasta que retires el artículo y finalizará al recibirlo."
                : "La cobertura permanecerá vigente durante el traslado y finalizará al entregarse en tu domicilio. El envío está a cargo del comprador.";
        new AlertDialog.Builder(this)
                .setTitle("Confirmar compra")
                .setMessage(aviso)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Finalizar", (dialog, which) -> procesarCompra())
                .show();
    }

    private void procesarCompra() {
        procesando = true;
        btnFinalizar.setEnabled(false);
        String modalidadGuardada = compraActual == null ? null : compraActual.getModalidadEntrega();
        boolean cambioModalidad = modalidadGuardada == null || !modalidadSeleccionada.equalsIgnoreCase(modalidadGuardada);
        if (!entregaRegistrada || cambioModalidad) {
            guardarEntregaYPagar();
        } else {
            ejecutarPago();
        }
    }

    private void guardarEntregaYPagar() {
        api.definirEntregaCompra(token(), compraId, new ModalidadEntregaRequest(modalidadSeleccionada))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            entregaRegistrada = true;
                            if (compraActual != null) {
                                compraActual.setModalidadEntrega(modalidadSeleccionada);
                            }
                            ejecutarPago();
                        } else {
                            finalizarConError(leerError(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        finalizarConError("No se pudo registrar la entrega.");
                    }
                });
    }

    private void ejecutarPago() {
        api.pagarCompra(token(), compraId, new PagarCompraRequest(medioSeleccionado.getIdentificador()))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        procesando = false;
                        if (response.isSuccessful()) {
                            mostrarExito();
                        } else {
                            btnFinalizar.setEnabled(true);
                            mostrarModalError(leerError(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        procesando = false;
                        btnFinalizar.setEnabled(true);
                        mostrarModalError("No se pudo conectar con el servidor.");
                    }
                });
    }

    private void mostrarExitoAnterior() {
        new AlertDialog.Builder(this)
                .setTitle("La compra se realizó con éxito")
                .setMessage("El pago fue confirmado y la compra quedó registrada.")
                .setCancelable(false)
                .setPositiveButton("Ir a subastas", (dialog, which) -> {
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void mostrarExito() {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_compra_exitosa);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(
                            android.graphics.Color.TRANSPARENT));
        }

        TextView detalle = dialog.findViewById(R.id.tvCompraExitosaDetalle);
        String moneda = compraActual != null && compraActual.getSubasta() != null
                ? compraActual.getSubasta().getMoneda() : "";
        detalle.setText(
                "El pago fue confirmado por "
                        + FormatoPujas.moneda(
                                moneda,
                                compraActual == null
                                        ? null : compraActual.getTotal())
                        + ". Ya podes consultar y descargar tu factura.");

        dialog.findViewById(R.id.btnCompraExitosaFactura)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    cargarCompra();
                });
        dialog.findViewById(R.id.btnCompraExitosaSubastas)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    dialog.dismiss();
                });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void finalizarConError(String mensaje) {
        procesando = false;
        btnFinalizar.setEnabled(true);
        mostrarModalError(mensaje);
    }

    private void mostrarModalError(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("No se pudo completar la compra")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private String descripcionMedio(MedioPagoDTO medio) {
        if (medio == null || medio.getTipo() == null) return "Medio de pago";
        if ("tarjeta".equalsIgnoreCase(medio.getTipo())) {
            return "Tarjeta terminada en " + texto(medio.getUltimosDigitos());
        }
        if ("cuenta".equalsIgnoreCase(medio.getTipo())) {
            return "Cuenta " + texto(medio.getBanco())
                    + " · " + texto(medio.getMoneda())
                    + " " + numero(medio.getFondosReservados());
        }
        return "Cheque " + texto(medio.getNroCheque())
                + " · " + texto(medio.getMoneda())
                + " " + numero(medio.getMontoGarantia());
    }

    private String leerError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (Exception ignored) {
        }
        return "Error " + response.code();
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "--" : valor;
    }

    private String numero(Double valor) {
        return String.format(Locale.US, "%.2f", valor == null ? 0.0 : valor);
    }

    private void mostrarError(String mensaje) {
        error.setText(mensaje);
        error.setVisibility(View.VISIBLE);
    }

    private String token() {
        return "Bearer " + tokenManager.getToken();
    }
}
