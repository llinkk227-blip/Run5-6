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
        info.add
