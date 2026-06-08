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

    private String fechaSubasta;

    public ItemProductoAdapter(List<ItemCatalogo> items, Context context, Integer subastaId, String estadoSubasta, String fechaSubasta) {
        this.items = items;
        this.context = context;
        this.subastaId = subastaId;
        calcularItemActivo();
        this.estadoSubasta = estadoSubasta;
        this.fechaSubasta = fechaSubasta;
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

    // el primer ítem con estado "no" es el que está en vivo
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
            Glide.with(context)
                    .load(item.getProducto().getFotos().get(0).getUrlFoto())
                    .centerCrop()
                    .placeholder(R.drawable.logo_subastar) // Imagen de espera
                    .error(R.drawable.logo_subastar)       // Imagen si la URL está rota
                    .into(holder.ivImagen);
        } else {

            holder.ivImagen.setImageResource(R.drawable.logo_subastar);

            holder.ivImagen.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }


        boolean esVendido = "si".equalsIgnoreCase(item.getSubastado());
        boolean esEnVivo = item.getId().equals(idPrimerItemActivo) && "abierta".equalsIgnoreCase(estadoSubasta);


        if (esVendido) {

            holder.itemView.setAlpha(0.6f);

            holder.tvBadgeEnCurso.setVisibility(View.VISIBLE);
            holder.tvBadgeEnCurso.setText("VENDIDO");
            holder.tvBadgeEnCurso.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.DKGRAY));

            holder.cardContenedor.setStrokeWidth(1);
            holder.cardContenedor.setStrokeColor(context.getResources().getColor(R.color.bordes));

            Double precioMostrar = item.getPrecioFinal() != null ? item.getPrecioFinal() : item.getPrecioBase();
            holder.tvPrecio.setText(String.format("Vendido a: $%.2f", precioMostrar));
            holder.tvPrecio.setTextColor(context.getResources().getColor(R.color.texto_sec));

        } else if (esEnVivo) {
            // ESTADO: EN CURSO
            holder.itemView.setAlpha(1.0f);

            holder.tvBadgeEnCurso.setVisibility(View.VISIBLE);
            holder.tvBadgeEnCurso.setText("EN CURSO");
            holder.tvBadgeEnCurso.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getResources().getColor(R.color.secundario)));


            holder.cardContenedor.setStrokeWidth(5);
            holder.cardContenedor.setStrokeColor(context.getResources().getColor(R.color.secundario)); // Dorado

            holder.tvPrecio.setText(String.format("Base: $%.2f", item.getPrecioBase()));
            holder.tvPrecio.setTextColor(context.getResources().getColor(R.color.texto_ppal));

        } else {
            // ESTADO: PRÓXIMAMENTE
            holder.itemView.setAlpha(1.0f);

            holder.tvBadgeEnCurso.setVisibility(View.GONE); //

            holder.cardContenedor.setStrokeWidth(1);
            holder.cardContenedor.setStrokeColor(context.getResources().getColor(R.color.bordes));

            holder.tvPrecio.setText("PROXIMAMENTE");
            holder.tvPrecio.setTextColor(context.getResources().getColor(R.color.texto_sec));
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
            intent.putExtra("SUBASTA_FECHA", fechaSubasta);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagen;
        TextView tvTitulo, tvPrecio, tvBadgeEnCurso;
        com.google.android.material.card.MaterialCardView cardContenedor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardContenedor = itemView.findViewById(R.id.cardContenedorItem);
            ivImagen = itemView.findViewById(R.id.ivItemFoto);
            tvTitulo = itemView.findViewById(R.id.tvItemNombre);
            tvPrecio = itemView.findViewById(R.id.tvItemPrecio);
            tvBadgeEnCurso = itemView.findViewById(R.id.tvBadgeEnCurso);
        }
    }
}
