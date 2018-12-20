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
        startDate = Util.getFormattedDateFromDate(startNow.getTime());
        startTime = Util.getFormattedTimeFromDate(startNow.getTime());
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
        name = Util.getFormattedDistance(distance)+" on "+getStartDate();
    }

    /**
     * Called when user stops tracking
     * sets endDate and endTime upon end of this track
     * calculated duration
     */
    public void wrapUp(){
        Calendar endNow = Calendar.getInstance();
        endDate = Util.getFormattedDateFromDate(endNow.getTime());
        endTime = Util.getFormattedTimeFromDate(endNow.getTime());

        duration = endNow.getTime().getTime() - startNow.getTime().getTime();

        endLatLng = latlngs.get(latlngs.size()-1);
    }

    public String getName() {
        return name;
    }

    public double getDistance() {
        return distance;
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

    public long getDuration() {
        return duration;
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

    public Calendar getStartNow() {
        return startNow;
    }

    public Calendar getEndNow() {
        return endNow;
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
}
