package com.example.danie.runningtracker2;

import android.content.Context;
import android.widget.Toast;

public class Util {
    public static void Toast(Context c, String s){
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
    }
}