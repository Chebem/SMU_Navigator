package com.example.smunavigator2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Utils.PlaceUtils;
import com.example.smunavigator2.Activity.DetailActivity;
import com.example.smunavigator2.Domain.StoreModel;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.DistanceUtils;

import java.util.ArrayList;
import java.util.List;

public class NearestStoreAdapter extends RecyclerView.Adapter<NearestStoreAdapter.ViewHolder> {

    private List<StoreModel> storeList;
    private OnItemClickListener listener;

    // Where distances are measured from (student or campus), set by the screen
    private double originLat = DistanceUtils.CAMPUS_LAT;
    private double originLng = DistanceUtils.CAMPUS_LNG;
    private boolean originIsUser = false;
    private boolean isEnglish = true;


    public interface OnItemClickListener {
        void onClick(StoreModel item);
    }

    public NearestStoreAdapter(List<StoreModel> storeList) {
        this.storeList = new ArrayList<>(storeList);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOrigin(double lat, double lng, boolean fromUser, boolean isEnglish) {
        this.originLat = lat;
        this.originLng = lng;
        this.originIsUser = fromUser;
        this.isEnglish = isEnglish;
        notifyDataSetChanged();
    }

    public void submitList(List<StoreModel> updatedList) {
        storeList.clear();
        storeList.addAll(updatedList);
        notifyDataSetChanged();
    }


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

        // Real distance + place type instead of the layout's placeholder text
        float meters = DistanceUtils.meters(originLat, originLng, item.getLat(), item.getLng());
        String distance = DistanceUtils.label(meters, originIsUser, isEnglish);
        holder.info.setText(item.getActivity() != null && !item.getActivity().isEmpty()
                ? distance + " · " + item.getActivity() : distance);

        if (item.getOpening_hours() != null && !item.getOpening_hours().isEmpty()) {
            holder.hours.setVisibility(View.VISIBLE);
            holder.hours.setText(item.getOpening_hours().get(0));
        } else {
            holder.hours.setVisibility(View.GONE);
        }

        // Track expansion state
        String finalFullAddress = fullAddress;
        holder.itemView.setOnClickListener(v -> {
            // Toggle short/full address
            if (holder.address.getText().toString().endsWith("...")) {
                holder.address.setText(finalFullAddress); // Expand
            } else {
                holder.address.setText(shortAddress); // Collapse
            }

            // Detail page first (photo, hours, favorite, later reviews); its Explore button opens the map
            Context context = holder.itemView.getContext();
            context.startActivity(new Intent(context, DetailActivity.class).putExtra("object", item));

            // Optional listener
            if (listener != null) listener.onClick(item);
        });

        // Load image with category-specific PLACEHOLDER (not marker)
        String imageUrl = item.getImagePath();
        String category = item.getCategory();

        if (imageUrl == null || imageUrl.isEmpty()) {
            // ✅ Use PLACEHOLDER image (different from map marker)
            int placeholderRes = PlaceUtils.placeholderImage(category);
            Glide.with(holder.itemView.getContext())
                    .load(placeholderRes)
                    .circleCrop()
                    .into(holder.image);
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(PlaceUtils.placeholderImage(category)) // Use category placeholder
                    .circleCrop()
                    .error(PlaceUtils.placeholderImage(category)) // Fallback to placeholder
                    .into(holder.image);
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, address, info, hours;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.storeImage);
            name = itemView.findViewById(R.id.storeName);
            address = itemView.findViewById(R.id.storeAddress);
            info = itemView.findViewById(R.id.storeInfo);
            hours = itemView.findViewById(R.id.storeHours);
        }
    }
}