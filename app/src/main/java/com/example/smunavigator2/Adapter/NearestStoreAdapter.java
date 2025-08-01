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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Activity.GoogleMapActivity;
import com.example.smunavigator2.Domain.StoreModel;
import com.example.smunavigator2.R;

import java.util.ArrayList;
import java.util.List;

public class NearestStoreAdapter extends RecyclerView.Adapter<NearestStoreAdapter.ViewHolder> {

    private List<StoreModel> storeList;
    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onClick(StoreModel item);
    }

    public NearestStoreAdapter(List<StoreModel> storeList) {
        this.storeList = new ArrayList<>(storeList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<StoreModel> updatedList) {
        storeList.clear();
        storeList.addAll(updatedList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nearest_store, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoreModel item = storeList.get(position);

        holder.name.setText(item.getName());

        // Prepare address
        String fullAddress = item.getAddress() != null ? item.getAddress().toLowerCase() : "";
        fullAddress = fullAddress.replace("jecheon-si", "")
                .replace("chungbuk", "")
                .replace("republic of korea", "")
                .replace(",", "")
                .trim();

        String shortAddress = trimToMaxWords(fullAddress, 5);
        holder.address.setText(shortAddress);

        // Track expansion state
        String finalFullAddress = fullAddress;
        holder.itemView.setOnClickListener(v -> {
            // Toggle short/full address
            if (holder.address.getText().toString().endsWith("...")) {
                holder.address.setText(finalFullAddress); // Expand
            } else {
                holder.address.setText(shortAddress); // Collapse
            }

            // Launch GoogleMapActivity with store info
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, GoogleMapActivity.class);
            intent.putExtra("storeLat", item.getLat());
            intent.putExtra("storeLng", item.getLng());
            intent.putExtra("storeName", item.getName());
            intent.putExtra("storeAddress", item.getAddress());
            intent.putExtra("storeHours",
                    item.getOpening_hours() != null
                            ? TextUtils.join(", ", item.getOpening_hours())
                            : "Opening hours not available"
            );
            intent.putExtra("storeImage", item.getImagePath());
            String layoutName = item.getCategory().equalsIgnoreCase("Restaurant") ? "food_marker" :
                    item.getCategory().equalsIgnoreCase("Coffee") ? "coffee_marker" :
                            "store_marker"; // default fallback
            intent.putExtra("storeCategory", item.getCategory()); // ✅ Add this line

            intent.putExtra("markerLayout", layoutName);
            intent.putExtra("storeDescription", item.getActivity() != null ? item.getActivity() : "");

            // ✅ Use layoutKey mapping helper
            String layoutKey = getMarkerLayoutKeyFromCategory(item.getCategory());
            intent.putExtra("markerLayout", layoutKey);

            context.startActivity(intent);

            // Optional listener
            if (listener != null) listener.onClick(item);
        });

        // Load image
        String imageUrl = item.getImagePath();
        String category = item.getCategory();

        if (imageUrl == null || imageUrl.isEmpty()) {
            // Use category fallback drawable
            int fallbackRes = getFallbackImageRes(category);
            Glide.with(holder.itemView.getContext())
                    .load(fallbackRes)
                    .into(holder.image);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_marker_food)
                    .circleCrop()
                    .error(getFallbackImageRes(category)) // fallback if image loading fails
                    .into(holder.image);
        }
    }

    private int getFallbackImageRes(String category) {
        if (category == null) return R.drawable.smu_logo;
        switch (category.toLowerCase()) {
            case "restaurant":
            case "restaurants":
                return R.drawable.smu_logo;
            case "coffee":
                return R.drawable.smu_logo;
            case "mart":
                return R.drawable.smu_logo;
            case "convenience":
                return R.drawable.smu_logo;
            case "accommodation":
                return R.drawable.smu_logo;
            case "bars":
                return R.drawable.smu_logo;
            default:
                return R.drawable.smu_logo; // fallback
        }
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

    private String getMarkerLayoutKeyFromCategory(String category) {
        if (category == null) return "store_marker";
        switch (category) {
            case "Coffee": return "coffee_marker";
            case "Restaurant":
            case "Restaurants": return "food_marker";
            case "Dorms": return "dorm_marker";
            case "Facilities": return "facilities_marker";
            case "Convenience": return "convenience_marker";
            case "Bars": return "bars_marker";     // ✅ Match layout filename
            case "Mart": return "mart_marker";     // ✅ Match layout filename
            default: return "store_marker";        // fallback
        }
    }

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