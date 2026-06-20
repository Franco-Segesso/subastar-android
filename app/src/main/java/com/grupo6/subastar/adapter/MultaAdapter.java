package com.grupo6.subastar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.grupo6.subastar.R;
import com.grupo6.subastar.dto.MultaDTO;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MultaAdapter extends RecyclerView.Adapter<MultaAdapter.ViewHolder> {

    private static final ZoneId ZONA_NEGOCIO =
            ZoneId.of("America/Argentina/Buenos_Aires");

    public interface OnAccionClick {
        void onPagarMulta(MultaDTO multa);
        void onPagarCompra(MultaDTO multa);
    }

    private final boolean pendientes;
    private final OnAccionClick listener;
    private List<MultaDTO> items = new ArrayList<>();

    public MultaAdapter(boolean pendientes, OnAccionClick listener) {
        this.pendientes = pendientes;
        this.listener = listener;
    }

    public void setItems(List<MultaDTO> items) {
        this.items = items == null ? new ArrayList<>() : items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(pendientes
                                ? R.layout.item_multa_pendiente
                                : R.layout.item_multa_historial,
                        parent,
                        false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MultaDTO multa = items.get(position);
        String moneda = moneda(multa.getSubasta());
        holder.titulo.setText("Incumplimiento de pago");
        holder.subasta.setText(limpiarSubasta(multa.getSubasta()));
        holder.importe.setText(formatear(moneda, multa.getImporte()));

        if (pendientes) {
            boolean multaPendiente = "pendiente".equalsIgnoreCase(multa.getEstado());
            boolean compraPendiente = "pendiente".equalsIgnoreCase(multa.getEstadoCompra());
            holder.boton.setVisibility(View.VISIBLE);
            holder.titulo.setText(multaPendiente
                    ? "Multa pendiente de pago"
                    : "Multa pagada - compra pendiente");
            holder.importe.setText(multaPendiente
                    ? formatear(moneda, multa.getImporte())
                    : formatear(moneda, multa.getTotalCompra()));
            holder.oferta.setText(
                    "Multa: " + estadoVisible(multa.getEstado())
                            + "  |  Compra: " + estadoVisible(multa.getEstadoCompra())
                            + (compraPendiente
                                    ? "\nTotal compra pendiente: "
                                    + formatear(moneda, multa.getTotalCompra())
                                    : ""));
            holder.contador.setText(tiempoRestante(multa.getFechaVencimiento()));
            holder.fechas.setText("Generada: " + fecha(multa.getFechaGeneracion())
                    + "     Vence: " + fecha(multa.getFechaVencimiento()));
            if (multaPendiente) {
                holder.boton.setText("PAGAR MULTA - "
                        + formatear(moneda, multa.getImporte()));
                holder.boton.setOnClickListener(v -> listener.onPagarMulta(multa));
            } else if (compraPendiente && multa.getCompraId() != null) {
                holder.boton.setText("IR A PAGAR LA COMPRA");
                holder.boton.setOnClickListener(v -> listener.onPagarCompra(multa));
            } else {
                holder.boton.setVisibility(View.GONE);
            }
        } else {
            holder.fechas.setText(fecha(multa.getFechaPago() != null
                    ? multa.getFechaPago()
                    : multa.getFechaGeneracion()));
            holder.estado.setText(multa.getEstado() == null
                    ? ""
                    : multa.getEstado().toUpperCase(Locale.ROOT));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String tiempoRestante(String fechaVencimiento) {
        try {
            Duration restante = Duration.between(
                    LocalDateTime.now(ZONA_NEGOCIO),
                    LocalDateTime.parse(fechaVencimiento));
            long segundos = Math.max(0, restante.getSeconds());
            long horas = segundos / 3600;
            long minutos = (segundos % 3600) / 60;
            long secs = segundos % 60;
            return String.format(Locale.getDefault(), "%02dhs  %02dmin  %02dseg",
                    horas, minutos, secs);
        } catch (Exception e) {
            return "--hs  --min  --seg";
        }
    }

    private String fecha(String valor) {
        if (valor == null) return "--";
        try {
            return LocalDateTime.parse(valor)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return valor;
        }
    }

    private String moneda(String subasta) {
        if (subasta != null && subasta.endsWith("(USD)")) return "USD";
        return "ARS";
    }

    private String limpiarSubasta(String subasta) {
        if (subasta == null) return "";
        return subasta.replace(" (USD)", "").replace(" (ARS)", "");
    }

    private String formatear(String moneda, Double importe) {
        NumberFormat formato = NumberFormat.getIntegerInstance(new Locale("es", "AR"));
        return moneda + " " + formato.format(importe == null ? 0 : importe);
    }

    private String estadoVisible(String estado) {
        if ("pagada".equalsIgnoreCase(estado)) return "pagada";
        if ("judicial".equalsIgnoreCase(estado)) return "judicial";
        return "pendiente";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titulo;
        TextView subasta;
        TextView importe;
        TextView oferta;
        TextView contador;
        TextView fechas;
        TextView estado;
        Button boton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.tvMultaTitulo);
            subasta = itemView.findViewById(R.id.tvMultaSubasta);
            importe = itemView.findViewById(R.id.tvMultaImporte);
            oferta = itemView.findViewById(R.id.tvMultaOferta);
            contador = itemView.findViewById(R.id.tvMultaContador);
            fechas = itemView.findViewById(R.id.tvMultaFechas);
            estado = itemView.findViewById(R.id.tvMultaEstado);
            boton = itemView.findViewById(R.id.btnPagarMulta);
        }
    }
}
