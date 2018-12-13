package com.example.danie.runningtracker2.Activities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.danie.runningtracker2.ContentProviders.TracksProvider;
import com.example.danie.runningtracker2.R;
import com.example.danie.runningtracker2.Track;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TodayStats extends AppCompatActivity {
    private static final String TAG = "TodayStats";
    private static final String TODAY = "today";
    private static final String TOMONTH = "toMonth";

    private TextView totalDistance;
    private TextView aveDistance;
    private TextView bestDistance;
    private TextView bestTime;
    private TextView bestSpeed;

    private ContentResolver contentResolver;
    private Gson gson = new Gson();
    private List<Track> filteredTracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_stats);
        contentResolver = getApplicationContext().getContentResolver();

        filteredTracks = filterTracks(getTracksFromProvider(), TODAY);
        initComponents();
    }

    private void initComponents(){
        totalDistance = findViewById(R.id.today_stats_distance_tv);
        aveDistance = findViewById(R.id.today_stats_ave_distance_tv);
        bestDistance = findViewById(R.id.today_stats_best_distance_tv);
        bestTime = findViewById(R.id.today_stats_best_time_tv);
        bestSpeed = findViewById(R.id.today_stats_best_speed_tv);

        totalDistance.setText("Total distance: "+formatDistance(totalDistance()));
        aveDistance.setText("Ave distance: "+formatDistance(aveDistance()));
        bestDistance.setText("Best distance: "+formatDistance())

    }

    private String formatDistance(double distance){
        final String METER = "m";
        final String KILOMETER = "km";
        String unit = METER;
        //converts to km if distance is above 1000m
        if(distance>1000){
            distance = distance/1000;
            unit = KILOMETER;
        }
        return String.format("%.2f", distance)+unit;
    }

    private double totalDistance(){
        double totalDistance = 0;
        for(Track track:filteredTracks){
            totalDistance+=track.getDistance();
        }
        return totalDistance;
    }

    private double aveDistance(){
        return filteredTracks.size()!=0?totalDistance()/filteredTracks.size():-1;
    }

    /*
    * create best speed function
    * */


    private List<Track> filterTracks(List<Track> tracks, String filter){
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
                Log.d(TAG, "filterTracks: Error. Wrong filter");
        }

        return  filteredTracks;
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

        return tracks;
    }

}
