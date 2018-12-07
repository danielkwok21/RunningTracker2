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

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    public static final String START_LOCATION = "startLocation";
    public static final String END_LOCATION = "endLocation";
    public static final String DISTANCE = "distance";
    public final String METER = "m";
    public final String KILOMETER = "km";


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
            float calcDistance=0;

            boolean converted = false;
            String unit = METER;

            @Override
            public void onLocationChanged(Location location) {

                currentLocation = location;

                if(firstCall){
                    startLocation = currentLocation;
                    prevLocation = currentLocation;
                    firstCall = false;
                }

                calcDistance = calcDistance+prevLocation.distanceTo(currentLocation);

                Log.d(TAG, "calcDistance: "+calcDistance);
                //converts to km if distance is above 1000m
                if(calcDistance>1000 && !converted){
                    calcDistance/=1000;
                    unit = KILOMETER;
                    converted=true;
                }

                prevLocation = location;

                Log.d(TAG, "onLocationChanged: startLocation: "+startLocation);
                Log.d(TAG, "onLocationChanged: currentLocation: "+currentLocation);
                Log.d(TAG, "onLocationChanged: calcDistance: "+calcDistance);
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