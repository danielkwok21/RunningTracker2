package com.example.danie.runningtracker2.Activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;

import com.example.danie.runningtracker2.Adapters.TracksRecyclerAdapter;
import com.example.danie.runningtracker2.ContentProviders.TracksProvider;
import com.example.danie.runningtracker2.R;
import com.example.danie.runningtracker2.Track;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ViewTracks extends AppCompatActivity {
    private static final String TAG = "ViewTracks";

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    TracksRecyclerAdapter tracksRecyclerAdapter;
    ContentResolver contentResolver;

    Button distanceToday;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tracks);

        contentResolver = getApplicationContext().getContentResolver();

        initRecyclerView(getTracksFromProvider());
        initComponents();
    }

    private void initComponents(){
        distanceToday = findViewById(R.id.tracks_view_today_stats);

        distanceToday.setOnClickListener((v)->{
            Intent i = new Intent(this, TodayStats.class);
            startActivity(i);
        });
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
                Log.d(TAG, "getTracksFromProvider: No tracks");
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

    private void initRecyclerView(List<Track> tracks){
        recyclerView = findViewById(R.id.tracks_view_tracks_rv);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        tracksRecyclerAdapter = new TracksRecyclerAdapter(tracks);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(tracksRecyclerAdapter);
    }
}