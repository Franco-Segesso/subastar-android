package com.grupo6.subastar.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.grupo6.subastar.CatalogoActivity;
import com.grupo6.subastar.R;
import com.grupo6.subastar.model.Subasta;
import java.util.List;

public class SubastaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Subasta> subastas;

    // Identificadores para decirle a Android qué diseño inflar
    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_ACTIVA = 1;

    public SubastaAdapter(List<Subasta> subastas) {
        this.subastas = subastas;
    }

    // 1. Magia: Analizamos la fila y decidimos el tipo de vista
    @Override
    public int getItemViewType(int position) {
        Subasta subasta = subastas.get(position);
        if ("abierta".equalsIgnoreCase(subasta.getEstado())) {
            return VIEW_TYPE_ACTIVA;
        }
        return VIEW_TYPE_NORMAL;
    }

    // 2. Inflamos el XML que corresponda al tipo
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ACTIVA) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subasta_activa, parent, false);
            return new ActivaViewHolder(view);
        } else {
            // Asegurate de que este es el nombre real de tu XML normal
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subasta, parent, false);
            return new NormalViewHolder(view);
        }
    }

    // 3. Llenamos los datos basándonos en qué ViewHolder se instanció
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Subasta subasta = subastas.get(position);

        if (holder instanceof ActivaViewHolder) {
            ActivaViewHolder activaHolder = (ActivaViewHolder) holder;

            activaHolder.tvTitulo.setText(subasta.getUbicacion());
            activaHolder.tvSubDetalle.setText("Categoría " + subasta.getCategoria().toUpperCase() + " • " + subasta.getMoneda());

            // El clic va en el botón de ingresar
            activaHolder.btnIngresar.setOnClickListener(v -> navegarAlCatalogo(v, subasta));

        } else if (holder instanceof NormalViewHolder) {
            NormalViewHolder normalHolder = (NormalViewHolder) holder;

            // Llenamos los datos
            normalHolder.tvTitulo.setText(subasta.getUbicacion() != null ? subasta.getUbicacion() : "Subasta Especial");
            normalHolder.tvFecha.setText(subasta.getFecha() + " - " + subasta.getHora());

            if (subasta.getCategoria() != null) {
                normalHolder.tvCategoria.setText("CATEGORÍA: " + subasta.getCategoria().toUpperCase());
            }

            // Le asignamos el clic tanto al botón "VER" como a toda la tarjeta (mejor UX)
            normalHolder.btnVer.setOnClickListener(v -> navegarAlCatalogo(v, subasta));
            normalHolder.itemView.setOnClickListener(v -> navegarAlCatalogo(v, subasta));
        }
    }

    private void navegarAlCatalogo(View v, Subasta subasta) {
        Intent intent = new Intent(v.getContext(), CatalogoActivity.class);
        intent.putExtra("SUBASTA_ID", subasta.getId());
        v.getContext().startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return subastas != null ? subastas.size() : 0;
    }

    // --- VIEWHOLDERS ---

    // El ViewHolder para las subastas futuras/pasadas
    public static class NormalViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvFecha, tvCategoria; // Ajustá estos tipos según tu item_subasta.xml
        MaterialButton btnVer;

        public NormalViewHolder(@NonNull View itemView) {
            super(itemView);
            // Reemplazá con los IDs correctos de tu layout normal
            tvTitulo = itemView.findViewById(R.id.tvTituloSubasta);
            tvFecha = itemView.findViewById(R.id.tvFechaSubasta);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaSubasta);
            btnVer = itemView.findViewById(R.id.btnVer);
        }
    }

    // El ViewHolder para la subasta en vivo
    public static class ActivaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvSubDetalle;
        MaterialButton btnIngresar;

        public ActivaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloDestacado);
            tvSubDetalle = itemView.findViewById(R.id.tvSubDetalle);
            btnIngresar = itemView.findViewById(R.id.btnIngresar);
        }
    }
}