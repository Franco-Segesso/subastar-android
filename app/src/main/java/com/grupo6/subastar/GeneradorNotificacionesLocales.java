package com.grupo6.subastar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class GeneradorNotificacionesLocales {

    public static void lanzarNotificacion(Context context, String titulo, String mensaje) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "subastar_alertas_urgentes";

        // En Android 8+ es obligatorio crear un canal de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // IMPORTANCE_HIGH es lo que hace que salte desde arriba de la pantalla (Slide)
            NotificationChannel channel = new NotificationChannel(channelId, "Alertas Subastar", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        // Si el usuario toca la notificación del celular, que lo lleve a la Activity de Notificaciones
        Intent intent = new Intent(context, NotificacionesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notifications) // Icono que aparece en la barra superior
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Para versiones anteriores a Android 8
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Activa sonido y vibración por defecto
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // Se borra al tocarla

        // Lanzamos la notificación
        nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}