package com.fufu.luoketooldemo;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.view.View;

public class ClickerFloatWindow {
    private Context context;
    private WindowManager windowManager;
    private View floatView;
    private WindowManager.LayoutParams params;
    private boolean isShowing = false;
    private Button btnToggle;
    private TextView tvCountdown;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isTaskRunning = false;
    private int currentClickIndex = 0;
    private long cycleIntervalMillis;
    private long clickIntervalMillis = 3000;
    private OnTaskToggleListener toggleListener;

    private float lastTouchX, lastTouchY;
    private int lastParamsX, lastParamsY;
    private boolean isDragging = false;

    public interface OnTaskToggleListener {
        void onTaskStarted();
        void onTaskStopped();
    }

    public ClickerFloatWindow(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        createView();
    }

    private void createView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        floatView = inflater.inflate(R.layout.float_clicker_window, null);
        btnToggle = floatView.findViewById(R.id.btnToggleTask);
        tvCountdown = floatView.findViewById(R.id.tvCountdown);

        btnToggle.setOnClickListener(v -> {
            if (isTaskRunning) {
                stopTask();
                btnToggle.setText("开启");
                btnToggle.setBackgroundColor(0xFF4CAF50);
                if (toggleListener != null) toggleListener.onTaskStopped();
            } else {
                startTask();
                btnToggle.setText("关闭");
                btnToggle.setBackgroundColor(0xFFF44336);
                if (toggleListener != null) toggleListener.onTaskStarted();
            }
        });

        floatView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = event.getRawX();
                    lastTouchY = event.getRawY();
                    lastParamsX = params.x;
                    lastParamsY = params.y;
                    isDragging = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - lastTouchX;
                    float dy = event.getRawY() - lastTouchY;
                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                        isDragging = true;
                        params.x = lastParamsX + (int) dx;
                        params.y = lastParamsY + (int) dy;
                        windowManager.updateViewLayout(floatView, params);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    return !isDragging;
            }
            return false;
        });
    }

    public void show() {
        if (isShowing) return;

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;

        windowManager.addView(floatView, params);
        isShowing = true;
    }

    public void dismiss() {
        if (isShowing && floatView != null) {
            windowManager.removeView(floatView);
            isShowing = false;
        }
        stopTask();
        handler.removeCallbacksAndMessages(null);
    }

    // 新增：判断悬浮窗是否显示
    public boolean isShowing() {
        return isShowing;
    }

    public void setCycleInterval(long minutes) {
        this.cycleIntervalMillis = minutes * 60 * 1000;
    }

    public void setOnTaskToggleListener(OnTaskToggleListener listener) {
        this.toggleListener = listener;
    }

    private void startTask() {
        if (isTaskRunning) return;
        isTaskRunning = true;
        currentClickIndex = 0;
        scheduleNextClick();
    }

    private void stopTask() {
        isTaskRunning = false;
        handler.removeCallbacksAndMessages(null);
        tvCountdown.setText("00:00");
    }

    private void scheduleNextClick() {
        if (!isTaskRunning) return;

        int pointCount = PointManager.getInstance().getPointCount();
        if (pointCount == 0) {
            stopTask();
            if (btnToggle != null) {
                btnToggle.setText("开启");
                btnToggle.setBackgroundColor(0xFF4CAF50);
            }
            Toast.makeText(context, "没有可点击的位置，请先添加", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentClickIndex >= pointCount) {
            startCountdown();
            return;
        }

        handler.postDelayed(() -> {
            if (!isTaskRunning) return;
            PointManager.Point point = PointManager.getInstance().getPoint(currentClickIndex);
            if (point != null) {
                // 先检查无障碍服务状态
                if (!ClickAccessibilityService.isServiceRunning()) {
                    Log.e("Clicker", "无障碍服务未运行");
                    Toast.makeText(context, "无障碍服务未运行，请检查设置", Toast.LENGTH_LONG).show();
                    stopTask();
                    btnToggle.setText("开启");
                    btnToggle.setBackgroundColor(0xFF4CAF50);
                    return;
                }

                // 显示小红点
                showRedDotAtPosition(point.x, point.y);

                // 延迟50ms后执行点击，让红点先显示
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Log.d("Clicker", "准备点击: (" + point.x + ", " + point.y + ")");
                    ClickAccessibilityService.performClick(point.x, point.y);
                }, 50);
            }
            currentClickIndex++;
            scheduleNextClick();
        }, clickIntervalMillis);
    }

    // 在点击位置显示小红点（更小）
    private void showRedDotAtPosition(int x, int y) {
        View redDot = new View(context);
        redDot.setBackgroundColor(Color.RED);

        int dotSize = 15;
        float density = context.getResources().getDisplayMetrics().density;
        int dotPx = (int) (dotSize * density);

        WindowManager.LayoutParams dotParams = new WindowManager.LayoutParams(
                dotPx, dotPx,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        );
        dotParams.gravity = Gravity.TOP | Gravity.START;
        // 红点中心对齐点击坐标
        dotParams.x = x - dotPx / 2;
        dotParams.y = y - dotPx / 2;

        windowManager.addView(redDot, dotParams);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                windowManager.removeView(redDot);
            } catch (Exception e) {}
        }, 300);
    }

    private void startCountdown() {
        long remainingMillis = cycleIntervalMillis;
        final long startTime = System.currentTimeMillis();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isTaskRunning) return;
                long elapsed = System.currentTimeMillis() - startTime;
                long left = remainingMillis - elapsed;
                if (left <= 0) {
                    currentClickIndex = 0;
                    scheduleNextClick();
                } else {
                    long minutes = left / 60000;
                    long seconds = (left % 60000) / 1000;
                    String timeStr = String.format("%02d:%02d", minutes, seconds);
                    tvCountdown.setText(timeStr);
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }
}