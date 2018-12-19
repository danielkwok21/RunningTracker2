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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Track{
    private final String TAG = "Track";

    private String name;
    private LatLng startLatLng;
    private String startDate;
    private String startTime;

    private LatLng endLatLng;
    private String endDate;
    private String endTime;

    private Double distance=0.0d;
    private long duration =0;
    private Calendar startNow;
    private Calendar endNow;

    private List<LatLng> latlngs = new ArrayList<>();

    public Track(){
        startNow = Calendar.getInstance();
        startDate = getFormattedDate(startNow.getTime());
        startTime = getFormattedTime(startNow.getTime());
    }

    public void updateTrack(Location location){
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        Location currentLocation = getLocationFromLatLng(currentLatLng);
        LatLng prevLatLng;
        Location prevLocation;

        //if this is the first latlng
        if(latlngs.size()==0){
            prevLatLng = currentLatLng;
            startLatLng = currentLatLng;
        }else{
            prevLatLng = latlngs.get(latlngs.size()-1);
        }
        prevLocation = getLocationFromLatLng(prevLatLng);

        latlngs.add(currentLatLng);

        //set thus far distance and duration
        distance = distance+prevLocation.distanceTo(currentLocation);
        duration = Calendar.getInstance().getTime().getTime() - startNow.getTime().getTime();

        Log.d(TAG, "updateTrack: distance="+distance);
        name = getFormattedDistance()+" on "+getStartDate();
    }

    private Location getLocationFromLatLng(LatLng latLng){
        Location l = new Location("");
        l.setLatitude(latLng.latitude);
        l.setLongitude(latLng.longitude);

        return l;
    }

    public void wrapUp(){
        Calendar endNow = Calendar.getInstance();
        endDate = getFormattedDate(endNow.getTime());
        endTime = getFormattedTime(endNow.getTime());

        this.duration = endNow.getTime().getTime() - startNow.getTime().getTime();

        endLatLng = latlngs.get(latlngs.size()-1);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
        return String.format("%.3f", newDistance)+unit;
    }
    public String getFormattedDuration(){
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
        return hms;
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

    public LatLng getStartLatLng() {
        return startLatLng;
    }

    public LatLng getEndLatLng() {
        return endLatLng;
    }

    public List<LatLng> getLatLngs() {
        return latlngs;
    }

    public double getSpeed(){
        return distance/duration;
    }

    public String getFormattedSpeed(){
        double speed;
        double km;
        double hour;

        //convert to km
        km = distance/1000;
        hour = duration*0.0000036;
        speed = km/hour;

        return String.format("%.3f", speed)+"km/h";
    }

    private String getFormattedTime(Date d){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(d);
    }

    private String getFormattedDate(Date d){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        return sdf.format(d);
    }

    public Calendar getStartNow() {
        return startNow;
    }

    public Calendar getEndNow() {
        return endNow;
    }
}