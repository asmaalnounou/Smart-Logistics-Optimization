package com.example.lab4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;

    private EditText et_fullName, et_email, et_password, et_employeeID, et_phoneNumber;
    private RadioGroup roleGroup;
    private String selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Authentication and Database
        mAuth = FirebaseAuth.getInstance();
        userDatabase = FirebaseDatabase.getInstance().getReference("users");

        // Bind views to variables
        et_fullName = findViewById(R.id.FullName);
        et_email = findViewById(R.id.email);
        et_password = findViewById(R.id.Password);
        et_employeeID = findViewById(R.id.employeeID);
        et_phoneNumber = findViewById(R.id.phoneNumber);
        roleGroup = findViewById(R.id.radioGroup);

        // Set listener to detect selected role
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioManager) {
                selectedRole = "Manager";
            } else if (checkedId == R.id.radioDriver) {
                selectedRole = "Driver";
            }
        });
    }

    // Register button onClick method
    public void registerDB(View view) {
        Log.d("SignUp", "Register button pressed");
        String name = et_fullName.getText().toString().trim();
        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        String employeeID = et_employeeID.getText().toString().trim();
        String phoneNumber = et_phoneNumber.getText().toString().trim();

        // Validate inputs before registration
        if (!validateInputs(name, email, password, employeeID, phoneNumber)) return;

        if (selectedRole == null) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        User user = new User(name, email, employeeID, phoneNumber, selectedRole);
                        saveUserToDatabase(uid, user);
                    } else {
                        showError(task.getException(), "Registration failed");
                    }
                });
    }

    // Validate user inputs
    private boolean validateInputs(String name, String email, String password,
                                   String employeeID, String phoneNumber) {
        if (name.isEmpty()) {
            et_fullName.setError("Full Name is required");
            et_fullName.requestFocus();
            return false;
        }
        // Validate Employee ID
        if (employeeID.isEmpty() || !isValidEmployeeID(employeeID)) {
            et_employeeID.setError("Employee ID must be 7 digits long and numeric");
            et_employeeID.requestFocus();
            return false;
        }
        // Validate Phone Number
        if (phoneNumber.isEmpty() || !isValidPhoneNumber(phoneNumber)) {
            et_phoneNumber.setError("Phone number must be 10 digits long and numeric");
            et_phoneNumber.requestFocus();
            return false;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_email.setError("Please provide a valid email");
            et_email.requestFocus();
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            et_password.setError("Password must be at least 6 characters");
            et_password.requestFocus();
            return false;
        }
        return true;
    }

    // Check if Employee ID is valid (6 digits)
    private boolean isValidEmployeeID(String employeeID) {
        return employeeID.matches("\\d{7}"); // Matches exactly 6 digits
    }

    // Check if Phone Number is valid (10 digits)
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10}"); // Matches exactly 10 digits
    }

    // Save user data to Firebase Realtime Database
    // Save user data to Firebase Realtime Database
    private void saveUserToDatabase(String uid, User user) {
        userDatabase.child(uid).setValue(user) // Use UID as the key
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "User registered successfully!", Toast.LENGTH_LONG).show();
                        // TODO: Navigate to the main or login screen
                    } else {
                        showError(task.getException(), "Failed to save user data");
                    }
                });
    }

    // Show error message
    private void showError(Exception e, String defaultMsg) {
        String errorMessage = e != null ? e.getMessage() : defaultMsg;
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }


        public void onClick (View view)
        {
            ImageView returnIcon = findViewById(R.id.returnIcon);
            Intent intent = new Intent(SignUp.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);


        }
    }