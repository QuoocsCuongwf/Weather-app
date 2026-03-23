package com.example.weatherapp.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.weatherapp.R;

public class CompassBackgroundView extends View {

    private Paint dartPaint;
    private int windDeg = 0;

    public CompassBackgroundView(Context context) {
        super(context);
        init();
    }

    public CompassBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        dartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dartPaint.setColor(ContextCompat.getColor(getContext(), R.color.compass_dart_color));
        dartPaint.setStyle(Paint.Style.FILL);
    }

    public void setWindDeg(int deg) {
        this.windDeg = deg;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        float cx = width / 2f;
        float cy = height / 2f;
        
        // Dart occupies a good portion of the cell
        float size = Math.min(width, height) / 2.5f;

        Path dartPath = new Path();
        dartPath.moveTo(0, -size);
        dartPath.lineTo(size * 0.7f, size);
        dartPath.lineTo(0, size * 0.5f);
        dartPath.lineTo(-size * 0.7f, size);
        dartPath.close();

        canvas.save();
        canvas.translate(cx, cy);
        canvas.rotate(windDeg);
        canvas.drawPath(dartPath, dartPaint);
        canvas.restore();
    }
}
