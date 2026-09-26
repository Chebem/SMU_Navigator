package com.example.smunavigator2.Adapter;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smunavigator2.Activity.ProfilePageActivity;
import com.example.smunavigator2.R;
import com.example.smunavigator2.databinding.ItemPersonBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** A row per person: photo, name, department and a Follow / Following button. */
public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.Viewholder> {

    public static class Person {
        public final String uid;
        public final String name;
        public final String department;
        public final String imageUrl;

        public Person(String uid, String name, String department, String imageUrl) {
            this.uid = uid;
            this.name = name;
            this.department = department;
            this.imageUrl = imageUrl;
        }
    }

    public interface OnFollowToggle {
        void onToggle(Person person, boolean follow);
    }

    private final List<Person> people = new ArrayList<>();
    private final Set<String> followingUids = new HashSet<>();
    private final String myUid;
    private final OnFollowToggle onFollowToggle;

    public PeopleAdapter(String myUid, OnFollowToggle onFollowToggle) {
        this.myUid = myUid;
        this.onFollowToggle = onFollowToggle;
    }

    public void setPeople(List<Person> newPeople) {
        people.clear();
        people.addAll(newPeople);
        notifyDataSetChanged();
    }

    public void setFollowing(Set<String> uids) {
        followingUids.clear();
        followingUids.addAll(uids);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Viewholder(ItemPersonBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Person person = people.get(position);
        holder.binding.personName.setText(TextUtils.isEmpty(person.name) ? "Unknown" : person.name);
        holder.binding.personDepartment.setText(person.department);
        holder.binding.personDepartment.setVisibility(TextUtils.isEmpty(person.department) ? View.GONE : View.VISIBLE);
        Glide.with(holder.itemView.getContext()).load(person.imageUrl)
                .placeholder(R.drawable.smu_logo)
                .error(R.drawable.smu_logo)
                .into(holder.binding.personPic);

        holder.itemView.setOnClickListener(v -> v.getContext().startActivity(
                new Intent(v.getContext(), ProfilePageActivity.class)
                        .putExtra(ProfilePageActivity.EXTRA_USER_ID, person.uid)));

        // No button on your own row
        if (person.uid.equals(myUid)) {
            holder.binding.personFollowBtn.setVisibility(View.GONE);
            return;
        }
        boolean following = followingUids.contains(person.uid);
        holder.binding.personFollowBtn.setVisibility(View.VISIBLE);
        holder.binding.personFollowBtn.setText(following ? R.string.following_state : R.string.follow);
        holder.binding.personFollowBtn.setBackgroundResource(following ? R.drawable.bg_button_outline : R.drawable.rounded_button2);
        holder.binding.personFollowBtn.setTextColor(holder.itemView.getContext()
                .getColor(following ? R.color.blue_dark : R.color.white));
        holder.binding.personFollowBtn.setOnClickListener(v -> onFollowToggle.onToggle(person, !following));
    }

    @Override
    public int getItemCount() {
        return people.size();
    }

    static class Viewholder extends RecyclerView.ViewHolder {
        final ItemPersonBinding binding;

        Viewholder(ItemPersonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
