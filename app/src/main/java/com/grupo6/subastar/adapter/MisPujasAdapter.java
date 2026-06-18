package com.grupo6.subastar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.grupo6.subastar.R;
import com.grupo6.subastar.dto.SubastaParticipacionDTO;
import com.grupo6.subastar.util.FormatoPujas;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MisPujasAdapter extends RecyclerView.Adapter<MisPujasAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(SubastaParticipacionDTO item);
    }

    private final OnClickListener listener;
    private List<SubastaParticipacionDTO> items = new ArrayList<>();

    public MisPujasAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<SubastaParticipacionDTO> nuevosItems) {
        items = nuevosItems == null ? new ArrayList<>() : nuevosItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mis_puja, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubastaParticipacionDTO item = items.get(position);
        SubastaParticipacionDTO.SubastaDTO subasta = item.getSubasta();
        boolean ganada = Boolean.TRUE.equals(item.getGano());
        String moneda = subasta == null ? "" : subasta.getMoneda();

        holder.fecha.setText(subasta == null ? "--" :
                FormatoPujas.fechaHoraSubasta(subasta.getFecha(), subasta.getHora()));
        holder.nombre.setText(subasta == null ? "Subasta" : subasta.getNombre());
        holder.detalle.setText(detalleSubasta(item));
        holder.resultado.setText(ganada ? "GANADA" : "PERDIDA");
        holder.resultado.setBackgroundResource(
                ganada ? R.drawable.bg_chip_activo : R.drawable.bg_chip_inactivo);
        holder.resultado.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                ganada ? R.color.secundario : R.color.error));
        holder.cantidad.setText("Mis pujas\n" + valor(item.getCantidadPujas()));
        holder.mayorOferta.setText("Mayor oferta\n"
                + FormatoPujas.moneda(moneda, item.getMayorOfertaRealizada()));
        holder.pagado.setText("Importe pagado\n"
                + (ganada ? FormatoPujas.moneda(moneda, item.getImportePagado()) : "—"));
        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String detalleSubasta(SubastaParticipacionDTO item) {
        SubastaParticipacionDTO.SubastaDTO subasta = item.getSubasta();
        if (subasta == null) return "";
        String categoria = subasta.getCategoria() == null
                ? ""
                : subasta.getCategoria().toUpperCase(Locale.ROOT);
        String lote = item.getLoteGanado() == null ? "" : " · Ítem #" + item.getLoteGanado();
        return categoria + " · " + subasta.getMoneda() + lote;
    }

    private int valor(Integer numero) {
        return numero == null ? 0 : numero;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView fecha;
        final TextView nombre;
        final TextView resultado;
        final TextView detalle;
        final TextView cantidad;
        final TextView mayorOferta;
        final TextView pagado;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            fecha = itemView.findViewById(R.id.tvPujaFecha);
            nombre = itemView.findViewById(R.id.tvPujaNombre);
            resultado = itemView.findViewById(R.id.tvPujaResultado);
            detalle = itemView.findViewById(R.id.tvPujaDetalle);
            cantidad = itemView.findViewById(R.id.tvPujaCantidad);
            mayorOferta = itemView.findViewById(R.id.tvPujaMayorOferta);
            pagado = itemView.findViewById(R.id.tvPujaPagado);
        }
    }
}
