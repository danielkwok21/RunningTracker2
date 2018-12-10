package com.example.danie.runningtracker2;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Util {
    public static void Toast(Context c, String s){
        Toast toast = Toast.makeText(c, s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 600);
        toast.show();
    }

}