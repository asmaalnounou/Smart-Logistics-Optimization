package com.example.lab4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivityMakkah extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private DatabaseReference orderReference;

    // Store markers to control adding and removing
    private final Map<String, Marker> storeMarkers = new HashMap<>();
    private final Map<String, LatLng> storeLocations = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps); // Ensure this matches your XML layout

        // Get the orderId passed from Order
        String currentOrderId = getIntent().getStringExtra("orderId");
        Log.d("OrderID", "Received Order ID: " + currentOrderId);
        if (currentOrderId == null) {
            Toast.makeText(this, "No order ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Update Firebase reference to match Driver activity
        orderReference = FirebaseDatabase.getInstance().getReference("order").child(currentOrderId);

        // Initialize the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Request location permissions if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Fetch store locations from Firebase
        fetchLocationsFromFirebase();

        // Set up CheckBox listeners
        setupCheckBoxListeners();

        // Check and request location permissions at startup
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable location features if permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Center the map on Makkah
        LatLng makkah = new LatLng(21.3891, 39.8579);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(makkah, 12));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable location features
                if (mMap != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                // Permission denied, show a warning
                Toast.makeText(this, "Location permissions are required to use this feature.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Set up listeners for each CheckBox
    private void setupCheckBoxListeners() {
        CheckBox store1 = findViewById(R.id.store1);
        CheckBox store2 = findViewById(R.id.store2);
        CheckBox store3 = findViewById(R.id.store3);

        store1.setOnCheckedChangeListener((buttonView, isChecked) -> handleMarker(isChecked, "danube"));
        store2.setOnCheckedChangeListener((buttonView, isChecked) -> handleMarker(isChecked, "panda"));
        store3.setOnCheckedChangeListener((buttonView, isChecked) -> handleMarker(isChecked, "othaim"));
    }

    // Add or remove marker based on CheckBox state
    private void handleMarker(boolean shouldShow, String storeName) {
        LatLng location = storeLocations.get(storeName);

        if (location == null) {
            Toast.makeText(this, storeName + " location not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (shouldShow) {
            // Add marker if checked
            Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(storeName));
            storeMarkers.put(storeName, marker);
        } else {
            // Remove marker if unchecked
            Marker marker = storeMarkers.get(storeName);
            if (marker != null) {
                marker.remove();
                storeMarkers.remove(storeName);
            }
        }
    }

    private void updateOrderStores() {
        // Ensure orderReference is not null
        if (orderReference == null) {
            Log.e("MapsActivityMakkah", "Order reference is null");
            Toast.makeText(this, "Error: Cannot update stores", Toast.LENGTH_SHORT).show();
            return;
        }
        // Prepare a list to store selected stores with consistent capitalization
        List<String> selectedStores = new ArrayList<>();
        // Check the status of each CheckBox and add the corresponding store name to the list
        if (((CheckBox) findViewById(R.id.store1)).isChecked()) {
            selectedStores.add("Danube");
        }
        if (((CheckBox) findViewById(R.id.store2)).isChecked()) {
            selectedStores.add("Panda");
        }
        if (((CheckBox) findViewById(R.id.store3)).isChecked()) {
            selectedStores.add("Othaim");
        }
        // Prepare a map for the updates using updateChildren
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("stores", selectedStores);
        // Update the "stores" child node in Firebase using updateChildren
        orderReference.updateChildren(updateMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("MapsActivityJeddah", "Stores updated successfully: " + selectedStores);
                    Toast.makeText(MapsActivityMakkah.this, "Stores updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("MapsActivityJeddah", "Failed to update stores", e);
                    Toast.makeText(MapsActivityMakkah.this, "Failed to update stores: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method triggered when the "Done" button is clicked
    public void onDoneButtonClick(View view) {
        updateOrderStores(); // Save the selected stores to Firebase
        StringBuilder selectedStores = new StringBuilder();

        if (((CheckBox) findViewById(R.id.store1)).isChecked()) selectedStores.append("danube\n");
        if (((CheckBox) findViewById(R.id.store2)).isChecked()) selectedStores.append("panda\n");
        if (((CheckBox) findViewById(R.id.store3)).isChecked()) selectedStores.append("othaim\n");

        if (selectedStores.length() == 0) {
            Toast.makeText(this, "No stores selected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, selectedStores.toString(), Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.putExtra("selectedStores", selectedStores.toString().trim());
            setResult(RESULT_OK, intent);  // Return result to Order activity
            finish();
        }
    }

    private void fetchLocationsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("locations/Makkah");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear previous store locations
                storeLocations.clear();

                for (DataSnapshot storeSnapshot : snapshot.getChildren()) {
                    String storeName = storeSnapshot.getKey();
                    Double latitude = storeSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = storeSnapshot.child("longitude").getValue(Double.class);

                    if (storeName != null && latitude != null && longitude != null) {
                        storeName = storeName.toLowerCase(); // Normalize key
                        storeLocations.put(storeName, new LatLng(latitude, longitude));
                    }
                }

                // If map is ready, add markers for pre-selected stores
                if (mMap != null) {
                    CheckBox store1 = findViewById(R.id.store1);
                    CheckBox store2 = findViewById(R.id.store2);
                    CheckBox store3 = findViewById(R.id.store3);

                    if (store1.isChecked()) handleMarker(true, "danube");
                    if (store2.isChecked()) handleMarker(true, "panda");
                    if (store3.isChecked()) handleMarker(true, "othaim");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseData", "Failed to load locations: " + error.getMessage());
                Toast.makeText(MapsActivityMakkah.this,
                        "Failed to load store locations: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClick (View view)
    {
        ImageView returnIcon = findViewById(R.id.returnIcon);
        Intent intent = new Intent(this,Order.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}