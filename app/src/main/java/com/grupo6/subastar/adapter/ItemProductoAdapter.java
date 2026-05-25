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

    public ItemProductoAdapter(List<ItemCatalogo> items) {
        this.items = items;
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

        // Seteamos textos
        holder.tvTitulo.setText(item.getProducto().getTipo());

        // Validación de regla de negocio: si viene nulo, el usuario no está logueado
        if (item.getPrecioBase() != null) {
            holder.tvPrecio.setText("Base: USD " + item.getPrecioBase());
        } else {
            holder.tvPrecio.setText("Base: Oculto (Iniciá sesión)");
        }

        // Cargar imagen con Glide
        List<Foto> fotos = item.getProducto().getFotos();
        if (fotos != null && !fotos.isEmpty()) {
            String urlPrimeraFoto = fotos.get(0).getUrlFoto();

            Glide.with(holder.itemView.getContext())
                    .load(urlPrimeraFoto)
                    .placeholder(R.drawable.bg_chip_inactivo) // Un fondo mientras carga
                    .into(holder.ivFoto);
        }
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