package com.example.danie.runningtracker2.Services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.example.danie.runningtracker2.Activities.Tracking;
import com.example.danie.runningtracker2.Util;

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    public static final String LOC_LAT = "location_latitude";
    public static final String LOC_LONG = "location_longitude";

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
            Location prevLocation;
            boolean firstCall = true;
            @Override
            public void onLocationChanged(Location location) {

                if(firstCall){
                    prevLocation = location;
                    firstCall = false;
                }

                Log.d(TAG, "onLocationChanged: Lat: "+location.getLatitude()+"|Long: "+location.getLongitude());

                //broadcasts info
                broadcastIntent = new Intent();
                broadcastIntent.setAction(Tracking.BROADCAST_ACTION);
                broadcastIntent.putExtra(LOC_LAT, location.getLatitude());
                broadcastIntent.putExtra(LOC_LONG, location.getLongitude());
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
        locationManager.requestLocationUpdates(locationManager.getBestProvider(new Criteria(), false), 0, 0, locationListener);
        Location x = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(x!=null){
            locationListener.onLocationChanged(x);
        }else{
            Util.Toast(this, "Cannot detect current location");
        }
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