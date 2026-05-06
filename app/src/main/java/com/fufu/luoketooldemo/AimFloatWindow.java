package com.fufu.luoketooldemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

public class AimFloatWindow {
    private Context context;
    private WindowManager windowManager;
    private ImageView imageView;
    private WindowManager.LayoutParams params;

    private String style = "cross";
    private int color = Color.BLACK;
    private int size = 40;
    private int alpha = 255;
    private int offsetX = 0;
    private int offsetY = 0;
    private int screenWidth, screenHeight;
    private boolean isShowing = false;

    public AimFloatWindow(Context context, String style, int color, int size, int alpha, int offsetX, int offsetY) {
        this.context = context;
        this.style = style;
        this.color = color;
        this.size = size;
        this.alpha = alpha;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        createView();
    }

    private void createView() {
        imageView = new ImageView(context);
        imageView.setImageBitmap(createAimBitmap());
        imageView.setLayoutParams(new WindowManager.LayoutParams(size, size, 0, 0, PixelFormat.TRANSLUCENT));
    }

    public void show() {
        if (isShowing) return;
        params = new WindowManager.LayoutParams(
                size, size,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        updatePosition();
        windowManager.addView(imageView, params);
        isShowing = true;
    }

    public void dismiss() {
        if (isShowing && imageView != null) {
            windowManager.removeView(imageView);
            isShowing = false;
        }
    }

    public void updateParams(String newStyle, int newColor, int newSize, int newAlpha, int newOffsetX, int newOffsetY) {
        this.style = newStyle;
        this.color = newColor;
        this.size = newSize;
        this.alpha = newAlpha;
        this.offsetX = newOffsetX;
        this.offsetY = newOffsetY;

        if (isShowing) {
            imageView.setImageBitmap(createAimBitmap());
            params.width = size;
            params.height = size;
            updatePosition();
            windowManager.updateViewLayout(imageView, params);
        }
    }

    private void updatePosition() {
        // 以屏幕中心为基准点，加上偏移量
        int centerX = screenWidth / 2 + offsetX;
        int centerY = screenHeight / 2 + offsetY;
        params.x = centerX - size / 2;
        params.y = centerY - size / 2;
    }

    private Bitmap createAimBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        int colorWithAlpha = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
        paint.setColor(colorWithAlpha);

        float centerX = size / 2f;
        float centerY = size / 2f;

        switch (style) {
            case "cross":
                paint.setStrokeWidth(Math.max(2, size * 0.05f));
                paint.setStyle(Paint.Style.STROKE);
                float lineLen = size * 0.4f;
                canvas.drawLine(centerX, centerY - lineLen, centerX, centerY + lineLen, paint);
                canvas.drawLine(centerX - lineLen, centerY, centerX + lineLen, centerY, paint);
                break;
            case "circle":
                paint.setStrokeWidth(Math.max(2, size * 0.05f));
                paint.setStyle(Paint.Style.STROKE);
                float radius = size * 0.35f;
                canvas.drawCircle(centerX, centerY, radius, paint);
                break;
            case "bracket":
                paint.setStrokeWidth(Math.max(2, size * 0.05f));
                paint.setStyle(Paint.Style.STROKE);
                float boxW = size * 0.42f;
                float boxH = size * 0.35f;
                float leftX = centerX - boxW;
                float rightX = centerX + boxW;
                float topY = centerY - boxH;
                float bottomY = centerY + boxH;
                float corner = boxW * 0.35f;
                canvas.drawLine(leftX, topY, leftX + corner, topY, paint);
                canvas.drawLine(leftX, topY, leftX, topY + corner, paint);
                canvas.drawLine(rightX - corner, topY, rightX, topY, paint);
                canvas.drawLine(rightX, topY, rightX, topY + corner, paint);
                canvas.drawLine(leftX, bottomY - corner, leftX, bottomY, paint);
                canvas.drawLine(leftX, bottomY, leftX + corner, bottomY, paint);
                canvas.drawLine(rightX - corner, bottomY, rightX, bottomY, paint);
                canvas.drawLine(rightX, bottomY - corner, rightX, bottomY, paint);
                break;
            case "dot":
            default:
                paint.setStyle(Paint.Style.FILL);
                float dotSize = Math.max(3, size * 0.08f);
                canvas.drawCircle(centerX, centerY, dotSize, paint);
                break;
        }
        return bitmap;
    }

    public boolean isShowing() {
        return isShowing;
    }
}