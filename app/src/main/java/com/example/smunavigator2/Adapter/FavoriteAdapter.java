package com.example.smunavigator2.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Domain.FavoriteItem;
import com.example.smunavigator2.R;
import com.example.smunavigator2.Utils.DistanceUtils;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    // Where distances are measured from (student or campus), set by the screen
    private double originLat = DistanceUtils.CAMPUS_LAT;
    private double originLng = DistanceUtils.CAMPUS_LNG;
    private boolean originIsUser = false;

    public void setOrigin(double lat, double lng, boolean fromUser) {
        originLat = lat;
        originLng = lng;
        originIsUser = fromUser;
        notifyDataSetChanged();
    }


    public interface OnFavoriteActionListener {
        void onUnfavorite(FavoriteItem item);
    }

    private final Context context;
    private final List<FavoriteItem> favoriteList;
    private final OnFavoriteActionListener listener;

    public FavoriteAdapter(Context context, List<FavoriteItem> favoriteList, OnFavoriteActionListener listener) {
        this.context = context;
        this.favoriteList = favoriteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.favorites_card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteItem place = favoriteList.get(position);

        holder.name.setText(place.getName());
        holder.category.setText(place.getCategory());
        float meters = DistanceUtils.meters(originLat, originLng, place.getLat(), place.getLng());
        holder.distance.setText(DistanceUtils.label(meters, originIsUser, DistanceUtils.isEnglishLocale()));
        holder.favoriteCount.setText(String.valueOf(place.getFavoriteCount()));
        Glide.with(context).load(place.getImageUrl()).into(holder.image);

        holder.itemView.setOnLongClickListener(v -> {
            listener.onUnfavorite(place); // Call activity method to handle removal
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, category, distance, favoriteCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.placeImage);
            name = itemView.findViewById(R.id.placeName);
            category = itemView.findViewById(R.id.placeCategory);
            distance = itemView.findViewById(R.id.placeDistance);
            favoriteCount = itemView.findViewById(R.id.favoriteCount);
        }
    }
}