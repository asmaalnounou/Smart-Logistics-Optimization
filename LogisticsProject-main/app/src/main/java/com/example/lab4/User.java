package com.example.lab4;

public class User {
    private String name;
    private String email;
    private String employeeID;
    private String phoneNumber;
    private String role;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String email, String employeeID, String phoneNumber, String role) {
        this.name = name;
        this.email = email;
        this.employeeID = employeeID;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRole() {
        return role;
    }
}
