package com.example.jancsi_pc.playingwithsensors;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.StepCounterPackage.StepDetector;
import com.example.jancsi_pc.playingwithsensors.StepCounterPackage.StepListener;
import com.example.jancsi_pc.playingwithsensors.Utils.Accelerometer;
import com.example.jancsi_pc.playingwithsensors.Utils.FirebaseUtil;
import com.example.jancsi_pc.playingwithsensors.Utils.UserRecordObject;
import com.example.jancsi_pc.playingwithsensors.Utils.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Activity that handles collecting the data sample of the user's walking and ....
 */
public class DataCollectorActivity extends AppCompatActivity implements SensorEventListener, StepListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "DataCollectorActivity";

    private boolean NO_PYTHON_SERVER_YET = true;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEventListener accelerometerEventListener;
    private Button sendToServerButton;
    private Button startButton;
    private Button stopButton;
    private Button saveToFirebaseButton;
    //PORT: 21567                         "<ip>:<port>"
    private String IP_ADDRESS = "192.168.137.90:21456";
    public static String wifiModuleIp = "";
    public static int wifiModulePort = 0;
    public static String CMD = "0";
    private boolean isRecording = false;
    private ArrayList<Accelerometer> accArray = new ArrayList<>();
    private long recordCount = 0;
    private ArrayList<String> accArrayStringGroups = new ArrayList<>();
    private final int RECORDS_PER_PACKAGE_LIMIT = 128;
    public static int stepNumber = 0;
    public static final int MAX_STEP_NUMBER = 10;
    public static final int MIN_STEP_NUMBER = 5;
    private TextView accelerometerTitleTextView;
    private TextView textViewStatus;
    private TextView accelerometerX;
    private TextView accelerometerY;
    private TextView accelerometerZ;
    private TextView goToRegistrationTextView;
    private TextView goToLoginTextView;
    private TextView reportErrorTextView;
    private ImageView pythonServerImageView;
    private TextView navigationMenuUserName;
    private TextView navigationMenuEmail;

    Date mDate;
    private String mFileName;

    //private final MediaPlayer mp = MediaPlayer.create(DataCollectorActivity.this, R.raw.sound2);

    //queue for containing the fixed number of steps that has to be processed
    //TODO

    // For Step Detecting:
    private StepDetector simpleStepDetector;

    // Firebase:
    private FirebaseStorage mFirestore;            // used to upload files
    private StorageReference mStorageReference;  // to storage
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DocumentReference mDocRef; // = FirebaseFirestore.getInstance().document("usersFiles/information");

    // Internal Files:
    private File rawdataUserFile;
    private File featureUserFile;

    // Progress:
    //private ProgressDialog progressDialog;

    // Debug Mode:
    // private Switch debugSwitch;

    // Proxy sensor:
    private SensorManager mSensorManager;
    private Sensor mProximity;
    private static final int SENSOR_SENSITIVITY = 4;

    // WakeLock (for proxy)
    //WAKELOCK//private PowerManager powerManager;
    //WAKELOCK//private PowerManager.WakeLock wakeLock;
    //WAKELOCK//private int field = 0x00000020;

    View attachedLayout;

    /*
     *
     *   OnCreate
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collector_nav);

        findViewByIDs();


        //
        //
        //

        //setSupportActionBar(toolbar);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //
        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //                .setAction("Action", null).show();
        //    }
        //});
        //
        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        //        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.addDrawerListener(toggle);
        //toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        navigationMenuUserName = findViewById(R.id.nav_header_name);
        navigationMenuEmail = findViewById(R.id.nav_header_email);

        // in Resume:
        //navigationMenuUserName.setText("---");
        //navigationMenuEmail.setText( Util.userEmail );

        //
        //
        //


        Log.d(TAG, ">>>RUN>>>onCreate()");

        Util.progressDialog = new ProgressDialog(DataCollectorActivity.this);

        //########################################################
        //########################################################
        //########################################################



        //########################################################
        //########################################################
        //########################################################

        // hide keyboard if needed:
        try {
            Util.hideKeyboard(DataCollectorActivity.this);
        } catch (Exception ignore) {

        }
        //
        // Internal Saving Location for ALL hidden files:
        //
        Util.internalFilesRoot = new File(getFilesDir().toString());
        Log.i(TAG, "Util.internalFilesRoot.getAbsolutePath() = " + Util.internalFilesRoot.getAbsolutePath());


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

        // Creating user's raw data file path:
        Util.rawdata_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/rawdata_" + mAuth.getUid() + ".csv";
        Util.feature_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_" + mAuth.getUid() + ".arff";   //*// we need this for validation only
        Util.feature_dummy_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_dummy.arff";                   //*//  - dummy exists and it is not empty
        rawdataUserFile = new File(Util.rawdata_user_path);
        featureUserFile = new File(Util.feature_user_path);                                                                          //*//
        Log.i(TAG, "PATH: Util.rawdata_user_path  = " + Util.rawdata_user_path);
        Log.i(TAG, "PATH: Util.rawdata_user_path  = " + Util.feature_user_path);                                                   //*//

        // Creating user's raw data file (if not exists):
        if (!rawdataUserFile.exists()) {
            try {
                rawdataUserFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + Util.rawdata_user_path);
            }
        }
        // Creating user's feature file (if not exists):
        if (!featureUserFile.exists()) {
            try {
                featureUserFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + Util.feature_user_path);
            }
        }

        //FIREBASE INIT:
        mFirestore = FirebaseStorage.getInstance();
        mStorageReference = mFirestore.getReference();

        //SENSOR:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometerSensor == null) {
            Toast.makeText(this, "The device has no com.example.jancsi_pc.playingwithsensors.Utils.Accelerometer !", Toast.LENGTH_SHORT).show();
            finish();
        }

        textViewStatus.setText(R.string.startRecording);

        stopButton.setEnabled(false);
        sendToServerButton.setEnabled(false);
        saveToFirebaseButton.setEnabled(false);

        reportErrorTextView.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>reportErrorTextViewClickListener");
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "abc@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Problem with authentication.");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, "");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        });

        final DecimalFormat df = new DecimalFormat("0");
        df.setMaximumIntegerDigits(20);
        // 123...45E9 -> 123...459234
        //         ==            ====

        //Step Detecting:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        // HIDE ACCELEROMETER COORDINATES:

        accelerometerTitleTextView.setVisibility(View.INVISIBLE);
        accelerometerX.setVisibility(View.INVISIBLE);
        accelerometerY.setVisibility(View.INVISIBLE);
        accelerometerZ.setVisibility(View.INVISIBLE);

        // Proxy sensor:
        mSensorManager = (SensorManager) getSystemService(DataCollectorActivity.this.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // wake lock (for proxy)
        //WAKELOCK//powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        //WAKELOCK//try {
        //WAKELOCK//    // Yeah, this is hidden field.
        //WAKELOCK//    field = PowerManager.class.getClass().getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null);
        //WAKELOCK//} catch (Throwable ignored) {
        //WAKELOCK//}
        //WAKELOCK//wakeLock = powerManager.newWakeLock(field, getLocalClassName());

        if (NO_PYTHON_SERVER_YET) {
            sendToServerButton.setVisibility(View.INVISIBLE);
            pythonServerImageView.setVisibility((View.INVISIBLE));
        }

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

                accelerometerX.setText(("X: " + x));
                accelerometerY.setText(("Y: " + y));
                accelerometerZ.setText(("Z: " + z));

                if (isRecording) {
                    accArray.add(new Accelerometer(timeStamp, x, y, z, stepNumber));
                    recordCount++;
                    /*(STEPCOUNT)
                    stepArray.add(numSteps);
                    */
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
            sensorManager.registerListener(DataCollectorActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
            accArray.clear();
            isRecording = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            sendToServerButton.setEnabled(false);
            saveToFirebaseButton.setEnabled(false);
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
            mDate = new Date();
            Util.recordDateAndTimeFormatted = DateFormat.format("yyyyMMdd_HHmmss", mDate.getTime());
            //Toast.makeText(DataCollectorActivity.this, Util.recordDateAndTimeFormatted, Toast.LENGTH_LONG).show();
            isRecording = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            sendToServerButton.setEnabled(true);
            saveToFirebaseButton.setEnabled(true);
            sensorManager.unregisterListener(DataCollectorActivity.this);
            Log.d("ConnectionActivity", "Stop Rec. - Generating CMD");
            textViewStatus.setText(R.string.calculating);
            CMD = accArrayToString();
            CMD += ",end";
            Log.d("ConnectionActivity", "CMD Generated.");
            textViewStatus.setText(("Recorded: " + recordCount + " datapoints and " + stepNumber + " step cycles."));
            // Proxy sensor:
            mSensorManager.unregisterListener(DataCollectorActivity.this);
        });

        /*
         *
         *   Sending records to server
         *
         */
        sendToServerButton.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>sendButtonClickListener");
            sendToServerButton.setEnabled(false);
            Toast.makeText(DataCollectorActivity.this, "freq1: " + Util.samplingFrequency(accArray), Toast.LENGTH_LONG).show();
            //TODO check if connected to wifi before attempting to send
            //extract features first TODO
            //ArrayList<byte[]> byteList = new FeatureExtractor(accArray).getByteList();

            // STOP button generates the CMD
            //sentTextView.setText( CMD );
            // Sending the array in multiple packages:
            accArrayGroupArrayToString();
            for (int i = 0; i < accArrayStringGroups.size(); ++i) {
                Log.i("accArrayString", "aASG.get(" + i + ")= " + accArrayStringGroups.get(i));
                CMD = accArrayStringGroups.get(i);  //group of RECORDS_LIMIT_PER_PACKAGE records
                //Prepare and Send
                getIPandPort();
                Socket_AsyncTask cmd_send_data = new Socket_AsyncTask();
                cmd_send_data.execute();
            }
            Toast.makeText(DataCollectorActivity.this, "Data has been sent. " + Calendar.getInstance().getTime(), Toast.LENGTH_LONG).show();
        });

        /*
         *
         *   Sending to Firebase
         *   from Start to End
         *
         */

        saveToFirebaseButton.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>saveToFirebaseButtonClickListener");

            Util.progressDialog = new ProgressDialog(DataCollectorActivity.this, ProgressDialog.STYLE_SPINNER);
            Util.progressDialog.setTitle("Progress Dialog");
            Util.progressDialog.setMessage("Uploading");
            Util.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            Util.progressDialog.show();

            try {

                //region Explanation
                /*
                    We have to upload the files withDate then after upload
                    the files has to be renamed withoutDate to make sure
                    there will be no copy in the internal storage.
                 */
                //endregion
                //RENAME//if( ! renameIternalFiles_to_withDate() ){ //return false if an error occured     // will be renamed back after uploads
                //RENAME//    throw new MyFileRenameException("Error renaming file to \"..._<date>_<time>...\"");
                //RENAME//}
                if (checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DataCollectorActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.REQUEST_CODE);
                }
                if (checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DataCollectorActivity.this, new String[]{Manifest.permission.INTERNET}, Util.REQUEST_CODE);
                }

                // Change Debug DIR
                String fileStorageName;
                String collectionName;
                if (Util.debugMode) {
                    fileStorageName = FirebaseUtil.STORAGE_FILES_DEBUG_KEY;
                    collectionName = FirebaseUtil.USER_RECORDS_DEBUG_KEY;
                } else {
                    fileStorageName = FirebaseUtil.STORAGE_FILES_KEY;
                    collectionName = FirebaseUtil.USER_RECORDS_NEW_KEY;
                }

                // Saving array into .CSV file (Local):
                Util.SaveAccArrayIntoCsvFile(accArray, rawdataUserFile);

                // Saving CSV File to FireBase Storage:
                StorageReference ref = mStorageReference.child(fileStorageName + "/" + rawdataUserFile.getName());
                FirebaseUtil.UploadFileToFirebaseStorage(DataCollectorActivity.this, rawdataUserFile, ref);

                // Updating (JSON) Object in the FireStore: (Collection->Documents->Collection->Documents->...)
                String randomId = UUID.randomUUID().toString();
                String downloadUrl = ref.getDownloadUrl().toString();
                UserRecordObject info = new UserRecordObject(mDate.toString(), rawdataUserFile.getName(), downloadUrl);

                mDocRef = FirebaseFirestore.getInstance()
                        .collection(collectionName + "/")
                        .document(mAuth.getUid() + "")
                        .collection(Util.deviceId)
                        .document(randomId);
                FirebaseUtil.UploadObjectToFirebaseFirestore(DataCollectorActivity.this, info, mDocRef);
                FirebaseUtil.updateStatsInFirestore(stepNumber);

                // TODO: VARJA BE OKET ES FUTTASSA LE EZT: !!!
                // Wait until these two async uploads finish !
                //RENAME//if (!renameIternalFiles_to_withoutDate()) { //return false if an error occured     // will be renamed back after uploads
                //RENAME//    Toast.makeText(DataCollectorActivity.this,"ERROR (renamig file)",Toast.LENGTH_LONG).show();
                //RENAME//    throw new MyFileRenameException("Error renaming file to \"..._<date>_<time>...\"");
                //RENAME//}

                //RENAME//}catch( MyFileRenameException e ){
                //RENAME//    Util.progressDialog.dismiss();
                //RENAME//    Log.e(TAG,"ERROR (MyFileRenameError): File cannot be renamed !");
                //RENAME//    e.printStackTrace();
            } catch (Exception e) {
                Util.progressDialog.dismiss();
                e.printStackTrace();
            }

//                /*DELETE_ME_PLEASE*/ // SAVE TO FIREBASE FOR TIMI FOR RAW DATA -> MODEL
//                String r = UUID.randomUUID().toString();
//                String fileNameWithDate = "/rawdata_" + mAuth.getUid() + "_" + Util.recordDateAndTimeFormatted + ".csv";
//                /*DELETE_ME_PLEASE*/ StorageReference ref = mStorageReference.child( "jancsi_raw_data_for_model/" + fileNameWithDate );
//                /*DELETE_ME_PLEASE*/ FirebaseUtil.UploadFileToFirebaseStorage(DataCollectorActivity.this,rawdataUserFile, ref);


            // TODO: if( az utolso 4 fuggveny hibatlanul lefutott ) ==> ROLLBACK


        });


    }// OnCreate

    // step: 1,2,3 in FirebaseUtil


    //region HELP
    /*
        renameIternalFiles_withDate()
                | Before upload add "_<date>_<time>" to the end of the file (and path)
                | ( After reload rename it back! )
                | return:
                |   true - No errors
                |   false - Error
    */
    //endregion
    /**
     * Renames the files used by the app to contain the date.
     * @return true if the renaming run successful and false if not.
     *
     * @author Mille Janos
     */
    private boolean renameIternalFiles_to_withDate() {
        Log.d(TAG, ">>RUN>>renameIternalFiles_to_withDate()");
        File f = null;
        Util.rawdata_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/rawdata_" + mAuth.getUid() + "_" + Util.recordDateAndTimeFormatted + ".csv";

        try {
            f = new File(Util.rawdata_user_path);
            rawdataUserFile.renameTo(f);
        } catch (Exception e) {
            Log.e(TAG, "renameIternalFiles_withDate() - CANNOT RENAME FILE TO: " + f.getAbsolutePath());
            e.printStackTrace();
            Log.d(TAG, "<<FINISHED<<renameIternalFiles_to_withDate() - ERROR");
            return false;
        }
        Log.d(TAG, "<<FINISHED<<renameIternalFiles_to_withDate() - OK");
        return true;
    }

    //region HELP
    /*
        renameIternalFiles_withDate()
                | ( Before upload add "_<date>_<time>" to the end of the file (and path) )
                | After reload rename it back!
                | return:
                |   true - No errors
                |   false - Error
    */
    //endregion
    /**
     * Renames the files used by the app to not contain the date.
     * @return true if the renaming run successful and false if not.
     *
     * @author Mille Janos
     */
    private boolean renameIternalFiles_to_withoutDate() {
        Log.d(TAG, ">>RUN>>renameIternalFiles_to_withoutDate()");
        File f = null;
        Util.rawdata_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/rawdata_" + mAuth.getUid() + "_0_0" + ".csv";

        try {
            f = new File(Util.rawdata_user_path);
            rawdataUserFile.renameTo(f);

        } catch (Exception e) {
            Log.e(TAG, "renameIternalFiles_withoutDate() - CANNOT RENAME FILE TO: " + f.getAbsolutePath());
            e.printStackTrace();
            Log.d(TAG, "<<FINISHED<<renameIternalFiles_to_withoutDate() - ERROR");
            return false;
        }
        Log.d(TAG, "<<FINISHED<<renameIternalFiles_to_withoutDate() - OK");
        return true;
    }


    //region HELP
    /*
    ArrayList<Accelerometer> accArray ==> String str

    output format:   "timestamp,x,y,z,currentStepCount,timestamp,x,y,z,currentStepCount,timestamp,x,y,z,timestamp,currentStepCount, ... ,end"
    */
    //endregion
    /**
     * Converts array of Accelerometers to string.
     * @return the converted string.
     *
     * @author Mille Janos
     */
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


    //region HELP
    /*
      Same as accArrayToString just grouped in N groups
      NO return value, the result is in accArrayStringGroups variable !
      adds "end" to the end of the package-chain
    */
    //endregion
    /**
     *  Converts the local array of Accelerometers (accArray) into strings using
     *  RECORDS_PER_PACKAGE_LIMIT constant to limit the string size.
     *  The string list will be saved into accArrayStringGroups local variable.
     *
     *  @author Mille Janos
     */
    public void accArrayGroupArrayToString() {
        Log.d(TAG, ">>>RUN>>>accArrayGroupArrayToString()");
        accArrayStringGroups.clear();
        StringBuilder sb = new StringBuilder();
        int i;
        int c = 0;  // counter
        boolean limitReached = true;
        for (i = 0; i < accArray.size(); ++i) {
            ++c;
            sb.append(accArray.get(i).getTimeStamp())
                    .append(",")
                    .append(accArray.get(i).getX())
                    .append(",")
                    .append(accArray.get(i).getY())
                    .append(",")
                    .append(accArray.get(i).getZ())
                    .append(",")
                    .append(accArray.get(i).getStep())
                    .append(",");
            limitReached = false;
            if (c == RECORDS_PER_PACKAGE_LIMIT) {
                accArrayStringGroups.add(sb.toString());
                //str = "";
                sb.setLength(0);
                c = 0;
                limitReached = true;
                continue;
            }
            sb.append(",");
        }
        //If the last group has no exactly N elements then we have to add it on the end
        if (!limitReached) {
            sb.append("end");
            accArrayStringGroups.add(sb.toString());
        }
    }

    /**
     * Sets up the Ip and the Port using local variables.
     * For Ip: String wifiModuleIp
     * For Port: int wifiModulePort
     *
     * @author Mille Janos
     */
    public void getIPandPort() {
        Log.d(TAG, ">>>RUN>>>getIPandPort()");
        String iPandPort = IP_ADDRESS;
        Log.d("getIPandPort", "IP String: " + iPandPort);
        String temp[] = iPandPort.split(":");
        wifiModuleIp = temp[0];
        wifiModulePort = Integer.parseInt(temp[1]);
        Log.d("getIPandPort", "IP: " + wifiModuleIp);
        Log.d("getIPandPort", "Port: " + wifiModulePort);
    }

    /**
     * This class is used to send package set by getIPandPort() method.
     * Sends the content of the local CMD string variable.
     *
     * @author Mille Janos
     */
    // <String, String, TCPClient>            // TODO: Modify if needed
    public static class Socket_AsyncTask extends AsyncTask<Void, Void, Void> {
        Socket socket;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                InetAddress inetAddress = InetAddress.getByName(DataCollectorActivity.wifiModuleIp);
                Log.i(TAG, "doInBackground: 1");
                Log.d(TAG, "doInBackground: 1");
                socket = new Socket(inetAddress, DataCollectorActivity.wifiModulePort);
                Log.d(TAG, "doInBackground: 2");
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                Log.d(TAG, "doInBackground: 3");
                Log.i("SocketAsyncT", "SENDING: " + CMD + " (" + DataCollectorActivity.wifiModuleIp + " : " + DataCollectorActivity.wifiModulePort + ")");
                //DataOutputStream.writeBytes( CMD );
                byte byteArray[] = CMD.getBytes();
                Log.d(TAG, "doInBackground: 4");
                dataOutputStream.write(byteArray);
                Log.d(TAG, "doInBackground: 5");
                dataOutputStream.flush();
                Log.d(TAG, "doInBackground: 6");
                dataOutputStream.close();
                Log.d(TAG, "doInBackground: 7");
                socket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    /**
     * Gait validation for current logged in user using AlertDialogs.
     *
     * @author Mille Janos
     */
    private void ShowGaitResult() {
        if (!Util.validatedOnce) {
            //region GAIT VALIDATER
            startButton.callOnClick();

            Util.hideKeyboard(DataCollectorActivity.this);

            AlertDialog.Builder builderInitial = new AlertDialog.Builder(this);
            builderInitial.setTitle("Usage");
            builderInitial.setMessage("Press OK then put the device in you pocket then after a walk press OK again");
            builderInitial.setCancelable(false);
            builderInitial.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //builderInitial.dismiss();
                }
            });
            builderInitial.setNeutralButton(android.R.string.ok,
                    (dialogInitial, id) -> {
                        dialogInitial.cancel();


                        AlertDialog.Builder builder = new AlertDialog.Builder(DataCollectorActivity.this);
                        builder.setTitle("Authentificate yourselfe");
                        builder.setMessage("Walk then press OK.");
                        builder.setCancelable(false);
                        builder.setNeutralButton(android.R.string.ok,
                                (dialog, id1) -> {
                                    stopButton.callOnClick();
                                    saveToFirebaseButton.setEnabled(false);

                                    dialog.cancel();

                                    Toast.makeText(DataCollectorActivity.this, "CALCULATING...", Toast.LENGTH_LONG).show();

                                    /*LoadDial4Gener*/
                                    Util.progressDialog = new ProgressDialog(DataCollectorActivity.this, ProgressDialog.STYLE_SPINNER);
                                    /*LoadDial4Gener*/
                                    Util.progressDialog.setTitle("Processing");
                                    /*LoadDial4Gener*/
                                    Util.progressDialog.setMessage("Calculating result...");
                                    /*LoadDial4Gener*/
                                    Util.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    /*LoadDial4Gener*/
                                    Util.progressDialog.setCancelable(true);
                                    /*LoadDial4Gener*/
                                    if (!Util.progressDialog.isShowing()) {
                                        /*LoadDial4Gener*/
                                        Util.progressDialog.show();
                                        /*LoadDial4Gener*/
                                    }
                                    if (checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(DataCollectorActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.REQUEST_CODE);
                                    }

                                    // Download user model from Firebase Storage:
                                    String modelFileName = "model_" + Util.mAuth.getUid() + ".mdl";
                                    StorageReference ref = Util.mStorage.getReference().child(FirebaseUtil.STORAGE_MODELS_KEY + "/" + modelFileName);
                                    File downloadedUserModelFile = new File(Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/downloadedmodel_" + mAuth.getUid() + ".mdl");
                                    String downloadedUserModelFilePath = downloadedUserModelFile.getAbsolutePath();

                                    //FirebaseUtil.DownloadFileFromFirebaseStorage(DataCollectorActivity.this, ref, downloadedUserModelFile);
                                    FirebaseUtil.DownloadFileFromFirebaseStorage_AND_CheckUserInPercentage(DataCollectorActivity.this, ref, downloadedUserModelFile);

                                    textViewStatus.setText(R.string.press_start_to_collect);

                                    // Saving array into .CSV file (Local):
                                    if (Util.SaveAccArrayIntoCsvFile(accArray, rawdataUserFile) == 1) {
                                        Toast.makeText(DataCollectorActivity.this, "Error saving raw data!", Toast.LENGTH_LONG).show();
                                    }

                                    // String rawDataUserFilePath = rawdataUserFile.getAbsolutePath(); // == Util.rawdata_user_path

                                    // User raw (VAN)
                                    // Feature user (LESZ)
                                    // Dummy feature (VAN)

                                    // double percentage = Util.CheckUserInPercentage(
                                    //         DataCollectorActivity.this,
                                    //         Util.rawdata_user_path,
                                    //         Util.feature_user_path,
                                    //         Util.feature_dummy_path,
                                    //         downloadedUserModelFilePath,
                                    //         Util.mAuth.getUid() );
                                    //
                                    //               //dialog.cancel();
                                    //
                                    // AlertDialog.Builder builder1 = new AlertDialog.Builder(DataCollectorActivity.this);
                                    // builder1.setTitle("Gait Validation");
                                    // builder1.setMessage("Result: " + percentage );
                                    // builder1.setCancelable(true);
                                    // builder1.setNeutralButton(android.R.string.ok,
                                    //         new DialogInterface.OnClickListener() {
                                    //             public void onClick(DialogInterface dialog1, int id) {
                                    //                 dialog1.cancel();
                                    //             }
                                    //         });
                                    //
                                    // AlertDialog alert11 = builder1.create();
                                    // alert11.show();

                                    /*LoadDial4Gener*/
                                    if (Util.progressDialog.isShowing()) {
                                        /*LoadDial4Gener*/
                                        Util.progressDialog.dismiss();
                                        /*LoadDial4Gener*/
                                    }

                                });

                        AlertDialog alert = builder.create();
                        alert.show();

                    });

            AlertDialog alertInitial = builderInitial.create();
            alertInitial.show();

            //endregion
            Util.validatedOnce = true;
        }
    }

    /**
     * Finds the views used by this context.
     *
     * @author Mille Janos
     */
    private void findViewByIDs() {
        textViewStatus = findViewById(R.id.textViewStatus);
        startButton = findViewById(R.id.buttonStart);
        stopButton = findViewById(R.id.buttonStop);
        sendToServerButton = findViewById(R.id.buttonSend);
        saveToFirebaseButton = findViewById(R.id.saveToFirebaseButton);
        accelerometerX = findViewById(R.id.textViewAX2);
        accelerometerY = findViewById(R.id.textViewAY2);
        accelerometerZ = findViewById(R.id.textViewAZ2);
        //goToRegistrationTextView = findViewById(R.id.);
        //goToLoginTextView = findViewById(R.id.goToLoginTextView);
        reportErrorTextView = findViewById(R.id.errorReportTextView);
        accelerometerTitleTextView = findViewById(R.id.textViewAccelerometer2);
        pythonServerImageView = findViewById(R.id.pythonServerImageView);

        attachedLayout = findViewById(R.id.datacollector_main_layout);

    }

    @Override
    public void onStart() {
        Log.d(TAG, ">>>RUN>>>onStart()");
        super.onStart();
        Util.deviceId = Settings.Secure.getString(DataCollectorActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, ">>>RUN>>>onResume()");
        super.onResume();

        // If close all Activityes
        if (Util.isFinished) {
            Log.d(TAG, " isFinished() = true");
            //Util.isFinished = false;    // TODO: VIGYAZZ MERT ITT MEGSZAKITJA A LANCOLT KILEPEST, MIVEL EZ AZ UTOLSO ACTIVITY A STACKBEN, ugy(e nelkul) ujrainditaskor is kikapcsolt
            finish();
        }

        // Check if user is signed in (non-null) and update UI accordingly.
        if (!Util.isSignedIn) {
            Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
            Intent intent = new Intent(DataCollectorActivity.this, AuthenticationActivity.class);
            startActivity(intent);
        } else {
            // Set email and username on Slide Menu
            //navigationMenuUserName.setText("---");
            //navigationMenuEmail.setText( Util.userEmail );
            // Test if user is imposter:
            // TODO DELETE THIS IF !
            // TODO something
            if (mAuth.getUid().equals("LnntbFQGpBeHx3RwMu42e2yOks32")) {
                ShowGaitResult();
            }
        }

        sensorManager.registerListener(accelerometerEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

        //MOVED TO SETTINGS
        //
        // // Admin Mode:
        // if( Util.isAdminLoggedIn ){
        //     debugSwitch.setChecked(false);
        //     debugSwitch.setVisibility(View.VISIBLE);
        //     debugSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
        //         if (isChecked) {
        //             Util.debugMode = true;
        //             Log.i(TAG, "Debug Mode -> ON");
        //         } else {
        //             Util.debugMode = false;
        //             Log.i(TAG, "Debug Mode -> OFF");
        //         }
        //     });
        // }else{
        //     debugSwitch.setChecked(false);
        //     debugSwitch.setVisibility(View.INVISIBLE);
        // }

        // Show on screen model status
        if (Util.isSetUserModel) {
            Log.d(TAG, "User Model is set.");
            if (Util.hasUserModel) {
                Log.d(TAG, "Snackbar: \"Model found :)\"");
                Snackbar.make(findViewById(R.id.datacollector_main_layout), "Model found! :)", Snackbar.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Snackbar: \"No model found! :(\"");
                Snackbar.make(findViewById(R.id.datacollector_main_layout), "No model found! :(", Snackbar.LENGTH_SHORT).show();
                Log.d(TAG, ">>>START ACTIVITY>>>ModelUploaderActivity");
                // Addig a ModelUploaderActivity-nel kell maradjon amig nincs Modelje:
                startActivity(new Intent(DataCollectorActivity.this, ModelUploaderActivity.class));
            }
        } else {
            Log.d(TAG, "User Model is not set yet.");
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, ">>>RUN>>>onPause()");
        super.onPause();

        Util.mSharedPref = getSharedPreferences(Util.sharedPrefFile, MODE_PRIVATE);
        Util.mSharedPrefEditor = Util.mSharedPref.edit();
        Util.mSharedPrefEditor.putString(Util.LAST_LOGGED_IN_EMAIL_KEY, Util.userEmail);
        Util.mSharedPrefEditor.putString(Util.LAST_LOGGED_IN_ID_KEY, mAuth.getUid());
        Util.mSharedPrefEditor.apply();

        sensorManager.unregisterListener(accelerometerEventListener);
    }

    //(STEPCOUNT)

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }

        // Proxy sensor:
        //WAKELOCK//if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
        //WAKELOCK//    if (event.values[0] >= -SENSOR_SENSITIVITY && event.values[0] <= SENSOR_SENSITIVITY) {
        //WAKELOCK//        //near
        //WAKELOCK//        //Toast.makeText(getApplicationContext(), "near", Toast.LENGTH_SHORT).show();
        //WAKELOCK//        wakeLock.release();
        //WAKELOCK//    } else {
        //WAKELOCK//        //far
        //WAKELOCK//        //Toast.makeText(getApplicationContext(), "far", Toast.LENGTH_SHORT).show();
        //WAKELOCK//        wakeLock.acquire();
        //WAKELOCK//    }
        //WAKELOCK//}
    }

    /**
     * Increases the stepNumber variable by 1.
     * @param timeNs
     */
    @Override
    public void step(long timeNs) {
        Log.d(TAG, ">>>RUN>>>step()");
        //mp.start();
        DataCollectorActivity.stepNumber++;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Snackbar.make(attachedLayout, "HOME", Snackbar.LENGTH_SHORT).show();
        } else if (id == R.id.nav_profile) {
            //Snackbar.make(attachedLayout, "PROFILE", Snackbar.LENGTH_SHORT).show();
            Intent intent = new Intent(DataCollectorActivity.this, UserProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_collection) {
            Snackbar.make(attachedLayout, "COLLECTION", Snackbar.LENGTH_SHORT).show();
            Log.d(TAG, "onNavigationItemSelected: Launching ListDataFromFirebaseActivity");
            Intent intent = new Intent(DataCollectorActivity.this, ListDataFromFirebaseActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            //Snackbar.make(attachedLayout,"SETTINGS",Snackbar.LENGTH_SHORT).show();
            Intent intent = new Intent(DataCollectorActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            //Snackbar.make(attachedLayout,"LOGOUT",Snackbar.LENGTH_SHORT).show();
            mAuth.signOut();
            Util.isSignedIn = false;
            Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
            Util.userEmail = "";
            Util.validatedOnce = false;
            startActivity(new Intent(DataCollectorActivity.this, AuthenticationActivity.class));
        } else if (id == R.id.nav_exit) {
            //Snackbar.make(attachedLayout,"EXIT APP",Snackbar.LENGTH_SHORT).show();
            Util.isFinished = true;
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}