package com.fufu.luoketooldemo;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class ClickAccessibilityService extends AccessibilityService {

    private static ClickAccessibilityService instance;
    private static final String TAG = "ClickAccessibility";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "服务已创建");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 不需要处理事件
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "服务被中断");
    }

    @Override
    public void onDestroy() {
        instance = null;
        Log.d(TAG, "服务已销毁");
        super.onDestroy();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "服务已连接，无障碍服务已就绪");
    }

    /**
     * 模拟点击指定坐标
     */
    public static void performClick(int x, int y) {
        if (instance == null) {
            Log.e(TAG, "performClick: 服务实例为null，请检查无障碍服务是否开启");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "执行点击: (" + x + ", " + y + ")");

            Path clickPath = new Path();
            clickPath.moveTo(x, y);

            GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 50));

            GestureDescription gesture = gestureBuilder.build();

            instance.dispatchGesture(gesture, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    Log.d(TAG, "点击手势完成: (" + x + ", " + y + ")");
                    super.onCompleted(gestureDescription);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    Log.e(TAG, "点击手势被取消: (" + x + ", " + y + ")");
                    super.onCancelled(gestureDescription);
                }
            }, new Handler(Looper.getMainLooper()));
        } else {
            Log.e(TAG, "Android版本过低，不支持手势点击 (需要 Android 7.0+)");
        }
    }

    public static boolean isServiceRunning() {
        boolean running = instance != null;
        Log.d(TAG, "isServiceRunning: " + running);
        return running;
    }
}