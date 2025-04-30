package com.example.smunavigator2.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Domain.Category;
import com.example.smunavigator2.R;
import com.example.smunavigator2.databinding.ViewholderCategoryBinding;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.Viewholder> {
    private final List<Category> items;
    private int selectedPosition = 1;
    private int lastSelectedPosition = -1;
    private Context context;


    public CategoryAdapter(List<Category> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CategoryAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        context=  parent.getContext();
        LayoutInflater inflater= LayoutInflater.from(context);
        ViewholderCategoryBinding binding=ViewholderCategoryBinding.inflate(inflater,parent,false);
        return new Viewholder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.Viewholder holder, int position)
    {
        Category item=items.get(position);
        holder.binding.title.setText(item.getName());


        Glide.with(holder.itemView.getContext())
                .load(item.getImagePath())
                .into(holder.binding.pic);

        holder.binding.getRoot().setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                lastSelectedPosition=selectedPosition;
                selectedPosition=position;
                notifyItemChanged(lastSelectedPosition);
                notifyItemChanged(selectedPosition);

            }
        });
        holder.binding.title.setTextColor(context.getResources().getColor(R.color.white));

        if(selectedPosition==position)
        {
            holder.binding.pic.setBackgroundResource(0);
            holder.binding.mainLayout.setBackgroundResource(R.drawable.blue_bg);
            holder.binding.title.setVisibility(View.VISIBLE);
        }else
        {
            holder.binding.pic.setBackgroundResource(R.drawable.grey_bg);
            holder.binding.mainLayout.setBackgroundResource(0);
            holder.binding.title.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        private final ViewholderCategoryBinding binding;

        public Viewholder(ViewholderCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }
}
