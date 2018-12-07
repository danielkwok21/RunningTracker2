package com.example.danie.runningtracker2.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.example.danie.runningtracker2.ContentProviders.TracksProvider;
import com.example.danie.runningtracker2.R;
import com.example.danie.runningtracker2.Services.LocationService;
import com.example.danie.runningtracker2.Track;
import com.example.danie.runningtracker2.Util;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class Tracking extends AppCompatActivity {
    private static final String TAG = "Tracking";
    public static final String BROADCAST_ACTION = "GET_LOCATION";

    Intent i;
    IntentFilter filter;
    LocationReceiver locationReceiver;

    static TextView distance;
    static Chronometer stopWatch;
    static Button start;

    boolean serviceRunning = false;
    Track newTrack;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            filter = new IntentFilter();
            filter.addAction(BROADCAST_ACTION);
            locationReceiver = new LocationReceiver();
            registerReceiver(locationReceiver, filter);
            initComponents();
        }
    }

    private void initComponents() {
        distance = findViewById(R.id.tracking_distance_tv);
        stopWatch = findViewById(R.id.tracking_duration_chr);
        start = findViewById(R.id.tracking_start_btn);

        start.setOnClickListener((v)->{
            if (!serviceRunning) {
                //start detecting location
                start.setText(R.string.stop);
                i = new Intent(getApplicationContext(), LocationService.class);
                startService(i);

                stopWatch.setBase(SystemClock.elapsedRealtime());
                stopWatch.start();

                serviceRunning = true;
            } else {
                //stop detecting location
                start.setText(R.string.start);
                stopService(i);

                stopWatch.stop();

                uploadToDB(newTrack);

                serviceRunning = false;
            }
        });

        start.performClick();
    }

    private boolean uploadToDB(Track track){
        ContentValues values = new ContentValues();
        gson = new Gson();
        try{
            values.put(TracksProvider.JSON_OBJECT, gson.toJson(track));
            getContentResolver().insert(TracksProvider.CONTENT_URL, values);

            return true;
        }catch(Exception e){
            Log.d(TAG, "uploadToDB: "+e);
            return false;
        }
    }

    public class LocationReceiver extends BroadcastReceiver {
        Gson gson = new Gson();
        Double distance;
        String unit;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: "+intent.getAction());

            if(intent.getAction().equals(filter.getAction(0))){
                String json = intent.getStringExtra(LocationService.TRACK);
                newTrack = gson.fromJson(json, Track.class);
                newTrack.setDuration(stopWatch.getBase());

                distance= newTrack.getDistance();
                unit = newTrack.getUnit();
                Tracking.distance.setText(String.format("%.2f", distance)+unit);
            }else{
                Log.d(TAG, "onReceive: Error");
            }
        }
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Util.Toast(this, "Please allow app to access location");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Util.Toast(this, "onRequestPermissionsResult: Permission granted");

                filter = new IntentFilter();
                filter.addAction(BROADCAST_ACTION);
                locationReceiver = new LocationReceiver();
                registerReceiver(locationReceiver, filter);
                initComponents();
            }else{
                Util.Toast(this, "onRequestPermissionsResult: Permission not granted");
            }
        }
    }
}