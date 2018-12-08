package com.example.danie.runningtracker2.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.danie.runningtracker2.Adapters.TracksRecyclerAdapter;
import com.example.danie.runningtracker2.Track;
import com.example.danie.runningtracker2.R;
import com.example.danie.runningtracker2.Util;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;

public class ViewTrackDetailed extends AppCompatActivity {
    private static final String TAG = "ViewTrackDetailed";

    TextView startDate;
    TextView endDate;
    TextView startTime;
    TextView endTime;
    TextView distance;
    TextView duration;

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
                jsonObject = extras.getString(TracksRecyclerAdapter.JSON_OBJECT);
                track = gson.fromJson(jsonObject, Track.class);
            }
        }

        initComponent();
    }


    private void initComponent(){
        startDate = findViewById(R.id.log_detailed_startdate_tv);
        endDate = findViewById(R.id.log_detailed_enddate_tv);
        startTime = findViewById(R.id.log_detailed_starttime_tv);
        endTime = findViewById(R.id.log_detailed_endtime_tv);
        distance = findViewById(R.id.log_detailed_distance);
        duration = findViewById(R.id.log_detailed_duration);

        startDate.setText(track.getStartDate());
        startTime.setText(track.getStartTime());
        endDate.setText(track.getEndDate());
        endTime.setText(track.getEndTime());

        distance.setText(track.getFormattedDistance());
        duration.setText(track.getFormattedDuration());
    }
}