package com.grupo6.subastar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.grupo6.subastar.R;
import com.grupo6.subastar.dto.MedioPagoDTO;
import java.util.List;

public class MedioPagoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_TARJETA = 0;
    private static final int TIPO_CUENTA  = 1;
    private static final int TIPO_CHEQUE  = 2;

    private List<MedioPagoDTO> lista;
    private OnEliminarClickListener listener;

    public interface OnEliminarClickListener {
        void onEliminar(MedioPagoDTO item);
    }

    public MedioPagoAdapter(List<MedioPagoDTO> lista, OnEliminarClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        String tipo = lista.get(position).getTipo();
        if ("tarjeta".equals(tipo)) return TIPO_TARJETA;
        if ("cuenta".equals(tipo))  return TIPO_CUENTA;
        return TIPO_CHEQUE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TIPO_TARJETA) {
            View v = inf.inflate(R.layout.item_medio_pago_tarjeta, parent, false);
            return new TarjetaViewHolder(v);
        } else if (viewType == TIPO_CUENTA) {
            View v = inf.inflate(R.layout.item_medio_pago_cuenta, parent, false);
            return new CuentaViewHolder(v);
        } else {
            View v = inf.inflate(R.layout.item_medio_pago_cheque, parent, false);
            return new ChequeViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MedioPagoDTO item = lista.get(position);
        if (holder instanceof TarjetaViewHolder) {
            TarjetaViewHolder h = (TarjetaViewHolder) holder;
            h.tvTitular.setText(item.getTitular());
            h.tvUltimosDigitos.setText("•••• •••• •••• " + item.getUltimosDigitos());
            h.tvVencimiento.setText("Vence: " + item.getVencimiento());
            h.btnEliminar.setOnClickListener(v -> listener.onEliminar(item));
        } else if (holder instanceof CuentaViewHolder) {
            CuentaViewHolder h = (CuentaViewHolder) holder;
            h.tvBanco.setText(item.getBanco());
            h.tvCbuIban.setText("CBU/IBAN: " + item.getCbuIban());
            h.tvAlias.setText(item.getAlias() != null ? "Alias: " + item.getAlias() : "");
            h.tvMoneda.setText(item.getMoneda());
            h.btnEliminar.setOnClickListener(v -> listener.onEliminar(item));
        } else {
            ChequeViewHolder h = (ChequeViewHolder) holder;
            h.tvBanco.setText(item.getBanco());
            h.tvNroCheque.setText("Nro: " + item.getNroCheque());
            h.tvMonto.setText("Garantía: " + item.getMoneda() + " " + item.getMontoGarantia());
            h.tvFecha.setText("Entrega: " + item.getFechaEntrega());
            h.btnEliminar.setOnClickListener(v -> listener.onEliminar(item));
        }
    }

    @Override
    public int getItemCount() { return lista.size(); }

    // --- VIEW HOLDERS ---

    static class TarjetaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitular, tvUltimosDigitos, tvVencimiento;
        ImageButton btnEliminar;
        TarjetaViewHolder(View v) {
            super(v);
            tvTitular        = v.findViewById(R.id.tvTitular);
            tvUltimosDigitos = v.findViewById(R.id.tvUltimosDigitos);
            tvVencimiento    = v.findViewById(R.id.tvVencimiento);
            btnEliminar      = v.findViewById(R.id.btnEliminarTarjeta);
        }
    }

    static class CuentaViewHolder extends RecyclerView.ViewHolder {
        TextView tvBanco, tvCbuIban, tvAlias, tvMoneda;
        ImageButton btnEliminar;
        CuentaViewHolder(View v) {
            super(v);
            tvBanco     = v.findViewById(R.id.tvBancoCuenta);
            tvCbuIban   = v.findViewById(R.id.tvCbuIban);
            tvAlias     = v.findViewById(R.id.tvAlias);
            tvMoneda    = v.findViewById(R.id.tvMonedaCuenta);
            btnEliminar = v.findViewById(R.id.btnEliminarCuenta);
        }
    }

    static class ChequeViewHolder extends RecyclerView.ViewHolder {
        TextView tvBanco, tvNroCheque, tvMonto, tvFecha;
        ImageButton btnEliminar;
        ChequeViewHolder(View v) {
            super(v);
            tvBanco     = v.findViewById(R.id.tvBancoCheque);
            tvNroCheque = v.findViewById(R.id.tvNroCheque);
            tvMonto     = v.findViewById(R.id.tvMontoCheque);
            tvFecha     = v.findViewById(R.id.tvFechaEntrega);
            btnEliminar = v.findViewById(R.id.btnEliminarCheque);
        }
    }
}