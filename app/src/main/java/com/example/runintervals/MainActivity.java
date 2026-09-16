package com.example.runintervals;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView text = new TextView(this);
        text.setText("RunIntervals");
        text.setTextSize(24);

        setContentView(text);
    }
}    private LocationCallback locationCallback;

    private boolean running = false;
    private boolean paused = false;

    private long startTime;
    private long pausedTime;
    private long pauseStarted;

    private float distance;
    private android.location.Location lastLocation;

    private double weight = 60;

    private final ArrayList<GeoPoint> points = new ArrayList<>();

    private SharedPreferences prefs;

    private final Handler handler = new Handler();

    private final Runnable timer = new Runnable() {
        @Override
        public void run() {
            if (running) {
                updateMetrics();
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance()
                .setUserAgentValue(getPackageName());

        prefs = getSharedPreferences("runs", MODE_PRIVATE);

        weight = Double.longBitsToDouble(
                prefs.getLong(
                        "weight",
                        Double.doubleToLongBits(60)
                )
        );

        locationClient =
                LocationServices.getFusedLocationProviderClient(this);

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
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(247, 248, 250));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(10, 5, 10, 5);

        Button menu = new Button(this);
        menu.setText("☰");
        menu.setTextSize(20);

        menu.setOnClickListener(v -> showMenu());

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(22);
        titleView.setTypeface(null, 1);
        titleView.setTextColor(Color.rgb(28, 35, 40));

        header.addView(menu, new LinearLayout.LayoutParams(60, 60));
        header.addView(titleView);

        root.addView(header);

        setContentView(root);
    }

    private void showMenu() {

        PopupWindow popup =
                new PopupWindow(this, 650, 700, true);

        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setPadding(30, 30, 30, 30);
        menu.setBackgroundColor(Color.WHITE);

        TextView title = menuItem("RUNINTERVALS", 22);
        TextView run = menuItem("🏃  Пробежка", 18);
        TextView history = menuItem("📋  История", 18);
        TextView statistics = menuItem("📊  Статистика", 18);
        TextView settings = menuItem("⚙  Настройки", 18);

        menu.addView(title);
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
           
