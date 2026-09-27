package com.example.smunavigator2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Utils.PlaceUtils;
import com.example.smunavigator2.Activity.DetailActivity;
import com.example.smunavigator2.Domain.StoreModel;
import com.example.smunavigator2.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PopularStoreAdapter extends RecyclerView.Adapter<PopularStoreAdapter.ViewHolder> {

    private final List<StoreModel> storeList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(StoreModel item);
    }

    public PopularStoreAdapter(List<StoreModel> initialList) {
        submitList(initialList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    //using DiffUtil (replaces notifyDataSetChanged)
    public void submitList(List<StoreModel> updatedList) {
        if (updatedList == null) updatedList = new ArrayList<>();

        final List<StoreModel> newList = new ArrayList<>(updatedList);
        final List<StoreModel> oldList = new ArrayList<>(this.storeList);

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                StoreModel oldItem = oldList.get(oldItemPosition);
                StoreModel newItem = newList.get(newItemPosition);

                return safe(oldItem.getName()).equals(safe(newItem.getName()))
                        && oldItem.getLat() == newItem.getLat()
                        && oldItem.getLng() == newItem.getLng();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                StoreModel oldItem = oldList.get(oldItemPosition);
                StoreModel newItem = newList.get(newItemPosition);

                return safe(oldItem.getName()).equals(safe(newItem.getName()))
                        && safe(oldItem.getAddress()).equals(safe(newItem.getAddress()))
                        && safe(oldItem.getCategory()).equals(safe(newItem.getCategory()))
                        && safe(oldItem.getImagePath()).equals(safe(newItem.getImagePath()))
                        && safeJoin(oldItem.getOpening_hours()).equals(safeJoin(newItem.getOpening_hours()))
                        && safe(oldItem.getActivity()).equals(safe(newItem.getActivity()))
                        && safe(oldItem.getPhone_number()).equals(safe(newItem.getPhone_number()))
                        && safe(oldItem.getShortAddress()).equals(safe(newItem.getShortAddress()));
            }
        });

        this.storeList.clear();
        this.storeList.addAll(newList);
        diffResult.dispatchUpdatesTo(this);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String safeJoin(List<String> list) {
        if (list == null) return "";
        return TextUtils.join(", ", list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_popular_store, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoreModel item = storeList.get(position);

        holder.name.setText(item.getName());

        // ✅ Locale-safe lowercasing (prevents Turkish 'i' bug)
        String fullAddress = item.getAddress() != null
                ? item.getAddress().toLowerCase(Locale.ROOT)
                : "";

        // Clean address
        fullAddress = fullAddress.replace("jecheon-si", "")
                .replace("chungbuk", "")
                .replace("republic of korea", "")
                .replace(",", "")
                .trim();

        String shortAddress = trimToMaxWords(fullAddress, 5);
        holder.address.setText(shortAddress);

        String finalFullAddress = fullAddress;

        String imageUrl = item.getImagePath();
        String category = item.getCategory();

        // Category-specific placeholder
        int placeholderRes = PlaceUtils.placeholderImage(category);

        if (imageUrl == null || imageUrl.isEmpty()) {
            // No photo yet: the category cover (gradient + icon) fills the card like a photo
            holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(holder.itemView.getContext()).load(placeholderRes).into(holder.image);
        } else {
            // A real photo fills the card
            holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(placeholderRes)
                    .error(placeholderRes)
                    .centerCrop()
                    .into(holder.image);
        }

        holder.itemView.setOnClickListener(v -> {
            // Toggle short/full address on tap
            if (holder.address.getText().toString().endsWith("...")) {
                holder.address.setText(finalFullAddress);
            } else {
                holder.address.setText(shortAddress);
            }

            // Detail page first (photo, hours, favorite, later reviews); its Explore button opens the map
            Context context = holder.itemView.getContext();
            context.startActivity(new Intent(context, DetailActivity.class).putExtra("object", item));

            if (listener != null) listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    private String trimToMaxWords(String text, int maxWords) {
        if (text == null) return "";
        String[] words = text.split("\\s+");
        if (words.length <= maxWords) return text;

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            result.append(words[i]).append(" ");
        }
        result.append("...");
        return result.toString().trim();
    }

    /**
     * ✅ Returns placeholder images for RecyclerView items (NOT map markers)
     */

    /**Returns layout keys for MAP MARKERS */

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, address;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.storeImage);
            name = itemView.findViewById(R.id.storeName);
            address = itemView.findViewById(R.id.storeAddress);
        }
    }
}