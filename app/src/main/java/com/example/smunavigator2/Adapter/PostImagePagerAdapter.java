package com.example.smunavigator2.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.R;

import java.util.List;

public class PostImagePagerAdapter extends RecyclerView.Adapter<PostImagePagerAdapter.PagerViewHolder> {

    private final List<String> imageUrls;

    public PostImagePagerAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate from XML (use full width match_parent)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_image, parent, false);
        return new PagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        Glide.with(holder.imageView.getContext())
                .load(imageUrl)
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class PagerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public PagerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pagerImageView);
        }
    }
}