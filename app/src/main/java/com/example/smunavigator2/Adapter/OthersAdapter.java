package com.example.smunavigator2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Activity.DetailActivity;
import com.example.smunavigator2.Domain.FacilityModel;
import com.example.smunavigator2.R;
import com.example.smunavigator2.databinding.ViewholderOthersBinding;

import java.util.ArrayList;

public class OthersAdapter extends RecyclerView.Adapter<OthersAdapter.Viewholder> {

   private final  ArrayList<FacilityModel> items;
  private  Context context;

    public OthersAdapter(ArrayList<FacilityModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OthersAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderOthersBinding binding = ViewholderOthersBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();
        return new Viewholder(binding);
    }


    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        FacilityModel item = items.get(position);

        // ✅ Set title (English if available, fallback to nameString or "Facility")
        holder.binding.titleTxt.setText(item.getName());

        // ✅ Set description
        holder.binding.addressTxt.setText(item.getDescription());

        // ✅ Set operating hours or fallback
        holder.binding.openTxt.setText(item.getOperatingHours());

        // ✅ Load image with fallback
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            Glide.with(context)
                    .load(item.getImagePath())
                    .placeholder(R.drawable.pic_1)
                    .error(R.drawable.pic_1)
                    .into(holder.binding.picC);
        } else {
            holder.binding.picC.setImageResource(R.drawable.pic_1);
        }

        // ✅ On item click
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("object", items.get(pos));
                context.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        ViewholderOthersBinding binding;

        public Viewholder(ViewholderOthersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}