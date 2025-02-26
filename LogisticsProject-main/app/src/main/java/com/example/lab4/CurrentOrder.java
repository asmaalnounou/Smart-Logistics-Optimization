package com.example.lab4;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton; // Add this import
import androidx.appcompat.app.AlertDialog; // Add this import
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CurrentOrder extends AppCompatActivity implements OnMapReadyCallback
{
    private TextView orderDetailsTextView;
    private GoogleMap googleMap;
    private String orderCity;
    private boolean isMapReady = false;
    private boolean isCityLoaded = false;
    private ToggleButton toggleStatus; // Declare the ToggleButton

    // Coordinates for Al Faisaliah (starting point)
    private static final LatLng AL_FAISALIAH = new LatLng(21.5798559, 39.1806253);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_order);

        // Initialize views
        orderDetailsTextView = findViewById(R.id.tv_order_details);
        toggleStatus = findViewById(R.id.toggle_status); // Initialize ToggleButton

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Current Order");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Firebase reference
        String currentOrderId = getIntent().getStringExtra("orderId");
        if (currentOrderId == null) {
            Toast.makeText(this, "No order ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("order").child(currentOrderId);
        loadOrderDetails(orderReference);

        // Setup Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Handle toggle button changes
        toggleStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Completion")
                        .setMessage("Are you sure you want to mark this order as completed?")
                        .setPositiveButton("Yes", (dialog, which) -> updateOrderStatus("Completed"))
                        .setNegativeButton("No", (dialog, which) -> toggleStatus.setChecked(false))
                        .show();
            } else {
                updateOrderStatus("In Progress");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        onBackPressed();
        return true;
    }

    private void loadOrderDetails(DatabaseReference orderReference)
    {
        orderReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    OrderData order = snapshot.getValue(OrderData.class);
                    if (order != null) {
                        orderCity = order.getCity();
                        List<String> stores = order.getStores();
                        isCityLoaded = true;

                        // Display order details in the TextView
                        displayOrderDetails(order);

                        // Load store locations
                        loadStoreLocations(orderCity, stores);
                    }
                } else {
                    Toast.makeText(CurrentOrder.this, "Order not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CurrentOrder.this, "Failed to load order details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOrderDetails(OrderData order)
    {
        // Display order details in the TextView
        String orderDetails = "Order ID: " + order.getOrderId() +
                "\nDelivery Date: " + order.getDeliveryDate() +
                "\nCustomer: " + order.getCustomerDetails() +
                "\nAmount: " + order.getOrderAmount() +
                "\nWeight: " + order.getOrderWeight() +
                "\nCity: " + order.getCity() +
                "\nStores: " + order.getStores() +
                "\nStatus: " + order.getStatus();

        orderDetailsTextView.setText(orderDetails);
    }

    private void loadStoreLocations(String city, List<String> stores)
    {
        if (city == null || stores == null || stores.isEmpty()) {
            Toast.makeText(this, "City or stores information is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference locationsReference = FirebaseDatabase.getInstance().getReference("locations").child(city);

        locationsReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Store> storeLocations = new ArrayList<>();

                    // Loop through stores to get their locations
                    for (String store : stores) {
                        String storeKey = store.toLowerCase();
                        DataSnapshot storeSnapshot = snapshot.child(storeKey);
                        if (storeSnapshot.exists()) {
                            Double latitude = storeSnapshot.child("latitude").getValue(Double.class);
                            Double longitude = storeSnapshot.child("longitude").getValue(Double.class);

                            if (latitude != null && longitude != null) {
                                LatLng storeLocation = new LatLng(latitude, longitude);
                                double distance = calculateDistance(AL_FAISALIAH, storeLocation);
                                storeLocations.add(new Store(store, storeLocation, distance));
                            }
                        }
                    }

                    // Sort stores by distance
                    storeLocations.sort((s1, s2) -> Double.compare(s1.distance, s2.distance));

                    // Now, draw the path from Al Faisaliah to each store
                    drawPath(storeLocations);
                }
                else
                {
                    Toast.makeText(CurrentOrder.this, "City not found in locations database: " + city, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(CurrentOrder.this, "Failed to load store locations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawPath(List<Store> storeLocations)
    {
        if (googleMap != null) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .width(10)
                    .color(0xFF0000FF); // Blue color for the line

            // Start the polyline from Al Faisaliah
            polylineOptions.add(AL_FAISALIAH);

            // Add store locations in order of proximity
            for (Store store : storeLocations) {
                googleMap.addMarker(new MarkerOptions()
                        .position(store.location)
                        .title(store.name));
                polylineOptions.add(store.location);
            }

            googleMap.addPolyline(polylineOptions);

            // Move camera to Al Faisaliah
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(AL_FAISALIAH, 12));
        }
    }

    private double calculateDistance(LatLng start, LatLng end) {
        // Haversine formula to calculate the distance between two points
        double lat1 = start.latitude;
        double lon1 = start.longitude;
        double lat2 = end.latitude;
        double lon2 = end.longitude;

        final int R = 6371; // Earth radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Distance in kilometers
        return distance;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        isMapReady = true;


        googleMap.addMarker(new MarkerOptions()
                .position(AL_FAISALIAH)
                .title("Start Here"));

        // Center map on Al Faisaliah if city is loaded
        if (isMapReady && isCityLoaded) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(AL_FAISALIAH, 10));
        }
    }

    // Method to update order status in the database
    private void updateOrderStatus(String status) {
        String currentOrderId = getIntent().getStringExtra("orderId");
        DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("order").child(currentOrderId);
        orderReference.child("status").setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CurrentOrder.this, "Order status updated to: " + status, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CurrentOrder.this, "Failed to update order status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Store class to represent store data with its location and distance
    private static class Store {
        String name;
        LatLng location;
        double distance;

        Store(String name, LatLng location, double distance) {
            this.name = name;
            this.location = location;
            this.distance = distance;
        }
    }
}
