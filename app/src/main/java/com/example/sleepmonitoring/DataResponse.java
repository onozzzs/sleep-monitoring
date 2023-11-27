package com.example.sleepmonitoring;

import com.google.gson.annotations.SerializedName;

public class DataResponse {
    @SerializedName("message")
    private String message;

    public String getMessage() {
        return message;
    }
}
