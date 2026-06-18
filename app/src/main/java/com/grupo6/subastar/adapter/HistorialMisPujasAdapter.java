package com.grupo6.subastar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.grupo6.subastar.R;
import com.grupo6.subastar.dto.HistorialPujasClienteDTO;
import com.grupo6.subastar.util.FormatoPujas;
import java.util.ArrayList;
import java.util.List;

public class HistorialMisPujasAdapter
        extends RecyclerView.Adapter<HistorialMisPujasAdapter.ViewHolder> {

    private List<HistorialPujasClienteDTO.PujaDTO> items = new ArrayList<>();
    private String moneda = "";

    public void setItems(List<HistorialPujasClienteDTO.PujaDTO> nuevosItems, String moneda) {
        items = nuevosItems == null ? new ArrayList<>() : nuevosItems;
        this.moneda = moneda == null ? "" : moneda;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial_mis_puja, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistorialPujasClienteDTO.PujaDTO item = items.get(position);
        boolean ganadora = Boolean.TRUE.equals(item.getEsGanadora());
        holder.estado.setText(ganadora ? "GANADORA" : "PUJA #" + item.getOrden());
        holder.estado.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                ganadora ? R.color.secundario : R.color.texto_sec));
        holder.fecha.setText(FormatoPujas.fechaHoraPuja(item.getFechaHora()));
        holder.importe.setText(FormatoPujas.moneda(moneda, item.getImporte()));

        if (item.getSuperadaPor() == null) {
            holder.superada.setVisibility(View.GONE);
        } else {
            holder.superada.setVisibility(View.VISIBLE);
            holder.superada.setText("Superada por " + item.getSuperadaPor().getPostor()
                    + " · " + FormatoPujas.moneda(
                    moneda, item.getSuperadaPor().getImporte()));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView estado;
        final TextView fecha;
        final TextView superada;
        final TextView importe;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            estado = itemView.findViewById(R.id.tvHistorialEstado);
            fecha = itemView.findViewById(R.id.tvHistorialFecha);
            superada = itemView.findViewById(R.id.tvHistorialSuperada);
            importe = itemView.findViewById(R.id.tvHistorialImporte);
        }
    }
}
