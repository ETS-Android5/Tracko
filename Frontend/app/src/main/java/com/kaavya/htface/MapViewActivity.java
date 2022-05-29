package com.kaavya.htface;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String name = null;
    private double lat = 0;
    private double lon = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        Bundle bundle = this.getIntent().getExtras();

        if (bundle != null) {
            if (bundle.containsKey("NAME")) {
                name = bundle.getString("NAME");
            }
            if (bundle.containsKey("LAT")) {
                lat = bundle.getDouble("LAT");
            }
            if (bundle.containsKey("LON")) {
                lon = bundle.getDouble("LON");
            }
        }

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng loc = new LatLng(lat, lon);
        googleMap.addMarker(new MarkerOptions()
                .position(loc)
                .title(name));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc,14));
    }
}