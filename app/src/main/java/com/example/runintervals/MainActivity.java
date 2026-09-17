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

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;

    private MapView mapView;
    private MapLibreMap map;

    private TextView timeText;
    private TextView distanceText;
    private TextView paceText;
    private TextView caloriesText;
    private TextView statusText;
    private TextView intervalsText;

    private Button startButton;
    private Button stopButton;
    private Button finishButton;
    private Button settingsButton;
    private Button historyButton;
    private Button statisticsButton;

    private boolean workoutStarted = false;
    private boolean intervalRunning = false;

    private long totalTimeSeconds = 0;
    private long intervalStartTime = 0;

    private float totalDistance = 0;
    private float intervalStartDistance = 0;

    private Location lastLocation;

    private double weight = 60;

    private int intervalNumber = 0;

    private final ArrayList<String> intervalResults =
            new ArrayList<>();

    private final Handler timerHandler =
            new Handler();

    private long workoutStartTime = 0;

    private final Runnable timerRunnable =
            new Runnable() {

                @Override
                public void run() {

                    if (workoutStarted) {

                        long now =
                                System.currentTimeMillis();

                        totalTimeSeconds =
                                (now - workoutStartTime)
                                        / 1000;

                        updateStats();

                        timerHandler.postDelayed(
                                this,
                                1000
                        );
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);

        locationClient =
                LocationServices
                        .getFusedLocationProviderClient(this);

        loadWeight();

        createInterface();

        createLocationCallback();

        requestLocationPermission();
    }

    private void loadWeight() {

        SharedPreferences prefs =
                getSharedPreferences(
                        "run_data",
                        MODE_PRIVATE
                );

        weight =
                Double.longBitsToDouble(
                        prefs.getLong(
                                "weight",
                                Double.doubleToLongBits(60.0)
                        )
                );
    }

    private void requestLocationPermission() {

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

    private TextView createText(
            String text,
            float size,
            boolean bold) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(Color.WHITE);

        if (bold) {
            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        view.setPadding(
                8,
                8,
                8,
                8
        );

        return view;
    }

    private Button createButton(String text) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextSize(15);
        button.setAllCaps(false);

        button.setBackgroundResource(
                R.drawable.button_3d
        );

        button.setTextColor(Color.WHITE);
        button.setElevation(8f);

        return button;
    }

    private void createInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(
                Color.rgb(30, 120, 200)
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
                10,
                15,
                10,
                10
        );

        root.addView(title);

        statusText =
                createText(
                        "Готов к тренировке",
                        18,
                        true
                );

        statusText.setGravity(
                Gravity.CENTER
        );

        root.addView(statusText);

        /*
         * MAPLIBRE
         */

        mapView =
                new MapView(this);

        mapView.onCreate(null);

        root.addView(
                mapView,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        0.38f
                )
        );

        mapView.getMapAsync(
                mapLibreMap -> {

                    map = mapLibreMap;

                    mapLibreMap.setStyle(
                            new Style.Builder()
                                    .fromUri(
                                            "https://tiles.openfreemap.org/styles/liberty"
                                    ),
                            style -> {

                                mapLibreMap.setCameraPosition(
                                        new CameraPosition.Builder()
                                                .target(
                                                        new LatLng(
                                                                44.7866,
                                                                20.4489
                                                        )
                                                )
                                                .zoom(13.0)
                                                .build()
                                );
                            }
                    );
                }
        );

        TextView mapCopyright =
                new TextView(this);

        mapCopyright.setText(
                "© OpenFreeMap © OpenStreetMap contributors"
        );

        mapCopyright.setTextSize(11);

        mapCopyright.setTextColor(
                Color.DKGRAY
        );

        mapCopyright.setBackgroundColor(
                Color.argb(
                        190,
                        255,
                        255,
                        255
                )
        );

        mapCopyright.setGravity(
                Gravity.CENTER
        );

        mapCopyright.setPadding(
                6,
                2,
                6,
                2
        );

        root.addView(
                mapCopyright,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        ScrollView scroll =
                new ScrollView(this);

        LinearLayout info =
                new LinearLayout(this);

        info.setOrientation(
                LinearLayout.VERTICAL
        );

        info.setPadding(
                12,
                5,
                12,
                70
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
                        "🏃 Общий\n--:-- мин/км",
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

        TextView intervalTitle =
                createText(
                        "ИНТЕРВАЛЫ",
                        16,
                        true
                );

        intervalTitle.setGravity(
                Gravity.CENTER
        );

        info.addView(intervalTitle);

        intervalsText =
                createText(
                        "Пока нет интервалов",
                        16,
                        false
                );

        intervalsText.setPadding(
                15,
                5,
                15,
                5
        );

        info.addView(intervalsText);

        LinearLayout controls =
                new LinearLayout(this);

        controls.setOrientation(
                LinearLayout.HORIZONTAL
        );

        startButton =
                createButton("▶ СТАРТ");

        stopButton =
                createButton("■ СТОП");

        finishButton =
                createButton("✓ ФИНИШ");

        stopButton.setEnabled(false);
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
                stopButton,
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

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        params.setMargins(
                0,
                4,
                0,
                4
        );

        info.addView(
                historyButton,
                params
        );

        info.addView(
                statisticsButton,
                params
        );

        info.addView(
                settingsButton,
                params
        );

        TextView bottomSpace =
                new TextView(this);

        bottomSpace.setHeight(60);

        info.addView(bottomSpace);

        scroll.addView(info);

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        0.62f
                )
        );

        setContentView(root);

        startButton.setOnClickListener(
                v -> startInterval()
        );

        stopButton.setOnClickListener(
                v -> stopInterval()
        );

        finishButton.setOnClickListener(
                v -> finishWorkout()
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

                            if (!intervalRunning) {
                                continue;
                            }

                            if (lastLocation != null) {

                                float delta =
                                        lastLocation.distanceTo(
                                                location
                                        );

                                if (delta > 1 &&
                                        delta < 100) {

                                    totalDistance += delta;
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

    private void startInterval() {

        if (!workoutStarted) {

            workoutStarted = true;

            totalTimeSeconds = 0;
            totalDistance = 0;
            intervalNumber = 0;

            intervalResults.clear();

            workoutStartTime =
                    System.currentTimeMillis();

            timerHandler.post(
                    timerRunnable
            );
        }

        intervalRunning = true;

        intervalNumber++;

        intervalStartTime =
                System.currentTimeMillis();

        intervalStartDistance =
                totalDistance;

        statusText.setText(
                "🏃 Интервал " +
                        intervalNumber +
                        " выполняется"
        );

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        finishButton.setEnabled(true);

        startLocationUpdates();
    }

    private void stopInterval() {

        if (!intervalRunning) {
            return;
        }

        long now =
                System.currentTimeMillis();

        long intervalTime =
                (now - intervalStartTime) / 1000;

        float intervalDistance =
                totalDistance -
                        intervalStartDistance;

        double intervalKm =
                intervalDistance / 1000.0;

        double intervalPace =
                intervalKm > 0
                        ? intervalTime / intervalKm
                        : 0;

        int paceMin =
                (int) (intervalPace / 60);

        int paceSec =
                (int) (intervalPace % 60);

        String result =
                String.format(
                        Locale.getDefault(),
                        "Интервал %d: %.2f км   %02d:%02d мин/км",
                        intervalNumber,
                        intervalKm,
                        paceMin,
                        paceSec
                );

        intervalResults.add(result);

        updateIntervalsText();

        intervalRunning = false;

        stopLocationUpdates();

        statusText.setText(
                "⏸ Интервал завершён"
        );

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        finishButton.setEnabled(true);

        lastLocation = null;
    }

    private void updateIntervalsText() {

        StringBuilder builder =
                new StringBuilder();

        for (String item :
                intervalResults) {

            builder.append(item)
                    .append("\n");
        }

        if (intervalRunning) {

            builder.append(
                    "Интервал "
            )
            .append(intervalNumber)
            .append(
                    ": выполняется..."
            );
        }

        if (builder.length() == 0) {

            intervalsText.setText(
                    "Пока нет интервалов"
            );

        } else {

            intervalsText.setText(
                    builder.toString()
            );
        }
    }

    private void finishWorkout() {

        if (!workoutStarted) {
            return;
        }

        if (intervalRunning) {
            stopInterval();
        }

        double km =
                totalDistance / 1000.0;

        double calories =
                CalorieCalculator.calculate(
                        km,
                        weight
                );

        RunHistory history =
                new RunHistory(this);

        history.addRun(
                km,
                totalTimeSeconds,
                calories
        );

        workoutStarted = false;
        intervalRunning = false;

        timerHandler.removeCallbacks(
                timerRunnable
        );

        statusText.setText(
                "✓ Тренировка завершена"
        );

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        finishButton.setEnabled(false);

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
                totalTimeSeconds
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

        if (map == null) {
            return;
        }

        LatLng point =
                new LatLng(
                        location.getLatitude(),
                        location.getLongitude()
                );

        map.setCameraPosition(
                new CameraPosition.Builder()
                        .target(point)
                        .zoom(16.0)
                        .build()
        );
    }

    private void updateStats() {

        timeText.setText(
                String.format(
                        Locale.getDefault(),
                        "⏱\n%02d:%02d",
                        totalTimeSeconds / 60,
                        totalTimeSeconds % 60
                )
        );

        double km =
                totalDistance / 1000.0;

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

        if (km > 0.01 &&
                totalTimeSeconds > 0) {

            double pace =
                    totalTimeSeconds / km;

            int paceMin =
                    (int) (pace / 60);

            int paceSec =
                    (int) (pace % 60);

            paceText.setText(
                    String.format(
                            Locale.getDefault(),
                            "🏃 Общий\n%02d:%02d мин/км",
                            paceMin,
                            paceSec
                    )
            );
        }

        updateIntervalsText();
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

    @Override
    protected void onStart() {

        super.onStart();

        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (mapView != null) {
            mapView.onResume();
        }

        loadWeight();

        if (intervalRunning) {
            updateStats();
        }
    }

    @Override
    protected void onPause() {

        if (mapView != null) {
            mapView.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {

        if (mapView != null) {
            mapView.onStop();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        timerHandler.removeCallbacks(
                timerRunnable
        );

        stopLocationUpdates();

        if (mapView != null) {
            mapView.onDestroy();
        }

        super.onDestroy();
    }

    @Override
    public void onLowMemory() {

        super.onLowMemory();

        if (mapView != null) {
            mapView.onLowMemory();
        }
    }
}
