package com.androidworks.nikhil.rapidodemo.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Nikhil on 07-Jun-17.
 */
public class DirectionResults {
    @SerializedName("routes")
    private List<Route> routes;

    public List<Route> getRoutes() {
        return routes;
    }}

