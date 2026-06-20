package com.grupo6.subastar;

import android.content.Context;
import android.content.Intent;

public final class NotificacionDestino {

    private NotificacionDestino() {
    }

    public static Intent crearIntent(
            Context context,
            String tipo,
            Integer referenciaId) {
        Intent intent;

        if ("CONSIGNACION".equalsIgnoreCase(tipo) && referenciaId != null) {
            intent = new Intent(context, DetalleConsignacionActivity.class);
            intent.putExtra("CONSIGNACION_ID", referenciaId);
        } else if ("GANADA".equalsIgnoreCase(tipo) && referenciaId != null) {
            intent = new Intent(context, FacturaCompraActivity.class);
            intent.putExtra("COMPRA_ID", referenciaId);
        } else if ("MULTA".equalsIgnoreCase(tipo)) {
            intent = new Intent(context, MultasActivity.class);
            if (referenciaId != null) {
                intent.putExtra("MULTA_ID", referenciaId);
            }
        } else {
            intent = new Intent(context, NotificacionesActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }
}
