package com.example.sleepmonitoring;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StopwatchViewModel extends ViewModel {
    private MutableLiveData<Long> elapsedTimeMillis = new MutableLiveData<>();

    public LiveData<Long> getElapsedTimeMillis() {
        return elapsedTimeMillis;
    }

    public void setElapsedTimeMillis(long elapsedTimeMillis) {
        this.elapsedTimeMillis.setValue(elapsedTimeMillis);
    }
}