package com.example.jancsi_pc.playingwithsensors.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.jancsi_pc.playingwithsensors.R;
import com.example.jancsi_pc.playingwithsensors.activityes.main.AuthenticationActivity;
import com.example.jancsi_pc.playingwithsensors.utils.firebase.UserDataObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ro.sapientia.gaitbiom.GaitHelperFunctions;
import ro.sapientia.gaitbiom.GaitModelBuilder;
import ro.sapientia.gaitbiom.GaitVerification;
import ro.sapientia.gaitbiom.IGaitModelBuilder;
import ro.sapientia.gaitbiom.IGaitVerification;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.SerializationHelper;

/**
 * Utility class containing more often used methods
 */
public class Util {

    // Singleton
    private Util() {

    }

    private static final String TAG = "Util";

    /**
     * Function that calculates the sampling frequency of the accelerometer
     *
     * @param data list containing the Accelerometer data the frequency will be calculated from
     * @return sampling frequency value
     */
    public static double samplingFrequency(List<Accelerometer> data) {
        double period = 0;
        for (int i = 1; i < data.size(); ++i) {
            period += data.get(i).getTimeStamp() - data.get(i - 1).getTimeStamp();
        }
        period /= (data.size() - 1);
        period /= 1000000000;
        Log.i(TAG, "samplingFrequency: " + 1 / period);
        return 1 / period;
    }

    // logged in user
    public static String userEmail = "";
    public static Boolean isSignedIn = false;
    public static String deviceId = "";

    // login/register
    public enum ScreenModeEnum {
        EMAIL_MODE, PASSWORD_MODE, REGISTER_MODE
    }

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
    public static String firebaseDummyFileName = "features_rRHyStiEKkN4Cq5rVSxlpvrCwA72.arff";


    // used to finish all activities:
    public static boolean isFinished = false;

    // user model
    public static boolean hasUserModel = false;
    public static boolean isSetUserModel = false;

    // validation
    public static boolean validatedOnce = false;

    /**
     * Method that hides the keyboard in the given activity
     *
     * @param activity the activity context where the method will hide the keyboard
     */
    public static void hideKeyboard(Activity activity) {
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
    public static FirebaseAuth mAuth;// = FirebaseAuth.getInstance(); //TODO??
    public static StorageReference mRef;
    public static FirebaseStorage mStorage;// = FirebaseStorage.getInstance(); //TODO??

    // Shared Preferences
    public static String sharedPrefFile = "sharedPref";  // "com.example/jancsi_pc.playingwithsensors";
    public static SharedPreferences mSharedPref;// DataCollectorAct. onStart: getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
    public static SharedPreferences.Editor mSharedPrefEditor;
    public static final String LAST_LOGGED_IN_EMAIL_KEY = "lastloggedinemail";
    public static final String LAST_LOGGED_IN_ID_KEY = "lastloggedinid";
    public static final String LAST_LOGGED_IN_DATE_KEY = "lastloggedindate";
    public static final String LAST_MODEL_EMAIL_KEY = "lastmodelemailkey";
    public static final String LAST_MODEL_ID_KEY = "lastmodelidkey";
    public static final String LAST_MODEL_DATE_KEY = "lastmodeldatekey";
    public static final String SETTING_DEBUG_MODE_KEY = "debugmode";
    public static final String LAST_LOGGED_IN_USER_NAME_KEY = "lastloggedinusername";


    // progressDialog
    public static ProgressDialog progressDialog;


    public static final int REQUEST_CODE = 212;

    public static boolean isAdminLoggedIn = false;
    public static boolean debugMode = false;
    public static final List<String> adminList = new ArrayList<String>() {
        {
            add("fuloptimea1427@gmail.com");
            add("margitantal68@gmail.com");
            add("millejanos31@gmail.com");
            add("wolterwill31@gmail.com");
        }
    };

    // Temp variable to help return from Firebase Util function's async part:
    public static UserDataObject mUserDataObject_Temp = new UserDataObject();

    //region HELP

    /**
     * This method saves the accArray<Accelerometer> list into file including header.
     *
     * @param accArray array that contains the data that will be writtem to the file
     * @param file     descriptor of the file all the writing will be made into
     * @return 0 if there is no error
     * 1 if there occurred an error
     */
    //endregion
    public static short saveAccArrayIntoCsvFile(ArrayList<Accelerometer> accArray, File file) {
        String TAG = "Util";
        Log.d(TAG, ">>>RUN>>>savingAccArrayIntoCSV()");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "IOException: file.createNewFile()");
                e.printStackTrace();
                return 1;
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);

            // Header:
            if (Util.rawDataHasHeader) {
                pw.println(Util.rawDataHeaderStr);
            }

            for (Accelerometer a : accArray) {
                pw.println(a.toString());
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
        Log.d(TAG, "<<<FINISH<<<savingAccArrayIntoCSV()");
        return 0;
    }

    /**
     * Runs the classifier for the given parameters and calculates the match percentage of it's result
     *
     * @param activity             the activity context where the method will display progress messages
     * @param userRawDataFilePath  String path of the user's ray_data_file
     * @param userFeatureFilePath  String path of the user's features_file
     * @param dummyFeatureFilePath String path of the dummy's features_file
     * @param userModelFilePath    String path of the user's model_file
     * @param userId               user's ID
     * @return the resulting percentage
     */
    public static double checkUserInPercentage(Activity activity, String userRawDataFilePath, String userFeatureFilePath, String dummyFeatureFilePath, String userModelFilePath, String userId) {

        // region OLD STUFF
        //       ArrayList<Feature> features = null;
        //       try {
        //           features = FeatureExtractor.extractFeaturesFromCsvFileToArrayListOfFeatures(userRawDataFilePath);
        //       } catch (FeatureExtractorException ex) {
        //           //Logger.getLogger(GaitVerification.class.getName()).log(Level.SEVERE, null, ex);
        //           Toast.makeText(activity, "Feature extraction failed!", Toast.LENGTH_LONG).show();
        //       }
        //
        //       FeatureExtractorLibrary.IUtil utility = new FeatureExtractorLibrary.Util();
        //     // Feature -> Instance
        //       Instances instances = null;
        //
        //       try {
        //           //instances = GaitModelBuilder.loadDataset(/*ide add meg a model teljes pathjet*/); //utility.arrayListOfFeaturesToInstances(features);
        //           instances = testLoadDataset(userModelFilePath, activity); //utility.arrayListOfFeaturesToInstances(features);
        //       }
        //       catch (Exception ex){
        //           //Logger.getLogger(GaitVerification.class.getName()).log(Level.SEVERE, null, ex);
        //           Toast.makeText(activity, "Instances failed!", Toast.LENGTH_LONG).show();
        //       }
        //endregion

        double percentage = -1;

        IGaitModelBuilder builder = new GaitModelBuilder();
        Classifier classifier;
        try {
            classifier = (RandomForest) SerializationHelper.read(new FileInputStream(userModelFilePath)); //new RandomForest();
           /* try {
                classifier.buildClassifier(instances);
            } catch (Exception ex) {
                //Logger.getLogger(GaitHelperFunctions.class.getName()).log(Level.SEVERE, null, ex);
                Toast.makeText(activity, "Classifier failed!", Toast.LENGTH_LONG).show();
            }*/

            GaitHelperFunctions.createFeaturesFileFromRawFile(
                    userRawDataFilePath,
                    userFeatureFilePath.substring(0, Util.feature_user_path.length() - (".arff").length()),
                    userId);
            // features_dummy + features_user
            GaitHelperFunctions.mergeEquallyArffFiles(
                    dummyFeatureFilePath,
                    userFeatureFilePath);

            ArrayList<Attribute> attributes = builder.getAttributes(userFeatureFilePath); ///feature (mar letezo)

            IGaitVerification verifier = new GaitVerification();
            percentage = verifier.verifyUser(classifier, attributes, userRawDataFilePath); //user raw data

        } catch (FileNotFoundException e) {
            Log.e(TAG, "*********File not found!");
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "*********Error!");
            e.printStackTrace();
        }

        return percentage;
    }



    /*
     *
     *  Connection Testers:
     *
     */

    // A + B and feedback with Snackbar to the user

    /**
     * Checks if the device has an active network connection.
     *
     * @param activity the activity context where the method will display progress messages
     * @return true the device has an active network connection
     */
    public static boolean requireEnabledInternetAndInternetConnection(Activity activity) {
        Log.d(TAG, ">>>RUN>>>requireEnabledInternetAndInternetConnection()");
        Util.hideKeyboard(activity);

        //Asking the user to enable WiFi:
        boolean isNetworkEnabled = checkWiFiNetwork(activity);

        //Asking for connection:
        boolean isNetworkConnection = requireInternetConnection(activity);

        if (!isNetworkEnabled) {
            //authButton.setError("Please enable internet connection!");
            Log.d("TAG", " isNetworkEnabled = false");
            View mainLayoutView = activity.findViewById(R.id.auth_main_layout);
            Snackbar.make(mainLayoutView, "Please enable internet connection!", Snackbar.LENGTH_SHORT).show();
        } else {
            if (!isNetworkConnection) {
                //authButton.setError("No internet connection detected!");
                Log.d("TAG", " isNetworkConnection = false");
                View view = activity.findViewById(R.id.auth_main_layout);
                Snackbar.make(view, "No internet connection detected!", Snackbar.LENGTH_SHORT).show();
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests the internet connection
     *
     * @param activity
     * @return true if there is internet connection and false if not.
     */
    // B
    public static boolean requireInternetConnection(Activity activity) {
        Log.d(TAG, ">>>RUN>>>requireInternetConnection()");
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        return isConnected;
    }

    /**
     * Tests the WiFi module
     *
     * @param context
     * @return true if the FiWi module is on and false if not.
     */
    // A
    public static boolean checkWiFiNetwork(Context context) {
        Log.d(TAG, ">>>RUN>>>checkWiFiNetwork()");

        final WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (!mWifiManager.isWifiEnabled()) {
            return false;
        }
        // else:
        return true;
    }


}