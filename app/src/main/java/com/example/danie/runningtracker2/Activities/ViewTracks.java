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
import android.view.View;
import android.widget.Button;

import com.example.danie.runningtracker2.Adapters.TracksRecyclerAdapter;
import com.example.danie.runningtracker2.ContentProviders.TracksProvider;
import com.example.danie.runningtracker2.R;
import com.example.danie.runningtracker2.Track;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.View.VISIBLE;

public class ViewTracks extends AppCompatActivity {
    private static final String TAG = "ViewTracks";

    private ContentResolver contentResolver;

    private Button stats;
    private Gson gson = new Gson();
    private List<Track> tracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tracks);

        contentResolver = getApplicationContext().getContentResolver();

        tracks = getTracksFromProvider();
        initRecyclerView(tracks);
        initComponents();
    }

    private void initComponents(){
        stats = findViewById(R.id.tracks_view_today_stats);
        if(!tracks.isEmpty()){
            stats.setVisibility(VISIBLE);
            stats.setOnClickListener((v)->{
                Intent i = new Intent(this, Stats.class);
                startActivity(i);
            });
        }else{
            stats.setVisibility(View.INVISIBLE);
        }

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

        Collections.reverse(tracks);
        return tracks;
    }

    private void initRecyclerView(List<Track> tracks){
        RecyclerView recyclerView = findViewById(R.id.tracks_view_rv);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        TracksRecyclerAdapter tracksRecyclerAdapter = new TracksRecyclerAdapter(tracks);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(tracksRecyclerAdapter);
    }
}