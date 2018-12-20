package com.example.danie.runningtracker2;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Class to store all misc functions
 */
public class Util {

    public static void setToast(Context c, String s){
        Toast toast = Toast.makeText(c, s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 600);
        toast.show();
    }

    public static String getFormattedDistance(double distance){
        final String METER = "m";
        final String KILOMETER = "km";
        String unit = METER;

        //converts to km if distance is above 1000m
        if(distance>1000){
            distance = distance/1000;
            unit = KILOMETER;
        }
        return String.format("%.3f", distance)+unit;
    }

    public static String getFormattedDurationFromMils(long duration){
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
        return hms;
    }

    public static String getFormattedSpeed(double speed){
        if(speed!=-1){
            return String.format("%.3f", speed*=3.6)+"km/h";
        }else{
            return "∞ "+"km/h";
        }
    }

    public static String getFormattedTimeFromDate(Date date){
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        return timeFormat.format(date);
    }

    public static String getFormattedDateFromDate(Date date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        return dateFormat.format(date);
    }
}