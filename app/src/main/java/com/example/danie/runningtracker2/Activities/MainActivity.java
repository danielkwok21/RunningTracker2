package com.example.danie.runningtracker2.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.example.danie.runningtracker2.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ImageView startTrackingIv;
    static Button startTracking;
    private ImageView viewTracksIv;
    private Button viewTracks;

    private ServiceStatusReceiver statusReceiver;
    private IntentFilter filter;
    private boolean isReceiverRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statusReceiver = new ServiceStatusReceiver();
        filter = new IntentFilter();
        filter.addAction(Tracking.GET_TIME);
        filter.addAction(Tracking.SERVICE_ENDED);

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
    
    @Override
    protected void onPause() {
        super.onPause();
        if(isReceiverRegistered){
            unregisterReceiver(statusReceiver);
            isReceiverRegistered = false;
        }
        Log.d(TAG, "onPause: ");
    }

    /**
     * Unregisters receiver whenever activity created, or device rotated
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(!isReceiverRegistered){
            registerReceiver(statusReceiver, filter);
            isReceiverRegistered = true;
        }
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }
    
    public class ServiceStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                //if location service is running
                case Tracking.GET_TIME:
                    startTracking.setText(context.getString(R.string.resume_track));
                    break;
                //if location service is not running
                case Tracking.SERVICE_ENDED:
                    startTracking.setText(context.getString(R.string.new_track));
                    break;
                default:
                    break;
            }

        }
    }
}