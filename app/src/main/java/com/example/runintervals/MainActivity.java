package com.example.runintervals;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;

public class MainActivity extends Activity {

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;

    private MapView map;

    private TextView timeText;
    private TextView distanceText;
    private TextView paceText;
    private TextView caloriesText;
    private TextView statusText;

    private Button startButton;
    private Button pauseButton;
    private Button finishButton;
    private Button settingsButton;
    private Button historyButton;
    private Button statisticsButton;

    private boolean running = false;
    private boolean paused = false;

    private long startTime = 0;
    private long pausedDuration = 0;
    private long pauseStart = 0;

    private float distance = 0;
    private Location lastLocation;

    private double weight = 60;

    private final Handler handler = new Handler();

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (running && !paused) {
                updateStats();
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences(
                        "osmdroid",
                        MODE_PRIVATE
                )
        );

        Configuration.getInstance().setUserAgentValue(
                getPackageName()
        );

        locationClient =
                LocationServices.getFusedLocationProviderClient(this);

        loadWeight();
        createInterface();
        createLocationCallback();

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    100
            );
        }
    }

    private void loadWeight() {

        SharedPreferences prefs =
                getSharedPreferences(
                        "run_data",
                        MODE_PRIVATE
                );

        weight = Double.longBitsToDouble(
                prefs.getLong(
                        "weight",
                        Double.doubleToLongBits(60.0)
                )
        );
    }

    private TextView createText(
            String text,
            float size,
            boolean bold) {

        TextView view = new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(
                Color.rgb(35, 35, 35)
        );

        if (bold) {
            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        view.setPadding(
                8, 8, 8, 8
        );

        return view;
    }

    private Button createButton(
            String text) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextSize(15);
        button.setAllCaps(false);

        return button;
    }

    private void createInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                Color.rgb(248, 249, 250)
        );

        TextView title =
                createText(
                        "🏃 RUN INTERVALS",
                        25,
                        true
                );

        title.setGravity(
                Gravity.CENTER
        );

        title.setPadding(
                10, 18, 10, 12
        );

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        statusText =
                createText(
                        "Готов к пробежке",
                        18,
                        true
                );

        statusText.setGravity(
                Gravity.CENTER
        );

        statusText.setPadding(
                10, 8, 10, 10
        );

        root.addView(statusText);

        map = new MapView(this);

        map.setMultiTouchControls(true);

        root.addView(
                map,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        0.43f
                )
        );

        GeoPoint startPoint =
                new GeoPoint(
                        44.7866,
                        20.4489
                );

        map.getController().setZoom(
                14.0
        );

        map.getController().setCenter(
                startPoint
        );

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout info =
                new LinearLayout(this);

        info.setOrientation(
                LinearLayout.VERTICAL
        );

        info.setPadding(
                12, 8, 12, 16
        );

        TextView metricsTitle =
                createText(
                        "ПОКАЗАТЕЛИ",
                        16,
                        true
                );

        metricsTitle.setGravity(
                Gravity.CENTER
        );

        info.addView(metricsTitle);

        LinearLayout row1 =
                new LinearLayout(this);

        row1.setOrientation(
                LinearLayout.HORIZONTAL
        );

        timeText =
                createText(
                        "⏱\n00:00",
                        21,
                        true
                );

        distanceText =
                createText(
                        "📍\n0.00 км",
                        21,
                        true
                );

        timeText.setGravity(
                Gravity.CENTER
        );

        distanceText.setGravity(
                Gravity.CENTER
        );

        row1.addView(
                timeText,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        row1.addView(
                distanceText,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        info.addView(row1);

        LinearLayout row2 =
                new LinearLayout(this);

        row2.setOrientation(
                LinearLayout.HORIZONTAL
        );

        paceText =
                createText(
                        "🏃\n--:-- мин/км",
                        21,
                        true
                );

        caloriesText =
                createText(
                        "🔥\n0 ккал",
                        21,
                        true
                );

        paceText.setGravity(
                Gravity.CENTER
        );

        caloriesText.setGravity(
                Gravity.CENTER
        );

        row2.addView(
                paceText,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        row2.addView(
                caloriesText,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        info.addView(row2);

        LinearLayout controls =
                new LinearLayout(this);

        controls.setOrientation(
                LinearLayout.HORIZONTAL
        );

        startButton =
                createButton("▶ СТАРТ");

        pauseButton =
                createButton("Ⅱ ПАУЗА");

        finishButton =
                createButton("■ ФИНИШ");

        pauseButton.setEnabled(false);
        finishButton.setEnabled(false);

        controls.addView(
                startButton,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        controls.addView(
                pauseButton,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        controls.addView(
                finishButton,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        info.addView(controls);

        historyButton =
                createButton(
                        "📋 История пробежек"
                );

        statisticsButton =
                createButton(
                        "📊 Статистика"
                );

        settingsButton =
                createButton(
                        "⚙ Настройки"
                );

        info.addView(
                historyButton,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        info.addView(
                statisticsButton,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        info.addView(
                settingsButton,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        scroll.addView(info);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        0.57f
                )
        );

        setContentView(root);

        startButton.setOnClickListener(
                v -> startRun()
        );

        pauseButton.setOnClickListener(
                v -> togglePause()
        );

        finishButton.setOnClickListener(
                v -> finishRun()
        );

        historyButton.setOnClickListener(
                v -> openHistory()
        );

        statisticsButton.setOnClickListener(
                v -> openStatistics()
        );

        settingsButton.setOnClickListener(
                v -> openSettings()
        );
    }

    private void openHistory() {

        startActivity(
                new Intent(
                        this,
                        HistoryActivity.class
                )
        );
    }

    private void openStatistics() {

        startActivity(
                new Intent(
                        this,
                        StatisticsActivity.class
                )
        );
    }

    private void openSettings() {

        startActivity(
                new Intent(
                        this,
                        SettingsActivity.class
                )
        );
    }

    private void createLocationCallback() {

        locationCallback =
                new LocationCallback() {

                    @Override
                    public void onLocationResult(
                            LocationResult result) {

                        if (result == null) {
                            return;
                        }

                        for (Location location :
                                result.getLocations()) {

                            if (!running || paused) {
                                continue;
                            }

                            if (lastLocation != null) {

                                float delta =
                                        lastLocation.distanceTo(
                                                location
                                        );

                                if (delta > 1) {
                                    distance += delta;
                                }
                            }

                            lastLocation =
                                    location;

                            updateMap(location);
                            updateStats();
                        }
                    }
                };
    }

    private void startRun() {

        running = true;
        paused = false;

        distance = 0;
        lastLocation = null;
        pausedDuration = 0;

        startTime =
                System.currentTimeMillis();

        statusText.setText(
                "🏃 Пробежка идёт"
        );

        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        finishButton.setEnabled(true);

        startLocationUpdates();

        handler.post(timerRunnable);
    }

    private void togglePause() {

        if (!running) {
            return;
        }

        if (!paused) {

            paused = true;

            pauseStart =
                    System.currentTimeMillis();

            statusText.setText(
                    "⏸ Пауза"
            );

            pauseButton.setText(
                    "▶ ПРОДОЛЖИТЬ"
            );

        } else {

            paused = false;

            pausedDuration +=
                    System.currentTimeMillis()
                            - pauseStart;

            statusText.setText(
                    "🏃 Пробежка идёт"
            );

            pauseButton.setText(
                    "Ⅱ ПАУЗА"
            );

            handler.post(
                    timerRunnable
            );
        }
    }

    private void finishRun() {

        long elapsed =
                System.currentTimeMillis()
                        - startTime
                        - pausedDuration;

        if (elapsed < 0) {
            elapsed = 0;
        }

        long seconds =
                elapsed / 1000;

        double km =
                distance / 1000.0;

        double calories =
                CalorieCalculator.calculate(
                        km,
                        weight
                );

        RunHistory runHistory =
                new RunHistory(this);

        runHistory.addRun(
                km,
                seconds,
                calories
        );

        running = false;
        paused = false;

        stopLocationUpdates();

        statusText.setText(
                "✓ Пробежка завершена"
        );

        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        finishButton.setEnabled(false);

        pauseButton.setText(
                "Ⅱ ПАУЗА"
        );

        updateStats();

        Intent intent =
                new Intent(
                        this,
                        ResultActivity.class
                );

        intent.putExtra(
                "distance",
                km
        );

        intent.putExtra(
                "time",
                seconds
        );

        intent.putExtra(
                "calories",
                calories
        );

        startActivity(intent);
    }

    private void startLocationUpdates() {

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        LocationRequest request =
                new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        2000
                )
                        .setMinUpdateIntervalMillis(
                                1000
                        )
                        .build();

        locationClient.requestLocationUpdates(
                request,
                locationCallback,
                getMainLooper()
        );
    }

    private void stopLocationUpdates() {

        locationClient.removeLocationUpdates(
                locationCallback
        );
    }

    private void updateMap(
            Location location) {

        GeoPoint point =
                new GeoPoint(
                        location.getLatitude(),
                        location.getLongitude()
                );

        map.getController().setCenter(
                point
        );

        Marker marker =
                new Marker(map);

        marker.setPosition(point);
        marker.setTitle(
                "Вы здесь"
        );

        map.getOverlays().clear();
        map.getOverlays().add(marker);

        map.invalidate();
    }

    private void updateStats() {

        if (startTime == 0) {
            return;
        }

        long elapsed =
                System.currentTimeMillis()
                        - startTime
                        - pausedDuration;

        if (elapsed < 0) {
            elapsed = 0;
        }

        long seconds =
                elapsed / 1000;

        timeText.setText(
                String.format(
                        Locale.getDefault(),
                        "⏱\n%02d:%02d",
                        seconds / 60,
                        seconds % 60
                )
        );

        double km =
                distance / 1000.0;

        distanceText.setText(
                String.format(
                        Locale.getDefault(),
                        "📍\n%.2f км",
                        km
                )
        );

        double calories =
                CalorieCalculator.calculate(
                        km,
                        weight
                );

        caloriesText.setText(
                String.format(
                        Locale.getDefault(),
                        "🔥\n%.0f ккал",
                        calories
                )
        );

        if (km > 0.01 && seconds > 0) {

            double paceSeconds =
                    seconds / km;

            int paceMin =
                    (int) (paceSeconds / 60);

            int paceSec =
                    (int) (paceSeconds % 60);

            paceText.setText(
                    String.format(
                            Locale.getDefault(),
                            "🏃\n%02d:%02d мин/км",
                            paceMin,
                            paceSec
                    )
            );
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        loadWeight();

        if (running) {
            handler.post(
                    timerRunnable
            );
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        stopLocationUpdates();

        handler.removeCallbacks(
                timerRunnable
        );
    }
}
