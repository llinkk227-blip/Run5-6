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
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int BG =
            Color.rgb(15, 17, 21);

    private static final int CARD =
            Color.rgb(27, 30, 36);

    private static final int FIELD =
            Color.rgb(34, 37, 44);

    private static final int ORANGE =
            Color.rgb(252, 76, 2);

    private static final int WHITE =
            Color.WHITE;

    private static final int SECONDARY =
            Color.rgb(167, 173, 183);

    private static final int GREEN =
            Color.rgb(54, 194, 117);

    private static final int RED =
            Color.rgb(229, 72, 77);

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private FusedLocationProviderClient fusedLocationClient;

    private LocationCallback locationCallback;

    private Location lastLocation;

    private double totalDistanceMeters = 0;

    private long workoutStartTime = 0;

    private long workoutTimeSeconds = 0;

    private boolean workoutRunning = false;

    private boolean workoutFinished = false;

    private double intervalDistanceMeters = 0;

    private long intervalStartTime = 0;

    private boolean intervalRunning = false;

    private final List<String> savedIntervals =
            new ArrayList<>();

    private final Deque<LocationPoint> recentLocations =
            new ArrayDeque<>();

    private TextView timeValue;
    private TextView distanceValue;
    private TextView currentPaceValue;
    private TextView totalPaceValue;
    private TextView intervalPaceValue;
    private TextView statusValue;
    private TextView intervalsText;

    private Button startButton;
    private Button stopButton;
    private Button finishButton;

    private SharedPreferences settings;

    private int paceWindowSeconds = 10;

    private int dp(float value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    private android.graphics.drawable.GradientDrawable background(
            int color,
            float radius) {

        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();

        drawable.setColor(color);

        drawable.setCornerRadius(
                dp(radius)
        );

        return drawable;
    }

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        settings =
                getSharedPreferences(
                        "settings",
                        MODE_PRIVATE
                );

        paceWindowSeconds =
                settings.getInt(
                        "pace_window",
                        10
                );

        if (paceWindowSeconds < 3) {
            paceWindowSeconds = 3;
        }

        if (paceWindowSeconds > 60) {
            paceWindowSeconds = 60;
        }

        fusedLocationClient =
                LocationServices
                        .getFusedLocationProviderClient(
                                this
                        );

        createLocationCallback();

        createInterface();

        checkLocationPermission();
    }

    @Override
    protected void onResume() {

        super.onResume();

        paceWindowSeconds =
                settings.getInt(
                        "pace_window",
                        10
                );

        if (paceWindowSeconds < 3) {
            paceWindowSeconds = 3;
        }

        if (paceWindowSeconds > 60) {
            paceWindowSeconds = 60;
        }

        if (workoutRunning) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {

        super.onPause();

        if (!workoutRunning) {
            stopLocationUpdates();
        }
    }

    private TextView text(
            String value,
            float size,
            boolean bold) {

        TextView view =
                new TextView(this);

        view.setText(value);
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

    private TextView metricValue() {

        TextView view =
                text(
                        "—",
                        30,
                        true
                );

        view.setGravity(
                Gravity.CENTER
        );

        return view;
    }

    private LinearLayout metricCard(
            String title,
            TextView value) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setGravity(
                Gravity.CENTER
        );

        card.setBackground(
                background(
                        CARD,
                        14
                )
        );

        card.setPadding(
                dp(8),
                dp(12),
                dp(8),
                dp(12)
        );

        TextView label =
                text(
                        title,
                        12,
                        true
                );

        label.setTextColor(
                SECONDARY
        );

        label.setGravity(
                Gravity.CENTER
        );

        card.addView(label);

        LinearLayout.LayoutParams valueParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        valueParams.setMargins(
                0,
                dp(7),
                0,
                0
        );

        card.addView(
                value,
                valueParams
        );

        return card;
    }

    private void createInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BG);

        root.setPadding(
                dp(12),
                dp(12),
                dp(12),
                0
        );

        // --------------------------------
        // ЗАГОЛОВОК
        // --------------------------------

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView title =
                text(
                        "RUNINTERVALS",
                        24,
                        true
                );

        title.setTextColor(
                ORANGE
        );

        header.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        statusValue =
                text(
                        "ГОТОВ",
                        12,
                        true
                );

        statusValue.setTextColor(
                SECONDARY
        );

        statusValue.setGravity(
                Gravity.CENTER
        );

        header.addView(
                statusValue
        );

        root.addView(header);

        // --------------------------------
        // ВРЕМЯ
        // --------------------------------

        LinearLayout timeCard =
                new LinearLayout(this);

        timeCard.setOrientation(
                LinearLayout.VERTICAL
        );

        timeCard.setGravity(
                Gravity.CENTER
        );

        timeCard.setBackground(
                background(
                        CARD,
                        16
                )
        );

        timeCard.setPadding(
                dp(12),
                dp(14),
                dp(12),
                dp(14)
        );

        LinearLayout.LayoutParams timeParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        timeParams.setMargins(
                0,
                dp(12),
                0,
                0
        );

        root.addView(
                timeCard,
                timeParams
        );

        TextView timeLabel =
                text(
                        "ВРЕМЯ",
                        12,
                        true
                );

        timeLabel.setTextColor(
                SECONDARY
        );

        timeCard.addView(timeLabel);

        timeValue =
                text(
                        "00:00",
                        48,
                        true
                );

        timeValue.setGravity(
                Gravity.CENTER
        );

        timeCard.addView(
                timeValue
        );

        // --------------------------------
        // ОСНОВНЫЕ ПОКАЗАТЕЛИ
        // --------------------------------

        LinearLayout metrics =
                new LinearLayout(this);

        metrics.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout row1 =
                new LinearLayout(this);

        row1.setOrientation(
                LinearLayout.HORIZONTAL
        );

        distanceValue =
                metricValue();

        currentPaceValue =
                metricValue();

        LinearLayout distanceCard =
                metricCard(
                        "ДИСТАНЦИЯ, КМ",
                        distanceValue
                );

        LinearLayout currentPaceCard =
                metricCard(
                        "ТЕКУЩИЙ ПЕЙС",
                        currentPaceValue
                );

        LinearLayout.LayoutParams half =
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                );

        half.setMargins(
                0,
                dp(10),
                dp(5),
                0
        );

        row1.addView(
                distanceCard,
                half
        );

        LinearLayout.LayoutParams half2 =
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                );

        half2.setMargins(
                dp(5),
                dp(10),
                0,
                0
        );

        row1.addView(
                currentPaceCard,
                half2
        );

        metrics.addView(row1);

        LinearLayout row2 =
                new LinearLayout(this);

        row2.setOrientation(
                LinearLayout.HORIZONTAL
        );

        totalPaceValue =
                metricValue();

        intervalPaceValue =
                metricValue();

        LinearLayout totalPaceCard =
                metricCard(
                        "ОБЩИЙ ПЕЙС",
                        totalPaceValue
                );

        LinearLayout intervalCard =
                metricCard(
                        "ПЕЙС ИНТЕРВАЛА",
                        intervalPaceValue
                );

        LinearLayout.LayoutParams half3 =
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                );

        half3.setMargins(
                0,
                dp(10),
                dp(5),
                0
        );

        row2.addView(
                totalPaceCard,
                half3
        );

        LinearLayout.LayoutParams half4 =
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                );

        half4.setMargins(
                dp(5),
                dp(10),
                0,
                0
        );

        row2.addView(
                intervalCard,
                half4
        );

        metrics.addView(row2);

        root.addView(
                metrics
        );

        // --------------------------------
        // ИНТЕРВАЛЫ
        // --------------------------------

        LinearLayout intervalCard =
                new LinearLayout(this);

        intervalCard.setOrientation(
                LinearLayout.VERTICAL
        );

        intervalCard.setBackground(
                background(
                        CARD,
                        14
                )
        );

        intervalCard.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
        );

        LinearLayout.LayoutParams intervalParams =
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                );

        intervalParams.setMargins(
                0,
                dp(10),
                0,
                dp(8)
        );

        root.addView(
                intervalCard,
                intervalParams
        );

        TextView intervalTitle =
                text(
                        "ИНТЕРВАЛЫ",
                        12,
                        true
                );

        intervalTitle.setTextColor(
                ORANGE
        );

        intervalCard.addView(
                intervalTitle
        );

        intervalsText =
                text(
                        "Интервалы ещё не начаты",
                        13,
                        false
                );

        intervalsText.setTextColor(
                SECONDARY
        );

        intervalsText.setPadding(
                0,
                dp(7),
                0,
                0
        );

        intervalCard.addView(
                intervalsText
        );

        // --------------------------------
        // КНОПКИ УПРАВЛЕНИЯ
        // --------------------------------

        LinearLayout controls =
                new LinearLayout(this);

        controls.setOrientation(
                LinearLayout.HORIZONTAL
        );

        startButton =
                actionButton(
                        "START",
                        ORANGE
                );

        stopButton =
                actionButton(
                        "STOP",
                        RED
                );

        finishButton =
                actionButton(
                        "ФИНИШ",
                        GREEN
                );

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                );

        buttonParams.setMargins(
                0,
                0,
                dp(5),
                0
        );

        controls.addView(
                startButton,
                buttonParams
        );

        LinearLayout.LayoutParams buttonParams2 =
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                );

        buttonParams2.setMargins(
                dp(5),
                0,
                dp(5),
                0
        );

        controls.addView(
                stopButton,
                buttonParams2
        );

        LinearLayout.LayoutParams buttonParams3 =
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                );

        buttonParams3.setMargins(
                dp(5),
                0,
                0,
                0
        );

        controls.addView(
                finishButton,
                buttonParams3
        );

        root.addView(
                controls
        );

        // --------------------------------
        // НИЖНЯЯ НАВИГАЦИЯ
        // --------------------------------

        LinearLayout navigation =
                new LinearLayout(this);

        navigation.setOrientation(
                LinearLayout.HORIZONTAL
        );

        navigation.setGravity(
                Gravity.CENTER
        );

        navigation.setPadding(
                0,
                dp(8),
                0,
                dp(8)
        );

        Button historyButton =
                navigationButton(
                        "История"
                );

        Button statisticsButton =
                navigationButton(
                        "Статистика"
                );

        Button mapButton =
                navigationButton(
                        "Карта"
                );

        Button settingsButton =
                navigationButton(
                        "Настройки"
                );

        navigation.addView(
                historyButton,
                navigationParams()
        );

        navigation.addView(
                statisticsButton,
                navigationParams()
        );

        navigation.addView(
                mapButton,
                navigationParams()
        );

        navigation.addView(
                settingsButton,
                navigationParams()
        );

        root.addView(
                navigation
        );

        // --------------------------------
        // ОБРАБОТЧИКИ
        // --------------------------------

        startButton.setOnClickListener(
                v -> startWorkout()
        );

        stopButton.setOnClickListener(
                v -> stopWorkout()
        );

        finishButton.setOnClickListener(
                v -> finishWorkout()
        );

        historyButton.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                this,
                                HistoryActivity.class
                        )
                )
        );

        statisticsButton.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                this,
                                StatisticsActivity.class
                        )
                )
        );

        mapButton.setOnClickListener(
                v -> openMap()
        );

        settingsButton.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                this,
                                SettingsActivity.class
                        )
                )
        );

        stopButton.setEnabled(false);
        finishButton.setEnabled(false);

        setContentView(root);
    }

    private Button actionButton(
            String title,
            int color) {

        Button button =
                new Button(this);

        button.setText(title);
        button.setTextSize(13);
        button.setTextColor(WHITE);

        button.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        button.setAllCaps(false);

        button.setGravity(
                Gravity.CENTER
        );

        button.setPadding(
                0,
                0,
                0,
                0
        );

        button.setBackground(
                background(
                        color,
                        12
                )
        );

        return button;
    }

    private Button navigationButton(
            String title) {

        Button button =
                new Button(this);

        button.setText(title);
        button.setTextSize(11);
        button.setTextColor(SECONDARY);

        button.setAllCaps(false);

        button.setGravity(
                Gravity.CENTER
        );

        button.setPadding(
                0,
                0,
                0,
                0
        );

        button.setBackgroundColor(
                Color.TRANSPARENT
        );

        return button;
    }

    private LinearLayout.LayoutParams navigationParams() {

        return new LinearLayout.LayoutParams(
                0,
                dp(44),
                1
        );
    }

    // --------------------------------
    // GPS
    // --------------------------------

    private void checkLocationPermission() {

        if (android.os.Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
                    &&
                    checkSelfPermission(
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        LOCATION_PERMISSION_REQUEST
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode ==
                LOCATION_PERMISSION_REQUEST) {

            if (grantResults.length > 0 &&
                    (
                            grantResults[0] ==
                                    PackageManager.PERMISSION_GRANTED
                                    ||
                                    (
                                            grantResults.length > 1
                                                    &&
                                            grantResults[1] ==
                                                    PackageManager.PERMISSION_GRANTED
                                    )
                    )) {

                Toast.makeText(
                        this,
                        "GPS разрешён",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        this,
                        "Для пробежки нужен доступ к GPS",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
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

                            processLocation(location);
                        }
                    }
                };
    }

    private void startLocationUpdates() {

        if (android.os.Build.VERSION.SDK_INT >= 23) {

            boolean fine =
                    checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED;

            boolean coarse =
                    checkSelfPermission(
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED;

            if (!fine && !coarse) {
                checkLocationPermission();
                return;
            }
        }

        LocationRequest request =
                LocationRequest.create();

        request.setInterval(1000);
        request.setFastestInterval(500);
        request.setPriority(
                Priority.PRIORITY_HIGH_ACCURACY
        );
        request.setSmallestDisplacement(1f);

        fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    private void stopLocationUpdates() {

        if (fusedLocationClient != null &&
                locationCallback != null) {

            fusedLocationClient
                    .removeLocationUpdates(
                            locationCallback
                    );
        }
    }

    private void processLocation(
            Location location) {

        if (location == null ||
                !workoutRunning) {
            return;
        }

        long now =
                System.currentTimeMillis();

        recentLocations.addLast(
                new LocationPoint(
                        location,
                        now
                )
        );

        removeOldLocations(now);

        // Первый GPS-пункт.
        if (lastLocation == null) {

            lastLocation =
                    new Location(location);

            return;
        }

        float accuracy =
                location.getAccuracy();

        // Не принимаем явно плохие GPS-точки.
        if (accuracy > 50f) {

            return;
        }

        float delta =
                lastLocation.distanceTo(
                        location
                );

        /*
         * Защита от GPS-скачков.
         *
         * При интервале обновления около 1 сек
         * движение более 100 м за одну точку
         * считаем ошибкой GPS.
         */
        if (delta >= 1f &&
                delta <= 100f) {

            totalDistanceMeters += delta;

            if (intervalRunning) {
                intervalDistanceMeters += delta;
            }
        }

        lastLocation =
                new Location(location);

        updateScreen();
    }

    private void removeOldLocations(
            long now) {

        long windowMillis =
                paceWindowSeconds * 1000L;

        while (!recentLocations.isEmpty()) {

            LocationPoint first =
                    recentLocations.peekFirst();

            if (first == null) {
                break;
            }

            if (now - first.timeMillis >
                    windowMillis) {

                recentLocations.removeFirst();

            } else {

                break;
            }
        }
    }

    // --------------------------------
    // ТРЕНИРОВКА
    // --------------------------------

    private void startWorkout() {

        if (workoutRunning) {
            return;
        }

        workoutRunning = true;
        workoutFinished = false;

        workoutStartTime =
                System.currentTimeMillis();

        workoutTimeSeconds = 0;

        totalDistanceMeters = 0;

        intervalDistanceMeters = 0;

        intervalStartTime = 0;

        intervalRunning = false;

        lastLocation = null;

        recentLocations.clear();

        savedIntervals.clear();

        intervalsText.setText(
                "Бег начат. Можно начинать интервалы."
        );

        statusValue.setText(
                "БЕГ"
        );

        statusValue.setTextColor(
                GREEN
        );

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        finishButton.setEnabled(true);

        startLocationUpdates();

        updateScreen();
    }

    private void stopWorkout() {

        if (!workoutRunning) {
            return;
        }

        workoutRunning = false;

        stopLocationUpdates();

        if (intervalRunning) {

            finishCurrentInterval();
        }

        long now =
                System.currentTimeMillis();

        workoutTimeSeconds =
                Math.max(
                        0,
                        (
                                now -
                                        workoutStartTime
                        ) / 1000
                );

        statusValue.setText(
                "ПАУЗА"
        );

        statusValue.setTextColor(
                SECONDARY
        );

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        finishButton.setEnabled(true);

        updateScreen();
    }

    private void finishWorkout() {

        if (!workoutRunning &&
                workoutStartTime == 0) {
            return;
        }

        if (workoutRunning) {

            workoutRunning = false;

            stopLocationUpdates();

            long now =
                    System.currentTimeMillis();

            workoutTimeSeconds =
                    Math.max(
                            0,
                            (
                                    now -
                                            workoutStartTime
                            ) / 1000
                    );
        }

        if (intervalRunning) {

            finishCurrentInterval();
        }

        workoutFinished = true;

        statusValue.setText(
                "ФИНИШ"
        );

        statusValue.setTextColor(
                ORANGE
        );

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        finishButton.setEnabled(false);

        double distanceKm =
                totalDistanceMeters / 1000.0;

        double weight =
                settings.getFloat(
                        "weight",
                        0f
                );

        double calories =
                CalorieCalculator.calculate(
                        distanceKm,
                        weight
                );

        RunHistory history =
                new RunHistory(this);

        history.addRun(
                distanceKm,
                workoutTimeSeconds,
                calories,
                savedIntervals
        );

        openResult(
                distanceKm,
                workoutTimeSeconds,
                calories
        );
    }

    private void finishCurrentInterval() {

        if (!intervalRunning) {
            return;
        }

        long now =
                System.currentTimeMillis();

        long intervalTime =
                Math.max(
                        1,
                        (
                                now -
                                        intervalStartTime
                        ) / 1000
                );

        double distanceKm =
                intervalDistanceMeters / 1000.0;

        double paceSeconds =
                distanceKm > 0
                        ? intervalTime / distanceKm
                        : 0;

        String intervalText =
                String.format(
                        Locale.getDefault(),
                        "Интервал %d: %.2f км   %s мин/км",
                        savedIntervals.size() + 1,
                        distanceKm,
                        formatPace(
                                paceSeconds
                        )
                );

        savedIntervals.add(
                intervalText
        );

        updateIntervalsText();

        intervalRunning = false;

        intervalDistanceMeters = 0;

        intervalStartTime = 0;

        intervalPaceValue.setText(
                "—"
        );
    }

    /*
     * Этот метод оставлен для управления интервалами
     * внутри приложения.
     */
    private void startInterval() {

        if (!workoutRunning ||
                intervalRunning) {
            return;
        }

        intervalRunning = true;

        intervalDistanceMeters = 0;

        intervalStartTime =
                System.currentTimeMillis();

        intervalPaceValue.setText(
                "—"
        );

        intervalsText.setText(
                "Интервал выполняется..."
        );
    }

    private void updateIntervalsText() {

        if (savedIntervals.isEmpty()) {

            intervalsText.setText(
                    "Интервалы ещё не завершены"
            );

            return;
        }

        StringBuilder builder =
                new StringBuilder();

        for (String interval :
                savedIntervals) {

            builder.append(
                    interval
            );

            builder.append("\n");
        }

        if (intervalRunning) {

            builder.append(
                    "\nТекущий интервал..."
            );
        }

        intervalsText.setText(
                builder.toString()
        );
    }

    // --------------------------------
    // ЭКРАН
    // --------------------------------

    private void updateScreen() {

        long elapsed;

        if (workoutRunning) {

            elapsed =
                    (
                            System.currentTimeMillis()
                                    -
                            workoutStartTime
                    ) / 1000;

            workoutTimeSeconds =
                    elapsed;

        } else {

            elapsed =
                    workoutTimeSeconds;
        }

        timeValue.setText(
                formatTime(elapsed)
        );

        double distanceKm =
                totalDistanceMeters / 1000.0;

        distanceValue.setText(
                String.format(
                        Locale.getDefault(),
                        "%.2f",
                        distanceKm
                )
        );

        double totalPaceSeconds =
                distanceKm > 0
                        ? elapsed / distanceKm
                        : 0;

        if (totalPaceSeconds > 0) {

            totalPaceValue.setText(
                    formatPace(
                            totalPaceSeconds
                    )
            );

        } else {

            totalPaceValue.setText(
                    "—"
            );
        }

        double currentPaceSeconds =
                calculateCurrentPace();

        if (currentPaceSeconds > 0) {

            currentPaceValue.setText(
                    formatPace(
                            currentPaceSeconds
                    )
            );

        } else {

            currentPaceValue.setText(
                    "—"
            );
        }

        if (intervalRunning) {

            long intervalSeconds =
                    Math.max(
                            1,
                            (
                                    System.currentTimeMillis()
                                            -
                                    intervalStartTime
                            ) / 1000
                    );

            double intervalKm =
                    intervalDistanceMeters / 1000.0;

            if (intervalKm > 0) {

                double pace =
                        intervalSeconds /
                                intervalKm;

                intervalPaceValue.setText(
                        formatPace(pace)
                );
            }
        }
    }

    /*
     * Текущий пейс рассчитывается только
     * по GPS-точкам за последние X секунд.
     *
     * X берётся из:
     * SharedPreferences "settings"
     * ключ "pace_window".
     */
    private double calculateCurrentPace() {

        if (recentLocations.size() < 2) {
            return 0;
        }

        LocationPoint first =
                recentLocations.peekFirst();

        LocationPoint last =
                recentLocations.peekLast();

        if (first == null ||
                last == null) {
            return 0;
        }

        long elapsedMillis =
                last.timeMillis -
                        first.timeMillis;

        if (elapsedMillis < 1000) {
            return 0;
        }

        double distance = 0;

        Location previous =
                first.location;

        for (LocationPoint point :
                recentLocations) {

            if (point == first) {
                continue;
            }

            float delta =
                    previous.distanceTo(
                            point.location
                    );

            if (delta >= 1 &&
                    delta <= 100) {

                distance += delta;
            }

            previous =
                    point.location;
        }

        if (distance < 3) {
            return 0;
        }

        double distanceKm =
                distance / 1000.0;

        double seconds =
                elapsedMillis / 1000.0;

        return seconds /
                distanceKm;
    }

    private String formatTime(
            long seconds) {

        long hours =
                seconds / 3600;

        long minutes =
                (seconds % 3600) / 60;

        long secs =
                seconds % 60;

        if (hours > 0) {

            return String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d",
                    hours,
                    minutes,
                    secs
            );
        }

        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                secs
        );
    }

    private String formatPace(
            double secondsPerKm) {

        if (secondsPerKm <= 0 ||
                Double.isNaN(secondsPerKm) ||
                Double.isInfinite(secondsPerKm)) {

            return "—";
        }

        int totalSeconds =
                (int) Math.round(
                        secondsPerKm
                );

        int minutes =
                totalSeconds / 60;

        int seconds =
                totalSeconds % 60;

        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    private String formatDistance() {

        return String.format(
                Locale.getDefault(),
                "%.2f",
                totalDistanceMeters / 1000.0
        );
    }

    // --------------------------------
    // РЕЗУЛЬТАТ
    // --------------------------------

    private void openResult(
            double distanceKm,
            long timeSeconds,
            double calories) {

        Intent intent =
                new Intent(
                        this,
                        ResultActivity.class
                );

        intent.putExtra(
                "distance",
                distanceKm
        );

        intent.putExtra(
                "time",
                timeSeconds
        );

        intent.putExtra(
                "calories",
                calories
        );

        intent.putStringArrayListExtra(
                "intervals",
                new ArrayList<>(
                        savedIntervals
                )
        );

        startActivity(intent);
    }

    // --------------------------------
    // КАРТА
    // --------------------------------

    private void openMap() {

        try {

            Intent intent =
                    new Intent(
                            this,
                            Class.forName(
                                    "com.example.runintervals.MapActivity"
                            )
                    );

            startActivity(intent);

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Карта будет добавлена на следующем шаге",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // --------------------------------
    // ВНУТРЕННИЙ КЛАСС GPS-ТОЧКИ
    // --------------------------------

    private static class LocationPoint {

        final Location location;

        final long timeMillis;

        LocationPoint(
                Location location,
                long timeMillis) {

            this.location =
                    new Location(location);

            this.timeMillis =
                    timeMillis;
        }
    }
            }
