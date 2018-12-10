package com.example.danie.runningtracker2.Services;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.danie.runningtracker2.Activities.Tracking;
import com.example.danie.runningtracker2.R;
import com.example.danie.runningtracker2.Track;
import com.example.danie.runningtracker2.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnSuccessListener;
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

    Track newTrack;

    public GooglePlayLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        googlePlayLocationServiceBinder = new GooglePlayLocationServiceBinder();
        googleApiClient.connect();
        newTrack = new Track();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(UNIQUE_ID);
        if(googleApiClient.isConnected()){
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
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

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        onLocationChanged(location);

        long UPDATE_INTERVAL = 5000;  /* 5 secs */
        long FASTEST_INTERVAL = 1000; /* 1 secs */
        startNotification();

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(1);

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
            Log.d(TAG, "onLocationChanged: Lat: "+location.getLatitude()+"|Long: "+location.getLongitude());

            //broadcasts Track object
            broadcastIntent = new Intent();
            broadcastIntent.setAction(Tracking.BROADCAST_ACTION);
            broadcastIntent.putExtra(NEW_TRACK, gson.toJson(newTrack));

            sendBroadcast(broadcastIntent);

            Util.Toast(this, "Distance: "+newTrack.getDistance());
        }else{
            Log.d(TAG, "onLocationChanged: location is null");
        }
    }

    public class GooglePlayLocationServiceBinder extends Binder {
        public GooglePlayLocationServiceBinder getService(){
            return GooglePlayLocationServiceBinder.this;
        }
    }
}