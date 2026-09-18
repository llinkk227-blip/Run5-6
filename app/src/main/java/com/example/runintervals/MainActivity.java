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
import android.view.WindowManager;
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

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private SharedPreferences settings;
    private SharedPreferences routePreferences;

    private static final String ROUTE_PREFS = "routes";

    private boolean workoutRunning = false;
    private boolean intervalRunning = false;

    private long workoutStartTime = 0;
    private long workoutElapsedTime = 0;

    private long intervalStartTime = 0;
    private long intervalElapsedTime = 0;

    private double totalDistance = 0;
    private double intervalDistance = 0;

    private Location lastLocation;

    private final ArrayList<String> savedIntervals =
            new ArrayList<>();

    private Handler timerHandler =
            new Handler(Looper.getMainLooper());

    private Runnable timerRunnable;

    private TextView timerText;
    private TextView totalDistanceText;
    private TextView intervalDistanceText;
    private TextView currentPaceText;
    private TextView totalPaceText;
    private TextView intervalPaceText;
    private TextView statusText;
    private TextView intervalsText;

    private Button startStopButton;
    private Button finishButton;
    private Button intervalButton;

    private long paceWindow = 10;

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Не выключать экран во время работы приложения
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

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

    private void loadPaceWindow() {

        paceWindow =
                settings.getLong(
                        "pace_window",
                        10
                );
    }

    private void createInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                24,
                24,
                24,
                24
        );

        root.setGravity(
                Gravity.CENTER_HORIZONTAL
        );

        root.setBackgroundColor(
                Color.WHITE
        );

        statusText =
                createTextView(
                        "ГОТОВ",
                        20,
                        true
                );

        root.addView(statusText);

        timerText =
                createTextView(
                        "00:00",
                        48,
                        true
                );

        root.addView(timerText);

        totalDistanceText =
                createTextView(
                        "Дистанция: 0.00 км",
                        20,
                        false
                );

        root.addView(totalDistanceText);

        intervalDistanceText =
                createTextView(
                        "Интервал: 0.00 км",
                        20,
                        false
                );

        root.addView(intervalDistanceText);

        currentPaceText =
                createTextView(
                        "Текущий темп: --:--",
                        20,
                        false
                );

        root.addView(currentPaceText);

        totalPaceText =
                createTextView(
                        "Средний темп: --:--",
                        20,
                        false
                );

        root.addView(totalPaceText);

        intervalPaceText =
                createTextView(
                        "Темп интервала: --:--",
                        20,
                        false
                );

        root.addView(intervalPaceText);

        startStopButton =
                createButton(
                        "СТАРТ"
                );

        root.addView(startStopButton);

        intervalButton =
                createButton(
                        "НАЧАТЬ ИНТЕРВАЛ"
                );

        root.addView(intervalButton);

        finishButton =
                createButton(
                        "ФИНИШ"
                );

        root.addView(finishButton);

        TextView intervalsTitle =
                createTextView(
                        "ИНТЕРВАЛЫ",
                        22,
                        true
                );

        root.addView(intervalsTitle);

        intervalsText =
                createTextView(
                        "",
                        18,
                        false
                );

        root.addView(intervalsText);

        setContentView(root);

        startStopButton.setOnClickListener(
                v -> {

                    if (workoutRunning) {
                        stopWorkout();
                    } else {
                        startWorkout();
                    }
                }
        );

        intervalButton.setOnClickListener(
                v -> toggleInterval()
        );

        finishButton.setOnClickListener(
                v -> finishWorkout()
        );
    }

    private TextView createTextView(
            String text,
            int size,
            boolean bold) {

        TextView view =
                new TextView(this);

        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(Color.BLACK);
        view.setGravity(Gravity.CENTER);
        view.setPadding(
                8,
                8,
                8,
                8
        );

        if (bold) {
            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return view;
    }

    private Button createButton(
            String text) {

        Button button =
                new Button(this);

        button.setText(text);
        button.setTextSize(18);
        button.setAllCaps(false);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                8,
                0,
                8
        );

        button.setLayoutParams(params);

        return button;
    }

    private void startWorkout() {

        workoutRunning = true;

        workoutStartTime =
                System.currentTimeMillis();

        workoutElapsedTime = 0;

        totalDistance = 0;
        intervalDistance = 0;

        lastLocation = null;

        savedIntervals.clear();

        statusText.setText("ТРЕНИРОВКА");

        startStopButton.setText(
                "СТОП"
        );

        startLocationUpdates();
        startTimer();

        updateInterface();
    }

    private void stopWorkout() {

        if (!workoutRunning) {
            return;
        }

        workoutElapsedTime =
                System.currentTimeMillis()
                        - workoutStartTime;

        workoutRunning = false;

        stopLocationUpdates();
        stopTimer();

        if (intervalRunning) {
            finishCurrentInterval();
        }

        statusText.setText("ПАУЗА");

        startStopButton.setText(
                "СТАРТ"
        );

        updateInterface();
    }

    private void finishWorkout() {

        if (!workoutRunning) {
            return;
        }

        workoutElapsedTime =
                System.currentTimeMillis()
                        - workoutStartTime;

        workoutRunning = false;

        stopLocationUpdates();
        stopTimer();

        if (intervalRunning) {
            finishCurrentInterval();
        }

        statusText.setText("ЗАВЕРШЕНО");

        startStopButton.setText(
                "СТАРТ"
        );

        saveWorkout();

        updateInterface();
    }

    private void toggleInterval() {

        if (!workoutRunning) {
            Toast.makeText(
                    this,
                    "Сначала нажмите СТАРТ",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (intervalRunning) {
            finishCurrentInterval();
        } else {
            startInterval();
        }
    }

    private void startInterval() {

        intervalRunning = true;

        intervalStartTime =
                System.currentTimeMillis();

        intervalElapsedTime = 0;

        intervalDistance = 0;

        intervalButton.setText(
                "ЗАВЕРШИТЬ ИНТЕРВАЛ"
        );

        statusText.setText(
                "ИНТЕРВАЛ"
        );

        updateInterface();
    }

    private void finishCurrentInterval() {

        if (!intervalRunning) {
            return;
        }

        intervalElapsedTime =
                System.currentTimeMillis()
                        - intervalStartTime;

        double pace =
                calculatePace(
                        intervalDistance,
                        intervalElapsedTime
                );

        String interval =
                String.format(
                        Locale.getDefault(),
                        "%d. %s км • %s",
                        savedIntervals.size() + 1,
                        formatDistance(
                                intervalDistance
                        ),
                        formatPace(pace)
                );

        savedIntervals.add(
                interval
        );

        intervalRunning = false;

        intervalDistance = 0;
        intervalElapsedTime = 0;

        intervalButton.setText(
                "НАЧАТЬ ИНТЕРВАЛ"
        );

        statusText.setText(
                "ТРЕНИРОВКА"
        );

        updateIntervals();

        updateInterface();
    }

    private void startTimer() {

        timerRunnable =
                new Runnable() {

                    @Override
                    public void run() {

                        if (workoutRunning) {

                            workoutElapsedTime =
                                    System.currentTimeMillis()
                                            - workoutStartTime;

                            if (intervalRunning) {

                                intervalElapsedTime =
                                        System.currentTimeMillis()
                                                - intervalStartTime;
                            }

                            updateInterface();

                            timerHandler.postDelayed(
                                    this,
                                    1000
                            );
                        }
                    }
                };

        timerHandler.post(
                timerRunnable
        );
    }

    private void stopTimer() {

        if (timerRunnable != null) {

            timerHandler.removeCallbacks(
                    timerRunnable
            );
        }
    }

    private void createLocationCallback() {

        locationCallback =
                new LocationCallback() {

                    @Override
                    public void onLocationResult(
                            LocationResult result) {

                        for (Location location :
                                result.getLocations()) {

                            processLocation(
                                    location
                            );
                        }
                    }
                };
    }

    private void processLocation(
            Location location) {

        if (!workoutRunning) {
            return;
        }

        if (lastLocation != null) {

            float distance =
                    lastLocation.distanceTo(
                            location
                    );

            if (distance > 0 &&
                    distance < 100) {

                totalDistance +=
                        distance / 1000.0;

                if (intervalRunning) {

                    intervalDistance +=
                            distance / 1000.0;
                }
            }
        }

        lastLocation = location;

        updateInterface();
    }

    private void startLocationUpdates() {

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        LocationRequest request =
                new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        1000
                )
                        .setMinUpdateIntervalMillis(
                                500
                        )
                        .setMinUpdateDistanceMeters(
                                1
                        )
                        .build();

        fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    private void stopLocationUpdates() {

        fusedLocationClient.removeLocationUpdates(
                locationCallback
        );
    }

    private void updateInterface() {

        timerText.setText(
                formatTime(
                        workoutElapsedTime
                )
        );

        totalDistanceText.setText(
                "Дистанция: " +
                        formatDistance(
                                totalDistance
                        ) +
                        " км"
        );

        intervalDistanceText.setText(
                "Интервал: " +
                        formatDistance(
                                intervalDistance
                        ) +
                        " км"
        );

        double totalPace =
                calculatePace(
                        totalDistance,
                        workoutElapsedTime
                );

        double intervalPace =
                calculatePace(
                        intervalDistance,
                        intervalElapsedTime
                );

        totalPaceText.setText(
                "Средний темп: " +
                        formatPace(
                                totalPace
                        )
        );

        intervalPaceText.setText(
                "Темп интервала: " +
                        formatPace(
                                intervalPace
                        )
        );

        currentPaceText.setText(
                "Текущий темп: " +
                        formatPace(
                                totalPace
                        )
        );
    }

    private void updateIntervals() {

        StringBuilder builder =
                new StringBuilder();

        for (String interval :
                savedIntervals) {

            builder.append(interval)
                    .append("\n");
        }

        intervalsText.setText(
                builder.toString()
        );
    }

    private String formatTime(
            long milliseconds) {

        long seconds =
                milliseconds / 1000;

        long minutes =
                seconds / 60;

        seconds =
                seconds % 60;

        return String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
        );
    }

    private String formatDistance(
            double distance) {

        return String.format(
                Locale.getDefault(),
                "%.2f",
                distance
        );
    }

    private double calculatePace(
            double distance,
            long time) {

        if (distance <= 0 ||
                time <= 0) {

            return 0;
        }

        double minutes =
                time / 60000.0;

        return minutes / distance;
    }

    private String formatPace(
            double pace) {

        if (pace <= 0) {
            return "--:--";
        }

        int totalSeconds =
                (int) Math.round(
                        pace * 60
                );

        int minutes =
                totalSeconds / 60;

        int seconds =
                totalSeconds % 60;

        return String.format(
                Locale.getDefault(),
                "%d:%02d",
                minutes,
                seconds
        );
    }

    private void saveWorkout() {

        String key =
                "route_" +
                        System.currentTimeMillis();

        StringBuilder builder =
                new StringBuilder();

        builder.append(
                totalDistance
        );

        routePreferences
                .edit()
                .putString(
                        key,
                        builder.toString()
                )
                .apply();
    }

    private void checkLocationPermission() {

        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    100
            );
        }
    }

    @Override
    protected void onDestroy() {

        stopTimer();
        stopLocationUpdates();

        super.onDestroy();
    }
    }
