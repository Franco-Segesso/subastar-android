package com.grupo6.subastar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.grupo6.subastar.R;
import com.grupo6.subastar.model.ItemCatalogo;
import com.grupo6.subastar.model.Foto;
import java.util.List;

public class ItemProductoAdapter extends RecyclerView.Adapter<ItemProductoAdapter.ProductoViewHolder> {

    private List<ItemCatalogo> items;
    private int subastaId;
    private String subastaEstado;

    // El constructor ahora recibe los datos de la subasta
    public ItemProductoAdapter(List<ItemCatalogo> items, int subastaId, String subastaEstado) {
        this.items = items;
        this.subastaId = subastaId;
        this.subastaEstado = subastaEstado;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        ItemCatalogo item = items.get(position);

        holder.tvTitulo.setText(item.getProducto().getTipo());

        if (item.getPrecioBase() != null) {
            holder.tvPrecio.setText("Base: USD " + item.getPrecioBase());
        } else {
            holder.tvPrecio.setText("Base: Oculto (Iniciá sesión)");
        }

        List<Foto> fotos = item.getProducto().getFotos();
        if (fotos != null && !fotos.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(fotos.get(0).getUrlFoto())
                    .placeholder(R.drawable.bg_chip_inactivo)
                    .into(holder.ivFoto);
        }

        // CORREGIDO: Usamos las variables directas del constructor en vez de navegar por el objeto item
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), com.grupo6.subastar.DetalleItemActivity.class);
            intent.putExtra("ITEM_ID", item.getId());
            intent.putExtra("SUBASTA_ID", subastaId);
            intent.putExtra("SUBASTA_ESTADO", subastaEstado);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvPrecio;
        ImageView ivFoto;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvProductoTitulo);
            tvPrecio = itemView.findViewById(R.id.tvProductoPrecioBase);
            ivFoto = itemView.findViewById(R.id.ivProductoFoto);
        }
    }
}