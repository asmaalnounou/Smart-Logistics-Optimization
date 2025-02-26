package com.example.lab4;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    private BaseActivityHelper baseActivityHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);  // Base layout which contains BottomNavigationView

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        baseActivityHelper = new BaseActivityHelper(this, bottomNavigationView);
        baseActivityHelper.setupBottomNavigationView();  // Set up bottom navigation
    }
}
