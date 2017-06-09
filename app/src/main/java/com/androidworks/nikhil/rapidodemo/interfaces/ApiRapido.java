package com.androidworks.nikhil.rapidodemo.interfaces;

import com.androidworks.nikhil.rapidodemo.model.DirectionResults;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Nikhil on 07-Jun-17.
 */
public interface ApiRapido {

    /**
     * call location service API
     * @param origin
     * @param destination
     * @param isAlternativeEnabled
     * @return
     */
    @GET("maps/api/directions/json")
    Call<DirectionResults> getJson(@Query("origin") String origin, @Query("destination") String destination, @Query("alternatives") boolean isAlternativeEnabled);
}
