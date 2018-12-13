package com.example.danie.runningtracker2.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.danie.runningtracker2.ContentProviders.TracksProvider;
import com.example.danie.runningtracker2.Track;
import com.example.danie.runningtracker2.R;
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
    private TextView endDate;
    private TextView startTime;
    private TextView endTime;
    private TextView distance;
    private TextView duration;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private Track track;
    private Gson gson;

    private boolean googlePlayAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        googlePlayAvailable = googlePlayAvailable();

        super.onCreate(savedInstanceState);

        if(googlePlayAvailable){
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
                track = gson.fromJson(jsonObject, Track.class);
            }
        }

        initComponent();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(!track.getLatLngs().isEmpty()){
            //set starting & endingpoint
            LatLng start = track.getStartLatLng();
            mMap.addMarker( new MarkerOptions().position(start));
            LatLng end = track.getEndLatLng();
            mMap.addMarker( new MarkerOptions().position(end));

            //setting zoom to fit all markers
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(LatLng l:track.getLatLngs()){
                builder.include(l);
            }
            final int width = getResources().getDisplayMetrics().widthPixels;
            final int height = (int)Math.round(getResources().getDisplayMetrics().heightPixels*0.6);
            final int padding = (int)(width*0.12);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding));


            //populating latlngs list to draw route
            Polyline route = mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .addAll(track.getLatLngs()));

            route.setEndCap(new RoundCap());
            route.setWidth(10);
            route.setColor(getResources().getColor(R.color.colorAccent));
            route.setJointType(JointType.ROUND);
        }
    }

    private void initComponent(){
        startDate = findViewById(R.id.track_detailed_startdate_tv);
        endDate = findViewById(R.id.track_detailed_enddate_tv);
        startTime = findViewById(R.id.track_detailed_starttime_tv);
        endTime = findViewById(R.id.track_detailed_endtime_tv);
        distance = findViewById(R.id.track_detailed_distance);
        duration = findViewById(R.id.track_detailed_duration);

        startDate.setText(track.getStartDate());
        startTime.setText(track.getStartTime());
        endDate.setText(track.getEndDate());
        endTime.setText(track.getEndTime());

        distance.setText(track.getFormattedDistance());
        duration.setText(track.getFormattedDuration());

        if(googlePlayAvailable) {
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