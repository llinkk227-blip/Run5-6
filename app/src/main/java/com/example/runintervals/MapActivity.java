package com.example.runintervals;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;

public class MapActivity extends Activity {

    private static final int BG =
            Color.rgb(15, 17, 21);

    private static final int CARD =
            Color.rgb(27, 30, 36);

    private static final int ORANGE =
            Color.rgb(252, 76, 2);

    private static final int WHITE =
            Color.WHITE;

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);

        createInterface();

        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(mapLibreMap -> {

            mapLibreMap.setStyle(
                    new Style.Builder()
                            .fromUri(
                                    "https://tiles.openfreemap.org/styles/liberty"
                            ),
                    style -> {
                        // Карта загружена
                    }
            );
        });
    }

    private void createInterface() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(BG);

        LinearLayout header =
                new LinearLayout(this);

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );

        header.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
        );

        TextView title =
                new TextView(this);

        title.setText("КАРТА");
        title.setTextSize(22);
        title.setTextColor(ORANGE);
        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        header.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        Button backButton =
                new Button(this);

        backButton.setText("Назад");
        backButton.setTextSize(12);
        backButton.setTextColor(WHITE);
        backButton.setAllCaps(false);
        backButton.setBackgroundColor(CARD);

        backButton.setOnClickListener(
                v -> finish()
        );

        header.addView(backButton);

        root.addView(header);

        mapView =
                new MapView(this);

        LinearLayout.LayoutParams mapParams =
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                );

        root.addView(
                mapView,
                mapParams
        );

        setContentView(root);
    }

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
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

    @Override
    protected void onSaveInstanceState(
            Bundle outState) {

        super.onSaveInstanceState(
                outState
        );

        if (mapView != null) {
            mapView.onSaveInstanceState(
                    outState
            );
        }
    }
}
