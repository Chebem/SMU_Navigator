package com.example.smunavigator2.Utils;

import androidx.recyclerview.widget.DiffUtil;

import com.example.smunavigator2.Domain.ConvenienceFacility;

import java.util.List;

public class ConvenienceDiffCallback extends DiffUtil.Callback {
    private final List<ConvenienceFacility> oldList;
    private final List<ConvenienceFacility> newList;

    public ConvenienceDiffCallback(List<ConvenienceFacility> oldList, List<ConvenienceFacility> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

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
        // Compare by ID or unique name
        return oldList.get(oldItemPosition).getName().equals(newList.get(newItemPosition).getName());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Compare full content
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}