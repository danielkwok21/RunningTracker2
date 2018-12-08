package com.example.danie.runningtracker2.Services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.example.danie.runningtracker2.Activities.Tracking;
import com.example.danie.runningtracker2.Track;
import com.example.danie.runningtracker2.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Calendar;
import java.util.Date;

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    public static final String TRACK = "newTrack";

    private IBinder locationServiceBinder;
    private Intent broadcastIntent;

    private LocationManager locationManager;
    private LocationListener locationListener;


    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationServiceBinder = new LocationServiceBinder();
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        startLocationService();

        return START_STICKY;
    }


    @SuppressLint("MissingPermission")
    private void startLocationService() {
        Log.d(TAG, "startLocationService: ");

        locationListener = new LocationListener() {

            boolean firstCall=true;
            Location currentLocation;
            Location prevLocation;
            Location startLocation;
            double calcDistance=0;

            Track newTrack;
            Gson gson = new Gson();

            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;

                if(firstCall){
                    startLocation = currentLocation;
                    prevLocation = currentLocation;
                    firstCall = false;
                }

                calcDistance = calcDistance+prevLocation.distanceTo(currentLocation);

                prevLocation = location;

                Log.d(TAG, "onLocationChanged: calcDistance: "+calcDistance);

                //creating new track object
                newTrack = new Track(startLocation, currentLocation, calcDistance, Calendar.getInstance());

                //broadcasts info
                broadcastIntent = new Intent();
                broadcastIntent.setAction(Tracking.BROADCAST_ACTION);
                broadcastIntent.putExtra(TRACK, gson.toJson(newTrack));
                sendBroadcast(broadcastIntent);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "startLocationService: Status changed");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "startLocationService: Provider enabled");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "startLocationService: Provider disabled");

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        Location x = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationListener.onLocationChanged(x);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return locationServiceBinder;
    }

    public class LocationServiceBinder extends Binder {
        public LocationService getService(){
            return LocationService.this;
        }
    }
}