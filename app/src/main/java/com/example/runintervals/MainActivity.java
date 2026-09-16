package com.example.runintervals;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.*;

import com.google.android.gms.location.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    private static final int LOCATION_PERMISSION = 1001;

    private LinearLayout root;

    private TextView timeText;
    private TextView distanceText;
    private TextView paceText;
    private TextView caloriesText;

    private MapView map;
    private Polyline trackLine;

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;

    private boolean running = false;
    private boolean paused = false;

    private long startTime;
    private long pausedTime;
    private long pauseStarted;

    private float distance = 0;

    private android.location.Location lastLocation;

    private double weight = 60;

    private final ArrayList<GeoPoint> points =
            new ArrayList<>();

    private SharedPreferences prefs;

    private final Handler handler =
            new Handler();

    private final Runnable timer =
            new Runnable() {
                @Override
                public void run() {
                    if (running) {
                        updateMetrics();
                        handler.postDelayed(
                                this,
                                1000
                        );
                    }
                }
            };

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Configuration.getInstance()
                .setUserAgentValue(
                        getPackageName()
                );

        prefs =
                getSharedPreferences(
                        "runs",
                        MODE_PRIVATE
                );

        weight =
                Double.longBitsToDouble(
                        prefs.getLong(
                                "weight",
                                Double.doubleToLongBits(60)
                        )
                );

        locationClient =
                LocationServices
                        .getFusedLocationProviderClient(
                                this
                        );

        showRunScreen();

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION
            );
        }
    }

    private void base(String title) {

        root = new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                Color.rgb(247, 248, 250)
        );

        LinearLayout header =
                new LinearLayout(this);

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        header.setPadding(
                12, 10, 12, 10
        );

        Button menu =
                new Button(this);

        menu.setText("☰");
        menu.setTextSize(22);
        menu.setBackgroundColor(
                Color.TRANSPARENT
        );

        menu.setOnClickListener(
                v -> showMenu()
        );

        TextView titleText =
                new TextView(this);

        titleText.setText(title);
        titleText.setTextSize(22);
        titleText.setTypeface(null, 1);
        titleText.setTextColor(
                Color.rgb(28, 35, 40)
        );

        titleText.setPadding(
                12, 0, 0, 0
        );

        header.addView(
                menu,
                new LinearLayout.LayoutParams(
                        60, 60
                )
        );

        header.addView(titleText);

        root.addView(header);

        setContentView(root);
    }

    private void showMenu() {

        PopupWindow popup =
                new PopupWindow(
                        this,
                        650,
                        700,
                        true
                );

        LinearLayout menu =
                new LinearLayout(this);

        menu.setOrientation(
                LinearLayout.VERTICAL
        );

        menu.setPadding(
                30, 30, 30, 30
        );

        menu.setBackgroundColor(
                Color.WHITE
        );

        TextView head =
                menuItem(
                        "RUNINTERVALS",
                        22
                );

        TextView run =
                menuItem(
                        "🏃  Пробежка",
                        18
                );

        TextView history =
                menuItem(
                        "📋  История",
                        18
                );

        TextView statistics =
                menuItem(
                        "📊  Статистика",
                        18
                );

        TextView settings =
                menuItem(
                        "⚙  Настройки",
                        18
                );

        menu.addView(head);
        menu.addView(run);
        menu.addView(history);
        menu.addView(statistics);
        menu.addView(settings);

        run.setOnClickListener(v -> {
            popup.dismiss();
            showRunScreen();
        });

        history.setOnClickListener(v -> {
            popup.dismiss();
            showHistoryScreen();
        });

        statistics.setOnClickListener(v -> {
            popup.dismiss();
            showStatisticsScreen();
        });

        settings.setOnClickListener(v -> {
            popup.dismiss();
            showSettingsScreen();
        });

        popup.setContentView(menu);

        popup.setBackgroundDrawable(
                new android.graphics.drawable.ColorDrawable(
                        Color.WHITE
                )
        );

        popup.setElevation(20);

        popup.showAtLocation(
                root,
                Gravity.TOP | Gravity.LEFT,
                0,
                0
        );
    }

    private TextView menuItem(
            String text,
            int size) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(
                Color.rgb(28, 35, 40)
        );

        view.setPadding(
                20, 28, 20, 28
        );

        return view;
    }

    private TextView metric(
            String text) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(16);
        view.setGravity(Gravity.CENTER);
        view.setTextColor(Color.DKGRAY);

        view.setPadding(
                5, 8, 5, 8
        );

        return view;
    }

    private void showRunScreen() {

        base("Пробежка");

        LinearLayout metrics =
                new LinearLayout(this);

        metrics.setOrientation(
                LinearLayout.VERTICAL
        );

        metrics.setPadding(
                15, 0, 15, 5
        );

        timeText =
                metric(
                        "Время\n00:00:00"
                );

        distanceText =
                metric(
                        "Дистанция\n0.00 км"
                );

        paceText =
                metric(
                        "Темп\n—"
                );

        caloriesText =
                metric(
                        "Калории\n0 ккал"
                );

        metrics.addView(timeText);
        metrics.addView(distanceText);
        metrics.addView(paceText);
        metrics.addView(caloriesText);

        root.addView(metrics);

        map = new MapView(this);

        map.setMultiTouchControls(true);

        map.getController().setZoom(15);

        map.getController().setCenter(
                new GeoPoint(
                        44.7866,
                        20.4489
                )
        );

        trackLine =
                new Polyline();

        trackLine.setColor(
                Color.rgb(22, 160, 133)
        );

        trackLine.setWidth(8);

        map.getOverlays().add(
                trackLine
        );

        root.addView(
                map,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        LinearLayout buttons =
                new LinearLayout(this);

        buttons.setGravity(
                Gravity.CENTER
        );

        buttons.setPadding(
                10, 5, 10, 15
        );

        Button start =
                new Button(this);

        start.setText("СТАРТ");

        Button pause =
                new Button(this);

        pause.setText("ПАУЗА");

        Button finish =
                new Button(this);

        finish.setText("ФИНИШ");

        buttons.addView(
                start,
                new LinearLayout.LayoutParams(
                        0, 65, 1
                )
        );

        buttons.addView(
                pause,
                new LinearLayout.LayoutParams(
                        0, 65, 1
                )
        );

        buttons.addView(
                finish,
                new LinearLayout.LayoutParams(
                        0, 65, 1
                )
        );

        root.addView(buttons);

        start.setOnClickListener(
                v -> startRun()
        );

        pause.setOnClickListener(
                v -> pauseRun()
        );

        finish.setOnClickListener(
                v -> finishRun()
        );
    }

    private void startRun() {

        if (running && !paused)
            return;

        if (!running) {

            running = true;
            paused = false;

            startTime =
                    System.currentTimeMillis();

            pausedTime = 0;
            distance = 0;

            lastLocation = null;

            points.clear();

            trackLine.setPoints(
                    points
            );

        } else {

            pausedTime +=
                    System.currentTimeMillis()
                            - pauseStarted;

            paused = false;
        }

        requestLocation();

        handler.removeCallbacks(
                timer
        );

        handler.post(timer);
    }

    private void pauseRun() {

        if (!running)
            return;

        if (!paused) {

            paused = true;

            pauseStarted =
                    System.currentTimeMillis();

            if (locationCallback != null) {

                locationClient
                        .removeLocationUpdates(
                                locationCallback
                        );
            }

        } else {

            pausedTime +=
                    System.currentTimeMillis()
                            - pauseStarted;

            paused = false;

            requestLocation();
        }
    }

    private void requestLocation() {

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        LocationRequest request =
                LocationRequest.create();

        request.setInterval(2000);

        request.setFastestInterval(
                1000
        );

        request.setPriority(
                LocationRequest
                        .PRIORITY_HIGH_ACCURACY
        );

        locationCallback =
                new LocationCallback() {

                    @Override
                    public void onLocationResult(
                            LocationResult result) {

                        for (
                                android.location.Location location :
                                result.getLocations()
                        ) {

                            processLocation(
                                    location
                            );
                        }
                    }
                };

        locationClient
                .requestLocationUpdates(
                        request,
                        locationCallback,
                        getMainLooper()
                );
    }

    private void processLocation(
            android.location.Location location) {

        if (!running || paused)
            return;

        if (lastLocation != null) {

            float segment =
                    lastLocation.distanceTo(
                            location
                    );

            if (segment > 1 &&
                    segment < 100) {

                distance += segment;
            }
        }

        lastLocation = location;

        GeoPoint point =
                new GeoPoint(
                        location.getLatitude(),
                        location.getLongitude()
                );

        points.add(point);

        trackLine.setPoints(
                points
        );

        map.getController()
                .setCenter(point);

        map.invalidate();

        updateMetrics();
    }

    private long activeTime() {

        if (!running)
            return 0;

        long now =
                System.currentTimeMillis();

        long result =
                now - startTime - pausedTime;

        if (paused) {

            result -=
                    now - pauseStarted;
        }

        return Math.max(
                result,
                0
        );
    }

    private void updateMetrics() {

        if (!running)
            return;

        long seconds =
                activeTime() / 1000;

        long hours =
                seconds / 3600;

        long minutes =
                (seconds % 3600) / 60;

        long secs =
                seconds % 60;

        timeText.setText(
                String.format(
                        Locale.getDefault(),
                        "Время\n%02d:%02d:%02d",
                        hours,
                        minutes,
                        secs
                )
        );

        double km =
                distance / 1000.0;

        distanceText.setText(
                String.format(
                        Locale.getDefault(),
                        "Дистанция\n%.2f км",
                        km
                )
        );

        if (km > 0 &&
                seconds > 0) {

            double pace =
                    (seconds / 60.0) / km;

            int min =
                    (int) pace;

            int sec =
                    (int) Math.round(
                            (pace - min) * 60
                    );

            if (sec >= 60) {
                min++;
                sec = 0;
            }

            paceText.setText(
                    String.format(
                            Locale.getDefault(),
                            "Темп\n%d:%02d мин/км",
                            min,
                            sec
                    )
            );
        }

        double calories =
                km * weight * 1.036;

        caloriesText.setText(
                String.format(
                        Locale.getDefault(),
                        "Калории\n%.0f ккал",
                        calories
                )
        );
    }

    private void finishRun() {

        if (!running)
            return;

        if (paused) {

            pausedTime +=
                    System.currentTimeMillis()
                            - pauseStarted;

            paused = false;
        }

        if (locationCallback != null) {

            locationClient
                    .removeLocationUpdates(
                            locationCallback
                    );
        }

        long elapsed =
                activeTime();

        running = false;

        handler.removeCallbacks(
                timer
        );

        saveRun(elapsed);

        showResultScreen(
                elapsed
        );
    }

    private void saveRun(
            long elapsed) {

        try {

            JSONArray history =
                    new JSONArray(
                            prefs.getString(
                                    "history",
                                    "[]"
                            )
                    );

            JSONObject run =
                    new JSONObject();

            String date =
                    new SimpleDateFormat(
                            "dd.MM.yyyy HH:mm",
                            Locale.getDefault()
                    ).format(
                            new Date()
                    );

            double km =
                    distance / 1000.0;

            run.put(
                    "date",
                    date
            );

            run.put(
                    "distance",
                    km
            );

            run.put(
                    "time",
                    elapsed
            );

            run.put(
                    "calories",
                    km * weight * 1.036
            );

            history.put(run);

            prefs.edit()
                    .putString(
                            "history",
                            history.toString()
                    )
                    .apply();

        } catch (Exception ignored) {
        }
    }

    private void showResultScreen(
            long elapsed) {

        base("Результат");

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setGravity(
                Gravity.CENTER
        );

        box.setPadding(
                30, 40, 30, 30
        );

        long seconds =
                elapsed / 1000;

        long hours =
                seconds / 3600;

        long minutes =
                (seconds % 3600) / 60;

        long secs =
                seconds % 60;

        double km =
                distance / 1000.0;

        TextView result =
                new TextView(this);

        result.setGravity(
                Gravity.CENTER
        );

        result.setTextSize(21);

        result.setTextColor(
                Color.rgb(28,35,40)
        );

        result.setText(
                String.format(
                        Locale.getDefault(),
                        "Пробежка завершена\n\n" +
                        "⏱ %02d:%02d:%02d\n\n" +
                        "📍 %.2f км\n\n" +
                        "🔥 %.0f ккал",
                        hours,
                        minutes,
                        secs,
                        km,
                        km * weight * 1.036
                )
        );

        box.addView(result);

        Button history =
                new Button(this);

        history.setText(
                "Открыть историю"
        );

        box.addView(history);

        history.setOnClickListener(
                v -> showHistoryScreen()
        );

        root.addView(
                box,
                new LinearLayout.LayoutParams(
                        -1,
                        -1
                )
        );
    }

    private void showHistoryScreen() {

        base("История");

        LinearLayout list =
                new LinearLayout(this);

        list.setOrientation(
                LinearLayout.VERTICAL
        );

        list.setPadding(
                20, 10, 20, 20
        );

        try {

            JSONArray history =
                    new JSONArray(
                            prefs.getString(
                                    "history",
                                    "[]"
                            )
                    );

            if (history.length() == 0) {

                TextView empty =
                        new TextView(this);

                empty.setText(
                        "Пробежек пока нет"
                );

                empty.setTextSize(18);

                empty.setPadding(
                        20, 30, 20, 30
                );

                list.addView(empty);

            } else {

                for (
                        int i = history.length() - 1;
                        i >= 0;
                        i--
                ) {

                    JSONObject run =
                            history.getJSONObject(i);

                    TextView row =
                            new TextView(this);

                    row.setText(
                            run.getString("date")
                                    + "\n"
                                    + String.format(
                                    Locale.getDefault(),
                                    "%.2f км",
                                    run.getDouble(
                                            "distance"
                                    )
                            )
                    );

                    row.setTextSize(17);

                    row.setPadding(
                            20, 20, 20, 20
                    );

                    list.addView(row);
                }
            }

        } catch (Exception ignored) {
        }

        ScrollView scroll =
                new ScrollView(this);

        scroll.addView(list);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );
    }

    private void showStatisticsScreen() {

        base("Статистика");

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setPadding(
                25, 30, 25, 20
        );

        int count = 0;

        double totalKm = 0;

        long totalTime = 0;

        double totalCalories = 0;

        try {

            JSONArray history =
                    new JSONArray(
                            prefs.getString(
                                    "history",
                                    "[]"
                            )
                    );

            count = history.length();

            for (
                    int i = 0;
                    i < count;
                    i++
            ) {

                JSONObject run =
                        history.getJSONObject(i);

                totalKm +=
                        run.getDouble(
                                "distance"
                        );

                totalTime +=
                        run.getLong(
                                "time"
                        );

                totalCalories +=
                        run.getDouble(
                                "calories"
                        );
            }

        } catch (Exception ignored) {
        }

        TextView stats =
                new TextView(this);

        stats.setTextSize(20);

        stats.setTextColor(
                Color.rgb(28,35,40)
        );

        stats
