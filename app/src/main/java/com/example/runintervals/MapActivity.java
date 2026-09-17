package com.example.runintervals;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.maplibre.android.MapLibre;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;

import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.location.LocationComponentActivationOptions;
import org.maplibre.android.location.LocationComponentOptions;
import org.maplibre.android.location.LocationComponentPlugin;

public class MapActivity extends Activity {

    private static final int BG =
            Color.rgb(15, 17, 21);

    private static final int CARD =
            Color.rgb(27, 30, 36);

    private static final int ORANGE =
            Color.rgb(252, 76, 2);

    private static final int WHITE =
            Color.WHITE;

    private static final int LOCATION_REQUEST = 1001;

    private MapView mapView;

    private FusedLocationProviderClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);

        locationClient =
                LocationServices
                        .getFusedLocationProviderClient(this);

        createInterface();

        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(mapLibreMap -> {

            mapLibreMap.setStyle(
                    new Style.Builder()
                            .fromUri(
                                    "https://tiles.openfreemap.org/styles/liberty"
                            ),
                    style -> {

                        enableLocation();

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

    private void enableLocation() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_REQUEST
            );

            return;
        }

        LocationComponentPlugin locationPlugin =
                mapView
                        .getPlugin(
                                org.maplibre.android.plugins
                                        .locationcomponent
                                        .LocationComponentPluginImpl.class
                        );

        if (locationPlugin == null) {
            return;
        }

        LocationComponentOptions options =
                LocationComponentOptions.builder(this)
                        .pulseEnabled(true)
                        .build();

        LocationComponentActivationOptions activationOptions =
                LocationComponentActivationOptions
                        .builder(this, mapView)
                        .locationComponentOptions(options)
                        .build();

        locationPlugin.activateLocationComponent(
                activationOptions
        );

        locationPlugin.setLocationComponentEnabled(true);

        locationPlugin.setCameraMode(
                org.maplibre.android.location
                        .CameraMode.TRACKING
        );

        locationPlugin.setRenderMode(
                org.maplibre.android.location
                        .RenderMode.COMPASS
        );

        showLastLocation();
    }

    private void showLastLocation() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        locationClient
                .getLastLocation()
                .addOnSuccessListener(
                        location -> {

                            if (location != null) {

                                moveToLocation(location);

                            }
                        }
                );
    }

    private void moveToLocation(
            Location location) {

        mapView.getMapAsync(
                mapLibreMap -> {

                    mapLibreMap.animateCamera(
                            org.maplibre.android.camera.CameraUpdateFactory
                                    .newLatLngZoom(
                                            new LatLng(
                                                    location.getLatitude(),
                                                    location.getLongitude()
                                            ),
                                            15.5
                                    )
                    );
                }
        );
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

        if (requestCode == LOCATION_REQUEST) {

            if (grantResults.length > 0
                    &&
                    grantResults[0]
                            == PackageManager.PERMISSION_GRANTED) {

                enableLocation();

            }
        }
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
