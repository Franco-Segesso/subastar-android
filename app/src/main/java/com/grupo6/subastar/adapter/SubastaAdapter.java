package com.grupo6.subastar.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.grupo6.subastar.R;
import com.grupo6.subastar.model.Subasta;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class SubastaAdapter extends RecyclerView.Adapter<SubastaAdapter.SubastaViewHolder> {

    private List<Subasta> subastas;

    public SubastaAdapter(List<Subasta> subastas) {
        this.subastas = subastas;
    }

    @NonNull
    @Override
    public SubastaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subasta, parent, false);
        return new SubastaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubastaViewHolder holder, int position) {
        Subasta subasta = subastas.get(position);

        holder.tvTitulo.setText(subasta.getUbicacion()); // O el título que definas del catálogo
        holder.tvFecha.setText(subasta.getFecha() + " - " + subasta.getHora() + " hs");
        holder.tvCategoria.setText("CATEGORÍA: " + subasta.getCategoria().toUpperCase());

        // Cambiar el texto del botón según la moneda configurada en la BD
        holder.btnVer.setText("VER (" + subasta.getMoneda() + ") ->");

        //evento click para navegar al catálogo
        holder.btnVer.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), com.grupo6.subastar.CatalogoActivity.class);
            intent.putExtra("SUBASTA_ID", subasta.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return subastas != null ? subastas.size() : 0;
    }

    public static class SubastaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvFecha, tvCategoria;
        MaterialButton btnVer;

        public SubastaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloSubasta);
            tvFecha = itemView.findViewById(R.id.tvFechaSubasta);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaSubasta);
            btnVer = itemView.findViewById(R.id.btnVer);
        }
    }
}