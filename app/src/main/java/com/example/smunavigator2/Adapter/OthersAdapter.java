package com.example.smunavigator2.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Domain.ItemDomain;
import com.example.smunavigator2.databinding.ViewholderOthersBinding;

import java.util.ArrayList;

public class OthersAdapter extends RecyclerView.Adapter<OthersAdapter.Viewholder> {

    ArrayList<ItemDomain> items;
    Context context;
    ViewholderOthersBinding binding;


    public OthersAdapter(ArrayList<ItemDomain> items) {
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
    public void onBindViewHolder(@NonNull OthersAdapter.Viewholder holder, int position) {
        ItemDomain item = items.get(position);

        holder.binding.titleTxt.setText(item.getName());
        holder.binding.addressTxt.setText(item.getLocationDetails());
        holder.binding.openTxt.setText(item.getOpeningHours());

        Glide.with(context)
                .load(item.getImagePath())
                .into(holder.binding.picC);

        holder.itemView.setOnClickListener(v -> {
            // handle click here
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
