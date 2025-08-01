package com.example.smunavigator2.Adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smunavigator2.R;

import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ADD = 0;
    private static final int VIEW_TYPE_IMAGE = 1;

    private List<Uri> imageUris;
    private final OnImageRemoveListener listener;


    public interface OnImageRemoveListener {
        void onRemove(int position);
        void onAddImage();
    }

    public ImagePreviewAdapter(List<Uri> imageUris, OnImageRemoveListener listener) {
        this.imageUris = imageUris;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == imageUris.size()) ? VIEW_TYPE_ADD : VIEW_TYPE_IMAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ADD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
            return new AddViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_preview, parent, false);
            return new ImageViewHolder(view);
        }
    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            Uri uri = imageUris.get(position);
            ((ImageViewHolder) holder).bind(uri);
        } else {
            ((AddViewHolder) holder).itemView.setOnClickListener(v -> listener.onAddImage());
        }
    }

    @Override
    public int getItemCount() {
        return imageUris.size() < 5 ? imageUris.size() + 1 : 5;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, removeBtn;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imagePreview);
            removeBtn = itemView.findViewById(R.id.removeBtn);
        }

        void bind(Uri uri) {
            imageView.setImageURI(uri);
            removeBtn.setOnClickListener(v -> listener.onRemove(getAdapterPosition()));
        }
    }

    class AddViewHolder extends RecyclerView.ViewHolder {
        public AddViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}