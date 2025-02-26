package com.example.lab4;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Driver extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private List<OrderData> orderList;
    private DatabaseReference userReference, orderReference;
    private String driverId;
    public static String currentOrderId;
    private TextView welcomeMessage;
    private ImageButton logoutButton;
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);



        // Initialize Firebase references
        userReference = FirebaseDatabase.getInstance().getReference("users");
        orderReference = FirebaseDatabase.getInstance().getReference("order");
        welcomeMessage = findViewById(R.id.welcomeMessage); // Match this ID with your XML layout

        // Initialize order list
        orderList = new ArrayList<>();

        // Set up RecyclerView
        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(orderList, true);
        orderAdapter.setOnItemClickListener(this::onOrderSelected);
        recyclerViewOrders.setAdapter(orderAdapter);

        mAuth = FirebaseAuth.getInstance();
        logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logoutMethod());

        // Fetch driver's ID and orders
        fetchDriver();


        // Set item click listener
        orderAdapter.setOnItemClickListener(order -> {
            // Pass the selected orderId to the CurrentOrder activity
            Intent intent = new Intent(Driver.this, CurrentOrder.class);
            intent.putExtra("orderId", order.getOrderId()); // Pass the orderId
            startActivity(intent);
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();
    }

    private void fetchDriver() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        userReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String role = snapshot.child("role").getValue(String.class);
                    String driverName = snapshot.child("name").getValue(String.class);

                    if ("Driver".equals(role) && driverName != null) {
                        driverId = uid; // Use the current user's UID as the driver ID
                        welcomeMessage.setText("Welcome, " + driverName); // Display the driver's name
                        Log.d("Driver", "Driver Name fetched: " + driverName); // Debugging
                        fetchAssignedOrders(); // Fetch orders assigned to the driver
                    } else {
                        Toast.makeText(Driver.this, "You are not authorized to access this page.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(Driver.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Driver.this, "Failed to fetch user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAssignedOrders() {
        if (driverId == null || driverId.isEmpty()) {
            Toast.makeText(this, "Driver ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        userReference.child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String driverName = snapshot.child("name").getValue(String.class);
                    Log.d("Driver", "Driver Name fetched: " + driverName); // Debugging: log driver name

                    orderReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            orderList.clear();
                            for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                                OrderData order = orderSnapshot.getValue(OrderData.class);
                                if (order != null) {
                                    // Debugging: log assigned driver name and order details
                                    Log.d("Driver", "Order fetched: " + order.getOrderId() + " | AssignedDriver: " + order.getAssignedDriver());

                                    // Compare the assignedDriver name with the fetched driver's name
                                    if (driverName != null && driverName.equals(order.getAssignedDriver())) {
                                        orderList.add(order);
                                    }
                                }
                            }

                            // Log the number of orders fetched
                            Log.d("Driver", "Orders fetched: " + orderList.size());

                            // Sort orders by delivery date
                            Collections.sort(orderList, Comparator.comparing(OrderData::getDeliveryDate));
                            orderAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Driver.this, "Failed to fetch orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Driver.this, "Failed to fetch driver data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void onOrderSelected(OrderData selectedOrder) {
        // If there is a current order in progress, show the toast message and don't allow selecting a new one
        if (currentOrderId != null) {
            Toast.makeText(this, "Complete the current order before selecting another.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Otherwise, proceed with selecting the new order
        currentOrderId = selectedOrder.getOrderId(); // Set the new current order
        Intent intent = new Intent(this, CurrentOrder.class);
        intent.putExtra("orderId", currentOrderId);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Default to Home (Driver page)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Navigate to Driver Activity (current activity)
                startActivity(new Intent(Driver.this, Driver.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Navigate to Profile page
                startActivity(new Intent(Driver.this, Profile.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_orders) {
                // Show toast message for not allowed to access orders
                Toast.makeText(Driver.this, "Not allowed to access orders", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkCurrentOrderStatus();
    }

    private void checkCurrentOrderStatus() {
        if (currentOrderId == null) return;

        orderReference.child(currentOrderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("Completed".equals(status)) {
                        currentOrderId = null; // Reset currentOrderId after completion
                        Toast.makeText(Driver.this, "Current order completed. You may select a new order.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Driver.this, "Failed to check order status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void logoutMethod() {
        startActivity(new Intent(this, Login.class));
        finish();
    }
}