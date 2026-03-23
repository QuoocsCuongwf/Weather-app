package com.example.weatherapp.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.weatherapp.R;
import com.example.weatherapp.model.ChartForecastItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Custom view that renders a scrollable forecast chart supporting multiple display modes.
 *
 * <p>Use {@link #setForecastData(List, boolean)} to supply data and
 * {@link #setChartMode(ChartMode)} to switch between chart types at runtime.</p>
 */
public class ForecastChartView extends View {

    public enum ChartMode {
        CONDITIONS, WIND, UV_INDEX, PRECIPITATION, HUMIDITY
    }

    private List<ChartForecastItem> data = new ArrayList<>();
    private ChartMode currentMode = ChartMode.CONDITIONS;
    private boolean isDaily = true;

    private Paint textPaint;
    private Paint linePaint;
    private Paint linePaintNight;
    private Paint fillPaint;
    private Paint columnPaint;
    private Paint bgLinePaint;

    private int itemWidth;
    private int iconSize;
    private Map<String, Bitmap> iconCache = new HashMap<>();

    public ForecastChartView(Context context) {
        super(context);
        init();
    }

    public ForecastChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        itemWidth = dpToPx(75);
        iconSize = dpToPx(32);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_text_primary));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dpToPx(14));
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_line_day));
        linePaint.setStrokeWidth(dpToPx(3));
        linePaint.setStyle(Paint.Style.STROKE);

        linePaintNight = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaintNight.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_line_night));
        linePaintNight.setStrokeWidth(dpToPx(3));
        linePaintNight.setStyle(Paint.Style.STROKE);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_fill));
        fillPaint.setStyle(Paint.Style.FILL);

        columnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        columnPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_uv));
        columnPaint.setStyle(Paint.Style.FILL);

        bgLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_grid));
        bgLinePaint.setStrokeWidth(dpToPx(1));
        bgLinePaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * Replaces the current dataset and redraws the chart.
     *
     * @param items   the forecast items to display; {@code null} clears the chart.
     * @param isDaily {@code true} for a daily forecast (shows min/max lines),
     *                {@code false} for an hourly forecast.
     */
    public void setForecastData(List<? extends ChartForecastItem> items, boolean isDaily) {
        this.data = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.isDaily = isDaily;
        preloadIcons();
        requestLayout();
        invalidate();
    }

    /**
     * Switches the active chart type and triggers a redraw.
     *
     * @param mode one of the {@link ChartMode} constants.
     */
    public void setChartMode(ChartMode mode) {
        this.currentMode = mode;
        invalidate();
    }

    private void preloadIcons() {
        for (ChartForecastItem item : data) {
            loadIcon(item.getIcon());
            if (isDaily || currentMode == ChartMode.CONDITIONS) {
                loadIcon(item.getNightIcon());
            }
        }
    }

    private void loadIcon(String iconCode) {
        if (iconCode == null || iconCode.isEmpty() || iconCache.containsKey(iconCode)) return;

        String url = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        Glide.with(getContext())
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        iconCache.put(iconCode, resource);
                        invalidate();
                    }
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = data.size() * itemWidth;
        int height = MeasureSpec.getSize(heightMeasureSpec);
        // Fallback chiều cao khi parent đo ra 0 (thường xảy ra trong một số pass đo của HorizontalScrollView).
        // Dùng 220dp để khớp với android:layout_height="220dp" trong `layout_forecast_widget.xml`.
        if (height <= 0) height = dpToPx(220);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null || data.isEmpty()) return;

        int height = getHeight();
        int width = getWidth();

        // ---- Section: column grid lines ----
        for (int i = 0; i <= data.size(); i++) {
            float x = i * itemWidth;
            canvas.drawLine(x, dpToPx(60), x, height - dpToPx(40), bgLinePaint);
        }

        // ---- Section: column headers (label + date) ----
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
        Paint datePaint = new Paint(textPaint);
        datePaint.setTextSize(dpToPx(12));
        datePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        datePaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_date_text));

        for (int i = 0; i < data.size(); i++) {
            ChartForecastItem item = data.get(i);
            float cx = i * itemWidth + itemWidth / 2f;
            
            String labelStr = item.getLabel();
            if (isDaily && i == 0) labelStr = "Today";

            canvas.drawText(labelStr, cx, dpToPx(20), textPaint);
            
            // Print MM/dd to distinguish cross-midnight. If daily, it's just the date.
            canvas.drawText(dateFormat.format(new Date(item.getDt() * 1000L)), cx, dpToPx(40), datePaint);
        }

        // ---- Section: chart body ----
        if (currentMode == ChartMode.CONDITIONS) {
            drawConditionsChart(canvas, height);
        } else if (currentMode == ChartMode.UV_INDEX) {
            drawUvChart(canvas, height);
        } else if (currentMode == ChartMode.WIND) {
            drawWindChart(canvas, height);
        } else if (currentMode == ChartMode.PRECIPITATION) {
            drawPrecipitationChart(canvas, height);
        } else if (currentMode == ChartMode.HUMIDITY) {
            drawHumidityChart(canvas, height);
        }
    }

    private void drawConditionsChart(Canvas canvas, int height) {
        if (data.size() < 2) return;

        double maxT = Double.MIN_VALUE;
        double minT = Double.MAX_VALUE;
        for (ChartForecastItem item : data) {
            if (item.getMaxTemperature() > maxT) maxT = item.getMaxTemperature();
            if (item.getMinTemperature() < minT) minT = item.getMinTemperature();
        }

        if (maxT == minT) { maxT += 1; minT -= 1; }
        double range = maxT - minT;
        float topY = dpToPx(100);
        float bottomY = height - dpToPx(100);
        float usableHeight = bottomY - topY;

        Path maxPath = new Path();
        Path minPath = new Path();
        Path fillPath = new Path();

        float[] maxXs = new float[data.size()];
        float[] maxYs = new float[data.size()];
        float[] minXs = new float[data.size()];
        float[] minYs = new float[data.size()];

        for (int i = 0; i < data.size(); i++) {
            ChartForecastItem item = data.get(i);
            float cx = i * itemWidth + itemWidth / 2f;

            float yMax = (float) (topY + usableHeight * (1 - (item.getMaxTemperature() - minT) / range));
            float yMin = (float) (topY + usableHeight * (1 - (item.getMinTemperature() - minT) / range));

            maxXs[i] = cx; maxYs[i] = yMax;
            minXs[i] = cx; minYs[i] = yMin;

            if (i == 0) {
                maxPath.moveTo(cx, yMax);
                minPath.moveTo(cx, yMin);
                fillPath.moveTo(cx, yMax);
            } else {
                maxPath.lineTo(cx, yMax);
                minPath.lineTo(cx, yMin);
                fillPath.lineTo(cx, yMax);
            }

            drawBitmapCentered(canvas, item.getIcon(), cx, dpToPx(65));
            if (isDaily || item.getNightIcon() != null && !item.getNightIcon().isEmpty()) {
                drawBitmapCentered(canvas, item.getNightIcon(), cx, height - dpToPx(20));
            }

            canvas.drawText(Math.round(item.getMaxTemperature()) + "°", cx, yMax - dpToPx(10), textPaint);
            if (isDaily || Math.round(item.getMaxTemperature()) != Math.round(item.getMinTemperature())) {
                Paint minTextPaint = new Paint(textPaint);
                minTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_min_text));
                canvas.drawText(Math.round(item.getMinTemperature()) + "°", cx, yMin + dpToPx(20), minTextPaint);
            }
        }

        canvas.drawPath(maxPath, linePaint);
        if (isDaily) {
            canvas.drawPath(minPath, linePaintNight);
            for (int i = data.size() - 1; i >= 0; i--) {
                fillPath.lineTo(minXs[i], minYs[i]);
            }
        } else {
            for (int i = data.size() - 1; i >= 0; i--) {
                fillPath.lineTo(maxXs[i], bottomY + dpToPx(20));
            }
        }
        fillPath.close();
        canvas.drawPath(fillPath, fillPaint);
    }

    private void drawUvChart(Canvas canvas, int height) {
        double maxUv = 0;
        for (ChartForecastItem item : data) {
            if (item.getUvi() > maxUv) maxUv = item.getUvi();
        }
        if (maxUv == 0) maxUv = 11; 
        
        float topY = dpToPx(70);
        float bottomY = height - dpToPx(50);
        float usableHeight = bottomY - topY;
        
        columnPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_uv));

        for (int i = 0; i < data.size(); i++) {
            ChartForecastItem item = data.get(i);
            float cx = i * itemWidth + itemWidth / 2f;
            
            float colH = (float) ((item.getUvi() / maxUv) * usableHeight);
            if (colH < dpToPx(5)) colH = dpToPx(5);
            
            RectF rect = new RectF(cx - dpToPx(6), bottomY - colH, cx + dpToPx(6), bottomY);
            canvas.drawRoundRect(rect, dpToPx(6), dpToPx(6), columnPaint);
            
            canvas.drawText(String.valueOf(Math.round(item.getUvi())), cx, height - dpToPx(20), textPaint);
        }
    }

    private void drawPrecipitationChart(Canvas canvas, int height) {
        float topY = dpToPx(70);
        float bottomY = height - dpToPx(50);
        float usableHeight = bottomY - topY;
        
        columnPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_precipitation));
        
        for (int i = 0; i < data.size(); i++) {
            ChartForecastItem item = data.get(i);
            float cx = i * itemWidth + itemWidth / 2f;
            
            // POP is normally 0 to 1
            float popValue = (float) item.getPop();
            float colH = popValue * usableHeight;
            if (colH < dpToPx(2) && popValue > 0) colH = dpToPx(2);
            else if (popValue == 0) colH = 0;
            
            RectF rect = new RectF(cx - dpToPx(6), bottomY - colH, cx + dpToPx(6), bottomY);
            canvas.drawRoundRect(rect, dpToPx(6), dpToPx(6), columnPaint);
            
            canvas.drawText(Math.round(popValue * 100) + "%", cx, height - dpToPx(20), textPaint);
        }
    }

    private void drawHumidityChart(Canvas canvas, int height) {
        if (data.size() < 2) return;
        
        float topY = dpToPx(90);
        float bottomY = height - dpToPx(60);
        float usableHeight = bottomY - topY;
        
        Path path = new Path();
        Path fillPath = new Path();
        
        columnPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_humidity));
        columnPaint.setStyle(Paint.Style.STROKE);
        columnPaint.setStrokeWidth(dpToPx(3));

        float[] xs = new float[data.size()];
        float[] ys = new float[data.size()];
        
        for (int i = 0; i < data.size(); i++) {
            ChartForecastItem item = data.get(i);
            float cx = i * itemWidth + itemWidth / 2f;
            
            float y = bottomY - (item.getHumidity() / 100f) * usableHeight;
            xs[i] = cx; ys[i] = y;

            if (i == 0) {
                path.moveTo(cx, y);
                fillPath.moveTo(cx, y);
            } else {
                path.lineTo(cx, y);
                fillPath.lineTo(cx, y);
            }
            
            canvas.drawText(item.getHumidity() + "%", cx, y - dpToPx(10), textPaint);
            
            // Draw bullet
            Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            dotPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_text_primary));
            canvas.drawCircle(cx, y, dpToPx(4), dotPaint);
            dotPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_humidity));
            canvas.drawCircle(cx, y, dpToPx(2.5f), dotPaint);
        }
        
        canvas.drawPath(path, columnPaint);
        columnPaint.setStyle(Paint.Style.FILL);
        
        for (int i = data.size() - 1; i >= 0; i--) {
            fillPath.lineTo(xs[i], bottomY);
        }
        fillPath.close();
        Paint humidityFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        humidityFillPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_humidity_fill));
        canvas.drawPath(fillPath, humidityFillPaint);
    }

    private void drawWindChart(Canvas canvas, int height) {
        if (data.size() < 1) return;

        double maxW = Double.MIN_VALUE;
        double minW = Double.MAX_VALUE;
        for (ChartForecastItem item : data) {
            if (item.getWindGust() > maxW) maxW = item.getWindGust();
            if (item.getWindSpeed() > maxW) maxW = item.getWindSpeed();
            if (item.getWindGust() < minW) minW = item.getWindGust();
            if (item.getWindSpeed() < minW) minW = item.getWindSpeed();
        }
        if (maxW == minW) { maxW += 1; minW -= 1; }
        
        float topY = dpToPx(130);
        float bottomY = height - dpToPx(100);
        float usableHeight = bottomY - topY;
        
        int greenColor = ContextCompat.getColor(getContext(), R.color.weather_chart_wind);
        columnPaint.setColor(greenColor);

        Path dartPath = new Path();
        dartPath.moveTo(0, -dpToPx(8));
        dartPath.lineTo(dpToPx(6), dpToPx(8));
        dartPath.lineTo(0, dpToPx(4));
        dartPath.lineTo(-dpToPx(6), dpToPx(8));
        dartPath.close();

        Paint valueTextPaint = new Paint(textPaint);
        valueTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.weather_chart_date_text));
        valueTextPaint.setTextSize(dpToPx(13));
        valueTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        for (int i = 0; i < data.size(); i++) {
            ChartForecastItem item = data.get(i);
            float cx = i * itemWidth + itemWidth / 2f;
            
            canvas.save();
            canvas.translate(cx, dpToPx(70));
            canvas.rotate(item.getWindDeg());
            canvas.drawPath(dartPath, columnPaint);
            canvas.restore();

            float yGust = (float) (topY + usableHeight * (1 - (item.getWindGust() - minW) / (maxW - minW)));
            float ySpeed = (float) (topY + usableHeight * (1 - (item.getWindSpeed() - minW) / (maxW - minW)));

            if (yGust > ySpeed) {
                float temp = yGust;
                yGust = ySpeed;
                ySpeed = temp;
            }

            canvas.drawText(String.format(Locale.getDefault(), "%.1f", Math.max(item.getWindGust(), item.getWindSpeed())), cx, yGust - dpToPx(6), valueTextPaint);
            
            float pillTop = yGust;
            float pillBottom = yGust + dpToPx(12);
            canvas.drawRoundRect(new RectF(cx - dpToPx(2.5f), pillTop, cx + dpToPx(2.5f), pillBottom), dpToPx(2.5f), dpToPx(2.5f), columnPaint);

            canvas.drawText(String.format(Locale.getDefault(), "%.1f", Math.min(item.getWindGust(), item.getWindSpeed())), cx, ySpeed + dpToPx(14), valueTextPaint);
            
            float dotCenterY = ySpeed - dpToPx(4);
            canvas.drawCircle(cx, dotCenterY, dpToPx(2.5f), columnPaint);
            
            canvas.save();
            canvas.translate(cx, height - dpToPx(30));
            canvas.rotate(item.getWindDeg());
            canvas.drawPath(dartPath, columnPaint);
            canvas.restore();
        }
    }


    private void drawBitmapCentered(Canvas canvas, String iconCode, float cx, float cy) {
        Bitmap bitmap = iconCache.get(iconCode);
        if (bitmap != null) {
            float left = cx - iconSize / 2f;
            float top = cy - iconSize / 2f;
            canvas.drawBitmap(bitmap, null, new RectF(left, top, left + iconSize, top + iconSize), null);
        }
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
