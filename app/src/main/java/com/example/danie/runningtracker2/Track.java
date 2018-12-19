package com.example.danie.runningtracker2;


import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Track{
    private final String TAG = "Track";
    private final String METER = "m";
    private final String KILOMETER = "km";
    private String unit = METER;

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

    /**
     * sets startDate and startTime upon creation of this track
     */
    public Track(){
        startNow = Calendar.getInstance();
        startDate = getFormattedDateFromDate(startNow.getTime());
        startTime = getFormattedTimeFromDate(startNow.getTime());
    }

    /**
     * Called every time whenever there is a change in location to update attributes of this track
     * @param location
     */
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

        Log.d(TAG, "updateTrack: distance="+distance);
        name = getFormattedDistance()+" on "+getStartDate();
    }

    /**
     * Conversion function LatLng -> Location
     * @param latLng
     * @return location
     */
    private Location getLocationFromLatLng(LatLng latLng){
        Location l = new Location("");
        l.setLatitude(latLng.latitude);
        l.setLongitude(latLng.longitude);

        return l;
    }

    /**
     * Called when user stops tracking
     * sets endDate and endTime upon end of this track
     * calculated duration
     */
    public void wrapUp(){
        Calendar endNow = Calendar.getInstance();
        endDate = getFormattedDateFromDate(endNow.getTime());
        endTime = getFormattedTimeFromDate(endNow.getTime());

        duration = endNow.getTime().getTime() - startNow.getTime().getTime();

        endLatLng = latlngs.get(latlngs.size()-1);
    }

    public String getName() {
        return name;
    }

    public double getDistance() {
        return distance;
    }

    public String getFormattedDistance(){
        Double newDistance = distance;

        //converts to km if distance is above 1000m
        if(distance>1000 && unit.equals(METER)){
            newDistance = distance/1000;
            unit = KILOMETER;
        }
        return String.format("%.3f", newDistance)+unit;
    }
    public String getFormattedDuration(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(duration);
        return getFormattedTimeFromDate(calendar.getTime());
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

        //convert to km/h
        km = distance/1000;
        hour = duration*0.0000036;
        speed = km/hour;

        return String.format("%.3f", speed)+"km/h";
    }

    public Calendar getStartNow() {
        return startNow;
    }

    public Calendar getEndNow() {
        return endNow;
    }

    private static String getFormattedTimeFromDate(Date date){
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        return timeFormat.format(date);
    }

    private static String getFormattedDateFromDate(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        return dateFormat.format(date);
    }
}
