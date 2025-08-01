package com.example.smunavigator2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smunavigator2.Activity.NoticeDetailActivity;
import com.example.smunavigator2.Domain.NoticeModel;
import com.example.smunavigator2.R;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {
        private Context context;
        private List<NoticeModel> noticeList;
        private boolean isEnglish;

        public NoticeAdapter(Context context, List<NoticeModel> noticeList, boolean isEnglish) {
            this.context = context;
            this.noticeList = noticeList;
            this.isEnglish = isEnglish;
        }

        @NonNull
        @Override
        public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_notice, parent, false);
            return new NoticeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
            NoticeModel notice = noticeList.get(position);
            holder.title.setText(isEnglish ? notice.title_en : notice.title_ko);
            holder.dept.setText(notice.department);
            holder.date.setText(notice.date);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, NoticeDetailActivity.class);
                intent.putExtra("title", isEnglish ? notice.title_en : notice.title_ko);
                intent.putExtra("html", isEnglish ? notice.html_en : notice.html_ko);
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return noticeList.size();
        }

        public static class NoticeViewHolder extends RecyclerView.ViewHolder {
            TextView title, dept, date;

            public NoticeViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.noticeTitle);
                dept = itemView.findViewById(R.id.noticeDept);
                date = itemView.findViewById(R.id.noticeDate);
            }
        }
    }


