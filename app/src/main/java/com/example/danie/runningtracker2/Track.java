package com.example.danie.runningtracker2;


import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Track {
    private String name;
    private Double startLocationLat;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private Double startLocationLong;
    private Double endLocationLat;
    private Double endLocationLong;
    private Double distance;
    private long duration =0;
    private Calendar startNow;
    private Calendar endNow;
    private List<LatLng> latlngs;

    public Track(Location startLocation, Location endLocation, double distance, Calendar startNow){
        this.startLocationLat = startLocation.getLatitude();
        this.startLocationLong = startLocation.getLongitude();
        this.endLocationLat = endLocation.getLatitude();
        this.endLocationLong = endLocation.getLongitude();
        this.distance = distance;
        this.startNow = startNow;

        startDate = setDate(startNow.getTime());
        startTime = setTime(startNow.getTime());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Double getStartLocationLat() {
        return startLocationLat;
    }

    public Double getStartLocationLong() {
        return startLocationLong;
    }

    public Double getEndLocationLat() {
        return endLocationLat;
    }

    public Double getEndLocationLong() {
        return endLocationLong;
    }

    public double getDistance() {
        return distance;
    }

    public String getFormattedDistance(){
        Double newDistance = distance;
        final String METER = "m";
        final String KILOMETER = "km";
        String unit = METER;
        //converts to km if distance is above 1000m
        if(distance>1000){
            newDistance = distance/1000;
            unit = KILOMETER;
        }
        return String.format("%.2f", newDistance)+unit;
    }

    public long getDuration() {
        return duration;
    }

    public String getFormattedDuration(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(duration);
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setDuration(Calendar endNow) {
        this.endNow = endNow;
        endDate = setDate(endNow.getTime());
        endTime = setTime(endNow.getTime());

        this.duration = endNow.getTime().getTime() - startNow.getTime().getTime();
    }

    public void setLatlngs(List<LatLng> list){
        this.latlngs = list;
    }

    public List<LatLng> getLatlngs() {
        return latlngs;
    }

    private String setTime(Date d){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(d);
    }

    private String setDate(Date d){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        return sdf.format(d);
    }



}