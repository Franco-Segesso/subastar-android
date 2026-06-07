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

    public PujaHistorialAdapter(Integer miClienteId) {
    }

    public void setPujas(List<PujaMensajeDTO> nuevasPujas) {
        pujas.clear();
        if (nuevasPujas != null) {
            pujas.addAll(nuevasPujas);
        }
        notifyDataSetChanged();
    }

    public void agregarPuja(PujaMensajeDTO nuevaPuja) {
        if (nuevaPuja == null) return;
        pujas.add(0, nuevaPuja);
        notifyItemInserted(0);
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
        PujaMensajeDTO.ClienteDTO cliente = puja.getAsistente() != null ? puja.getAsistente().getCliente() : null;

        holder.tvMontoPuja.setText(String.format("USD %.2f", puja.getImporte()));
        holder.tvNombrePostor.setText(cliente != null ? cliente.getNombreCompleto() : "Postor");
        holder.tvHoraPuja.setText(formatearHora(puja.getFechaHora()));

        holder.tvEstadoPuja.setVisibility(View.VISIBLE);
        if (position == 0) {
            holder.tvEstadoPuja.setText("Mayor");
            holder.tvEstadoPuja.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.exito));
        } else {
            holder.tvEstadoPuja.setText("Superado");
            holder.tvEstadoPuja.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.texto_sec));
        }
    }

    @Override
    public int getItemCount() {
        return pujas.size();
    }

    private String formatearHora(String fechaHora) {
        if (fechaHora == null) return "Reciente";
        int inicioHora = fechaHora.indexOf('T');
        if (inicioHora < 0 || inicioHora + 6 > fechaHora.length()) return "Reciente";
        return fechaHora.substring(inicioHora + 1, inicioHora + 6);
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
