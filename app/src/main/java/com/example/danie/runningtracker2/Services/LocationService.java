package com.example.danie.runningtracker2.Services;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.danie.runningtracker2.Activities.Tracking;
import com.example.danie.runningtracker2.R;
import com.example.danie.runningtracker2.Track;
import com.example.danie.runningtracker2.Util;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.concurrent.Executor;

public class LocationService extends Service{
    private static final String TAG = "LocationService";
    public static final String NEW_TRACK = "newTrack";
    private static final int UNIQUE_ID = 1234;

    private IBinder locationServiceBinder;
    private NotificationManager notificationManager;

    //traditional methods
    private LocationManager locationManager;
    private LocationListener locationListener;

    Track newTrack;

    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationServiceBinder = new LocationServiceBinder();

        startLocationService();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(UNIQUE_ID);
        locationManager = null;

    }

    @SuppressLint("MissingPermission")
    private void startLocationService() {
        Log.d(TAG, "startLocationService: ");
        newTrack = new Track();
        startNotification();

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent broadcastIntent;
                Gson gson = new Gson();

                newTrack.updateTrack(location);
                Log.d(TAG, "onLocationChanged: Lat: "+location.getLatitude()+"|Long: "+location.getLongitude());

                //broadcasts Track object
                broadcastIntent = new Intent();
                broadcastIntent.setAction(Tracking.BROADCAST_ACTION);
                broadcastIntent.putExtra(NEW_TRACK, gson.toJson(newTrack));

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

        String provider= locationManager.getBestProvider(new Criteria(), false);
        locationManager.requestLocationUpdates(provider, 0, 0, locationListener);

        //get initial location
        Location location = locationManager.getLastKnownLocation(provider);
        if(location!=null){
            locationListener.onLocationChanged(location);
        }else{
            Util.Toast(this, "Cannot detect current location");
        }
    }

    private void startNotification(){
        NotificationCompat.Builder newNotificationBuilder;
        Intent intent = new Intent(this, Tracking.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //builds the body of the notification itself
        newNotificationBuilder = new NotificationCompat.Builder(this);
        newNotificationBuilder.setSmallIcon(R.drawable.icon)
                .setContentTitle("Distance covered: "+newTrack.getFormattedDistance())
                .setContentText("Duration: "+newTrack.getFormattedDuration())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        //sends notification to phone
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(UNIQUE_ID, newNotificationBuilder.build());
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