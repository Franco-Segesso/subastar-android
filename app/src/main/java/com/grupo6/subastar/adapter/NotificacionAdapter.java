package com.grupo6.subastar.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.grupo6.subastar.R;
import com.grupo6.subastar.dto.NotificacionDTO;
import java.util.List;

public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.ViewHolder> {

    private List<NotificacionDTO> notificaciones;
    private Context context;
    private OnNotificacionClickListener listener;

    // Interfaz para delegar el clic a la Activity
    public interface OnNotificacionClickListener {
        void onNotificacionClick(NotificacionDTO notificacion);
    }

    public NotificacionAdapter(List<NotificacionDTO> notificaciones, Context context, OnNotificacionClickListener listener) {
        this.notificaciones = notificaciones;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notificacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificacionDTO notif = notificaciones.get(position);

        holder.tvTitulo.setText(notif.getTitulo());
        holder.tvMensaje.setText(notif.getMensaje());

        // Formateo simple de fecha (de "2026-06-12T08:38:00" a "2026-06-12 08:38")
        if (notif.getFechaEnvio() != null) {
            holder.tvAccion.setText(notif.getFechaEnvio().replace("T", " ").substring(0, 16));
        }

        // --- LÓGICA DE ESTADO (LEÍDO VS NO LEÍDO) ---
        if (notif.getLeido()) {
            holder.cardNotificacion.setCardBackgroundColor(Color.TRANSPARENT);
            holder.tvTitulo.setAlpha(0.7f);
        } else {
            // Fondo ligeramente gris/primario para resaltar las nuevas
            holder.cardNotificacion.setCardBackgroundColor(Color.parseColor("#1A000000")); // Ajustá según tu paleta
            holder.tvTitulo.setAlpha(1.0f);
        }

        // --- MÁQUINA DE ESTADOS VISUAL POR TIPO ---
        switch (notif.getTipo()) {
            case "MULTA":
                holder.ivIcono.setImageResource(android.R.drawable.ic_dialog_alert); // Icono de alerta
                holder.ivIcono.setColorFilter(Color.RED);
                break;
            case "GANADA":
                holder.ivIcono.setImageResource(android.R.drawable.ic_input_add); // O un ícono de trofeo/billete
                holder.ivIcono.setColorFilter(Color.parseColor("#FFD700")); // Dorado
                break;
            default:
                holder.ivIcono.setImageResource(android.R.drawable.ic_dialog_info);
                holder.ivIcono.setColorFilter(Color.GRAY);
                break;
        }

        // Configurar el clic
        holder.itemView.setOnClickListener(v -> listener.onNotificacionClick(notif));
    }

    @Override
    public int getItemCount() {
        return notificaciones != null ? notificaciones.size() : 0;
    }

    // Método para actualizar la lista desde la Activity
    public void setNotificaciones(List<NotificacionDTO> nuevasNotificaciones) {
        this.notificaciones = nuevasNotificaciones;
        notifyDataSetChanged();
    }

    public List<NotificacionDTO> getNotificaciones() {
        return this.notificaciones;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardNotificacion;
        ImageView ivIcono;
        TextView tvTitulo, tvMensaje, tvAccion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNotificacion = itemView.findViewById(R.id.cardNotificacion);
            ivIcono = itemView.findViewById(R.id.ivIconoNotif);
            tvTitulo = itemView.findViewById(R.id.tvTituloItem);
            tvMensaje = itemView.findViewById(R.id.tvMensajeItem);
            tvAccion = itemView.findViewById(R.id.tvAccionItem);
        }
    }
}