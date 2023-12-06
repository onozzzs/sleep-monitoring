package com.example.sleepmonitoring;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;
import android.annotation.SuppressLint;

public class MenuActivity extends AppCompatActivity {

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private HomeFragment fragmentHome = new HomeFragment();
    private AnalysisFragment fragmentAnalysis = new AnalysisFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.menu_frame_layout, fragmentHome).commitAllowingStateLoss();

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) BottomNavigationView bottomNavigationView = findViewById(R.id.menu_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());
    }

    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            if (menuItem.getItemId() == R.id.home) {
                if (fragmentHome == null) {
                    fragmentHome = new MainmenuHome();
                }
                transaction.replace(R.id.menu_frame_layout, fragmentHome).commitAllowingStateLoss();
            } else if (menuItem.getItemId() == R.id.analysis) {
                if (fragmentAnalysis == null) {
                    fragmentAnalysis = new MainmenuAnalysis();
                }
                transaction.replace(R.id.menu_frame_layout, fragmentAnalysis).commitAllowingStateLoss();
            }

            return true;
        }
    }

}