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
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.danie.runningtracker2.Activities.Tracking;
import com.example.danie.runningtracker2.R;
import com.example.danie.runningtracker2.Track;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

public class GooglePlayLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = "GPLocationService";
    private static final int UNIQUE_ID = 1234;

    private IBinder googlePlayLocationServiceBinder;
    private NotificationManager notificationManager;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Track newTrack;
    private Handler stopwatchHandler;
    private Runnable stopwatchRunnable;
    private int seconds=0;

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

        startStopwatch();
    }

    private void startStopwatch(){

        //preparing timer
        stopwatchHandler = new Handler();
        stopwatchRunnable = new Runnable() {
            Intent broadcastIntent = new Intent();

            @Override
            public void run() {
                seconds++;

                String hms = String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(seconds),
                        TimeUnit.SECONDS.toMinutes(seconds) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.SECONDS.toSeconds(seconds) % TimeUnit.MINUTES.toSeconds(1));

                stopwatchHandler.postDelayed(stopwatchRunnable, 1000);

                broadcastIntent = new Intent();
                broadcastIntent.setAction(Tracking.GET_TIME);
                broadcastIntent.putExtra(Tracking.THIS_TIME, hms);
                sendBroadcast(broadcastIntent);
            }
        };
        stopwatchHandler.post(stopwatchRunnable);

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
        stopwatchHandler.removeCallbacks(stopwatchRunnable);
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
            broadcastIntent.putExtra(Tracking.THIS_TRACK, gson.toJson(newTrack));
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