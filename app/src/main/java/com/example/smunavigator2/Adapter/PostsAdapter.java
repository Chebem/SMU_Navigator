package com.example.smunavigator2.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Domain.ProfileModel;
import com.example.smunavigator2.databinding.ViewholderPostBinding;

import java.util.ArrayList;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.Viewholder>{
    ArrayList<ProfileModel.Post> list;

    public PostsAdapter(ArrayList<ProfileModel.Post> list)
    {
        this.list = list;
    }


    @NonNull
    @Override
    public PostsAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderPostBinding binding = ViewholderPostBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsAdapter.Viewholder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(list.get(position).imageUrl)
                .into(holder.binding.postImg);

    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    public class Viewholder extends RecyclerView.ViewHolder {
        ViewholderPostBinding binding;
        public Viewholder(ViewholderPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
