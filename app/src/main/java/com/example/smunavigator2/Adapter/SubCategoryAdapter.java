package com.example.smunavigator2.Adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Domain.CategoryModel;
import com.example.smunavigator2.R;

import java.util.ArrayList;
import java.util.List;

public class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.CategoryViewHolder> {

    private final List<CategoryModel> categoryList = new ArrayList<>();
    private final List<CategoryModel> fullList = new ArrayList<>();
    private String language = "en";
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(CategoryModel item);
    }

    public void setLanguage(String lang) {
        this.language = lang;
    }

    public void setData(List<CategoryModel> list) {
        categoryList.clear();
        fullList.clear();
        if (list != null) {
            categoryList.addAll(list);
            fullList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (TextUtils.isEmpty(query)) {
            categoryList.clear();
            categoryList.addAll(fullList);
        } else {
            List<CategoryModel> filtered = new ArrayList<>();
            for (CategoryModel item : fullList) {
                String name = language.equals("ko") ? item.getNameKo() : item.getNameEn();
                if (name.toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(item);
                }
            }
            categoryList.clear();
            categoryList.addAll(filtered);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CategoryModel getItem(int position) {
        return categoryList.get(position);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sub_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel item = categoryList.get(position);

        holder.nameText.setText(language.equals("ko") ? item.getNameKo() : item.getNameEn());

        Glide.with(holder.itemView.getContext())
                .load(item.getImagePath())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.image_error)
                .into(holder.iconImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;
        TextView nameText;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.categoryImage);
            nameText = itemView.findViewById(R.id.categoryName);
        }
    }
}