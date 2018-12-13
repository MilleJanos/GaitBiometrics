package com.example.jancsi_pc.playingwithsensors.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.AuthenticationActivity;
import com.example.jancsi_pc.playingwithsensors.DataCollectorActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.UUID;

public class Util {

    //public static Util Util = new Util();

    // Singleton
    private Util(){

    }

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

    // stored internal files location
    public static File internalFilesRoot;

    // internal stored Paths
    public static String feature_dummy_path = "";
    public static String rawdata_user_path = "";
    public static String feature_user_path = "";
    public static String model_user_path = "";

    public static boolean rawDataHasHeader = false;
    public static String rawDataHeaderStr = "timestamp,accx,accy,accz,stepnum";

    // util for internal paths: date & dirName
    public static CharSequence recordDateAndTimeFormatted;
    public static String customDIR = "";

    // download this dummy for generating
    public static String firebaseDumyFileName = "features_rRHyStiEKkN4Cq5rVSxlpvrCwA72.arff";   // TODO: hardcoded dummy (name) from firebase (!?)

    // show errors for user
    public static String intoTextViewString = "";

    // used to finish all activities:
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

    // Shared Preferences

    public static String sharedPrefFile = "sharedPref";  // "com.example/jancsi_pc.playingwithsensors";
    public static SharedPreferences mPreferences ;// DataCollectorAct. onStart: getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
    public static SharedPreferences.Editor preferencesEditor;
    public static final String LAST_LOGGED_IN_EMAIL_KEY = "lastloggedinemail";
    public static final String LAST_LOGGED_IN_ID_KEY = "lastloggedinid";
    public static final String LAST_LOGGED_IN_DATE_KEY = "lastloggedindate";
    public static final String LAST_MODEL_EMAIL_KEY = "lastmodelemailkey";
    public static final String LAST_MODEL_ID_KEY = "lastmodelidkey";
    public static final String LAST_MODEL_DATE_KEY = "lastmodeldatekey";


    public static final int REQUEST_CODE = 212;


    //region HELP
    /*
            | This method saves the accArray<Accelerometer> list
            | into file including header.
            | Return value:
            |   0 - No error
            |   1 - error
    */
    //endregion
    public static short SaveAccArrayIntoCsvFile(ArrayList<Accelerometer> accArray, File file){
        String TAG = "Util";
        Log.d(TAG,">>>RUN>>>savingAccArrayIntoCSV()");

        if( ! file.exists() ){
            try {
                file.createNewFile();
            }catch (IOException e){
                Log.e(TAG, "IOException: file.createNewFile()");
                e.printStackTrace();
                return 1;
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);

            // Header:
            if(Util.rawDataHasHeader) {
                pw.println(Util.rawDataHeaderStr);
            }

            for( Accelerometer a : accArray){
                pw.println( a.toString() );
            }
            pw.flush();
            pw.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "******* File not found.");
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        Log.d(TAG,"<<<FINISH<<<savingAccArrayIntoCSV()");
        return 0;
    }


}