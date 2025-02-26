package com.example.lab4;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<OrderData> orderList;
    private OnItemClickListener clickListener; // For the driver page (click to view order details)
    private boolean isDriverView; // Flag to distinguish between driver and manager views

    // Constructor
    public OrderAdapter(List<OrderData> orderList, boolean isDriverView) {
        this.orderList = orderList;
        this.isDriverView = isDriverView;
    }

    // Interfaces for click events
    public interface OnItemClickListener {
        void onItemClick(OrderData order);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderData order = orderList.get(position);
        holder.bind(order, clickListener, isDriverView);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textOrderId, textDeliveryDate, textOrderStatus;

        // For the manager's view (additional details)
        private final TextView textCustomerDetails, textAssignedDriver, textCity;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textDeliveryDate = itemView.findViewById(R.id.textDeliveryDate);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);

            // Optional fields for manager view
            textCustomerDetails = itemView.findViewById(R.id.textCustomerDetails);
            textAssignedDriver = itemView.findViewById(R.id.textAssignedDriver);
            textCity = itemView.findViewById(R.id.textCity);
        }

        public void bind(OrderData order, OnItemClickListener clickListener, boolean isDriverView) {
            // Always display these fields
            textOrderId.setText("Order ID: " + (order.getOrderId() != null ? order.getOrderId() : "Unknown"));
            textDeliveryDate.setText("Delivery Date: " + (order.getDeliveryDate() != null ? order.getDeliveryDate() : "Not Set"));

            // Color coding for order status
            String status = order.getStatus() != null ? order.getStatus() : "Unknown";
            switch (status) {
                case "In Progress":
                    textOrderStatus.setBackgroundColor(itemView.getResources().getColor(android.R.color.holo_orange_light));
                    textOrderStatus.setText("In Progress");
                    break;
                case "Completed":
                    textOrderStatus.setBackgroundColor(itemView.getResources().getColor(android.R.color.holo_green_light));
                    textOrderStatus.setText("Completed");
                    break;
                default:
                    textOrderStatus.setBackgroundColor(itemView.getResources().getColor(android.R.color.darker_gray));
                    textOrderStatus.setText("Unknown");
                    break;
            }

            // If this is the driver's view, hide additional fields
            if (isDriverView) {
                textCustomerDetails.setVisibility(View.GONE);
                textAssignedDriver.setVisibility(View.GONE);
                textCity.setVisibility(View.GONE);
            } else {
                // For the manager's view
                textCustomerDetails.setVisibility(View.VISIBLE);
                textCustomerDetails.setText("Customer: " + (order.getCustomerDetails() != null ? order.getCustomerDetails() : "No Customer Info"));

                textAssignedDriver.setVisibility(View.VISIBLE);
                textAssignedDriver.setText("Driver: " + (order.getAssignedDriver() != null ? order.getAssignedDriver() : "Not Assigned"));

                textCity.setVisibility(View.VISIBLE);
                textCity.setText("City: " + (order.getCity() != null ? order.getCity() : "Unknown"));
            }

            // Click listener for the driver's view
            itemView.setOnClickListener(v -> {
                if (clickListener != null && isDriverView) {
                    clickListener.onItemClick(order);
                }
            });
        }
    }
}