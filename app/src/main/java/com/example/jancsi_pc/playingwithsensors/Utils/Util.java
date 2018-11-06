package com.example.jancsi_pc.playingwithsensors.Utils;

import android.util.Log;

import com.example.jancsi_pc.playingwithsensors.Accelerometer;

import java.util.ArrayList;

public class Util {
    private static final String TAG = "Util";

    public static double samplingFrequency(ArrayList<Accelerometer> data){
        double period = 0;
        for(int i=1;i<data.size();++i){
            period+=data.get(i).getTimeStamp()-data.get(i-1).getTimeStamp();
        }
        period/=(data.size()-1);
        period/=1000000000;
        Log.i(TAG, "samplingFrequency: "+1/period);
        return 1/period;
    }

    public static double samplingFrequency2(ArrayList<Accelerometer> data){
        double period = 0;
        period=data.get(data.size()-1).getTimeStamp()-data.get(0).getTimeStamp();
        period/=(data.size()-1);
        period/=1000000000;
        Log.i(TAG, "samplingFrequency2: "+1/period);
        return 1/period;
    }
}
