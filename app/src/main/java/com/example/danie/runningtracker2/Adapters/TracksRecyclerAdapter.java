package com.example.danie.runningtracker2.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.danie.runningtracker2.ContentProviders.TracksProvider;
import com.example.danie.runningtracker2.Track;
import com.example.danie.runningtracker2.R;
import com.example.danie.runningtracker2.Activities.ViewTrackDetailed;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.List;

public class TracksRecyclerAdapter extends RecyclerView.Adapter<TracksRecyclerAdapter.TrackViewHolder>{
    private static final String TAG = "TracksRecyclerAdapter";
    private List<Track> tracks;

    public TracksRecyclerAdapter(List<Track> tracks){
        this.tracks = tracks;
    }

    public void updateData(List<Track> newTracks){
        tracks.clear();
        tracks.addAll(newTracks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        //inflate layout
        View view = layoutInflater.inflate(R.layout.recycler_view_layout, viewGroup, false);

        return new TrackViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder trackViewHolder, int i) {
        trackViewHolder.tv.setText(tracks.get(i).getName());
        trackViewHolder.thisTrack = tracks.get(i);
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public static class TrackViewHolder extends RecyclerView.ViewHolder{

        TextView tv;
        Track thisTrack;
        Gson gson = new Gson();

        TrackViewHolder(Context c, View v) {
            super(v);
            tv = v.findViewById(R.id.recyclerview_log_name);
            tv.setOnClickListener((tv)->{
                Intent i = new Intent(c, ViewTrackDetailed.class);
                String jsonObject = gson.toJson(thisTrack);

                i.putExtra(TracksProvider.JSON_OBJECT, jsonObject);
                c.startActivity(i);
            });
        }
    }
}