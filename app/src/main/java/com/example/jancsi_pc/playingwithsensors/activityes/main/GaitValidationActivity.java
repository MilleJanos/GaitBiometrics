package com.example.jancsi_pc.playingwithsensors.activityes.main;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.R;
import com.example.jancsi_pc.playingwithsensors.stepcounter.StepDetector;
import com.example.jancsi_pc.playingwithsensors.stepcounter.StepListener;
import com.example.jancsi_pc.playingwithsensors.utils.Accelerometer;
import com.example.jancsi_pc.playingwithsensors.utils.Util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class GaitValidationActivity extends AppCompatActivity implements SensorEventListener, StepListener {

    private final String TAG = "GaitValidationActivity";
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEventListener accelerometerEventListener;
    private Button backButton;
    private Button startButton;
    private Button stopButton;
    private Button gaitVerificationButton;
    public static String CMD = "0";
    private boolean isRecording = false;
    private ArrayList<Accelerometer> accArray = new ArrayList<>();
    private long recordCount = 0;
    public static int stepNumber = 0;
    public static final int MAX_STEP_NUMBER = 10;
    public static final int MIN_STEP_NUMBER = 5;
    private TextView textViewStatus;
    private TextView loggedInUserEmailTextView;
    private ImageView logoutImageView;
    private TextView reportErrorTextView;
    private boolean doubleBackToExitPressedOnce = false;
    // For Step Detecting:
    private StepDetector simpleStepDetector;
    // local stored files:
    private File featureDummyFile;  // local stored dummy file from firebase
    private File rawdataUserFile;
    private File featureUserFile;   // only the path exists !
    //private File modelUserFile;     // only the path exists !
    private Date mDate;
    // from shared pref;
    String offlineLastModelEmail;
    String offlineLastModelId;
    String offlineLastModelDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, ">>>RUN>>>onCreate()");
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gait_validation);

        Util.addToDebugActivityStackList(TAG);

        //
        // Reading from Shared preferences
        //


        Log.i(TAG, "offlineLastModelEmail: " + offlineLastModelEmail);
        Log.i(TAG, "offlineLastModelId: " + offlineLastModelId);
        Log.i(TAG, "offlineLastModelDate: " + offlineLastModelDate);

        //
        // Internal files Path:
        //
        mDate = new Date();

        // Create folder if not exists:
        File myInternalFilesRoot;

        myInternalFilesRoot = new File(Util.internalFilesRoot.getAbsolutePath() /*+ customDIR*/);
        if (!myInternalFilesRoot.exists()) {
            myInternalFilesRoot.mkdirs();
            Log.i(TAG, "Path not exists (" + myInternalFilesRoot.getAbsolutePath() + ") --> .mkdirs()");
        }


        //region
        /*
            storing:
                INTERNAL STORAGE            FIREBASE STORAGE
                feature_dummy.arff          -
                deature_<userId>.arff       deature_<userId>_<date>_<time>.arff
                gaitverif_<userId>.mdl          gaitverif_<userId>_<date>_<time>.mdl
                rawdata_<userId>.csv        rawdata_<userId>_<date>_<time>.csv
        */
        //endregion
        Util.feature_dummy_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_dummy.arff";
        Util.rawdata_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/rawdata_" + offlineLastModelId + ".csv";
        Util.feature_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_" + offlineLastModelId + ".arff";  // The date and time will be added before uploading the files
        //Util.model_user_path    = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/gaitverif_"   + mAuth.getUid() + ".mdl";
        //region Print this 4 paths
        Log.i(TAG, "PATH: Util.feature_dummy_path = " + Util.feature_dummy_path);
        Log.i(TAG, "PATH: Util.rawdata_user_path  = " + Util.rawdata_user_path);
        Log.i(TAG, "PATH: Util.feature_user_path  = " + Util.feature_user_path);
        //Log.i(TAG,"PATH: Util.model_user_path    = " + Util.model_user_path);
        //endregion

        // internal files as File type:
        featureDummyFile = new File(Util.feature_dummy_path);
        rawdataUserFile = new File(Util.rawdata_user_path);
        featureUserFile = new File(Util.feature_user_path);
        //modelUserFile    = new File( Util.model_user_path );


        if (!featureDummyFile.exists()) {
            Log.e(TAG, "File is missing: " + Util.feature_dummy_path);
        }
        if (!rawdataUserFile.exists()) {
            Log.e(TAG, "File is missing: " + Util.rawdata_user_path);
        }
        if (!featureUserFile.exists()) {
            Log.e(TAG, "File is missing: " + Util.feature_user_path);
        }
        /*
        if(!modelUserFile.exists()){
            Log.e(TAG,"File is missing: " + Util.model_user_path);
        }
        */


        // SENSOR:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometerSensor == null) {
            Toast.makeText(this, "The device has no Accelerometer !", Toast.LENGTH_SHORT).show();
            finish();
        }

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        textViewStatus = findViewById(R.id.gaitverif_textViewStatus);
        textViewStatus.setText(R.string.startRecording);

        startButton = findViewById(R.id.gaitverif_buttonStart);
        stopButton = findViewById(R.id.gaitverif_buttonStop);
        gaitVerificationButton = findViewById(R.id.gaitverif_verifyButton);
        stopButton.setEnabled(false);

        final DecimalFormat df = new DecimalFormat("0");
        df.setMaximumIntegerDigits(20);
        // 123...45E9 -> 123...459234
        //         ==            ====

        //Step Detecting:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        reportErrorTextView = findViewById(R.id.errorReportTextView);
        reportErrorTextView.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>reportErrorTextViewClickListener");
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "abc@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Problem with authentication.");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, "");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        });

        accelerometerEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //if(stepNumber>MAX_STEP_NUMBER){ //only N steps allowed
                //stopButton.callOnClick();
                //}
                //if(stepNumber>MIN_STEP_NUMBER && !stopButton.isEnabled()){ //at least M steps
                //stopButton.setEnabled(true);
                //}
                //long timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                long timeStamp = event.timestamp;
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                //queueing
                //keeping the queue size fixed

                if (isRecording) {
                    accArray.add(new Accelerometer(timeStamp, x, y, z, stepNumber));
                    recordCount++;
                    textViewStatus.setText(("Recording: " + stepNumber + " steps made."));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        /*
         *
         *   Start recording
         *
         */

        startButton.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>startButtonClickListener");
            //mediaPlayer.create(null,R.raw.start);
            //mediaPlayer.start();
            recordCount = 0;
            stepNumber = 0;
            sensorManager.registerListener(GaitValidationActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
            accArray.clear();
            isRecording = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            Log.d("ConnectionActivity_", "Start Rec.");
            //textViewStatus.setText("Recording ...");
        });

        /*
         *
         *   Stop recording
         *
         */

        stopButton.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>stopButtonClickListener");
            Util.recordDateAndTimeFormatted = DateFormat.format("yyyyMMdd_HHmmss", mDate.getTime());
            isRecording = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            gaitVerificationButton.setEnabled(true);
            sensorManager.unregisterListener(GaitValidationActivity.this);
            Log.d(TAG, "Stop Rec. - Generating CMD");
            textViewStatus.setText(R.string.calculating);
            CMD = accArrayToString();
            CMD += ",end";
            Log.d(TAG, "CMD Generated.");
            textViewStatus.setText(("Recorded: " + recordCount + " datapoints and " + stepNumber + " step cycles."));
        });

        /*
         *
         *   Sending to FireBase
         *   from Start to End
         *
         */

        gaitVerificationButton.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>gaitVerificationButtonClickListener");
        });


    }

    //region HELP
    /*
        accArrayToString()
            | ArrayList<Accelerometer> accArray ==> String str
            |
            | output format:   "timestamp,x,y,z,currentStepCount,timestamp,x,y,z,currentStepCount,timestamp,x,y,z,timestamp,currentStepCount, ... ,end"
     */
    //endregion
    public String accArrayToString() {
        Log.d(TAG, ">>>RUN>>>accArrayToString()");
        StringBuilder sb = new StringBuilder();
        int i;
        for (i = 0; i < accArray.size() - 1; ++i) {
            sb.append(accArray.get(i).getTimeStamp())
                    .append(",")
                    .append(accArray.get(i).getX())
                    .append(",")
                    .append(accArray.get(i).getY())
                    .append(",")
                    .append(accArray.get(i).getZ())
                    .append(",")
                    .append(stepNumber)
                    .append(",");
        }
        sb.append(accArray.get(i).getTimeStamp())
                .append(",")
                .append(accArray.get(i).getX())
                .append(",")
                .append(accArray.get(i).getY())
                .append(",")
                .append(accArray.get(i).getZ())
                .append(",")
                .append(stepNumber);
        //.append(",");
        return sb.toString();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, ">>>RUN>>>onPause()");
        super.onPause();
        sensorManager.unregisterListener(accelerometerEventListener);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, ">>>RUN>>>onStart()");
        super.onStart();

        Util.mSharedPref = getSharedPreferences(Util.sharedPrefFile, MODE_PRIVATE);

        //if (savedInstanceState != null){
        offlineLastModelEmail = Util.mSharedPref.getString(Util.LAST_MODEL_EMAIL_KEY, "");
        offlineLastModelId = Util.mSharedPref.getString(Util.LAST_MODEL_ID_KEY, "");
        offlineLastModelDate = Util.mSharedPref.getString(Util.LAST_MODEL_ID_KEY, "");
        //}
    }

    @Override
    public void onDestroy(){
        Util.removeFromDebugActivityStackList(TAG);
        super.onDestroy();
    }

    //STEPCOUNTER
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        Log.d(TAG, ">>>RUN>>>step()");
        //mp.start();
        ModelUploaderActivity.stepNumber++;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            Util.isFinished = true;
            finish();
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

}
