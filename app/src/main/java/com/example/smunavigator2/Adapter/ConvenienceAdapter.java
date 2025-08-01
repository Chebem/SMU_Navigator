package com.example.smunavigator2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Activity.DetailActivity;
import com.example.smunavigator2.Domain.ConvenienceFacility;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.ConvenienceDiffCallback;
import com.example.smunavigator2.databinding.ViewholderConvenienceBinding;

import java.util.ArrayList;
import java.util.List;

public class ConvenienceAdapter extends RecyclerView.Adapter<ConvenienceAdapter.Viewholder> {

    private final ArrayList<ConvenienceFacility> items;
    private Context context;

    public ConvenienceAdapter(ArrayList<ConvenienceFacility> items) {
        this.items = items;
    }

    // 🔽 INSERT HERE
    public void setItems(List<ConvenienceFacility> newItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new ConvenienceDiffCallback(this.items, newItems)
        );
        this.items.clear();
        this.items.addAll(newItems);
        diffResult.dispatchUpdatesTo(this);
    }




    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderConvenienceBinding binding = ViewholderConvenienceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        ConvenienceFacility item = items.get(position);

        holder.binding.titleTxt.setText(item.getName());
        holder.binding.addressTxt.setText(item.getLocation());
        holder.binding.openTxt.setText(item.getOperating_hours());

        // Load image using Glide with fallback
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            Glide.with(context)
                    .load(item.getImagePath())
                    .placeholder(R.drawable.pic_1)
                    .error(R.drawable.pic_1)
                    .into(holder.binding.picC);
        } else {
            holder.binding.picC.setImageResource(R.drawable.pic_1);
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("object", items.get(pos)); // Serializable
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        ViewholderConvenienceBinding binding;

        public Viewholder(ViewholderConvenienceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
