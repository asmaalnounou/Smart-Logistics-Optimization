package com.example.lab4;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Profile extends AppCompatActivity  {

    private TextView profileName, profileEmail, profileEmployeeID, profilePhoneNumber, profileRole;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileEmployeeID = findViewById(R.id.profileEmployeeID);
        profilePhoneNumber = findViewById(R.id.profilePhoneNumber);
        profileRole = findViewById(R.id.profileRole);

        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("users");

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        loadUserProfile();
        setupBottomNavigation();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            userDatabase.child(userId).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    User userInfo = snapshot.getValue(User.class);
                    if (userInfo != null) {
                        profileName.setText("Name: " + userInfo.getName());
                        profileEmail.setText("Email: " + userInfo.getEmail());
                        profileEmployeeID.setText("Employee ID: " + userInfo.getEmployeeID());
                        profilePhoneNumber.setText("Phone: " + userInfo.getPhoneNumber());
                        profileRole.setText("Role: " + userInfo.getRole());
                    }
                } else {
                    // Handle the case where the user data doesn't exist
                    profileName.setText("Name: Unknown");
                }
            }).addOnFailureListener(e -> {
                // Handle errors when reading the database
                profileName.setText("Failed to load profile");
            });
        }
    }
    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile); // Set profile as selected by default

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // When "Home" is clicked, check the user's role and redirect accordingly
                redirectBasedOnRole();
                return true;
            } else if (itemId == R.id.nav_orders) {
                // Check if the user is a Driver before navigating to ViewOrder activity
                checkIfDriverAndNavigateToOrders();
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true; // Stay on the Profile page if it's selected
            }
            return false;
        });
    }

    private void redirectBasedOnRole() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            userDatabase.child(userId).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    User userInfo = snapshot.getValue(User.class);
                    if (userInfo != null) {
                        String role = userInfo.getRole();
                        // Redirect based on role
                        if ("Manager".equals(role)) {
                            startActivity(new Intent(Profile.this, Manager.class)); // Redirect to Manager page
                            finish();
                        } else if ("Driver".equals(role)) {
                            startActivity(new Intent(Profile.this, Driver.class)); // Redirect to Driver page
                            finish();
                        } else {
                            Toast.makeText(Profile.this, "Role not recognized", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(Profile.this, "Failed to fetch user role", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void checkIfDriverAndNavigateToOrders() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            userDatabase.child(userId).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    User userInfo = snapshot.getValue(User.class);
                    if (userInfo != null) {
                        String role = userInfo.getRole();
                        // If the user is a Driver, show a message and don't allow access to ViewOrder
                        if ("Driver".equals(role)) {
                            Toast.makeText(Profile.this, "You are not allowed to access the Orders page.", Toast.LENGTH_SHORT).show();
                        } else {
                            // If the user is not a Driver, allow access to the ViewOrder page
                            startActivity(new Intent(Profile.this, ViewOrder.class));
                            finish(); // Close the Profile activity so it doesn't stay in the back stack
                        }
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(Profile.this, "Failed to fetch user role", Toast.LENGTH_SHORT).show();
            });
        }
    }

    public void logoutMethod(View view) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        if (bottomNavigationView.getSelectedItemId() == R.id.nav_home) {
            super.onBackPressed();
        } else {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
}
