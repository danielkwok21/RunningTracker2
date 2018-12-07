package com.example.danie.runningtracker2.Activities;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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

    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tracks);

        contentResolver = getApplicationContext().getContentResolver();

        initRecyclerView(getTracksFromProvider());
    }


    private List<Track> getTracksFromProvider(){
        Log.d(TAG, "getTracksFromProvider: ");
        Uri uri = TracksProvider.CONTENT_URL;
        Cursor c = contentResolver.query(uri, null, null, null, TracksProvider.ID);
        Track track;

        List<Track> tracks = new ArrayList<>();
        gson = new Gson();

        if(c!=null){
            if(c.moveToFirst()){
                do{
                    String json = c.getString(c.getColumnIndex(TracksProvider.JSON_OBJECT));
                    track = gson.fromJson(json, Track.class);
                    tracks.add(track);
                }while(c.moveToNext());
            }else{
                Log.d(TAG, "getTracksFromProvider: No tracks");
            }
        }else{
            Log.d(TAG, "getTracksFromProvider: cursor is null");
        }
        Log.d(TAG, "getTracksFromProvider: Track size:"+tracks.size());

        return tracks;
    }

    private void initRecyclerView(List<Track> tracks){
        recyclerView = findViewById(R.id.logs_view_log_rv);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        tracksRecyclerAdapter = new TracksRecyclerAdapter(tracks);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(tracksRecyclerAdapter);
    }
}