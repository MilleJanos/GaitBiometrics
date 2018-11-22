package com.example.jancsi_pc.playingwithsensors.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.jancsi_pc.playingwithsensors.Accelerometer;
import com.example.jancsi_pc.playingwithsensors.DataCollectorActivity;

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

    // logged in user
    public static String userEmail = "";
    public static Boolean isSignedIn = false;

    // login/register
    public enum ScreenModeEnum { EMAIL_MODE, PASSWORD_MODE, REGISTER_MODE }
    public static ScreenModeEnum screenMode;

    // show errors for user
    public static String intoTextViewString = "";

    // used to finish all activities
    public static boolean isFinished = false;





}
