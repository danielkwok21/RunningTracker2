package com.example.danie.runningtracker2.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.danie.runningtracker2.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Button startTracking;
    Button viewLogs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
    }

    private void initComponents(){
        startTracking = findViewById(R.id.main_start_track_btn);
        viewLogs = findViewById(R.id.main_logs_btn);

        startTracking.setOnClickListener((v)->{
            Intent i = new Intent(this, Tracking.class);
            startActivity(i);
        });

        viewLogs.setOnClickListener((v)->{
            Intent i = new Intent(this, ViewTracks.class);
            startActivity(i);
        });

    }
}