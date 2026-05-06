package com.fufu.luoketooldemo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class MacroFloatButton {
    private Context context;
    private WindowManager windowManager;
    private ImageView floatButton;
    private WindowManager.LayoutParams params;

    private int size = 120;
    private int offsetX = 0;
    private int offsetY = 0;
    private int screenWidth, screenHeight;
    private boolean isShowing = false;
    private boolean isLongPressing = false;  // 是否正在长按中

    private OnClickListener clickListener;
    private OnLongPressListener longPressListener;

    public interface OnClickListener {
        void onClick();
    }

    public interface OnLongPressListener {
        void onLongPressStart();  // 按下时调用
        void onLongPressEnd();    // 松手时调用
    }

    public MacroFloatButton(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        createView();
    }

    private void createView() {
        floatButton = new ImageView(context);
        floatButton.setBackgroundColor(Color.parseColor("#4CAF50"));
        floatButton.setAlpha(0.8f);
        floatButton.setScaleType(ImageView.ScaleType.CENTER);
        setPlusIcon();

        floatButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isLongPressing = false;
                    // 立即触发长按开始（持续按压）
                    if (longPressListener != null) {
                        isLongPressing = true;
                        longPressListener.onLongPressStart();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // 松手时结束长按
                    if (isLongPressing && longPressListener != null) {
                        longPressListener.onLongPressEnd();
                        isLongPressing = false;
                    } else if (!isLongPressing && clickListener != null) {
                        // 如果没有触发长按（点击时间很短），执行点击
                        clickListener.onClick();
                    }
                    return true;
            }
            return false;
        });
    }

    private void setPlusIcon() {
        int sizePx = size;
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStyle(android.graphics.Paint.Style.STROKE);
        paint.setStrokeWidth(sizePx * 0.1f);

        float center = sizePx / 2f;
        float lineLen = sizePx * 0.3f;

        canvas.drawLine(center - lineLen, center, center + lineLen, center, paint);
        canvas.drawLine(center, center - lineLen, center, center + lineLen, paint);

        floatButton.setImageBitmap(bitmap);
    }

    public void show() {
        if (isShowing) return;

        params = new WindowManager.LayoutParams(
                size, size,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        updatePosition();

        windowManager.addView(floatButton, params);
        isShowing = true;
    }

    public void dismiss() {
        if (isShowing && floatButton != null) {
            windowManager.removeView(floatButton);
            isShowing = false;
        }
    }

    public void updateParams(int newSize, int newOffsetX, int newOffsetY) {
        this.size = newSize;
        this.offsetX = newOffsetX;
        this.offsetY = newOffsetY;

        if (isShowing) {
            params.width = size;
            params.height = size;
            updatePosition();
            windowManager.updateViewLayout(floatButton, params);
            setPlusIcon();
        }
    }

    private void updatePosition() {
        params.x = screenWidth - size - offsetX;
        params.y = screenHeight / 2 - size / 2 + offsetY;
    }

    public void setOnClickListener(OnClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnLongPressListener(OnLongPressListener listener) {
        this.longPressListener = listener;
    }

    public boolean isShowing() {
        return isShowing;
    }
}