package com.androidworks.nikhil.rapidodemo.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Nikhil on 07-Jun-17.
 */
public class Route {
    @SerializedName("overview_polyline")
    private OverviewPolyLine overviewPolyLine;

    private List<Legs> legs;

    public OverviewPolyLine getOverviewPolyLine() {
        return overviewPolyLine;
    }

    public List<Legs> getLegs() {
        return legs;
    }
}