package com.example.smunavigator2.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smunavigator2.R;

public class MarkerUtils {

    public Bitmap createCustomMarker(Context context, int layoutResId) {
        View markerView = LayoutInflater.from(context).inflate(layoutResId, null);



        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int width = markerView.getMeasuredWidth();
        int height = markerView.getMeasuredHeight();

        if (width == 0 || height == 0) {
            width = 80;
            height = 80;
        }

        markerView.layout(0, 0, width, height);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        return bitmap;
    }
}