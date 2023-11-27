package com.example.sleepmonitoring;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("/")
    Call<DataResponse> getData();
}
