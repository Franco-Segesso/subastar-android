package com.grupo6.subastar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.grupo6.subastar.R;
import com.grupo6.subastar.dto.ConsignacionDTO;
import java.util.ArrayList;
import java.util.List;

public class ConsignacionAdapter extends RecyclerView.Adapter<ConsignacionAdapter.ViewHolder> {

    public interface OnConsignacionClick {
        void onClick(ConsignacionDTO consignacion);
    }

    private List<ConsignacionDTO> items = new ArrayList<>();
    private final OnConsignacionClick listener;

    public ConsignacionAdapter(OnConsignacionClick listener) {
        this.listener = listener;
    }

    public void setItems(List<ConsignacionDTO> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consignacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConsignacionDTO item = items.get(position);
        String titulo = item.getProducto() != null && item.getProducto().getDescripcion() != null
                ? item.getProducto().getDescripcion()
                : "Bien consignado";
        String tipo = item.getProducto() != null && item.getProducto().getTipoBien() != null
                ? item.getProducto().getTipoBien()
                : "Bien";

        holder.tvTitulo.setText(titulo);
        String estadoVisible = estadoVisible(item);
        holder.tvEstado.setText(estadoVisible);
        pintarEstado(holder, estadoVisible);
        holder.tvSubtitulo.setText(tipo + " - Enviado: " + fechaCorta(item.getFechaSolicitud()));
        holder.tvPrecio.setText("Precio base\n" + precioBase(item));
        if (tieneSubastaAsignada(item)) {
            holder.tvSubasta.setVisibility(View.VISIBLE);
            holder.tvSubasta.setText("Subasta\n" + subastaAsignada(item));
        } else {
            holder.tvSubasta.setVisibility(View.GONE);
        }
        holder.tvUbicacion.setText(ubicacion(item));
        holder.btnVerDetalle.setOnClickListener(v -> listener.onClick(item));
        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String estadoVisible(ConsignacionDTO item) {
        String estado = item.getEstado() == null ? "" : item.getEstado().toLowerCase();
        if ("rechazado".equals(estado)) return "RECHAZADA";
        if ("aceptado".equals(estado) && Boolean.TRUE.equals(item.getCondicionesAceptadas())) return "EN SUBASTA";
        if ("aceptado".equals(estado)) return "ACEPTADA";
        return "ACTIVA";
    }

    private void pintarEstado(ViewHolder holder, String estado) {
        int fondo = R.drawable.bg_chip_inactivo;
        int texto = R.color.texto_sec;
        if ("ACTIVA".equals(estado) || "ACEPTADA".equals(estado) || "EN SUBASTA".equals(estado)) {
            fondo = R.drawable.bg_chip_activo;
            texto = R.color.secundario;
        } else if ("RECHAZADA".equals(estado)) {
            texto = R.color.error;
        }
        holder.tvEstado.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), fondo));
        holder.tvEstado.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), texto));
    }

    private String precioBase(ConsignacionDTO item) {
        if (item.getCondicionesEmpresa() != null && item.getCondicionesEmpresa().getPrecioBase() != null) {
            return "USD " + String.format("%.0f", item.getCondicionesEmpresa().getPrecioBase());
        }
        return "Pendiente";
    }

    private boolean tieneSubastaAsignada(ConsignacionDTO item) {
        return item.getCondicionesEmpresa() != null
                && item.getCondicionesEmpresa().getSubastaAsignada() != null
                && !item.getCondicionesEmpresa().getSubastaAsignada().trim().isEmpty()
                && Boolean.TRUE.equals(item.getCondicionesAceptadas());
    }

    private String subastaAsignada(ConsignacionDTO item) {
        return item.getCondicionesEmpresa().getSubastaAsignada();
    }

    private String ubicacion(ConsignacionDTO item) {
        if (item.getUbicacionDeposito() != null && item.getUbicacionDeposito().getNombre() != null) {
            return item.getUbicacionDeposito().getNombre();
        }
        return "Ubicacion pendiente";
    }

    private String fechaCorta(String fecha) {
        if (fecha == null || fecha.length() < 10) return "--";
        return fecha.substring(0, 10);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvEstado, tvSubtitulo, tvPrecio, tvSubasta, tvUbicacion, btnVerDetalle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvConsignacionTitulo);
            tvEstado = itemView.findViewById(R.id.tvConsignacionEstado);
            tvSubtitulo = itemView.findViewById(R.id.tvConsignacionSubtitulo);
            tvPrecio = itemView.findViewById(R.id.tvConsignacionPrecio);
            tvSubasta = itemView.findViewById(R.id.tvConsignacionSubasta);
            tvUbicacion = itemView.findViewById(R.id.tvConsignacionUbicacion);
            btnVerDetalle = itemView.findViewById(R.id.btnVerDetalleConsignacion);
        }
    }
}
