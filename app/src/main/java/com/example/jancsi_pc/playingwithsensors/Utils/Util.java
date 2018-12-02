package com.example.jancsi_pc.playingwithsensors.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.jancsi_pc.playingwithsensors.Accelerometer;
import com.example.jancsi_pc.playingwithsensors.AuthenticationActivity;
import com.example.jancsi_pc.playingwithsensors.DataCollectorActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    public static String deviceId = "";

    // login/register
    public enum ScreenModeEnum { EMAIL_MODE, PASSWORD_MODE, REGISTER_MODE }
    public static ScreenModeEnum screenMode;

    // show errors for user
    public static String intoTextViewString = "";

    // used to finish all activities
    public static boolean isFinished = false;

    // user model
    public static boolean hasUserModel = false;
    public static boolean isSetUserModel = false;

    public static void hideKeyboard(Activity activity){
        // If keyboard is shown then hide:
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(AuthenticationActivity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it.
        View activityOnFocusView = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (activityOnFocusView == null) {
            activityOnFocusView = new View(activity);
        }
        imm.hideSoftInputFromWindow(activityOnFocusView.getWindowToken(), 0);
    }

    // Cloud
    public static FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public static StorageReference mRef;
    public static FirebaseStorage mStorage = FirebaseStorage.getInstance();
}
