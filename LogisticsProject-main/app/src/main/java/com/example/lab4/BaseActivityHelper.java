package com.example.lab4;


import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BaseActivityHelper {

    private FirebaseAuth mAuth;
    private Context context;
    private DatabaseReference userDatabase;
    private BottomNavigationView bottomNavigationView;

    public BaseActivityHelper(Context context, BottomNavigationView bottomNavigationView) {
        this.context = context;
        this.bottomNavigationView = bottomNavigationView;
        this.userDatabase = FirebaseDatabase.getInstance().getReference("users");
    }

    public void setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            userDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.hasChild("role")) {
                        String role = snapshot.child("role").getValue(String.class);
                        if (role != null) {
                            handleNavigation(item.getItemId(), role);
                        }
                    } else {
                        Toast.makeText(context, "User role not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        });
    }

    private void handleNavigation(int itemId, String role) {
        if (itemId == R.id.nav_home) {
            if ("Manager".equals(role)) {
                startNewActivity(Manager.class);
            } else if ("Driver".equals(role)) {
                startNewActivity(Driver.class);
            }
        } else if (itemId == R.id.nav_profile) {
            startNewActivity(Profile.class);
        } else if (itemId == R.id.nav_orders) {
            startNewActivity(ViewOrder.class);
        } else {
            Toast.makeText(context, "Navigation item not recognized", Toast.LENGTH_SHORT).show();
        }
    }

    private void startNewActivity(Class<?> activityClass) {
        Intent intent = new Intent(context, activityClass);
        context.startActivity(intent);
    }
}




