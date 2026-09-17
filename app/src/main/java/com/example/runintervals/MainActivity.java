package com.example.runintervals;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
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
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int BG =
            Color.rgb(15, 17, 21);

    private static final int CARD =
            Color.rgb(27, 30, 36);

    private static final int CARD_DARK =
            Color.rgb(22, 24, 29);

    private static final int ORANGE =
            Color.rgb(252, 76, 2);

    private static final int GREEN =
            Color.rgb(54, 194, 117);

    private static final int RED =
            Color.rgb(229, 72, 77);

    private static final int WHITE =
            Color.WHITE;

    private static final int SECONDARY =
            Color.rgb(167, 173, 183);

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

    private int dp(float value) {
        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

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
        view.setTextColor(WHITE);

        if (bold) {
            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return view;
    }

    private GradientDrawable roundedBackground(
            int color,
            float radius) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    private Button createButton(String text) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setTextColor(WHITE);

        button.setGravity(
                Gravity.CENTER
        );

        button.setPadding(
                dp(4),
                0,
                dp(4),
                0
        );

        button.setMinHeight(0);
        button.setMinimumHeight(0);

        button.setBackground(
                roundedBackground(
                        CARD,
                        12
                )
        );

        button.setElevation(
                dp(2)
        );

        return button;
    }

    private Button createNavButton(
            String icon,
            String text) {

        Button button =
                createButton(
                        icon + "\n" + text
                );

        button.setTextSize(11);

        return button;
    }

    private TextView createMetric(
            String label,
            String value) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setGravity(
                Gravity.CENTER
        );

        box.setPadding(
                dp(5),
                dp(6),
                dp(5),
                dp(6)
        );

        box.setBackground(
                roundedBackground(
                        CARD,
                        12
                )
        );

        TextView labelText =
                createText(
                        label,
                        10,
                        false
                );

        labelText.setTextColor(
                SECONDARY
        );

        labelText.setGravity(
                Gravity.CENTER
        );

        TextView valueText =
                createText(
                        value,
                        19,
                        true
                );

        valueText.setGravity(
                Gravity.CENTER
        );

        valueText.setPadding(
                0,
                dp(3),
                0,
                0
        );

        box.addView(labelText);
        box.addView(valueText);

        return createMetricWrapper(box);
    }

    private TextView createMetricWrapper(
            LinearLayout box) {

        TextView result =
                new TextView(this);

        result.setVisibility(View.GONE);

        return result;
    }

    private LinearLayout createMetricBox(
            String label,
            String value) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setGravity(
                Gravity.CENTER
        );

        box.setPadding(
                dp(5),
                dp(7),
                dp(5),
                dp(7)
        );

        box.setBackground(
                roundedBackground(
                        CARD,
                        12
                )
        );

        TextView labelText =
                createText(
                        label,
                        10,
                        false
                );

        labelText.setTextColor(
                SECONDARY
        );

        labelText.setGravity(
                Gravity.CENTER
        );

        TextView valueText =
                createText(
                        value,
                        20,
                        true
                );

        valueText.setGravity(
                Gravity.CENTER
        );

        box.addView(labelText);
        box.addView(valueText);

        return box;
    }

    private void createInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BG);

        // =========================
        // HEADER
        // =========================

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        header.setPadding(
                dp(18),
                dp(7),
                dp(18),
                dp(3)
        );

        TextView title =
                createText(
                        "RUN INTERVALS",
                        22,
                        true
                );

        title.setTextColor(WHITE);

        header.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1
                )
        );

        TextView runner =
                createText(
                        "●",
                        20,
                        true
                );

        runner.setTextColor(ORANGE);
        runner.setGravity(Gravity.CENTER);

        header.addView(
                runner,
                new LinearLayout.LayoutParams(
                        dp(35),
                        dp(42)
                )
        );

        root.addView(header);

        // =========================
        // STATUS
        // =========================

        statusText =
                createText(
                        "ГОТОВ К ТРЕНИРОВКЕ",
                        12,
                        true
                );

        statusText.setTextColor(
                SECONDARY
        );

        statusText.setGravity(
                Gravity.CENTER
        );

        root.addView(
                statusText,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(30)
                )
        );

        // =========================
        // MAP
        // =========================

        mapView =
                new MapView(this);

        mapView.onCreate(null);

        LinearLayout.LayoutParams mapParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(175)
                );

        mapParams.setMargins(
                dp(10),
                0,
                dp(10),
                dp(6)
        );

        root.addView(
                mapView,
                mapParams
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
                createText(
                        "© OpenFreeMap © OpenStreetMap",
                        9,
                        false
                );

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

        root.addView(
                mapCopyright,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(18)
                )
        );

        // =========================
        // METRICS
        // =========================

        LinearLayout metrics =
                new LinearLayout(this);

        metrics.setOrientation(
                LinearLayout.VERTICAL
        );

        metrics.setPadding(
                dp(10),
                dp(5),
                dp(10),
                dp(5)
        );

        LinearLayout metricsRow1 =
                new LinearLayout(this);

        metricsRow1.setOrientation(
                LinearLayout.HORIZONTAL
        );

        LinearLayout metricsRow2 =
                new LinearLayout(this);

        metricsRow2.setOrientation(
                LinearLayout.HORIZONTAL
        );

        timeText =
                createText(
                        "⏱\n00:00",
                        20,
                        true
                );

        distanceText =
                createText(
                        "📍\n0.00 км",
                        20,
                        true
                );

        paceText =
                createText(
                        "🏃\n--:--",
                        20,
                        true
                );

        caloriesText =
                createText(
                        "🔥\n0 ккал",
                        20,
                        true
                );

        timeText.setGravity(Gravity.CENTER);
        distanceText.setGravity(Gravity.CENTER);
        paceText.setGravity(Gravity.CENTER);
        caloriesText.setGravity(Gravity.CENTER);

        LinearLayout box1 =
                createMetricBoxView(
                        "ВРЕМЯ",
                        timeText
                );

        LinearLayout box2 =
                createMetricBoxView(
                        "ДИСТАНЦИЯ",
                        distanceText
                );

        LinearLayout box3 =
                createMetricBoxView(
                        "ОБЩИЙ ПЕЙС",
                        paceText
                );

        LinearLayout box4 =
                createMetricBoxView(
                        "КАЛОРИИ",
                        caloriesText
                );

        addMetricToRow(
                metricsRow1,
                box1
        );

        addMetricToRow(
                metricsRow1,
                box2
        );

        addMetricToRow(
                metricsRow2,
                box3
        );

        addMetricToRow(
                metricsRow2,
                box4
        );

        metrics.addView(
                metricsRow1,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        metrics.addView(
                metricsRow2,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        root.addView(
                metrics,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(120)
                )
        );

        // =========================
        // WORKOUT BUTTONS
        // =========================

        LinearLayout controls =
                new LinearLayout(this);

        controls.setOrientation(
                LinearLayout.HORIZONTAL
        );

        controls.setGravity(
                Gravity.CENTER
        );

        controls.setPadding(
                dp(10),
                dp(3),
                dp(10),
                dp(3)
        );

        startButton =
                createButton(
                        "▶ СТАРТ"
                );

        stopButton =
                createButton(
                        "■ СТОП"
                );

        finishButton =
                createButton(
                        "✓ ФИНИШ"
                );

        startButton.setTextSize(15);
        stopButton.setTextSize(13);
        finishButton.setTextSize(13);

        startButton.setTextColor(WHITE);
        stopButton.setTextColor(WHITE);
        finishButton.setTextColor(WHITE);

        startButton.setBackground(
                roundedBackground(
                        ORANGE,
                        14
                )
        );

        stopButton.setBackground(
                roundedBackground(
                        RED,
                        14
                )
        );

        finishButton.setBackground(
                roundedBackground(
                        GREEN,
                        14
                )
        );

        stopButton.setEnabled(false);
        finishButton.setEnabled(false);

        addControlButton(
                controls,
                startButton,
                1.3f
        );

        addControlButton(
                controls,
                stopButton,
                0.85f
        );

        addControlButton(
                controls,
                finishButton,
                0.85f
        );

        root.addView(
                controls,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                )
        );

        // =========================
        // INTERVALS
        // =========================

        LinearLayout intervalHeader =
                new LinearLayout(this);

        intervalHeader.setGravity(
                Gravity.CENTER_VERTICAL
        );

        intervalHeader.setPadding(
                dp(14),
                dp(3),
                dp(14),
                0
        );

        TextView intervalTitle =
                createText(
                        "ИНТЕРВАЛЫ",
                        12,
                        true
                );

        intervalTitle.setTextColor(
                SECONDARY
        );

        intervalHeader.addView(
                intervalTitle,
                new LinearLayout.LayoutParams(
                        0,
                        dp(28),
                        1
                )
        );

        TextView intervalHint =
                createText(
                        "каждый отрезок",
                        10,
                        false
                );

        intervalHint.setTextColor(
                SECONDARY
        );

        intervalHeader.addView(
                intervalHint
        );

        root.addView(
                intervalHeader,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(28)
                )
        );

        ScrollView intervalScroll =
                new ScrollView(this);

        intervalScroll.setFillViewport(true);

        intervalsText =
                createText(
                        "Пока нет интервалов",
                        13,
                        false
                );

        intervalsText.setTextColor(
                SECONDARY
        );

        intervalsText.setPadding(
                dp(14),
                dp(6),
                dp(14),
                dp(8)
        );

        intervalScroll.addView(
                intervalsText
        );

        root.addView(
                intervalScroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        // =========================
        // BOTTOM NAVIGATION
        // =========================

        LinearLayout navigation =
                new LinearLayout(this);

        navigation.setOrientation(
                LinearLayout.HORIZONTAL
        );

        navigation.setGravity(
                Gravity.CENTER
        );

        navigation.setPadding(
                dp(8),
                dp(4),
                dp(8),
                dp(6)
        );

        Button historyButton =
                createNavButton(
                        "▣",
                        "История"
                );

        Button statisticsButton =
                createNavButton(
                        "▥",
                        "Статистика"
                );

        Button settingsButton =
                createNavButton(
                        "⚙",
                        "Настройки"
                );

        addNavButton(
                navigation,
                historyButton
        );

        addNavButton(
                navigation,
                statisticsButton
        );

        addNavButton(
                navigation,
                settingsButton
        );

        root.addView(
                navigation,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(58)
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

    private LinearLayout createMetricBoxView(
            String label,
            TextView valueView) {

        LinearLayout box =
                new LinearLayout(this);

        box.setOrientation(
                LinearLayout.VERTICAL
        );

        box.setGravity(
                Gravity.CENTER
        );

        box.setBackground(
                roundedBackground(
                        CARD,
                        12
                )
        );

        TextView labelView =
                createText(
                        label,
                        9,
                        false
                );

        labelView.setTextColor(
                SECONDARY
        );

        labelView.setGravity(
                Gravity.CENTER
        );

        box.addView(
                labelView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(18)
                )
        );

        box.addView(
                valueView,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(34)
                )
        );

        return box;
    }

    private void addMetricToRow(
            LinearLayout row,
            View view) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        params.setMargins(
                dp(3),
                dp(2),
                dp(3),
                dp(2)
        );

        row.addView(
                view,
                params
        );
    }

    private void addControlButton(
            LinearLayout row,
            Button button,
            float weight) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        weight
                );

        params.setMargins(
                dp(3),
                0,
                dp(3),
                0
        );

        row.addView(
                button,
                params
        );
    }

    private void addNavButton(
            LinearLayout row,
            Button button) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                );

        params.setMargins(
                dp(3),
                0,
                dp(3),
                0
        );

        row.addView(
                button,
                params
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
                "ИНТЕРВАЛ " +
                        intervalNumber +
                        " • ВЫПОЛНЯЕТСЯ"
        );

        statusText.setTextColor(
                GREEN
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
                        "Интервал %d\n📍 %.2f км\n⏱ %02d:%02d\n🏃 %02d:%02d мин/км",
                        intervalNumber,
                        intervalKm,
                        intervalTime / 60,
                        intervalTime % 60,
                        paceMin,
                        paceSec
                );

        intervalResults.add(result);

        updateIntervalsText();

        intervalRunning = false;

        stopLocationUpdates();

        statusText.setText(
                "ПАУЗА • ИНТЕРВАЛ ЗАВЕРШЁН"
        );

        statusText.setTextColor(
                SECONDARY
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
                    .append("\n\n");
        }

        if (intervalRunning) {

            builder.append(
                    "Интервал "
            )
            .append(intervalNumber)
            .append(
                    " • выполняется..."
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

        List<String> savedIntervals =
                new ArrayList<>(
                        intervalResults
                );

        history.addRun(
                km,
                totalTimeSeconds,
                calories,
                savedIntervals
        );

        workoutStarted = false;
        intervalRunning = false;

        timerHandler.removeCallbacks(
                timerRunnable
        );

        statusText.setText(
                "ТРЕНИРОВКА ЗАВЕРШЕНА"
        );

        statusText.setTextColor(
                GREEN
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
                        "%02d:%02d",
                        totalTimeSeconds / 60,
                        totalTimeSeconds % 60
                )
        );

        double km =
                totalDistance / 1000.0;

        distanceText.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f км",
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
                        "%.0f ккал",
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
                            "%02d:%02d",
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
