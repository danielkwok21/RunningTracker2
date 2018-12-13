package com.example.danie.runningtracker2.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
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
import com.example.danie.runningtracker2.Services.AndroidLocationService;
import com.example.danie.runningtracker2.Services.GooglePlayLocationService;
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
    public static final String GET_LOCATION = "getLocation";
    public static final String GET_TIME = "getTime";
    public static final String THIS_TRACK = "thisTrack";
    public static final String THIS_TIME = "thisTime";

    private Intent serviceIntent;
    private IntentFilter filter;
    private LocationReceiver locationReceiver;
    private GooglePlayLocationService googlePlayLocationService;
    private AndroidLocationService androidLocationService;

    private TextView distance;
    private TextView stopWatch;
    private Button start;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;

    private boolean isGooglePlayAvailable;

    private Track newTrack = null;
    private boolean serviceBounded = false;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isGooglePlayAvailable = googlePlayAvailable();
        if(isGooglePlayAvailable){
            setContentView(R.layout.activity_tracking);
        }else{
            setContentView(R.layout.activity_tracking2);
        }

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            filter = new IntentFilter();
            filter.addAction(GET_LOCATION);
            filter.addAction(GET_TIME);
            locationReceiver = new LocationReceiver();
            registerReceiver(locationReceiver, filter);
            initComponents();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serviceBounded){
            unbindService(connection);
            Log.d(TAG, "onDestroy: Unbound service");
            serviceBounded = false;
        }
    }

    private void initComponents() {
        distance = findViewById(R.id.tracking_distance_tv);
        stopWatch = findViewById(R.id.tracking_duration_chr);
        start = findViewById(R.id.tracking_start_btn);

        if(isGooglePlayAvailable){
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.track_detailed_map);
            mapFragment.getMapAsync(this);
        }

        start.setOnClickListener((v)->{
            if (!serviceBounded) {
                if(isGooglePlayAvailable){
                    serviceIntent = new Intent(getApplicationContext(), GooglePlayLocationService.class);
                }else{
                    serviceIntent = new Intent(getApplicationContext(), AndroidLocationService.class);
                }
                startService(serviceIntent);
                bindService(serviceIntent, connection, BIND_AUTO_CREATE);

                serviceBounded = true;
            } else {
                try{
                    //stop detecting location
                    newTrack.wrapUp();
                    unbindService(connection);
                    stopService(serviceIntent);
                    serviceBounded = false;

                    //set ui
//                    stopWatch.stop();
                    start.setText(R.string.start);

                    uploadToDB(newTrack);

                    //open detailed view
                    Intent i = new Intent(this, ViewTrackDetailed.class);
                    String jsonObject = gson.toJson(newTrack);
                    i.putExtra(TracksProvider.JSON_OBJECT, jsonObject);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }catch(Exception e){
                    Log.d(TAG, "initComponents: "+e);
                }
            }
        });

        if(!serviceBounded){
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
        if(isGooglePlayAvailable) {
            List<LatLng> LatLngs = newTrack.getLatLngs();
            if (!LatLngs.isEmpty()) {

                mMap.clear();

                //set starting point
                LatLng start = LatLngs.get(0);
                mMap.addMarker(new MarkerOptions().position(start).title(THIS_TRACK));

                //set current point
                LatLng here = LatLngs.get(LatLngs.size()-1);
                mMap.addMarker(new MarkerOptions().position(here).title(THIS_TRACK));
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

        try{
            values.put(TracksProvider.JSON_OBJECT, gson.toJson(track));
            getContentResolver().insert(TracksProvider.CONTENT_URL, values);

            return true;
        }catch(Exception e){
            Log.d(TAG, "uploadToDB: "+e);
            Util.Toast(this, "Unable to store track");
            return false;
        }
    }

    public class LocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

                switch(intent.getAction()){
                    case GET_LOCATION:
                        String json = intent.getStringExtra(Tracking.THIS_TRACK);
                        newTrack = gson.fromJson(json, Track.class);

                        if(newTrack!=null) {
                            start.setText(R.string.stop);
                            distance.setText(newTrack.getFormattedDistance());
                            redrawRoute();

                            serviceBounded = true;
                        }else {
                            Log.d(TAG, "onReceive: newTrack is null");
                        }
                        break;
                    case GET_TIME:
                        String formattedSeconds = intent.getStringExtra(Tracking.THIS_TIME);

                        stopWatch.setText(formattedSeconds);
                        break;
                    default:
                        break;
                }

            }
        }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceBounded = true;
            Util.Toast(getApplicationContext(), "service connected");
            if(isGooglePlayAvailable){
                GooglePlayLocationService.ServiceBinder binder = (GooglePlayLocationService.ServiceBinder) service;
                googlePlayLocationService = binder.getService();
            }else{
                AndroidLocationService.ServiceBinder binder = (AndroidLocationService.ServiceBinder) service;
                androidLocationService = binder.getService();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBounded = false;
            Util.Toast(getApplicationContext(), "service disconnected");
            Log.d(TAG, "onServiceDisconnected: ");
            if(isGooglePlayAvailable){
                googlePlayLocationService = null;
            }else{
                androidLocationService = null;
            }
        }
    };

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
                filter.addAction(GET_LOCATION);
                filter.addAction(GET_TIME);
                locationReceiver = new LocationReceiver();
                registerReceiver(locationReceiver, filter);
                initComponents();
            }else{
                Util.Toast(this, "onRequestPermissionsResult: Permission not granted");
            }
        }
//
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