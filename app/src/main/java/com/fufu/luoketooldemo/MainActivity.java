package com.fufu.luoketooldemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.fufu.luoketooldemo.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // 在变量声明区域添加
    // 定时连点器相关
    // 定时连点器相关 - 按钮
    private View dimView;
    private WindowManager windowManager;
    private int lastScreenWidth, lastScreenHeight;
    private Button btnOpenFloatWindow, btnAddClickPoint, btnRemoveClickPoint;
    private EditText etIntervalMinutes;
    private ClickerFloatWindow clickerFloatWindow;
    private List<DragIcon> dragIcons = new ArrayList<>();
    private ImageView imgCross, imgCircle, imgBracket, imgDot;
    private PreferenceHelper prefHelper;
    private AimFloatWindow floatWindow;

    // 准心配置
    private String currentStyle = "cross";
    private int currentColor = Color.BLACK;
    private int currentSize = 50;
    private int currentAlpha = 255;
    private int offsetX = 0;
    private int offsetY = 0;

    // UI组件
    private ScrollView scrollViewAim;
    private LinearLayout layoutSunflower;
    private Button btnTabAim, btnTabSunflower;
    private Button btnSingleFlower, btnMultiFlower;

    // 准心UI组件
    private Button btnStyleCross, btnStyleCircle, btnStyleBracket, btnStyleDot;
    private Button btnColorBlack, btnColorWhite, btnColorRed, btnColorYellow, btnColorGreen;
    private SeekBar sizeBar, alphaBar, offsetXBar, offsetYBar;
    private TextView sizeValue, alphaValue;
    private EditText offsetXInput, offsetYInput;
    private Button offsetXSub, offsetXAdd, offsetYSub, offsetYAdd, offsetXBtn, offsetYBtn;
    private Button btnSave, btnCloseAim;

    private int screenWidth, screenHeight;
    private int maxOffsetX, maxOffsetY;
    private int minSize = 50, maxSize = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化 PointManager（只需要一次）
        PointManager.init(this);
        prefHelper = new PreferenceHelper(this);
        loadConfig();

        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        maxOffsetX = screenWidth;
        maxOffsetY = screenHeight;

        lastScreenWidth = screenWidth;
        lastScreenHeight = screenHeight;
        initViews();
        initListeners();
        updateUIFromConfig();

        checkOverlayPermission();
        // 检查无障碍权限（可选，不强制）
        if (!ClickAccessibilityService.isServiceRunning()) {
            // 延迟2秒后提示，不干扰启动
            new Handler().postDelayed(() -> {
                if (!ClickAccessibilityService.isServiceRunning()) {
                    Toast.makeText(this, "提示：定时连点器需要无障碍权限才能工作", Toast.LENGTH_LONG).show();
                }
            }, 2000);
        }
    }

    private void loadConfig() {
        currentStyle = prefHelper.getString(PreferenceHelper.KEY_STYLE, "cross");
        currentColor = prefHelper.getInt(PreferenceHelper.KEY_COLOR, Color.BLACK);
        currentSize = prefHelper.getInt(PreferenceHelper.KEY_SIZE, 50);
        currentAlpha = prefHelper.getInt(PreferenceHelper.KEY_ALPHA, 255);
        offsetX = prefHelper.getInt(PreferenceHelper.KEY_OFFSET_X, 0);
        offsetY = prefHelper.getInt(PreferenceHelper.KEY_OFFSET_Y, 0);
    }

    private void saveConfig() {
        prefHelper.putString(PreferenceHelper.KEY_STYLE, currentStyle);
        prefHelper.putInt(PreferenceHelper.KEY_COLOR, currentColor);
        prefHelper.putInt(PreferenceHelper.KEY_SIZE, currentSize);
        prefHelper.putInt(PreferenceHelper.KEY_ALPHA, currentAlpha);
        prefHelper.putInt(PreferenceHelper.KEY_OFFSET_X, offsetX);
        prefHelper.putInt(PreferenceHelper.KEY_OFFSET_Y, offsetY);
    }

    private void initViews() {
        // 刷向阳花页面新控件
        btnOpenFloatWindow = findViewById(R.id.btnOpenFloatWindow);
        btnAddClickPoint = findViewById(R.id.btnAddClickPoint);
        btnRemoveClickPoint = findViewById(R.id.btnRemoveClickPoint);
        etIntervalMinutes = findViewById(R.id.etIntervalMinutes);

        // 初始化样式图标 ImageView
        imgCross = findViewById(R.id.imgCross);
        imgCircle = findViewById(R.id.imgCircle);
        imgBracket = findViewById(R.id.imgBracket);
        imgDot = findViewById(R.id.imgDot);

        scrollViewAim = findViewById(R.id.scrollViewAim);
        layoutSunflower = findViewById(R.id.layoutSunflower);
        btnTabAim = findViewById(R.id.btnTabAim);
        btnTabSunflower = findViewById(R.id.btnTabSunflower);

        btnStyleCross = findViewById(R.id.btnStyleCross);
        btnStyleCircle = findViewById(R.id.btnStyleCircle);
        btnStyleBracket = findViewById(R.id.btnStyleBracket);
        btnStyleDot = findViewById(R.id.btnStyleDot);

        btnColorBlack = findViewById(R.id.btnColorBlack);
        btnColorWhite = findViewById(R.id.btnColorWhite);
        btnColorRed = findViewById(R.id.btnColorRed);
        btnColorYellow = findViewById(R.id.btnColorYellow);
        btnColorGreen = findViewById(R.id.btnColorGreen);

        sizeBar = findViewById(R.id.sizeBar);
        sizeValue = findViewById(R.id.sizeValue);
        alphaBar = findViewById(R.id.alphaBar);
        alphaValue = findViewById(R.id.alphaValue);

        offsetXBar = findViewById(R.id.offsetXBar);
        offsetYBar = findViewById(R.id.offsetYBar);
        offsetXInput = findViewById(R.id.offsetXInput);
        offsetYInput = findViewById(R.id.offsetYInput);
        offsetXSub = findViewById(R.id.offsetXSub);
        offsetXAdd = findViewById(R.id.offsetXAdd);
        offsetYSub = findViewById(R.id.offsetYSub);
        offsetYAdd = findViewById(R.id.offsetYAdd);
        offsetXBtn = findViewById(R.id.offsetXBtn);
        offsetYBtn = findViewById(R.id.offsetYBtn);

        btnSave = findViewById(R.id.btnSave);
        btnCloseAim = findViewById(R.id.btnCloseAim);

        // 设置范围
        sizeBar.setMax(maxSize - minSize);
        alphaBar.setMax(255);
        offsetXBar.setMax(maxOffsetX * 2);
        offsetYBar.setMax(maxOffsetY * 2);

        // 设置颜色按钮样式
        setupColorButton(btnColorBlack, Color.BLACK);
        setupColorButton(btnColorWhite, Color.WHITE);
        setupColorButton(btnColorRed, Color.parseColor("#F44336"));
        setupColorButton(btnColorYellow, Color.parseColor("#FFEB3B"));
        setupColorButton(btnColorGreen, Color.parseColor("#4CAF50"));

        btnColorWhite.setTextColor(Color.BLACK);
        btnColorYellow.setTextColor(Color.BLACK);
    }

    private void setupColorButton(Button button, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(8 * getResources().getDisplayMetrics().density);
        drawable.setColor(color);
        button.setBackground(drawable);
    }
    private void refreshDragIcons() {
        // 清除现有图标
        for (DragIcon icon : dragIcons) {
            icon.dismiss();
        }
        dragIcons.clear();

        // 根据PointManager重新创建
        List<PointManager.Point> points = PointManager.getInstance().getAllPoints();
        for (int i = 0; i < points.size(); i++) {
            PointManager.Point p = points.get(i);
            // p.x, p.y 就是中心坐标
            DragIcon icon = new DragIcon(this, i + 1, p.x, p.y);
            icon.show();
            dragIcons.add(icon);
        }
    }

    private void startCoordinatePicker() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // 创建灰色遮罩层
        dimView = new View(this);
        dimView.setBackgroundColor(Color.parseColor("#AA000000"));
        dimView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                // 添加点击位置
                PointManager.getInstance().addPoint(x, y);
                refreshDragIcons();

                // 移除遮罩
                windowManager.removeView(dimView);
                dimView = null;

                Toast.makeText(this, "已添加点击位置: (" + x + ", " + y + ")", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        windowManager.addView(dimView, params);
    }
    private void initListeners() {
// ========== 定时连点器按钮 ==========
        btnOpenFloatWindow.setOnClickListener(v -> {
            // 检查无障碍权限
            if (!checkAccessibilityPermission()) {
                return;
            }
            // 检查悬浮窗权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show();
                checkOverlayPermission();
                return;
            }

            // 如果悬浮窗已经显示，则关闭
            if (clickerFloatWindow != null && clickerFloatWindow.isShowing()) {
                clickerFloatWindow.dismiss();
                clickerFloatWindow = null;
                btnOpenFloatWindow.setText("打开悬浮窗");
                btnOpenFloatWindow.setBackgroundColor(Color.parseColor("#4CAF50"));
                return;
            }

            // 创建并显示悬浮窗
            if (clickerFloatWindow == null) {
                clickerFloatWindow = new ClickerFloatWindow(this);
                clickerFloatWindow.setOnTaskToggleListener(new ClickerFloatWindow.OnTaskToggleListener() {
                    @Override
                    public void onTaskStarted() {
                        Toast.makeText(MainActivity.this, "定时任务已开启", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onTaskStopped() {
                        Toast.makeText(MainActivity.this, "定时任务已关闭", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            String minutesStr = etIntervalMinutes.getText().toString();
            int minutes = 1;
            try {
                minutes = Integer.parseInt(minutesStr);
                if (minutes < 0) minutes = 0;
                if (minutes > 60) minutes = 60;
            } catch (NumberFormatException e) {}
            clickerFloatWindow.setCycleInterval(minutes);
            clickerFloatWindow.show();

            btnOpenFloatWindow.setText("关闭悬浮窗");
            btnOpenFloatWindow.setBackgroundColor(Color.parseColor("#F44336"));
        });

        btnAddClickPoint.setOnClickListener(v -> {
            if (PointManager.getInstance().getPointCount() >= 30) {
                Toast.makeText(this, "最多添加30个点击位置", Toast.LENGTH_SHORT).show();
                return;
            }
            startCoordinatePicker();
        });

        btnRemoveClickPoint.setOnClickListener(v -> {
            PointManager.getInstance().removeLastPoint();
            refreshDragIcons();
        });

        // Tab切换
        btnTabAim.setOnClickListener(v -> {
            scrollViewAim.setVisibility(View.VISIBLE);
            layoutSunflower.setVisibility(View.GONE);
            updateTabButtonBackground(btnTabAim);
        });

        btnTabSunflower.setOnClickListener(v -> {
            scrollViewAim.setVisibility(View.GONE);
            layoutSunflower.setVisibility(View.VISIBLE);
            updateTabButtonBackground(btnTabSunflower);
        });

        // 样式按钮
        // 样式按钮点击 - 监听 FrameLayout 而不是 Button
        View.OnClickListener styleClickListener = v -> {
            if (v.getId() == R.id.frameCross) currentStyle = "cross";
            else if (v.getId() == R.id.frameCircle) currentStyle = "circle";
            else if (v.getId() == R.id.frameBracket) currentStyle = "bracket";
            else if (v.getId() == R.id.frameDot) currentStyle = "dot";
            openAimWindow();
            updateStyleButtonBackground();
        };

        findViewById(R.id.frameCross).setOnClickListener(styleClickListener);
        findViewById(R.id.frameCircle).setOnClickListener(styleClickListener);
        findViewById(R.id.frameBracket).setOnClickListener(styleClickListener);
        findViewById(R.id.frameDot).setOnClickListener(styleClickListener);

        // 颜色按钮
        View.OnClickListener colorClickListener = v -> {
            if (v == btnColorBlack) currentColor = Color.BLACK;
            else if (v == btnColorWhite) currentColor = Color.WHITE;
            else if (v == btnColorRed) currentColor = Color.parseColor("#F44336");
            else if (v == btnColorYellow) currentColor = Color.parseColor("#FFEB3B");
            else if (v == btnColorGreen) currentColor = Color.parseColor("#4CAF50");

            updateAllPreviewIcons(currentColor);
            updateFloatWindow();
            Toast.makeText(this, "颜色已更改", Toast.LENGTH_SHORT).show();
        };
        btnColorBlack.setOnClickListener(colorClickListener);
        btnColorWhite.setOnClickListener(colorClickListener);
        btnColorRed.setOnClickListener(colorClickListener);
        btnColorYellow.setOnClickListener(colorClickListener);
        btnColorGreen.setOnClickListener(colorClickListener);

        // 大小
        sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentSize = minSize + progress;
                sizeValue.setText(currentSize + "px");
                updateFloatWindow();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 透明度
        alphaBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentAlpha = progress;
                int percent = (int) (progress / 255.0 * 100);
                alphaValue.setText(percent + "%");
                updateFloatWindow();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        setupOffsetControls();

        btnSave.setOnClickListener(v -> {
            saveConfig();
            Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
        });

        btnCloseAim.setOnClickListener(v -> {
            if (floatWindow != null && floatWindow.isShowing()) {
                floatWindow.dismiss();
                updateStyleButtonBackground();
                Toast.makeText(this, "准心已关闭", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "准心未打开", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTabButtonBackground(Button activeButton) {
        btnTabAim.setBackgroundColor(Color.parseColor("#9E9E9E"));
        btnTabSunflower.setBackgroundColor(Color.parseColor("#9E9E9E"));
        activeButton.setBackgroundColor(Color.parseColor("#4CAF50"));
    }

    private void setupOffsetControls() {
        offsetXBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                offsetX = progress - maxOffsetX;
                offsetXInput.setText(String.valueOf(offsetX));
                updateFloatWindow();
                saveConfig();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        offsetYBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                offsetY = progress - maxOffsetY;
                offsetYInput.setText(String.valueOf(offsetY));
                updateFloatWindow();
                saveConfig();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        offsetXSub.setOnClickListener(v -> {
            int newVal = offsetX - 1;
            if (newVal >= -maxOffsetX) {
                offsetX = newVal;
                offsetXBar.setProgress(offsetX + maxOffsetX);
                offsetXInput.setText(String.valueOf(offsetX));
                updateFloatWindow();
                saveConfig();
            }
        });

        offsetXAdd.setOnClickListener(v -> {
            int newVal = offsetX + 1;
            if (newVal <= maxOffsetX) {
                offsetX = newVal;
                offsetXBar.setProgress(offsetX + maxOffsetX);
                offsetXInput.setText(String.valueOf(offsetX));
                updateFloatWindow();
                saveConfig();
            }
        });

        offsetYSub.setOnClickListener(v -> {
            int newVal = offsetY - 1;
            if (newVal >= -maxOffsetY) {
                offsetY = newVal;
                offsetYBar.setProgress(offsetY + maxOffsetY);
                offsetYInput.setText(String.valueOf(offsetY));
                updateFloatWindow();
                saveConfig();
            }
        });

        offsetYAdd.setOnClickListener(v -> {
            int newVal = offsetY + 1;
            if (newVal <= maxOffsetY) {
                offsetY = newVal;
                offsetYBar.setProgress(offsetY + maxOffsetY);
                offsetYInput.setText(String.valueOf(offsetY));
                updateFloatWindow();
                saveConfig();
            }
        });

        offsetXBtn.setOnClickListener(v -> {
            try {
                int val = Integer.parseInt(offsetXInput.getText().toString());
                val = Math.max(-maxOffsetX, Math.min(maxOffsetX, val));
                offsetX = val;
                offsetXBar.setProgress(offsetX + maxOffsetX);
                updateFloatWindow();
                saveConfig();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
            }
        });

        offsetYBtn.setOnClickListener(v -> {
            try {
                int val = Integer.parseInt(offsetYInput.getText().toString());
                val = Math.max(-maxOffsetY, Math.min(maxOffsetY, val));
                offsetY = val;
                offsetYBar.setProgress(offsetY + maxOffsetY);
                updateFloatWindow();
                saveConfig();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawPreviewIcon(ImageView imageView, String style, int color) {
        // 图标大小 36dp
        int sizeDp = 36;
        int sizePx = (int) (sizeDp * getResources().getDisplayMetrics().density);

        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);

        float centerX = sizePx / 2f;
        float centerY = sizePx / 2f;

        switch (style) {
            case "cross":
                paint.setStrokeWidth(Math.max(2, sizePx * 0.08f));
                paint.setStyle(Paint.Style.STROKE);
                float lineLen = sizePx * 0.35f;
                canvas.drawLine(centerX, centerY - lineLen, centerX, centerY + lineLen, paint);
                canvas.drawLine(centerX - lineLen, centerY, centerX + lineLen, centerY, paint);
                break;
            case "circle":
                paint.setStrokeWidth(Math.max(2, sizePx * 0.08f));
                paint.setStyle(Paint.Style.STROKE);
                float radius = sizePx * 0.3f;
                canvas.drawCircle(centerX, centerY, radius, paint);
                break;
            case "bracket":
                paint.setStrokeWidth(Math.max(2, sizePx * 0.08f));
                paint.setStyle(Paint.Style.STROKE);
                float boxW = sizePx * 0.4f;
                float boxH = sizePx * 0.3f;
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
                float dotSize = Math.max(3, sizePx * 0.12f);
                canvas.drawCircle(centerX, centerY, dotSize, paint);
                break;
        }

        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        imageView.setImageDrawable(drawable);
    }

    private void updateAllPreviewIcons(int color) {
        drawPreviewIcon(imgCross, "cross", color);
        drawPreviewIcon(imgCircle, "circle", color);
        drawPreviewIcon(imgBracket, "bracket", color);
        drawPreviewIcon(imgDot, "dot", color);
    }

    private void updateStyleButtonBackground() {
        Button[] styleBtns = {btnStyleCross, btnStyleCircle, btnStyleBracket, btnStyleDot};
        for (Button btn : styleBtns) {
            btn.setBackgroundResource(R.drawable.btn_style_unselected);
        }

        Button selectedBtn = null;
        switch (currentStyle) {
            case "cross": selectedBtn = btnStyleCross; break;
            case "circle": selectedBtn = btnStyleCircle; break;
            case "bracket": selectedBtn = btnStyleBracket; break;
            case "dot": selectedBtn = btnStyleDot; break;
        }
        if (selectedBtn != null) {
            selectedBtn.setBackgroundResource(R.drawable.btn_style_selected);
        }
    }

    private void updateUIFromConfig() {
        sizeBar.setProgress(currentSize - minSize);
        sizeValue.setText(currentSize + "px");
        alphaBar.setProgress(currentAlpha);
        int percent = (int) (currentAlpha / 255.0 * 100);
        alphaValue.setText(percent + "%");
        offsetXBar.setProgress(offsetX + maxOffsetX);
        offsetYBar.setProgress(offsetY + maxOffsetY);
        offsetXInput.setText(String.valueOf(offsetX));
        offsetYInput.setText(String.valueOf(offsetY));

        updateAllPreviewIcons(currentColor);
    }

    private void openAimWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show();
            checkOverlayPermission();
            return;
        }

        if (floatWindow != null && floatWindow.isShowing()) {
            floatWindow.dismiss();
        }

        floatWindow = new AimFloatWindow(this, currentStyle, currentColor, currentSize, currentAlpha, offsetX, offsetY);
        floatWindow.show();
        updateStyleButtonBackground();
        Toast.makeText(this, "准心已打开", Toast.LENGTH_SHORT).show();
    }

    private void updateFloatWindow() {
        if (floatWindow != null && floatWindow.isShowing()) {
            floatWindow.updateParams(currentStyle, currentColor, currentSize, currentAlpha, offsetX, offsetY);
        }
    }

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                new AlertDialog.Builder(this)
                        .setTitle("需要悬浮窗权限")
                        .setMessage("准心功能需要在其他应用上层显示，请点击「去授权」并打开开关")
                        .setPositiveButton("去授权", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, 100);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        int newWidth = getResources().getDisplayMetrics().widthPixels;
        int newHeight = getResources().getDisplayMetrics().heightPixels;

        // 计算缩放比例
        float scaleX = (float) newWidth / lastScreenWidth;
        float scaleY = (float) newHeight / lastScreenHeight;

        // 更新屏幕尺寸
        lastScreenWidth = newWidth;
        lastScreenHeight = newHeight;
        screenWidth = newWidth;
        screenHeight = newHeight;
        maxOffsetX = screenWidth;
        maxOffsetY = screenHeight;

        // 刷新图标显示
        refreshDragIcons();
        updateSlidersRange();

        // 如果准心正在显示，更新其位置
        if (floatWindow != null && floatWindow.isShowing()) {
            openAimWindow(); // 重新打开以刷新位置
        }
    }

    private void updateSlidersRange() {
        maxOffsetX = screenWidth;
        maxOffsetY = screenHeight;
        offsetXBar.setMax(maxOffsetX * 2);
        offsetYBar.setMax(maxOffsetY * 2);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "权限已获取，可以打开准心了", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "未获取权限，准心无法显示", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    // 添加一个检查无障碍服务的方法
    private boolean checkAccessibilityPermission() {
        if (!ClickAccessibilityService.isServiceRunning()) {
            new AlertDialog.Builder(this)
                    .setTitle("需要无障碍权限")
                    .setMessage("定时连点器功能需要开启无障碍服务，请在设置中搜索「无障碍」→「已安装的服务」→ 开启「" + getString(R.string.app_name) + "」")
                    .setPositiveButton("去设置", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (floatWindow != null) {
            floatWindow.dismiss();
        }
        if (clickerFloatWindow != null) {
            clickerFloatWindow.dismiss();
        }
        for (DragIcon icon : dragIcons) {
            icon.dismiss();
        }
        if (dimView != null) {
            try {
                windowManager.removeView(dimView);
            } catch (Exception e) {}
        }
        super.onDestroy();
    }
}