package com.example.danie.runningtracker2;


import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Track implements Serializable {
    private final String TAG = "Track";

    private String name;
    private Double startLocationLat;
    private Double startLocationLong;
    private String startDate;
    private String startTime;

    private Double endLocationLat;
    private Double endLocationLong;
    private String endDate;
    private String endTime;

    private Double distance=0.0d;
    private long duration =0;
    private Calendar startNow;
    private List<LatLng> latlngs = new ArrayList<>();
    private List<Location> locations = new ArrayList<>();

    public Track(){
        startNow = Calendar.getInstance();
        startDate = getFormattedDate(startNow.getTime());
        startTime = getFormattedTime(startNow.getTime());
    }

    public void updateTrack(Location location){
        Double lat = location.getLatitude();
        Double lng = location.getLongitude();
        Location prevLocation;

        latlngs.add(new LatLng(lat, lng));
        Location currentLocation = new Location("");
        currentLocation.setLatitude(lat);
        currentLocation.setLongitude(lng);

        if(locations.size()==0){
            //if this location is the first location
            startLocationLat = currentLocation.getLatitude();
            startLocationLong = currentLocation.getLongitude();
            prevLocation = currentLocation;
        }else{
            prevLocation = locations.get(locations.size()-1);
        }

        locations.add(currentLocation);

        distance = distance+prevLocation.distanceTo(currentLocation);
        Log.d(TAG, "updateTrack: locations.size()="+locations.size());
        Log.d(TAG, "updateTrack: distance="+distance);
        name = getFormattedDistance()+" on "+getStartDate();
    }

    public void wrapUp(){
        Calendar endNow;
        endNow = Calendar.getInstance();
        endDate = getFormattedDate(endNow.getTime());
        endTime = getFormattedTime(endNow.getTime());

        this.duration = endNow.getTime().getTime() - startNow.getTime().getTime();

        endLocationLat = locations.get(locations.size()-1).getLatitude();
        endLocationLong = locations.get(locations.size()-1).getLongitude();
    }

    public List<Location> getLocations(){
        return locations;
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


    public void setLatlngs(List<LatLng> list){
        this.latlngs = list;
    }

    public List<LatLng> getLatLngs() {
        return latlngs;
    }

    private String getFormattedTime(Date d){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(d);
    }

    private String getFormattedDate(Date d){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        return sdf.format(d);
    }



}