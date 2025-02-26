package com.example.lab4;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;

        // Check user role and load appropriate layout
        String userRole = getArguments() != null ? getArguments().getString("role") : "";
        if ("Manager".equals(userRole)) {
            view = inflater.inflate(R.layout.activity_manager, container, false);
        } else {
            view = inflater.inflate(R.layout.activity_driver, container, false);
        }
        return view;
    }
}
