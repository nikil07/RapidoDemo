package com.androidworks.nikhil.rapidodemo.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Nikhil on 07-Jun-17.
 */
public class OverviewPolyLine {

    @SerializedName("points")
    public String points;

    public String getPoints() {
        return points;
    }
}