package com.grupo6.subastar.util; // Si lo pones en la raíz, borrá el ".util"

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.grupo6.subastar.R;

public class DialogUtils {

    // Método para mostrar errores (reemplaza a los Toast comunes)
    public static void mostrarError(Context context, String mensaje) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_error);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvMensaje = dialog.findViewById(R.id.tvMensajeError);
        tvMensaje.setText(mensaje);

        Button btnEntendido = dialog.findViewById(R.id.btnEntendidoError);
        btnEntendido.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Método para mostrar éxitos (reemplaza a los Toast de éxito y permite ejecutar una acción al cerrar)
    public static void mostrarExito(Context context, String mensaje, Runnable onAceptarAccion) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_exito);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvMensaje = dialog.findViewById(R.id.tvMensajeExito);
        tvMensaje.setText(mensaje);

        Button btnAceptar = dialog.findViewById(R.id.btnAceptarExito);
        btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            // Ejecutamos lo que sea que haya que hacer después del éxito (ej: cambiar de pantalla)
            if (onAceptarAccion != null) {
                onAceptarAccion.run();
            }
        });

        dialog.show();
    }
}