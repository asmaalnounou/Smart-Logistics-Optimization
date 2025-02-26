package com.example.lab4;

import java.util.List;

public class OrderData {
    private String orderId;
    private String deliveryDate;
    private String customerDetails;
    private String orderAmount;
    private String orderWeight;
    private String assignedDriver;
    private String city;
    private String status;
    private List<String> stores;  // Updated to List<String>

    // Empty constructor for Firebase
    public OrderData() {}

    // Parameterized constructor
    public OrderData(String orderId, String deliveryDate, String customerDetails,
                     String orderAmount, String orderWeight,
                     String assignedDriver, String city,String status, List<String> stores) {
        this.orderId = orderId;
        this.deliveryDate = deliveryDate;
        this.customerDetails = customerDetails;
        this.orderAmount = orderAmount;
        this.orderWeight = orderWeight;
        this.assignedDriver = assignedDriver;
        this.city = city;
        this.status = status;
        this.stores = stores;
    }



    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getCustomerDetails() {
        return customerDetails;
    }

    public void setCustomerDetails(String customerDetails) {
        this.customerDetails = customerDetails;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getOrderWeight() {
        return orderWeight;
    }

    public void setOrderWeight(String orderWeight) {
        this.orderWeight = orderWeight;
    }

    public String getAssignedDriver() {
        return assignedDriver;
    }

    public void setAssignedDriver(String assignedDriver) {
        this.assignedDriver = assignedDriver;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
    public String getStatus() {
        return status; // NEW GETTER
    }

    public void setStatus(String status) {
        this.status = status; // NEW SETTER
    }

    // Add getter and setter for stores
    public List<String> getStores() {
        return stores;
    }

    public void setStores(List<String> stores) {
        this.stores = stores;
    }
}