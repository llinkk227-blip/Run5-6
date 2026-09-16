package com.example.runintervals;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.LinearLayout;
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

        createInterface();
        createLocationCallback();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
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

    private void createInterface() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);

        map = new MapView(this);

        root.addView(
                map,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(20, 15, 20, 15);

        statusText = new TextView(this);
        statusText.setText("Готов к пробежке");
        statusText.setTextSize(18);

        timeText = new TextView(this);
        timeText.setText("Время: 00:00");
        timeText.setTextSize(20);

        distanceText = new TextView(this);
        distanceText.setText("Дистанция: 0.00 км");
        distanceText.setTextSize(20);

        paceText = new TextView(this);
        paceText.setText("Темп: --:-- мин/км");
        paceText.setTextSize(20);

        caloriesText = new TextView(this);
        caloriesText.setText("Калории: 0 ккал");
        caloriesText.setTextSize(20);

        info.addView(statusText);
        info.addView(timeText);
        info.addView(distanceText);
        info.addView(paceText);
        info.addView(caloriesText);

        LinearLayout buttons = new LinearLayout(this);

        startButton = new Button(this);
        startButton.setText("СТАРТ");

        pauseButton = new Button(this);
        pauseButton.setText("ПАУЗА");
        pauseButton.setEnabled(false);

        finishButton = new Button(this);
        finishButton.setText("ФИНИШ");
        finishButton.setEnabled(false);

        buttons.addView(
                startButton,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        buttons.addView(
                pauseButton,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        buttons.addView(
                finishButton,
                new LinearLayout.LayoutParams(0, -2, 1)
        );

        info.addView(buttons);

        settingsButton = new Button(this);
        settingsButton.setText("НАСТРОЙКИ");

        info.addView(settingsButton);

        root.addView(info);

        setContentView(root);

        startButton.setOnClickListener(v -> startRun());

        pauseButton.setOnClickListener(v -> togglePause());

        finishButton.setOnClickListener(v -> finishRun());

        settingsButton.setOnClickListener(v -> openSettings());

        map.setMultiTouchControls(true);

        GeoPoint startPoint =
                new GeoPoint(44.7866, 20.4489);

        map.getController().setZoom(14.0);
        map.getController().setCenter(startPoint);
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
            public void onLocationResult(LocationResult result) {

                if (result == null) {
                    return;
                }

                for (Location location : result.getLocations()) {

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

        startTime = System.currentTimeMillis();

        statusText.setText("Пробежка идёт");

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
            pauseStart = System.currentTimeMillis();

            statusText.setText("Пауза");
            pauseButton.setText("ПРОДОЛЖИТЬ");

        } else {

            paused = false;

            pausedDuration +=
                    System.currentTimeMillis() - pauseStart;

            statusText.setText("Пробежка идёт");
            pauseButton.setText("ПАУЗА");

            handler.post(timerRunnable);
        }
    }

    private void finishRun() {

        running = false;
        paused = false;

        stopLocationUpdates();

        statusText.setText("Пробежка завершена");

        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        finishButton.setEnabled(false);

        pauseButton.setText("ПАУЗА");

        updateStats();
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

        Marker marker = new Marker(map);

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

        long seconds = elapsed / 1000;

        timeText.setText(
                String.format(
                        Locale.getDefault(),
                        "Время: %02d:%02d",
                        seconds / 60,
                        seconds % 60
                )
        );

        double km = distance / 1000.0;

        distanceText.setText(
                String.format(
                        Locale.getDefault(),
                        "Дистанция: %.2f км",
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
                        "Калории: %.0f ккал",
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
                            "Темп: %02d:%02d мин/км",
                            paceMin,
                            paceSec
                    )
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (running) {
            handler.post(timerRunnable);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        stopLocationUpdates();
        handler.removeCallbacks(timerRunnable);
    }
        }
