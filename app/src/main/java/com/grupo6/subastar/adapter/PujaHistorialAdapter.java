package com.grupo6.subastar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.grupo6.subastar.R;
import com.grupo6.subastar.dto.PujaMensajeDTO;
import java.util.ArrayList;
import java.util.List;

public class PujaHistorialAdapter extends RecyclerView.Adapter<PujaHistorialAdapter.ViewHolder> {

    private final List<PujaMensajeDTO> pujas = new ArrayList<>();
    private final Integer miClienteId; // Para saber si yo soy el que va ganando

    public PujaHistorialAdapter(Integer miClienteId) {
        this.miClienteId = miClienteId;
    }

    // Método para agregar una nueva puja al tope de la lista
    public void agregarPuja(PujaMensajeDTO nuevaPuja) {
        pujas.add(0, nuevaPuja);
        notifyItemInserted(0);
        // Notificamos al anterior "ganador" que ahora fue superado
        if (pujas.size() > 1) notifyItemChanged(1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_puja_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PujaMensajeDTO puja = pujas.get(position);

        holder.tvMontoPuja.setText(String.format("USD %.2f", puja.getImporte()));

        // Simulación de nombre y hora (debes ajustarlo según cómo te llegue del JSON)
        holder.tvNombrePostor.setText("Postor #" + puja.getAsistente().getCliente().getIdentificador());
        holder.tvHoraPuja.setText("Reciente");

        // Lógica visual: Solo el primero de la lista está "Ganando"
        if (position == 0) {
            holder.tvEstadoPuja.setVisibility(View.VISIBLE);
            holder.tvEstadoPuja.setText("Mayor");
            holder.tvEstadoPuja.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.exito));
        } else {
            holder.tvEstadoPuja.setVisibility(View.VISIBLE);
            holder.tvEstadoPuja.setText("Superado");
            holder.tvEstadoPuja.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.texto_sec));
        }
    }

    @Override
    public int getItemCount() {
        return pujas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombrePostor, tvHoraPuja, tvMontoPuja, tvEstadoPuja;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombrePostor = itemView.findViewById(R.id.tvNombrePostor);
            tvHoraPuja = itemView.findViewById(R.id.tvHoraPuja);
            tvMontoPuja = itemView.findViewById(R.id.tvMontoPuja);
            tvEstadoPuja = itemView.findViewById(R.id.tvEstadoPuja);
        }
    }
}