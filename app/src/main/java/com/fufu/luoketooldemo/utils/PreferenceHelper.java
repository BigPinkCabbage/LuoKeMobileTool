package com.fufu.luoketooldemo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {
    private static final String PREFS_NAME = "aim_assistant";

    // 准心相关配置
    public static final String KEY_STYLE = "aim_style";
    public static final String KEY_COLOR = "aim_color";
    public static final String KEY_SIZE = "aim_size";
    public static final String KEY_ALPHA = "aim_alpha";
    public static final String KEY_OFFSET_X = "offset_x";
    public static final String KEY_OFFSET_Y = "offset_y";

    // 在类中添加以下常量
    public static final String KEY_MACRO_BTN_SIZE = "macro_btn_size";
    public static final String KEY_MACRO_BTN_OFFSET_X = "macro_btn_offset_x";
    public static final String KEY_MACRO_BTN_OFFSET_Y = "macro_btn_offset_y";
    public static final String KEY_CLICK_X = "click_x";
    public static final String KEY_CLICK_Y = "click_y";

    private SharedPreferences prefs;

    public PreferenceHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public void putInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }
}