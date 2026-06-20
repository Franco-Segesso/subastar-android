package com.grupo6.subastar;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MiFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // ESTO NOS AVISARÁ EN EL LOGCAT SI EL MENSAJE LLEGÓ AL CELULAR
        Log.d("FIREBASE_TEST", "¡¡¡Mensaje recibido desde Firebase!!!");

        if (remoteMessage.getData().size() > 0) {
            Log.d("FIREBASE_TEST", "Datos recibidos: " + remoteMessage.getData());

            String titulo = remoteMessage.getData().get("titulo");
            String mensaje = remoteMessage.getData().get("mensaje");
            String tipo = remoteMessage.getData().get("tipo");
            Integer referenciaId = convertirEntero(
                    remoteMessage.getData().get("referenciaId"));

            GeneradorNotificacionesLocales.lanzarNotificacion(
                    this, titulo, mensaje, tipo, referenciaId);
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d("FIREBASE_TEST", "Nuevo token generado: " + token);
    }

    private Integer convertirEntero(String valor) {
        if (valor == null) return null;
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
