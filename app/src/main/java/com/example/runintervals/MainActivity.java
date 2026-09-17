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
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
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

    private static final String ROUTE_PREFS =
            "current_route";

    private static final String ROUTE_POINTS =
            "points";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastLocation;

    private double totalDistanceMeters = 0;

    private long workoutStartTime = 0;
    private long workoutTimeSeconds = 0;

    private boolean workoutRunning = false;
    private boolean intervalRunning = false;

    private double intervalDistanceMeters = 0;
    private long intervalStartTime = 0;

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
    private Button intervalButton;

    private SharedPreferences settings;
    private SharedPreferences routePreferences;

    private int paceWindowSeconds = 10;

    private final Handler timerHandler =
            new Handler(Looper.getMainLooper());

    private final Runnable timerRunnable =
            new Runnable() {

                @Override
                public void run() {

                    if (workoutRunning) {

                        updateScreen();

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

    private GradientDrawable background(
            int color,
            float radius) {

        GradientDrawable drawable =
                new GradientDrawable();

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

        routePreferences =
                getSharedPreferences(
                        ROUTE_PREFS,
                        MODE_PRIVATE
                );

        loadPaceWindow();

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

        loadPaceWindow();

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

    private void loadPaceWindow() {

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
                        28,
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
                dp(6),
                dp(8),
                dp(6),
                dp(8)
        );

        TextView label =
                text(
                        title,
                        11,
                        true
                );

        label.setTextColor(
                SECONDARY
        );

        label.setGravity(
                Gravity.CENTER
        );

        card.addView(label);

        card.addView(
                value,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
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
                dp(8),
                dp(12),
                0
        );

        // ==========================================
        // HEADER
        // ==========================================

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
                        22,
                        true
                );

        title.setTextColor(ORANGE);

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
                        11,
                        true
                );

        statusValue.setTextColor(
                SECONDARY
        );

        header.addView(
                statusValue
        );

        root.addView(
                header,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(36)
                )
        );

        // ==========================================
        // CONTENT
        // ==========================================

        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        LinearLayout.LayoutParams contentParams =
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                );

        root.addView(
                content,
                contentParams
        );

        // ==========================================
        // TIME
        // ==========================================

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

        TextView timeLabel =
                text(
                        "ВРЕМЯ",
                        11,
                        true
                );

        timeLabel.setTextColor(
                SECONDARY
        );

        timeCard.addView(
                timeLabel
        );

        timeValue =
                text(
                        "00:00",
                        44,
                        true
                );

        timeValue.setGravity(
                Gravity.CENTER
        );

        timeCard.addView(
                timeValue
        );

        content.addView(
                timeCard,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(82)
                )
        );

        // ==========================================
        // METRICS
        // ==========================================

        LinearLayout metrics =
                new LinearLayout(this);

        metrics.setOrientation(
                LinearLayout.VERTICAL
        );

        distanceValue =
                metricValue();

        currentPaceValue =
                metricValue();

        totalPaceValue =
                metricValue();

        intervalPaceValue =
                metricValue();

        LinearLayout row1 =
                new LinearLayout(this);

        row1.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row1.addView(
                metricCard(
                        "ДИСТАНЦИЯ, КМ",
                        distanceValue
                ),
                metricParams(4)
        );

        row1.addView(
                metricCard(
                        "ТЕКУЩИЙ ПЕЙС",
                        currentPaceValue
                ),
                metricParams(0)
        );

        metrics.addView(
                row1,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                )
        );

        LinearLayout row2 =
                new LinearLayout(this);

        row2.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row2.addView(
                metricCard(
                        "ОБЩИЙ ПЕЙС",
                        totalPaceValue
                ),
                metricParams(4)
        );

        row2.addView(
                metricCard(
                        "ПЕЙС ИНТЕРВАЛА",
                        intervalPaceValue
                ),
                metricParams(0)
        );

        metrics.addView(
                row2,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                )
        );

        LinearLayout.LayoutParams metricsParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(148)
                );

        metricsParams.setMargins(
                0,
                dp(6),
                0,
                0
        );

        content.addView(
                metrics,
                metricsParams
        );

        // ==========================================
        // INTERVALS
        // ==========================================

        LinearLayout intervalsCard =
                new LinearLayout(this);

        intervalsCard.setOrientation(
                LinearLayout.VERTICAL
        );

        intervalsCard.setBackground(
                background(
                        CARD,
                        14
                )
        );

        intervalsCard.setPadding(
                dp(12),
                dp(8),
                dp(12),
                dp(8)
        );

        TextView intervalsTitle =
                text(
                        "ИНТЕРВАЛЫ",
                        11,
                        true
                );

        intervalsTitle.setTextColor(
                ORANGE
        );

        intervalsCard.addView(
                intervalsTitle
        );

        intervalsText =
                text(
                        "Интервалы ещё не начаты",
                        12,
                        false
                );

        intervalsText.setTextColor(
                SECONDARY
        );

        intervalsText.setMaxLines(2);

        intervalsCard.addView(
                intervalsText,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        intervalButton =
                actionButton(
                        "НАЧАТЬ ИНТЕРВАЛ",
                        FIELD
                );

        intervalsCard.addView(
                intervalButton,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(36)
                )
        );

        LinearLayout.LayoutParams intervalsParams =
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                );

        intervalsParams.setMargins(
                0,
                dp(6),
                0,
                dp(6)
        );

        content.addView(
                intervalsCard,
                intervalsParams
        );

        // ==========================================
        // FIXED CONTROL BUTTONS
        // ==========================================

        LinearLayout controls =
                new LinearLayout(this);

        controls.setOrientation(
                LinearLayout.HORIZONTAL
        );

        controls.setGravity(
                Gravity.CENTER_VERTICAL
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

        controls.addView(
                startButton,
                controlParams(4)
        );

        controls.addView(
                stopButton,
                controlParams(4)
        );

        controls.addView(
                finishButton,
                controlParams(0)
        );

        root.addView(
                controls,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(46)
                )
        );

        // ==========================================
        // FIXED BOTTOM NAVIGATION
        // ==========================================

        LinearLayout navigation =
                new LinearLayout(this);

        navigation.setOrientation(
                LinearLayout.HORIZONTAL
        );

        navigation.setGravity(
                Gravity.CENTER
        );

        Button historyButton =
                navigationButton("История");

        Button statisticsButton =
                navigationButton("Статистика");

        Button mapButton =
                navigationButton("Карта");

        Button settingsButton =
                navigationButton("Настройки");

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
                navigation,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(48)
                )
        );

        navigation.setOnApplyWindowInsetsListener(
                (v, insets) -> {

                    int bottomInset =
                            insets.getSystemWindowInsetBottom();

                    v.setPadding(
                            0,
                            0,
                            0,
                            bottomInset
                    );

                    LinearLayout.LayoutParams params =
                            (LinearLayout.LayoutParams)
                                    v.getLayoutParams();

                    params.height =
                            dp(48) + bottomInset;

                    v.setLayoutParams(params);

                    return insets;
                }
        );

        // ==========================================
        // BUTTON ACTIONS
        // ==========================================

        startButton.setOnClickListener(
                v -> startWorkout()
        );

        stopButton.setOnClickListener(
                v -> stopWorkout()
        );

        finishButton.setOnClickListener(
                v -> finishWorkout()
        );

        intervalButton.setOnClickListener(
                v -> toggleInterval()
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
        intervalButton.setEnabled(false);

        setContentView(root);
    }

    private LinearLayout.LayoutParams metricParams(
            int rightMargin) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        params.setMargins(
                0,
                0,
                dp(rightMargin),
                0
        );

        return params;
    }

    private LinearLayout.LayoutParams controlParams(
            int rightMargin) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        -1,
                        1
                );

        params.setMargins(
                0,
                0,
                dp(rightMargin),
                0
        );

        return params;
    }

    private Button actionButton(
            String title,
            int color) {

        Button button =
                new Button(this);

        button.setText(title);
        button.setTextSize(12);
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
                        10
                )
        );

        return button;
    }

    private Button navigationButton(
            String title) {

        Button button =
                new Button(this);

        button.setText(title);
        button.setTextSize(10);
        button.setTextColor(SECONDARY);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(0, 0, 0, 0);
        button.setBackgroundColor(
                Color.TRANSPARENT
        );

        return button;
    }

    private LinearLayout.LayoutParams navigationParams() {

        return new LinearLayout.LayoutParams(
                0,
                -1,
                1
        );
    }

    // ==========================================
    // GPS PERMISSION
    // ==========================================

    private void checkLocationPermission() {

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
                                    grantResults.length > 1 &&
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

    // ==========================================
    // GPS CALLBACK
    // ==========================================

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

    // ==========================================
    // GPS PROCESSING + ROUTE
    // ==========================================

    private void processLocation(
            Location location) {

        if (location == null ||
                !workoutRunning) {
            return;
        }

        if (location.getAccuracy() > 50f) {
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

        if (lastLocation == null) {

            lastLocation =
                    new Location(location);

            saveRoutePoint(location);

            updateScreen();

            return;
        }

        float delta =
                lastLocation.distanceTo(
                        location
                );

        if (delta >= 1f &&
                delta <= 100f) {

            totalDistanceMeters += delta;

            if (intervalRunning) {

                intervalDistanceMeters += delta;
            }

            saveRoutePoint(location);
        }

        lastLocation =
                new Location(location);

        updateScreen();
    }

    private void saveRoutePoint(
            Location location) {

        if (location == null) {
            return;
        }

        String oldPoints =
                routePreferences.getString(
                        ROUTE_POINTS,
                        ""
                );

        String point =
                String.format(
                        Locale.US,
                        "%.7f,%.7f",
                        location.getLatitude(),
                        location.getLongitude()
                );

        String newPoints;

        if (oldPoints.isEmpty()) {

            newPoints = point;

        } else {

            newPoints =
                    oldPoints +
                            ";" +
                            point;
        }

        routePreferences.edit()
                .putString(
                        ROUTE_POINTS,
                        newPoints
                )
                .apply();
    }

    private void clearRoute() {

        routePreferences.edit()
                .remove(ROUTE_POINTS)
                .apply();
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

    // ==========================================
    // WORKOUT
    // ==========================================

    private void startWorkout() {

        if (workoutRunning) {
            return;
        }

        loadPaceWindow();

        workoutRunning = true;

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

        clearRoute();

        intervalsText.setText(
                "Бег начат. Можно начинать интервалы."
        );

        statusValue.setText("БЕГ");
        statusValue.setTextColor(GREEN);

        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        finishButton.setEnabled(true);
        intervalButton.setEnabled(true);

        intervalButton.setText(
                "НАЧАТЬ ИНТЕРВАЛ"
        );

        startLocationUpdates();

        timerHandler.removeCallbacks(
                timerRunnable
        );

        timerHandler.post(
                timerRunnable
        );

        updateScreen();
    }

    private void stopWorkout() {

        if (!workoutRunning) {
            return;
        }

        workoutRunning = false;

        stopLocationUpdates();

        timerHandler.removeCallbacks(
                timerRunnable
        );

        workoutTimeSeconds =
                Math.max(
                        0,
                        (
                                System.currentTimeMillis()
                                        -
                                workoutStartTime
                        ) / 1000
                );

        if (intervalRunning) {

            finishCurrentInterval();
        }

        statusValue.setText("ПАУЗА");
        statusValue.setTextColor(SECONDARY);

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        finishButton.setEnabled(true);
        intervalButton.setEnabled(false);

        updateScreen();
    }

    private void finishWorkout() {

        if (workoutStartTime == 0) {
            return;
        }

        if (workoutRunning) {

            workoutTimeSeconds =
                    Math.max(
                            0,
                            (
                                    System.currentTimeMillis()
                                            -
                                    workoutStartTime
                            ) / 1000
                    );

            workoutRunning = false;

            stopLocationUpdates();

            timerHandler.removeCallbacks(
                    timerRunnable
            );
        }

        // Если пользователь нажал ФИНИШ,
        // пока интервал ещё выполняется,
        // автоматически завершаем его.
        if (intervalRunning) {

            finishCurrentInterval();
        }

        statusValue.setText("ФИНИШ");
        statusValue.setTextColor(ORANGE);

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        finishButton.setEnabled(false);
        intervalButton.setEnabled(false);

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

    // ==========================================
    // INTERVALS
    // ==========================================

    private void toggleInterval() {

        if (!workoutRunning) {
            return;
        }

        if (!intervalRunning) {

            startInterval();

        } else {

            finishCurrentInterval();
        }
    }

    private void startInterval() {

        intervalRunning = true;

        intervalDistanceMeters = 0;

        intervalStartTime =
                System.currentTimeMillis();

        intervalPaceValue.setText("—");

        intervalButton.setText(
                "ЗАВЕРШИТЬ ИНТЕРВАЛ"
        );

        intervalsText.setText(
                "Интервал выполняется..."
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

        // Если GPS не успел дать дистанцию,
        // интервал не записываем как 0.00 км.
        if (distanceKm <= 0) {

            intervalRunning = false;
            intervalDistanceMeters = 0;
            intervalStartTime = 0;

            intervalPaceValue.setText("—");

            intervalButton.setText(
                    "НАЧАТЬ ИНТЕРВАЛ"
            );

            updateIntervalsText();

            return;
        }

        double paceSeconds =
                intervalTime / distanceKm;

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

        intervalRunning = false;
        intervalDistanceMeters = 0;
        intervalStartTime = 0;

        intervalPaceValue.setText("—");

        intervalButton.setText(
                "НАЧАТЬ ИНТЕРВАЛ"
        );

        updateIntervalsText();
    }

    private void updateIntervalsText() {

        if (savedIntervals.isEmpty()) {

            if (intervalRunning) {

                intervalsText.setText(
                        "Интервал выполняется..."
                );

            } else {

                intervalsText.setText(
                        "Интервалы ещё не завершены"
                );
            }

            return;
        }

        StringBuilder builder =
                new StringBuilder();

        for (String interval :
                savedIntervals) {

            builder.append(interval)
                    .append("\n");
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

    // ==========================================
    // SCREEN
    // ==========================================

    private void updateScreen() {

        long elapsed;

        if (workoutRunning) {

            elapsed =
                    Math.max(
                            0,
                            (
                                    System.currentTimeMillis()
                                            -
                                    workoutStartTime
                            ) / 1000
                    );

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

        if (distanceKm > 0 &&
                elapsed > 0) {

            double totalPace =
                    elapsed / distanceKm;

            totalPaceValue.setText(
                    formatPace(totalPace)
            );

        } else {

            totalPaceValue.setText("—");
        }

        double currentPace =
                calculateCurrentPace();

        if (currentPace > 0) {

            currentPaceValue.setText(
                    formatPace(currentPace)
            );

        } else {

            currentPaceValue.setText("—");
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
                    intervalDistanceMeters /
                            1000.0;

            if (intervalKm > 0) {

                double intervalPace =
                        intervalSeconds /
                                intervalKm;

                intervalPaceValue.setText(
                        formatPace(intervalPace)
                );

            } else {

                intervalPaceValue.setText("—");
            }
        }
    }

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

        if (elapsedMillis < 2000) {
            return 0;
        }

        double distanceMeters = 0;

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

                distanceMeters += delta;
            }

            previous =
                    point.location;
        }

        if (distanceMeters < 3) {
            return 0;
        }

        double distanceKm =
                distanceMeters / 1000.0;

        double seconds =
                elapsedMillis / 1000.0;

        return seconds / distanceKm;
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

    // ==========================================
    // RESULT
    // ==========================================

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

    // ==========================================
    // MAP
    // ==========================================

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
                    "Не удалось открыть карту",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ==========================================
    // LOCATION POINT
    // ==========================================

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
