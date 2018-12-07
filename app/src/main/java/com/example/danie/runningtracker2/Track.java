package com.example.danie.runningtracker2;


import android.location.Location;

public class Track {
    private static int id = 0;
    private Location startLocation;
    private Location endLocation;
    private double distance;
    private String unit;
    private long duration =0;

    public Track(Location startLocation, Location endLocation, double distance, String unit, long duration){
        this.id = id++;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.distance = distance;
        this.unit = unit;
        this.duration = duration;
    }

    public Track(Location startLocation, Location endLocation, double distance, String unit){
        this.id = id++;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.distance = distance;
        this.unit = unit;
    }

    public static int getId() {
        return id;
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