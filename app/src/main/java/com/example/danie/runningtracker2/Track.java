package com.example.danie.runningtracker2;


import android.location.Location;

public class Track {
    private String name;
    private Location startLocation;
    private Location endLocation;
    private double distance;
    private String unit;
    private long duration =0;

    public Track(Location startLocation, Location endLocation, double distance, String unit, long duration){
        this.name = Double.toString(distance) + unit+" for " + Long.toString(duration);
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.distance = distance;
        this.unit = unit;
        this.duration = duration;
    }

    public Track(Location startLocation, Location endLocation, double distance, String unit){
        this.name = Double.toString(distance) + unit+" for " + Long.toString(duration);
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.distance = distance;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public double getDistance() {
        return distance;
    }

    public String getUnit() {
        return unit;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}