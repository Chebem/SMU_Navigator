package com.example.smunavigator2.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Domain.ProfileModel;
import com.example.smunavigator2.databinding.ViewholderFollowersBinding;

import java.util.ArrayList;

public class FollowersAdapter extends RecyclerView.Adapter<FollowersAdapter.Viewholder> {
    ArrayList<ProfileModel.Follower> list;

    public FollowersAdapter(ArrayList<ProfileModel.Follower> list)
    {
        this.list = list;
    }


    @NonNull
    @Override
    public FollowersAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderFollowersBinding binding = ViewholderFollowersBinding.inflate(LayoutInflater.from(parent.getContext()),
        parent,false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowersAdapter.Viewholder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(list.get(position).imageUrl)
                .into(holder.binding.profile);

        holder.binding.name.setText(list.get(position).name);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        ViewholderFollowersBinding binding;

        public Viewholder(ViewholderFollowersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
