package com.example.danie.runningtracker2.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.danie.runningtracker2.ContentProviders.TracksProvider;
import com.example.danie.runningtracker2.Track;
import com.example.danie.runningtracker2.R;
import com.example.danie.runningtracker2.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.gson.Gson;

public class ViewTrackDetailed extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "ViewTrackDetailed";

    private TextView startDate;
    private TextView startTime;
    private TextView distance;
    private TextView duration;
    private TextView speed;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private Track thisTrack;
    private Gson gson;

    private boolean isGooglePlayAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        isGooglePlayAvailable = googlePlayAvailable();

        super.onCreate(savedInstanceState);

        if(isGooglePlayAvailable){
            setContentView(R.layout.activity_view_track_detailed);
        }else{
            setContentView(R.layout.activity_view_track_detailed2);
        }

        if(savedInstanceState==null){
            Bundle extras = getIntent().getExtras();
            String jsonObject;
            gson = new Gson();

            if(extras!=null){
                jsonObject = extras.getString(TracksProvider.JSON_OBJECT);
                thisTrack = gson.fromJson(jsonObject, Track.class);
            }
        }

        initComponent();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(!thisTrack.getLatLngs().isEmpty()){
            //set starting & endingpoint
            LatLng start = thisTrack.getLatLngs().get(0);
            mMap.addMarker( new MarkerOptions().position(start));

            LatLng end = thisTrack.getLatLngs().get(thisTrack.getLatLngs().size()-1);
            mMap.addMarker( new MarkerOptions().position(end));

            //setting zoom to fit all markers
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(LatLng l: thisTrack.getLatLngs()){
                builder.include(l);
            }
            final int width = getResources().getDisplayMetrics().widthPixels;
            final int height = (int)Math.round(getResources().getDisplayMetrics().heightPixels*0.6);
            final int padding = (int)(width*0.12);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding));

            //populating latlngs list to draw route
            Polyline route = mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .addAll(thisTrack.getLatLngs()));

            route.setEndCap(new RoundCap());
            route.setWidth(10);
            route.setColor(getResources().getColor(R.color.colorAccent));
            route.setJointType(JointType.ROUND);
        }
    }

    private void initComponent(){
        startDate = findViewById(R.id.track_detailed_startdate_tv);
        startTime = findViewById(R.id.track_detailed_starttime_tv);
        distance = findViewById(R.id.track_detailed_distance_tv);
        duration = findViewById(R.id.track_detailed_duration_tv);
        speed = findViewById(R.id.track_detailed_speed_tv);

        startDate.setText(thisTrack.getStartDate());
        startTime.setText(thisTrack.getStartTime());

        distance.setText(Util.getFormattedDistance(thisTrack.getDistance()));
        duration.setText(Util.getFormattedDurationFromMils(thisTrack.getDuration()));
        speed.setText(Util.getFormattedSpeed(thisTrack.getSpeed()));

        if(isGooglePlayAvailable) {
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.track_detailed_map);
            mapFragment.getMapAsync(this);
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

}