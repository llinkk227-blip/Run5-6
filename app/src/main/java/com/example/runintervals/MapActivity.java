package com.example.runintervals;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
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

    private static final int LOCATION_REQUEST = 1001;

    private MapView mapView;

    private LocationManager locationManager;

    private boolean mapReady = false;
    private boolean cameraMoved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        MapLibre.getInstance(this);

        locationManager =
                (LocationManager)
                        getSystemService(
                                LOCATION_SERVICE
                        );

        createInterface();

        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(mapLibreMap -> {

            mapLibreMap.setStyle(
                    new Style.Builder()
                            .fromUri(
                                    "https://tiles.openfreemap.org/styles/liberty"
                            ),
                    style -> {

                        mapReady = true;

                        startGps();

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

    private void startGps() {

        if (!mapReady) {
            return;
        }

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
                    LOCATION_REQUEST
            );

            return;
        }

        Location lastLocation = null;

        try {

            lastLocation =
                    locationManager.getLastKnownLocation(
                            LocationManager.GPS_PROVIDER
                    );

        } catch (Exception ignored) {
        }

        if (lastLocation != null) {

            moveToLocation(
                    lastLocation
            );
        }

        try {

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    1,
                    locationListener
            );

        } catch (Exception ignored) {
        }

        try {

            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000,
                    2,
                    locationListener
            );

        } catch (Exception ignored) {
        }
    }

    private final LocationListener locationListener =
            new LocationListener() {

                @Override
                public void onLocationChanged(
                        Location location) {

                    if (location == null) {
                        return;
                    }

                    if (location.hasAccuracy()
                            && location.getAccuracy() > 50) {

                        return;
                    }

                    moveToLocation(location);
                }

                @Override
                public void onProviderEnabled(
                        String provider) {
                }

                @Override
                public void onProviderDisabled(
                        String provider) {
                }
            };

    private void moveToLocation(
            Location location) {

        if (!mapReady || mapView == null) {
            return;
        }

        mapView.getMapAsync(
                mapLibreMap -> {

                    double latitude =
                            location.getLatitude();

                    double longitude =
                            location.getLongitude();

                    float zoom =
                            cameraMoved
                                    ? 16.0f
                                    : 16.5f;

                    mapLibreMap.animateCamera(
                            CameraUpdateFactory
                                    .newLatLngZoom(
                                            new LatLng(
                                                    latitude,
                                                    longitude
                                            ),
                                            zoom
                                    )
                    );

                    cameraMoved = true;
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

            boolean granted = false;

            for (int result : grantResults) {

                if (result ==
                        PackageManager.PERMISSION_GRANTED) {

                    granted = true;
                    break;
                }
            }

            if (granted) {

                startGps();
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

        if (mapReady) {
            startGps();
        }
    }

    @Override
    protected void onPause() {

        stopGps();

        if (mapView != null) {
            mapView.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {

        stopGps();

        if (mapView != null) {
            mapView.onStop();
        }

        super.onStop();
    }

    private void stopGps() {

        if (locationManager != null) {

            try {

                locationManager.removeUpdates(
                        locationListener
                );

            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onDestroy() {

        stopGps();

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
