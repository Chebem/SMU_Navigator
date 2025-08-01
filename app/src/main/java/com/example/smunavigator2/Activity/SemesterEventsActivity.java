package com.example.smunavigator2.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smunavigator2.Adapter.EventAdapter;
import com.example.smunavigator2.Dialog.EventDetailsDialog;
import com.example.smunavigator2.Domain.Event;
import com.example.smunavigator2.R;
import com.google.firebase.database.*;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SemesterEventsActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private LocalDate selectedDate = null;
    private RecyclerView eventRecyclerView;
    private EventAdapter eventAdapter;
    private DatabaseReference eventRef;
    private Map<LocalDate, List<Event>> allEvents = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_semester_events);

        calendarView = findViewById(R.id.calendarView);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(new ArrayList<>());
        eventRecyclerView.setAdapter(eventAdapter);

        setupBottomNav(R.id.favorite);
        eventRef = FirebaseDatabase.getInstance().getReference("semester_events");

        preloadEvents();

        calendarView.setMonthHeaderBinder(new MonthHeaderFooterBinder<MonthHeaderContainer>() {
            @NonNull
            @Override
            public MonthHeaderContainer create(@NonNull View view) {
                return new MonthHeaderContainer(view);
            }

            @Override
            public void bind(@NonNull MonthHeaderContainer container, @NonNull CalendarMonth calendarMonth) {
                YearMonth yearMonth = calendarMonth.getYearMonth();
                String monthText = yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + yearMonth.getYear();
                container.monthText.setText(monthText);

                container.prevBtn.setOnClickListener(v -> calendarView.smoothScrollToMonth(yearMonth.minusMonths(1)));
                container.nextBtn.setOnClickListener(v -> calendarView.smoothScrollToMonth(yearMonth.plusMonths(1)));
            }
        });

        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
                TextView textView = container.textView;
                LinearLayout barLayout = container.bar;

                textView.setText(String.valueOf(day.getDate().getDayOfMonth()));
                LocalDate date = day.getDate();

                if (day.getPosition() == DayPosition.MonthDate) {
                    textView.setAlpha(1f);
                    if (date.equals(LocalDate.now())) {
                        textView.setTextColor(getColor(R.color.accent_light_blue));
                    } else {
                        textView.setTextColor(getColor(R.color.text_primary));
                    }

                    if (date.equals(selectedDate)) {
                        textView.setBackgroundResource(R.drawable.bg_day_selector);
                        textView.setTextColor(Color.WHITE);
                    } else {
                        textView.setBackgroundResource(R.drawable.bg_calendar_day_default);
                    }

                    barLayout.removeAllViews();
                    List<Event> events = allEvents.get(date);
                    if (events != null && !events.isEmpty()) {
                        barLayout.setVisibility(View.VISIBLE);
                        int maxBars = Math.min(events.size(), 3);

                        for (int i = 0; i < maxBars; i++) {
                            View indicator = new View(barLayout.getContext());
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 6, 1);
                            params.setMargins(2, 0, 2, 0);
                            indicator.setLayoutParams(params);
                            indicator.setBackgroundColor(Color.parseColor(getColorForIndex(i)));
                            barLayout.addView(indicator);
                        }
                    } else {
                        barLayout.setVisibility(View.GONE);
                    }

                } else {
                    textView.setAlpha(0.3f);
                    textView.setBackground(null);
                    barLayout.setVisibility(View.GONE);
                }

                textView.setOnClickListener(v -> {
                    LocalDate oldDate = selectedDate;
                    selectedDate = date;
                    calendarView.notifyDateChanged(date);
                    if (oldDate != null) calendarView.notifyDateChanged(oldDate);

                    List<Event> events = allEvents.getOrDefault(date, new ArrayList<>());
                    eventAdapter = new EventAdapter(events);
                    eventRecyclerView.setAdapter(eventAdapter);
                    if (!events.isEmpty()) {
                        new EventDetailsDialog(SemesterEventsActivity.this, events).show();
                    }
                });

                container.day = day;
            }
        });

        YearMonth currentMonth = YearMonth.now();
        calendarView.setup(currentMonth.minusMonths(6), currentMonth.plusMonths(6), DayOfWeek.SUNDAY);
        calendarView.scrollToMonth(currentMonth);

    }

    private String getColorForIndex(int index) {
        String[] colors = {
                "#FF6A3D", "#3A86FF", "#00D5D5", "#9147FF"
        };
        return colors[index % colors.length];
    }

    private void preloadEvents() {
        String lang = Locale.getDefault().getLanguage().equals("ko") ? "ko" : "en";
        eventRef.child(lang).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEvents.clear();
                for (DataSnapshot eventSnap : snapshot.getChildren()) {
                    String dateKey = eventSnap.getKey();
                    String value = eventSnap.getValue(String.class);
                    if (dateKey == null || value == null) continue;

                    if (dateKey.contains("~")) {
                        String[] range = dateKey.split("~");
                        LocalDate start = LocalDate.parse(range[0]);
                        LocalDate end = LocalDate.parse(range[1]);
                        LocalDate date = start;
                        while (!date.isAfter(end)) {
                            allEvents.computeIfAbsent(date, k -> new ArrayList<>()).add(new Event(value, ""));
                            date = date.plusDays(1);
                        }
                    } else {
                        LocalDate date = LocalDate.parse(dateKey);
                        for (String part : value.split("\\+")) {
                            allEvents.computeIfAbsent(date, k -> new ArrayList<>()).add(new Event(part.trim(), ""));
                        }
                    }
                }
                calendarView.notifyCalendarChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public static class DayViewContainer extends ViewContainer {
        public final TextView textView;
        public final LinearLayout bar;
        public CalendarDay day;

        public DayViewContainer(@NonNull View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);
            bar = view.findViewById(R.id.eventBars);
        }
    }

    public static class MonthHeaderContainer extends ViewContainer {
        public final TextView monthText;
        public final View prevBtn, nextBtn;

        public MonthHeaderContainer(@NonNull View view) {
            super(view);
            monthText = view.findViewById(R.id.monthText);
            prevBtn = view.findViewById(R.id.prevMonthBtn);
            nextBtn = view.findViewById(R.id.nextMonthBtn);
        }
    }

    private void setupBottomNav(int selectedItemId) {
        ChipNavigationBar bottomNav = findViewById(R.id.navigationBar);
        bottomNav.setItemSelected(selectedItemId, true);
        bottomNav.setOnItemSelectedListener(id -> {
            if (id == selectedItemId) return;
            Intent intent = null;
            if (id == R.id.home) intent = new Intent(this, MainActivity.class);
            else if (id == R.id.explore) intent = new Intent(this, ExploreActivity.class);
            else if (id == R.id.favorite) intent = new Intent(this, FavoritesActivity.class);
            else if (id == R.id.profile) intent = new Intent(this, ProfilePageActivity.class);
            else if (id == R.id.post) {intent = new Intent(this, UploadPostActivity.class);
            }
            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }
}
