package com.example.smunavigator2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Activity.DetailActivity;
import com.example.smunavigator2.Domain.Committee;
import com.example.smunavigator2.R;
import com.example.smunavigator2.databinding.ViewholderCommitteeBinding;

import java.util.ArrayList;

public class CommitteeAdapter extends RecyclerView.Adapter<CommitteeAdapter.Viewholder> {

    private final ArrayList<Committee> items;
    private Context context;

    public CommitteeAdapter(ArrayList<Committee> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCommitteeBinding binding = ViewholderCommitteeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        context = parent.getContext();
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Committee item = items.get(position);

        holder.binding.titleTxt.setText(item.getTitle());
        holder.binding.addressTxt.setText(item.getLocation());
        holder.binding.openTxt.setText(item.getOpeningHours());
        holder.binding.scoreTxt.setText(String.valueOf(item.getScore()));

        // Load image using Glide with fallback
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.pic_1)
                    .error(R.drawable.pic_1)
                    .into(holder.binding.picC);
        } else {
            holder.binding.picC.setImageResource(R.drawable.pic_1);
        }

        // Optional: click to detail activity
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("object", items.get(pos)); // Make sure Committee implements Serializable
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        ViewholderCommitteeBinding binding;

        public Viewholder(ViewholderCommitteeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
