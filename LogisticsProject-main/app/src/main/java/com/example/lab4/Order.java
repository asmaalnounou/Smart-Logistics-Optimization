package com.example.lab4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Order extends AppCompatActivity {

    // Declare the private fields for the views
    private EditText editTextOrderId, editTextDeliveryDate, editTextCustomerDetails,
            editTextOrderAmount, editTextOrderWeight;
    private Spinner spinnerAssignDriver, spinnerCity;
    private ArrayAdapter<String> driverAdapter;
    private ArrayList<String> driverList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        // Initialize the views
        editTextOrderId = findViewById(R.id.editTextOrderId);
        editTextDeliveryDate = findViewById(R.id.editTextDeliveryDate);
        editTextCustomerDetails = findViewById(R.id.editTextCustomerDetails);
        editTextOrderAmount = findViewById(R.id.editTextOrderAmount);
        editTextOrderWeight = findViewById(R.id.editTextOrderWeight);
        spinnerAssignDriver = findViewById(R.id.AssignDriverSpinner);
        spinnerCity = findViewById(R.id.spinnerCity);
        Button buttonSubmitOrder = findViewById(R.id.buttonSubmitOrder);

        // Initialize Firebase Database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        driverList = new ArrayList<>();
        driverAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, driverList);
        driverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAssignDriver.setAdapter(driverAdapter);

        // Load driver names from Firebase
        databaseReference.orderByChild("role").equalTo("Driver").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                driverList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String driverName = snapshot.child("name").getValue(String.class);
                    if (driverName != null) {
                        driverList.add(driverName);
                    }
                }
                driverAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Order.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });

        // Set onClickListener for the submit button
        buttonSubmitOrder.setOnClickListener(v -> {
            // Fetch user input
            String orderId = editTextOrderId.getText().toString();
            String deliveryDate = editTextDeliveryDate.getText().toString();
            String customerDetails = editTextCustomerDetails.getText().toString();
            String orderAmount = editTextOrderAmount.getText().toString();
            String orderWeight = editTextOrderWeight.getText().toString();
            String assignedDriver = spinnerAssignDriver.getSelectedItem() != null
                    ? spinnerAssignDriver.getSelectedItem().toString() : "";
            String city = spinnerCity.getSelectedItem() != null
                    ? spinnerCity.getSelectedItem().toString() : "";

            // Validate inputs
            if (!validateInputs(orderId, deliveryDate, orderAmount, orderWeight, assignedDriver, city)) {
                return; // Stop further processing if validation fails
            }

            String status = "In Progress";
            List<String> stores = Arrays.asList("1", "2", "3");

            // Create an OrderData object
            OrderData orderData = new OrderData(orderId, deliveryDate, customerDetails,
                    orderAmount, orderWeight,
                    assignedDriver, city, status, stores);

            // Start view order activity
            Intent intent = new Intent(Order.this, ViewOrder.class);
            startActivity(intent);
        });

        // Spinner listeners
        spinnerAssignDriver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isFirstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirstSelection) {
                    isFirstSelection = false;
                    return;
                }

                String selectedCity = parent.getItemAtPosition(position).toString();
                String orderId = editTextOrderId.getText().toString();
                OrderData orderData = new OrderData(
                        orderId,
                        editTextDeliveryDate.getText().toString(),
                        editTextCustomerDetails.getText().toString(),
                        editTextOrderAmount.getText().toString(),
                        editTextOrderWeight.getText().toString(),
                        spinnerAssignDriver.getSelectedItem().toString(),
                        selectedCity,
                        "In Progress",
                        Arrays.asList("1", "2", "3")
                );

                // Save the order to the database before starting the city-specific activity
                saveOrderToDatabase(orderId, orderData);

                Intent intent;
                if (selectedCity.equals("Jeddah")) {
                    intent = new Intent(Order.this, MapsActivityJeddah.class);
                } else if (selectedCity.equals("Makkah")) {
                    intent = new Intent(Order.this, MapsActivityMakkah.class);
                } else {
                    return;
                }

                // Pass the orderId to the respective city activity
                intent.putExtra("orderId", orderId);

                // Start the city-specific activity
                startActivity(intent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void saveOrderToDatabase(String orderId, OrderData orderData) {
        // Get reference to the "order" node in Firebase
        DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("order");

        // Save the order data to Firebase under the orderId
        orderReference.child(orderId).setValue(orderData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Show a success message
                        Toast.makeText(Order.this, "Order saved successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Show a failure message
                        Toast.makeText(Order.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String orderId, String deliveryDate, String orderAmount,
                                   String orderWeight, String assignedDriver, String city) {
        if (!orderId.matches("\\d{6}")) {
            Toast.makeText(this, "Order ID must be 6 digits", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!deliveryDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "Delivery Date must follow YYYY-MM-DD format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!orderAmount.matches("\\d+")) {
            Toast.makeText(this, "Order Amount must be numeric", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!orderWeight.matches("\\d+")) {
            Toast.makeText(this, "Order Weight must be numeric", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (assignedDriver.isEmpty()) {
            Toast.makeText(this, "Please assign a driver", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (city.isEmpty()) {
            Toast.makeText(this, "Please select a city", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void onClick(View view) {
        ImageView returnIcon = findViewById(R.id.returnIcon);
        Intent intent = new Intent(Order.this, Manager.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
