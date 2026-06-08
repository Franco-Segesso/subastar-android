package com.grupo6.subastar.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.grupo6.subastar.R;
import com.grupo6.subastar.model.Foto;
import java.util.List;

public class ImagenSliderAdapter extends RecyclerView.Adapter<ImagenSliderAdapter.SliderViewHolder> {

    private Context context;
    private List<Foto> fotos;

    public ImagenSliderAdapter(Context context, List<Foto> fotos) {
        this.context = context;
        this.fotos = fotos;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_imagen_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        Foto foto = fotos.get(position);
        Glide.with(context)
                .load(foto.getUrlFoto())
                .placeholder(R.drawable.logo_subastar) // Imagen mientras carga
                .error(R.drawable.logo_subastar)       // Se muestra si la URL está rota o da error 404
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return fotos != null ? fotos.size() : 0;
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivSlider);
        }
    }
}