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

        Configuration.getInstance().setUserAgentValue(getPackageName());

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
                getSharedPreferences("run_data", MODE_PRIVATE);

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
        view.setTextColor(Color.rgb(35, 35, 35));

        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        view.setPadding(8, 8, 8, 8);

        return view;
    }

    private Button createButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(15);
        button.setAllCaps(false);

        return button;
    }

    private void createInterface() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(248, 249, 250));

        TextView title =
                createText("RUN INTERVALS", 24, true);

        title.setGravity(Gravity.CENTER);
        title.setPadding(10, 18, 10, 12);

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        map = new MapView(this);
        map.setMultiTouchControls(true);

        root.addView(
                map,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        0.48f
                )
        );

        GeoPoint startPoint =
                new GeoPoint(44.7866, 20.4489);

        map.getController().setZoom(14.0);
        map.getController().setCenter(startPoint);

        ScrollView scrollView = new ScrollView(this);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(16, 10, 16, 16);

        statusText =
                createText("Готов к пробежке", 18, true);

        statusText.setGravity(Gravity.CENTER);
        info.addView(statusText);

        timeText =
                createText("Время\n00:00", 22, true);

        timeText.setGravity(Gravity.CENTER);
        info.addView(timeText);

        distanceText =
                createText("Дистанция\n0.00 км", 22, true);

        distanceText.setGravity(Gravity.CENTER);
        info.addView(distanceText);

        paceText =
                createText("Темп\n--:-- мин/км", 22, true);

        paceText.setGravity(Gravity.CENTER);
        info.addView(paceText);

        caloriesText =
                createText("Калории\n0 ккал", 22, true);

        caloriesText.setGravity(Gravity.CENTER);
        info.addView(caloriesText);

        LinearLayout buttons =
                new LinearLayout(this);

        buttons.setOrientation(LinearLayout.HORIZONTAL);

        startButton = createButton("▶ СТАРТ");

        pauseButton = createButton("Ⅱ ПАУЗА");
        pauseButton.setEnabled(false);

        finishButton = createButton("■ ФИНИШ");
        finishButton.setEnabled(false);

        buttons.addView(
                startButton,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        buttons.addView(
                pauseButton,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        buttons.addView(
                finishButton,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        info.addView(buttons);

        historyButton =
                createButton("📋  ИСТОРИЯ");

        historyButton.setTextSize(17);

        info.addView(
                historyButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        settingsButton =
                createButton("⚙  НАСТРОЙКИ");

        settingsButton.setTextSize(17);

        info.addView(
                settingsButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        scrollView.addView(info);

        root.addView(
                scrollView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        0.52f
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

        settingsButton.setOnClickListener(
                v -> openSettings()
        );
    }

    private void openHistory() {

        Intent intent =
                new Intent(
                        MainActivity.this,
                        HistoryActivity.class
                );

        startActivity(intent);
    }

    private void openSettings() {

        Intent intent =
                new Intent(
                        MainActivity.this,
                        SettingsActivity.class
                );

        startActivity(intent);
    }

    private void createLocationCallback() {

        locationCallback = new LocationCallback() {

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
                                lastLocation.distanceTo(location);

                        if (delta > 1) {
                            distance += delta;
                        }
                    }

                    lastLocation = location;

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

        statusText.setText("🏃 Пробежка идёт");

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

            statusText.setText("⏸ Пауза");
            pauseButton.setText("▶ ПРОДОЛЖИТЬ");

        } else {

            paused = false;

            pausedDuration +=
                    System.currentTimeMillis()
                            - pauseStart;

            statusText.setText("🏃 Пробежка идёт");

            pauseButton.setText("Ⅱ ПАУЗА");

            handler.post(timerRunnable);
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

        long seconds = elapsed / 1000;

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

        statusText.setText("✓ Пробежка завершена");

        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        finishButton.setEnabled(false);

        pauseButton.setText("Ⅱ ПАУЗА");

        updateStats();

        Intent intent =
                new Intent(
                        MainActivity.this,
                        ResultActivity.class
                );

        intent.putExtra("distance", km);
        intent.putExtra("time", seconds);
        intent.putExtra("calories", calories);

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
                        .setMinUpdateIntervalMillis(1000)
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

    private void updateMap(Location location) {

        GeoPoint point =
                new GeoPoint(
                        location.getLatitude(),
                        location.getLongitude()
                );

        map.getController().setCenter(point);

        Marker marker =
                new Marker(map);

        marker.setPosition(point);
        marker.setTitle("Вы здесь");

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
                        "Время\n%02d:%02d",
                        seconds / 60,
                        seconds % 60
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

        double calories =
                CalorieCalculator.calculate(
                        km,
                        weight
                );

        caloriesText.setText(
                String.format(
                        Locale.getDefault(),
                        "Калории\n%.0f ккал",
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
                            "Темп\n%02d:%02d мин/км",
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
            handler.post(timerRunnable);
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
