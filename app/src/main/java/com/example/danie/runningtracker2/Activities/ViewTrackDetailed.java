package com.example.danie.runningtracker2.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.danie.runningtracker2.Adapters.TracksRecyclerAdapter;
import com.example.danie.runningtracker2.ContentProviders.TracksProvider;
import com.example.danie.runningtracker2.Track;
import com.example.danie.runningtracker2.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.gson.Gson;

public class ViewTrackDetailed extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "ViewTrackDetailed";

    TextView startDate;
    TextView endDate;
    TextView startTime;
    TextView endTime;
    TextView distance;
    TextView duration;
    GoogleMap mMap;
    SupportMapFragment mapFragment;

    Track track;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_track_detailed);

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
        if(!track.getLatlngs().isEmpty()){
            //set starting & endingpoint
            LatLng start = new LatLng(track.getStartLocationLat(), track.getStartLocationLong());
            mMap.addMarker( new MarkerOptions().position(start));
            LatLng end = new LatLng(track.getEndLocationLat(), track.getEndLocationLong());
            mMap.addMarker( new MarkerOptions().position(end));

            //setting zoom to fit all markers
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(start).include(end);
            final int width = getResources().getDisplayMetrics().widthPixels;
            final int height = getResources().getDisplayMetrics().heightPixels;
            final int padding = (int)(width*0.12);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding));


            //populating latlngs list to draw route
            Polyline route = mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .addAll(track.getLatlngs()));

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

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.track_detailed_map);
        mapFragment.getMapAsync(this);
    }
}