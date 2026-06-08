package com.grupo6.subastar.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grupo6.subastar.DetalleItemActivity;
import com.grupo6.subastar.R;
import com.grupo6.subastar.model.ItemCatalogo;

import java.util.List;

public class ItemProductoAdapter extends RecyclerView.Adapter<ItemProductoAdapter.ViewHolder> {

    private List<ItemCatalogo> items;
    private Context context;
    private Integer subastaId;
    private Integer idPrimerItemActivo = -1;
    private String estadoSubasta;

    public ItemProductoAdapter(List<ItemCatalogo> items, Context context, Integer subastaId, String estadoSubasta) {
        this.items = items;
        this.context = context;
        this.subastaId = subastaId;
        calcularItemActivo();
        this.estadoSubasta = estadoSubasta;
    }

    public void updateData(List<ItemCatalogo> nuevosItems) {
        this.items = nuevosItems;
        calcularItemActivo(); // Recalculamos quién está en vivo cada vez que se actualiza la lista
        notifyDataSetChanged();
    }

    public void actualizarItemActivo(Integer itemId) {
        if (!esSubastaAbierta()) return;
        if (itemId == null) return;
        this.idPrimerItemActivo = itemId;
        notifyDataSetChanged();
    }

    // El corazón de la secuencia: el primer ítem con estado "no" es el que está en vivo
    private void calcularItemActivo() {
        idPrimerItemActivo = -1;
        if (!esSubastaAbierta()) return;
        for (ItemCatalogo item : items) {
            if ("no".equalsIgnoreCase(item.getSubastado())) {
                idPrimerItemActivo = item.getId();
                break;
            }
        }
    }

    private boolean esSubastaAbierta() {
        return "abierta".equalsIgnoreCase(estadoSubasta);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_producto_compacto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemCatalogo item = items.get(position);

        String titulo = item.getProducto() != null ? item.getProducto().getTipo() + " - Ítem #" + item.getId() : "Ítem #" + item.getId();
        holder.tvTitulo.setText(titulo);

        if (item.getProducto() != null && item.getProducto().getFotos() != null && !item.getProducto().getFotos().isEmpty()) {
            Glide.with(context).load(item.getProducto().getFotos().get(0).getUrlFoto()).centerCrop().into(holder.ivImagen);
        } else {
            holder.ivImagen.setImageResource(R.color.primario);
        }

        // --- MÁQUINA DE ESTADOS VISUALES ---
        if ("si".equalsIgnoreCase(item.getSubastado())) {
            // 1. ESTADO: VENDIDO (Ítems Anteriores)
            holder.itemView.setAlpha(0.5f); // Opacamos la tarjeta

            holder.tvEstado.setText("VENDIDO");
            holder.tvEstado.setTextColor(Color.WHITE);
            holder.tvEstado.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));

            holder.tvPrecio.setVisibility(View.VISIBLE);
            holder.tvPrecio.setTextColor(context.getResources().getColor(R.color.texto_sec));

            // Usamos el precio final real de la venta
            Double precioMostrar = item.getPrecioFinal() != null ? item.getPrecioFinal() : item.getPrecioBase();
            holder.tvPrecio.setText(String.format("$%.2f", precioMostrar));

        } else if (esSubastaAbierta() && item.getId().equals(idPrimerItemActivo)) {
            // 2. ESTADO: EN VIVO
            holder.itemView.setAlpha(1.0f);
            holder.tvEstado.setText("EN VIVO");
            holder.tvEstado.setTextColor(context.getResources().getColor(R.color.primario));
            holder.tvEstado.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.secundario)));

            holder.tvPrecio.setVisibility(View.VISIBLE);
            holder.tvPrecio.setText(String.format("Base: USD %.2f", item.getPrecioBase()));

        } else {
            // 3. ESTADO: PRÓXIMAMENTE
            holder.itemView.setAlpha(1.0f);

            holder.tvEstado.setText("PRÓXIMAMENTE");
            holder.tvEstado.setTextColor(context.getResources().getColor(R.color.texto_sec));
            holder.tvEstado.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));

            // Los ítems que vienen después no dicen nada
            holder.tvPrecio.setVisibility(View.GONE);
        }

        // Bloqueamos el clic si ya está vendido
        holder.itemView.setEnabled(!"si".equalsIgnoreCase(item.getSubastado()));

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, DetalleItemActivity.class);
            intent.putExtra("ITEM_ID", item.getId());
            intent.putExtra("SUBASTA_ID", subastaId);
            intent.putExtra("SUBASTA_ESTADO", estadoSubasta);
            intent.putExtra("ITEM_TITULO", titulo);
            intent.putExtra("ITEM_BASE", item.getPrecioBase());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagen;
        TextView tvTitulo, tvPrecio, tvEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // CONECTAMOS CON LOS IDs DEL XML COMPACTO
            ivImagen = itemView.findViewById(R.id.ivItemFoto);
            tvTitulo = itemView.findViewById(R.id.tvItemNombre);
            tvPrecio = itemView.findViewById(R.id.tvItemPrecio);
            tvEstado = itemView.findViewById(R.id.tvItemCategoria); // Usamos este como el badge de estado
        }
    }
}
