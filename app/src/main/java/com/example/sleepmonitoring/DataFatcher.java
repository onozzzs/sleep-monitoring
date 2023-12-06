package com.example.sleepmonitoring;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataFatcher {

    static public List<Integer> heartRates = new ArrayList<>();


    public static List<Integer> getHeartRates() {
        return heartRates;
    }

    public static void setHeartRates(List<Integer> heartRates) {
        DataFatcher.heartRates = heartRates;
    }
}
