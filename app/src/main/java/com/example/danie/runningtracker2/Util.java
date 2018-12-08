package com.example.danie.runningtracker2;

import android.content.Context;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Util {
    public static void Toast(Context c, String s){
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
    }


}