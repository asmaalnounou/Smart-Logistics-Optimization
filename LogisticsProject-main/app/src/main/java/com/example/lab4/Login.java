package com.example.lab4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private EditText et_email, et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("users");

        et_email = findViewById(R.id.email_Login);
        et_password = findViewById(R.id.password_Login);

    }

    public void registerPage(View view) {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
    }

    public void loginMethod(View view) {
        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();

        if (email.isEmpty()) {
            et_email.setError("Email is required");
            et_email.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_email.setError("Please provide a valid Email");
            et_email.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            et_password.setError("Password is required and must be > 6");
            et_password.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check the user's role in the database
                            userDatabase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    // Print the entire snapshot data for debugging
                                    System.out.println("DataSnapshot: " + snapshot.toString());

                                    if (snapshot.exists() && snapshot.hasChild("role")) {
                                        String role = snapshot.child("role").getValue(String.class);
                                        if ("Manager".equals(role)) {
                                            // Redirect to Manager activity
                                            Intent manager = new Intent(Login.this, Manager.class);
                                            startActivity(manager);
                                        } else if ("Driver".equals(role)) {
                                            // Redirect to Driver activity
                                            Intent driver = new Intent(Login.this, Driver.class);
                                            startActivity(driver);
                                        } else {
                                            Toast.makeText(Login.this, "Unknown role type.", Toast.LENGTH_SHORT).show();
                                        }
                                        finish(); // Finish login activity
                                    } else {
                                        Toast.makeText(Login.this, "Role not found in database.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(Login.this, "Failed to retrieve role.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "This account is not registered, please register.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}