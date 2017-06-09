package com.androidworks.nikhil.rapidodemo.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Nikhil on 07-Jun-17.
 */
public class Steps {
    private Location start_location;
    private Location end_location;
    private OverviewPolyLine polyline;
    private Distance distance;
    @SerializedName("html_instructions")
    private String instructions;

    public String getInstructions() {
        return instructions;
    }

    public Location getStart_location() {
        return start_location;
    }

    public Location getEnd_location() {
        return end_location;
    }

    public OverviewPolyLine getPolyline() {
        return polyline;
    }

    public Distance getDistance() {
        return distance;
    }
}