package com.fufu.luoketooldemo;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PointManager {
    private static PointManager instance;
    private List<Point> points = new ArrayList<>();
    private SharedPreferences prefs;
    private Gson gson;

    // 在 PointManager 中，Point 类的 x, y 应该表示中心坐标
    public static class Point {
        public int x, y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    // 添加一个方法，用于屏幕旋转时按比例缩放所有坐标
    private PointManager(Context context) {
        prefs = context.getSharedPreferences("clicker_points", Context.MODE_PRIVATE);
        gson = new Gson();
        loadPoints();
    }

    public static synchronized PointManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PointManager not initialized. Call init() first.");
        }
        return instance;
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new PointManager(context.getApplicationContext());
        }
    }

    public void addPoint(int x, int y) {
        if (points.size() >= 30) return;  // 10 → 15
        points.add(new Point(x, y));
        savePoints();
    }

    public void removeLastPoint() {
        if (points.size() > 0) {
            points.remove(points.size() - 1);
            savePoints();
        }
    }

    public void updatePoint(int index, int x, int y) {
        if (index >= 0 && index < points.size()) {
            points.get(index).x = x;
            points.get(index).y = y;
            savePoints();
        }
    }

    public int getPointCount() {
        return points.size();
    }

    public Point getPoint(int index) {
        if (index >= 0 && index < points.size()) return points.get(index);
        return null;
    }

    public List<Point> getAllPoints() {
        return new ArrayList<>(points);
    }

    public void clearPoints() {
        points.clear();
        savePoints();
    }

    private void savePoints() {
        String json = gson.toJson(points);
        prefs.edit().putString("points", json).apply();
    }

    private void loadPoints() {
        String json = prefs.getString("points", "");
        if (!json.isEmpty()) {
            try {
                // 使用 TypeToken 获取 List<Point> 的类型
                Type type = new TypeToken<List<Point>>(){}.getType();
                List<Point> loadedPoints = gson.fromJson(json, type);
                if (loadedPoints != null) {
                    points = loadedPoints;
                } else {
                    points = new ArrayList<>();
                }
            } catch (Exception e) {
                e.printStackTrace();
                points = new ArrayList<>();
            }
        } else {
            points = new ArrayList<>();
        }
    }
}