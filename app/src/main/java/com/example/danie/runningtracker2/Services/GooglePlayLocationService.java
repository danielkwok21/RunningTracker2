package com.example.danie.runningtracker2.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Chronometer;

import com.example.danie.runningtracker2.Activities.Tracking;
import com.example.danie.runningtracker2.R;
import com.example.danie.runningtracker2.Track;
import com.example.danie.runningtracker2.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.gson.Gson;

public class GooglePlayLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = "GPLocationService";
    public static final String NEW_TRACK = "newTrack";
    private static final int UNIQUE_ID = 1234;

    private IBinder googlePlayLocationServiceBinder;
    private NotificationManager notificationManager;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Chronometer stopWatch;
    private Track newTrack;

    public GooglePlayLocationService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        newTrack = new Track();
        stopWatch = new Chronometer(this);
        stopWatch.setBase(SystemClock.elapsedRealtime());
        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                Log.d(TAG, "onChronometerTick: Tick");
            }
        });
        stopWatch.start();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        googlePlayLocationServiceBinder = new ServiceBinder();
        googleApiClient.connect();

        startForeground(UNIQUE_ID, createNotification(""));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        notificationManager.cancel(UNIQUE_ID);
        if(googleApiClient.isConnected()){
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private Notification createNotification(String distance){
        Intent intent = new Intent(this, Tracking.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        builder.setSmallIcon(R.drawable.icon)
                .setContentTitle("Distance covered: "+distance)
                .setContentIntent(pendingIntent);

        return builder.build();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        onLocationChanged(location);

        long UPDATE_INTERVAL = 5000;  /* 5 secs */
        long FASTEST_INTERVAL = 1000; /* 1 secs */

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return googlePlayLocationServiceBinder;
    }

    @Override
    public void onLocationChanged(Location location) {
        Intent broadcastIntent;
        Gson gson = new Gson();
        if(location!=null){
            newTrack.updateTrack(location);

            //broadcasts Track object
            broadcastIntent = new Intent();
            broadcastIntent.setAction(Tracking.GET_LOCATION);
            broadcastIntent.putExtra(NEW_TRACK, gson.toJson(newTrack));

            sendBroadcast(broadcastIntent);

            notificationManager.notify(UNIQUE_ID, createNotification(newTrack.getFormattedDistance()));
        }else{
            Log.d(TAG, "onLocationChanged: location is null");
        }
    }

    public class ServiceBinder extends Binder {
        public GooglePlayLocationService getService(){
            return GooglePlayLocationService.this;
        }
    }
}