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

import java.util.ArrayList;
import java.util.List;

public class ViewTracks extends AppCompatActivity {
    private static final String TAG = "ViewTracks";

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    TracksRecyclerAdapter tracksRecyclerAdapter;
    ContentResolver contentResolver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tracks);

        contentResolver = getApplicationContext().getContentResolver();

        initRecyclerView(getTracksFromProvider());
    }


    private List<Track> getTracksFromProvider(){
        Log.d(TAG, "getTracksFromProvider: ");

        Uri uri = TracksProvider.CONTENT_URI;

        Cursor c = contentResolver.query(uri, null, null, null, TracksProvider.ID);
        Track track;
        List<Track> tracks = new ArrayList<>();

        if(c!=null){
            if(c.moveToFirst()){
                do{
                    String startLocation = c.getString(c.getColumnIndex(TracksProvider.START_LOCATION));
                    String endLocation = c.getString(c.getColumnIndex(TracksProvider.END_LOCATION));
                    Double distance = c.getDouble(c.getColumnIndex(TracksProvider.DISTANCE));
                    String unit = c.getString(c.getColumnIndex(TracksProvider.UNIT));
                    long duration = c.getLong(c.getColumnIndex(TracksProvider.DURATION));

                    track = new Track(startLocation, endLocation, distance, unit, duration);
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