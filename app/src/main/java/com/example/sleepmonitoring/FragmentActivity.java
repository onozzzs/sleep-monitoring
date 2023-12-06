package com.example.sleepmonitoring;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FragmentActivity extends AppCompatActivity {

    private StopwatchViewModel stopwatchViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        stopwatchViewModel = new ViewModelProvider(this).get(StopwatchViewModel.class);

        // MainmenuHome 및 MainmenuAnalysis 프래그먼트 생성
        HomeFragment home = new HomeFragment();
        Log.d("YourMainActivity", "Setting ViewModel in MainmenuHome: " + stopwatchViewModel);
        home.setStopwatchViewModel(stopwatchViewModel);

        AnalysisFragment mainmenuAnalysis = new AnalysisFragment();
        Log.d("YourMainActivity", "Setting ViewModel in MainmenuAnalysis: " + stopwatchViewModel);
        mainmenuAnalysis.setStopwatchViewModel(stopwatchViewModel);

        // 프래그먼트를 화면에 추가 (예: transaction 사용)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainmenuHomeFragment, home)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainmenuAnalysisFragment, mainmenuAnalysis)
                .commit();
    }

    // Getter method to access the ViewModel from fragments
    public StopwatchViewModel getStopwatchViewModel() {
        return stopwatchViewModel;
    }
}
