package com.example.smunavigator2.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.example.smunavigator2.Domain.Event;
import com.example.smunavigator2.R;

import java.util.List;

public class EventDetailsDialog extends Dialog {

    private final List<Event> events;

    public EventDetailsDialog(Context context, List<Event> events) {
        super(context);
        this.events = events;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_event_details);
        // Apply custom animation
        getWindow().getAttributes().windowAnimations = R.style.DialogFadeSlide;

        TextView eventDetailsText = findViewById(R.id.eventDetailsText);
        TextView closeButton = findViewById(R.id.closeButton);

        StringBuilder details = new StringBuilder();
        for (Event event : events) {
            details.append("• ").append(event.getTitle()).append("\n");
            if (!event.getTime().isEmpty()) {
                details.append("   ").append(event.getTime()).append("\n");
            }
        }

        eventDetailsText.setText(details.toString());
        closeButton.setOnClickListener(v -> dismiss());
    }
}
