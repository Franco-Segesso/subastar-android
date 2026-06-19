package com.grupo6.subastar;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MiFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Firebase nos manda los datos en un formato "Data" (Clave-Valor)
        if (remoteMessage.getData().size() > 0) {
            String titulo = remoteMessage.getData().get("titulo");
            String mensaje = remoteMessage.getData().get("mensaje");

            // Llamamos a la clase que creamos en el paso anterior para que salte desde arriba
            GeneradorNotificacionesLocales.lanzarNotificacion(this, titulo, mensaje);
        }
    }
}