package com.example.smunavigator2.Utils;

import android.content.Context;
import android.util.TypedValue;
import android.widget.LinearLayout;

import com.example.smunavigator2.R;
import com.google.android.material.button.MaterialButton;

/** Outlined, rounded filter buttons. */
public final class FilterButtons {

    private FilterButtons() {}

    /** Adds a checkable filter button to {@code row}; the checked one is filled dark blue. */
    public static MaterialButton add(LinearLayout row, Object tag, boolean checked) {
        Context context = row.getContext();
        float dp = context.getResources().getDisplayMetrics().density;

        MaterialButton button = new MaterialButton(context, null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setTag(tag);
        button.setCheckable(true);
        button.setAllCaps(false);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        button.setCornerRadius((int) (24 * dp));
        button.setElevation(4 * dp);
        button.setStrokeColorResource(R.color.blue_dark);
        button.setBackgroundTintList(context.getColorStateList(R.color.notice_filter_bg));
        button.setTextColor(context.getColorStateList(R.color.notice_filter_text));
        button.setChecked(checked);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd((int) (8 * dp));
        row.addView(button, params);
        return button;
    }

    /** Single selection: checks the button whose tag equals {@code tag}, unchecks the rest. */
    public static void select(LinearLayout row, Object tag) {
        for (int i = 0; i < row.getChildCount(); i++) {
            MaterialButton button = (MaterialButton) row.getChildAt(i);
            button.setChecked(tag.equals(button.getTag()));
        }
    }
}
