package com.fufu.luoketooldemo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

public class DragIcon {
    private Context context;
    private WindowManager windowManager;
    private TextView iconView;
    private WindowManager.LayoutParams params;
    private boolean isShowing = false;
    private int number;
    private int centerX, centerY;
    private int iconSizeDp = 50; // 图标大小 dp
    private int iconSizePx;       // 实际像素大小

    public DragIcon(Context context, int number, int initialX, int initialY) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.number = number;
        this.centerX = initialX;
        this.centerY = initialY;

        // 提前计算像素大小
        float density = context.getResources().getDisplayMetrics().density;
        this.iconSizePx = (int) (iconSizeDp * density);

        createView();
    }

    private void createView() {
        iconView = new TextView(context);
        iconView.setText(String.valueOf(number));
        iconView.setTextColor(Color.WHITE);
        iconView.setTextSize(14);
        iconView.setGravity(Gravity.CENTER);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.parseColor("#AA4CAF50"));
        drawable.setSize(iconSizePx, iconSizePx);
        iconView.setBackground(drawable);

        iconView.setWidth(iconSizePx);
        iconView.setHeight(iconSizePx);
        iconView.setClickable(false);
        iconView.setFocusable(false);
    }

    public void show() {
        if (isShowing) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !android.provider.Settings.canDrawOverlays(context)) {
            return;
        }

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                iconSizePx, iconSizePx,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;

        // 左上角坐标 = 中心坐标 - 一半宽度
        params.x = centerX - iconSizePx / 2;
        params.y = centerY - iconSizePx / 2;

        windowManager.addView(iconView, params);
        isShowing = true;
    }

    public void dismiss() {
        if (isShowing && iconView != null) {
            windowManager.removeView(iconView);
            isShowing = false;
        }
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public int getNumber() {
        return number;
    }
}