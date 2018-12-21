package com.example.danie.runningtracker2.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.danie.runningtracker2.Activities.Tracking;
import com.example.danie.runningtracker2.R;
import com.example.danie.runningtracker2.Track;
import com.example.danie.runningtracker2.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

public class AndroidLocationService extends Service{
    private static final String TAG = "AndroidLocationService";
    private static final int UNIQUE_ID = 520;

    private IBinder locationServiceBinder;
    private NotificationManager notificationManager;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Track thisTrack;
    private Handler stopwatchHandler;
    private Runnable stopwatchRunnable;
    private Intent broadcastIntent;
    private int seconds=0;

    public AndroidLocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        thisTrack = new Track();
        startStopwatch();
    }

    /**
     * Dismisses notification
     * Stops stopwatch
     * Stops location listener
     * Sends broadcast to inform ending of service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        thisTrack.wrapUp();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(Tracking.SERVICE_ENDED);
        sendBroadcast(broadcastIntent);

        notificationManager.cancel(UNIQUE_ID);
        locationManager.removeUpdates(locationListener);
        stopwatchHandler.removeCallbacks(stopwatchRunnable);
    }

    /**
     * Starts a second counter on a separate thread
     * This counter is only for visual purposes
     * Real duration is calculated by Calendar.getInstance()
     */
    private void startStopwatch(){
        stopwatchHandler = new Handler();
        stopwatchRunnable = new Runnable() {
            Intent broadcastIntent = new Intent();

            @Override
            public void run() {
                seconds++;

                String time = String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(seconds),
                        TimeUnit.SECONDS.toMinutes(seconds) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.SECONDS.toSeconds(seconds) % TimeUnit.MINUTES.toSeconds(1));

                stopwatchHandler.postDelayed(stopwatchRunnable, 1000);

                broadcastIntent = new Intent();
                broadcastIntent.setAction(Tracking.GET_TIME);
                broadcastIntent.putExtra(Tracking.THIS_TIME, time);
                sendBroadcast(broadcastIntent);
            }
        };
        stopwatchHandler.post(stopwatchRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationServiceBinder = new ServiceBinder();
        startForeground(UNIQUE_ID, createNotification(""));
        startLocationService();

        return START_STICKY;
    }

    /**
     * Starts location service by
     * 1. Getting last known location
     * 2. Calls onLocationChanged() whenever a new location is detected
     * 3. Broadcasts new location object
     */
    @SuppressLint("MissingPermission")
    private void startLocationService() {
        Log.d(TAG, "startLocationService: ");

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Gson gson =  new GsonBuilder().setPrettyPrinting().create();

                if(location!=null){

                    Log.d(TAG, "onLocationChanged: Lat: "+location.getLatitude()+"|Long: "+location.getLongitude());
                    thisTrack.updateTrack(location);

                    //broadcasts Track object
                    broadcastIntent = new Intent();
                    broadcastIntent.setAction(Tracking.GET_LOCATION);
                    broadcastIntent.putExtra(Tracking.THIS_TRACK, gson.toJson(thisTrack));
                    sendBroadcast(broadcastIntent);

                    notificationManager.notify(UNIQUE_ID, createNotification(Util.getFormattedDistance(thisTrack.getDistance())));
                }else{
                    Log.d(TAG, "onLocationChanged: location is null");
                }

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
            Util.setToast(this, "Cannot detect current location. Try moving around?");
        }
    }

    private Notification createNotification(String distance){
        Intent intent = new Intent(this, Tracking.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        builder.setSmallIcon(R.drawable.icon)
                .setContentTitle("Distance covered: "+distance)
                .setContentIntent(pendingIntent);

        return builder.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return locationServiceBinder;
    }

    public class ServiceBinder extends Binder {
        public AndroidLocationService getService(){
            return AndroidLocationService.this;
        }
    }
}