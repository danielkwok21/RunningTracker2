package com.example.danie.runningtracker2.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.example.danie.runningtracker2.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ImageView startTrackingIv;
    static Button startTracking;
    private ImageView viewTracksIv;
    private Button viewTracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponents();
    }

    private void initComponents(){
        startTrackingIv = findViewById(R.id.main_start_track_iv);
        startTracking = findViewById(R.id.main_start_track_btn);
        viewTracksIv = findViewById(R.id.main_start_track_iv);
        viewTracks = findViewById(R.id.main_view_tracks_btn);

        startTrackingIv.setOnClickListener((v)->{
            Intent i = new Intent(this, Tracking.class);
            startActivity(i);
        });
        startTracking.setOnClickListener((v)->{
            Intent i = new Intent(this, Tracking.class);
            startActivity(i);
        });

        viewTracksIv.setOnClickListener((v)->{
            Intent i = new Intent(this, ViewTracks.class);
            startActivity(i);
        });
        viewTracks.setOnClickListener((v)->{
            Intent i = new Intent(this, ViewTracks.class);
            startActivity(i);
        });
    }
}