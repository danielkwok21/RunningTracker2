package com.example.danie.runningtracker2.Activities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.danie.runningtracker2.ContentProviders.TracksProvider;
import com.example.danie.runningtracker2.R;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class Stats extends AppCompatActivity  implements OnMapReadyCallback {
    private static final String TAG = "Stats";
    private static final String TODAY = "today";
    private static final String TOMONTH = "toMonth";

    private Button todayFilter;
    private Button tomonthFilter;
    private TextView totalDistance;
    private TextView aveDistance;
    private TextView bestDate;
    private TextView bestDistance;
    private TextView bestTime;
    private TextView bestSpeed;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private ContentResolver contentResolver;

    private Gson gson = new Gson();
    private Track bestTrack;
    private boolean isGooglePlayAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isGooglePlayAvailable = googlePlayAvailable();
        if(isGooglePlayAvailable){
            setContentView(R.layout.activity_stats);
        }else{
            setContentView(R.layout.activity_stats2);
        }
        contentResolver = getApplicationContext().getContentResolver();

        initComponents();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(bestTrack!=null){
            if(!bestTrack.getLatLngs().isEmpty()){
                //set starting & endingpoint
                LatLng start = bestTrack.getLatLngs().get(0);
                mMap.addMarker( new MarkerOptions().position(start));

                LatLng end = bestTrack.getLatLngs().get(bestTrack.getLatLngs().size()-1);
                mMap.addMarker( new MarkerOptions().position(end));

                //setting zoom to fit all markers
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for(LatLng l:bestTrack.getLatLngs()){
                    builder.include(l);
                }
                final int width = getResources().getDisplayMetrics().widthPixels;
                final int height = (int)Math.round(getResources().getDisplayMetrics().heightPixels*0.6);
                final int padding = (int)(width*0.12);
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding));

                //populating latlngs list to draw route
                Polyline route = mMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .addAll(bestTrack.getLatLngs()));

                route.setEndCap(new RoundCap());
                route.setWidth(10);
                route.setColor(getResources().getColor(R.color.colorAccent));
                route.setJointType(JointType.ROUND);
            }
        }
    }

    private void initComponents(){
        todayFilter = findViewById(R.id.stats_today_filter_btn);
        tomonthFilter = findViewById(R.id.stats_tomonth_filter_btn);
        totalDistance = findViewById(R.id.stats_distance_tv);
        aveDistance = findViewById(R.id.tats_ave_distance_tv);
        bestDate = findViewById(R.id.stats_best_track_date_tv);
        bestDistance = findViewById(R.id.stats_best_distance_tv);
        bestTime = findViewById(R.id.stats_best_time_tv);
        bestSpeed = findViewById(R.id.stats_best_speed_tv);

        if(isGooglePlayAvailable){
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.track_detailed_map);
            mapFragment.getMapAsync(this);
        }

        todayFilter.setOnClickListener((v)->{
            todayFilter.setEnabled(false);
            tomonthFilter.setEnabled(true);
            List<Track> filteredTracks = getFilterTracks(getTracksFromProvider(), TODAY);
            if(!filteredTracks.isEmpty()){
                refreshStats(filteredTracks);
            }else{
                Util.setToast(this, getApplicationContext().getString(R.string.no_track_today));
            }
        });

        tomonthFilter.setOnClickListener((v)->{
            todayFilter.setEnabled(true);
            tomonthFilter.setEnabled(false);
            List<Track> filteredTracks = getFilterTracks(getTracksFromProvider(), TOMONTH);
            if(!filteredTracks.isEmpty()){
                refreshStats(filteredTracks);
            }else{
                Util.setToast(this, getApplicationContext().getString(R.string.no_track_tomonth));
            }
        });

        todayFilter.performClick();
    }

    private void refreshStats(List<Track> newTracks){
        totalDistance.setText(Util.getFormattedDistance(getTotalDistance(newTracks)));
        aveDistance.setText(Util.getFormattedDistance(getAveDistance(newTracks)));

        bestTrack = getBestTrack(newTracks);

        bestDate.setText(getApplicationContext().getString(R.string.best_track_sentence)+" "+bestTrack.getStartDate()+" "+getApplicationContext().getString(R.string.at)+" "+bestTrack.getStartTime());
        bestDistance.setText(Util.getFormattedDistance(bestTrack.getDistance()));
        bestTime.setText(Util.getFormattedDurationFromMils(bestTrack.getDuration()));
        bestSpeed.setText(Util.getFormattedSpeed(bestTrack.getSpeed()));
    }

    private double getTotalDistance(List<Track> tracks){
        double totalDistance = 0;
        for(Track track:tracks){
            totalDistance+=track.getDistance();
        }
        return totalDistance;
    }

    private double getAveDistance(List<Track> tracks){
        return tracks.size()!=0? getTotalDistance(tracks)/tracks.size():-1;
    }

    private Track getBestTrack(List<Track> tracks){
        Track bestTrack = tracks.get(0);
        for(Track track:tracks){
            if(track.getSpeed()>bestTrack.getSpeed()){
                bestTrack = track;
            }
        }
        return bestTrack;
    }

    private List<Track> getFilterTracks(List<Track> tracks, String filter){
        List<Track> filteredTracks = new ArrayList<>();

        switch(filter){
            case TODAY:
                for(Track track:tracks){
                    if(track.getStartNow().get(Calendar.DAY_OF_YEAR)==Calendar.getInstance().get(Calendar.DAY_OF_YEAR)){
                        filteredTracks.add(track);
                    }
                }
                break;
            case TOMONTH:
                for(Track track:tracks){
                    if(track.getStartNow().get(Calendar.MONTH)==Calendar.getInstance().get(Calendar.MONTH)){
                        filteredTracks.add(track);
                    }
                }
                break;
            default:
                Log.d(TAG, "getFilterTracks: Error. Wrong filter");
        }

        return filteredTracks;
    }

    private List<Track> getTracksFromProvider(){
        Uri uri = TracksProvider.CONTENT_URL;
        Cursor c = contentResolver.query(uri, null, null, null, TracksProvider.ID);
        Track track;
        List<Track> tracks = new ArrayList<>();

        if(c!=null){
            if(c.moveToFirst()){
                do{
                    try{
                        String json = c.getString(c.getColumnIndex(TracksProvider.JSON_OBJECT));
                        track = gson.fromJson(json, Track.class);
                        tracks.add(track);
                    }catch(Exception e){
                        Log.d(TAG, "getTracksFromProvider: "+e);
                    }
                }while(c.moveToNext());
            }else{
                Log.d(TAG, "getTracksFromProvider: No todayTracks");
            }
        }else{
            Log.d(TAG, "getTracksFromProvider: cursor is null");
        }
        Log.d(TAG, "getTracksFromProvider: Track size:"+tracks.size());

        if(c!=null){
            c.close();
        }

        Collections.reverse(tracks);
        return tracks;
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
