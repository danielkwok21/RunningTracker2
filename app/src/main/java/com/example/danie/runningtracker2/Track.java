package com.example.danie.runningtracker2;


public class Track {
    private static int id=0;
    private String startLocation;
    private String endLocation;
    private Float distance;
    private String unit;
    private Float duration;

    public Track(String startLocation, String endLocation, Float distance, String unit, Float duration){
        this.id = id++;

        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.distance = distance;
        this.unit = unit;
        this.duration = duration;
    }

    public static int getId() {
        return id;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public Float getDistance() {
        return distance;
    }

    public String getUnit() {
        return unit;
    }

    public Float getDuration() {
        return duration;
    }
}