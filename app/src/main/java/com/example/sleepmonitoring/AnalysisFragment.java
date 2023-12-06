package com.example.sleepmonitoring;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalysisFragment extends Fragment {

    private StopwatchViewModel stopwatchViewModel;
    private TextView elapsedTimeTextView;
    private ProgressBar graphProgressBar;
    private TextView scoreTextView;
    private TextView commentTextView;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private BarChart barChart;
    private LineChart lineChart;
    private View rootView;

    private List<Integer> heartRates = new ArrayList<>();
    private List<Integer> scores = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fetchScoreDataFromDatabase();
        fetchHeartRateDataFromDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_mainmenu_analysis, container, false);

        elapsedTimeTextView = rootView.findViewById(R.id.elapsedTimeTextView);

        // ViewModelProvider를 통해 ViewModel을 초기화하고 Observer를 추가합니다.
        stopwatchViewModel = new ViewModelProvider(requireActivity()).get(StopwatchViewModel.class);

        // Observer를 통해 LiveData의 변경을 감지하고 UI를 업데이트합니다.
        stopwatchViewModel.getElapsedTimeMillis().observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long elapsedMillis) {
                updateElapsedTime(elapsedMillis);
            }
        });

        graphProgressBar = rootView.findViewById(R.id.graphProgressBar);
        scoreTextView = rootView.findViewById(R.id.scoreTextView);
        commentTextView = rootView.findViewById(R.id.commentTextView);

        return rootView;
    }

    public void fetchHeartRateDataFromDatabase(){
        databaseReference = database.getReference("meditation").child("heartRate").child(getFormattedCurrentDate());
        Log.d("Firebase", getFormattedCurrentDate());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Integer heartRate = dataSnapshot.getValue(Integer.class);
                        heartRates.add(heartRate);
                        Log.d("Firebase", String.valueOf(heartRate));
                    }

                    lineChart = rootView.findViewById(R.id.lineChart);
                    setupLineChart(lineChart);
                    loadLineChartData(lineChart);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Firebase", error.getMessage());
            }
        });
    }
    private void fetchScoreDataFromDatabase() {
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("meditation").child("score");
        Query recentScoresQuery = databaseReference.orderByKey().limitToLast(7);
        recentScoresQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    String score = snapshot.getValue(String.class);
                    scores.add(Integer.parseInt(score));
                    Log.d("Firebase", String.valueOf(score) + " " + String.valueOf(scores.get(0)));
                }
                updateGraph(scores.get(scores.size()-1));
                updateScoreText(scores.get(scores.size()-1));

                barChart = rootView.findViewById(R.id.barChart);
                setupBarChart(barChart);
                loadBarChartData(barChart);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Firebase", error.getMessage());
            }
        });
    }

    public void setStopwatchViewModel(StopwatchViewModel viewModel) {
        this.stopwatchViewModel = viewModel;

        // ViewModel이 설정되었을 때, 초기값으로 0을 전달하거나, 필요에 따라 다른 값을 전달할 수 있습니다.
        long initialElapsedTime = 0;
        updateElapsedTime(initialElapsedTime);
    }
    public void updateElapsedTime(long elapsedMillis) {
        Log.d("StopwatchViewModel", "updateElapsedTime() 호출됨");
        if (stopwatchViewModel != null && elapsedTimeTextView != null) {
            Log.d("StopwatchViewModel", "Elapsed Time: " + elapsedMillis);
            String formattedElapsedTime = formatElapsedTime(elapsedMillis);
            elapsedTimeTextView.setText(formattedElapsedTime);
        } else {
            Log.e("StopwatchViewModel", "stopwatchViewModel or elapsedTimeTextView is null");
        }
    }


    private void setupLineChart(LineChart lineChart) {
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setDrawGridLines(false);

        xAxis.setAxisMinimum(1f);
        xAxis.setAxisMaximum(5f);
        xAxis.setGranularity(1f);

    }

    private void loadLineChartData(LineChart lineChart) {
        List<Entry> entries = new ArrayList<>();
        for(int i=0; i<heartRates.size(); i++){
            Log.d("Firebase", "heartreate" + String.valueOf(heartRates.get(i)));
            entries.add(new Entry(i+1, heartRates.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "Heart Rate");
        lineDataSet.setColor(Color.RED);
        lineDataSet.setCircleColor(Color.RED);

        LineData lineData = new LineData(lineDataSet);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"0m", "1m", "2m", "3m", "4m", "5m"}));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void setupBarChart(BarChart barChart) {
        // Chart setup code (similar to the previous example)
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setDrawGridLines(false);

    }

    private void loadBarChartData(BarChart barChart) {
        List<BarEntry> entries = new ArrayList<>();
        String[] days = getPreviousDaysOfWeek(7);
        for(int i=0; i<scores.size(); i++){
            entries.add(new BarEntry(i, scores.get(i)));
        }

        BarDataSet barDataSet = new BarDataSet(entries, "Label");
        BarData barData = new BarData(barDataSet);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setDrawGridLines(false);

        barChart.setData(barData);
        barChart.invalidate();
    }

    public String[] getPreviousDaysOfWeek(int numberOfDays){
        String[] days = new String[numberOfDays];
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        for(int i=0; i<numberOfDays; i++){
            String previousDayOfWeek = findDayOfWeek(currentDate);
            days[i] = previousDayOfWeek;

            calendar.add(Calendar.DAY_OF_MONTH, 1);
            currentDate = calendar.getTime();
        }

        return days;
    }

    public String findDayOfWeek(Date date){
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        return dayFormat.format(date);
    }

    private String getFormattedCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }

    private String formatElapsedTime(long millis) {
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
    private void updateGraph(int score) {
        // 점수에 따라 프로그레스바 업데이트
        graphProgressBar.setProgress(score);

        // 도넛 형태의 프로그레스바 설정
        graphProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.donut_progress_bar));

        // 점수 표시
        scoreTextView.setText(String.valueOf(score));
    }
    private void updateScoreText(int score) {
        String scoreText;

        // 특정 점수 범위에 따라 다른 문구 설정
        if (score >= 90) {
            scoreText = "Excellent!\n You Reach a Perfect\nMeditation";
        } else if (score >= 70) {
            scoreText = "Good Job!\nGive a Try To a Perfect\nMeditation Next Time";
        } else if (score == 0 ) {
            scoreText = "Start a Meditation!";
        } else {
            scoreText = "Keep Going!\nYour Efforts Matter,\nand Positive Changes\nare On The Way.\nYou've Got This!";
        }

        commentTextView.setText(scoreText);
    }
}


