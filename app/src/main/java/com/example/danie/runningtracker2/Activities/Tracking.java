package com.example.danie.runningtracker2.Activities;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.gson.Gson;

import java.util.List;

public class Tracking extends AppCompatActivity implements OnMapReadyCallback{
    private static final String TAG = "Tracking";
    public static final String BROADCAST_ACTION = "GET_LOCATION";
    private static final String THIS_TRACK = "thisTrack";
    private static final int UNIQUE_ID = 1234;

    Intent serviceIntent;
    IntentFilter filter;
    LocationReceiver locationReceiver;
    NotificationCompat.Builder newNotificationBuilder;
    NotificationManager notificationManager;

    static TextView distance;
    Chronometer stopWatch;
    Button start;
    SupportMapFragment mapFragment;
    GoogleMap mMap;

    static boolean serviceRunning = false;
    boolean googlePlayAvailable;

    Track newTrack;

    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googlePlayAvailable = googlePlayAvailable();
        if(googlePlayAvailable){
            setContentView(R.layout.activity_tracking);
        }else{
            setContentView(R.layout.activity_tracking2);
        }


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

        if(googlePlayAvailable){
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.track_detailed_map);
            mapFragment.getMapAsync(this);
        }

        start.setOnClickListener((v)->{
            if (!serviceRunning) {
                //start detecting location
                serviceIntent = new Intent(getApplicationContext(), LocationService.class);
                startService(serviceIntent);

                newTrack = new Track();
            } else {
                //stop detecting location
                newTrack.wrapUp();
                stopService(serviceIntent);
                notificationManager.cancel(UNIQUE_ID);
                serviceRunning = false;

                //set ui
                stopWatch.stop();
                start.setText(R.string.start);

                uploadToDB(newTrack);

                //open detailed view
                Intent i = new Intent(this, ViewTrackDetailed.class);
                String jsonObject = gson.toJson(newTrack);
                i.putExtra(TracksProvider.JSON_OBJECT, jsonObject);
                i.putExtra("prevActivity", "Tracking");
                startActivity(i);
            }
        });

        if(!serviceRunning){
            start.performClick();
        }
    }

    private boolean googlePlayAvailable(){
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);

        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void redrawRoute(){
        if(googlePlayAvailable) {
            List<LatLng> LatLngs = newTrack.getLatLngs();
            if (!LatLngs.isEmpty()) {

                mMap.clear();

                //set starting point
                LatLng start = LatLngs.get(0);
                mMap.addMarker(new MarkerOptions().position(start).title(THIS_TRACK));

                //set current point
                LatLng here = LatLngs.get(LatLngs.size()-1);
                mMap.addMarker(new MarkerOptions().position(start).title(THIS_TRACK));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(here));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(20.0f));

                //populating latlngs list to draw route
                Polyline route = mMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .addAll(LatLngs));
                route.setTag(THIS_TRACK);

                route.setEndCap(new RoundCap());
                route.setWidth(10);
                route.setColor(getResources().getColor(R.color.colorAccent));
                route.setJointType(JointType.ROUND);
            }
        }
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

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: "+intent.getAction());

            if(intent.getAction().equals(filter.getAction(0))){
                Double loclat = intent.getDoubleExtra(LocationService.LOC_LAT, 0.0f);
                Double loclong = intent.getDoubleExtra(LocationService.LOC_LONG, 0.0f);

                newTrack.updateTrack(loclat, loclong);


                //set ui
                if(newTrack.getLocations().size()==1){
                    stopWatch.setBase(SystemClock.elapsedRealtime());
                }
                stopWatch.start();
                start.setText(R.string.stop);
                Tracking.distance.setText(newTrack.getFormattedDistance());
                redrawRoute();
                startNotification();

                serviceRunning = true;
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
//
    }

    private void startNotification(){
        Intent intent = new Intent(this, Tracking.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //builds the body of the notification itself
        newNotificationBuilder = new NotificationCompat.Builder(this);
        newNotificationBuilder.setSmallIcon(R.drawable.icon)
                .setContentTitle("Distance covered: "+newTrack.getFormattedDistance())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        //sends notification to phone
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(UNIQUE_ID, newNotificationBuilder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(locationReceiver!=null) {
            registerReceiver(locationReceiver, filter);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(locationReceiver!=null){
            unregisterReceiver(locationReceiver);
        }
    }
}