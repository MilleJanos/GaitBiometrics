package com.example.jancsi_pc.playingwithsensors;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.StepCounterPackage.StepCounterActivity;
import com.example.jancsi_pc.playingwithsensors.StepCounterPackage.StepDetector;
import com.example.jancsi_pc.playingwithsensors.StepCounterPackage.StepListener;
import com.example.jancsi_pc.playingwithsensors.Utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class DataCollectorActivity extends AppCompatActivity implements SensorEventListener, StepListener {
    private final String TAG = "DataCollectorActivity";

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEventListener accelerometerEventListener;
    private Button sendButton;
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
    public static int stepNumber=0;
    public static final int MAX_STEP_NUMBER=10;
    public static final int MIN_STEP_NUMBER=5;
    private TextView textViewStatus;
    private TextView accelerometerX;
    private TextView accelerometerY;
    private TextView accelerometerZ;
    private TextView loggedInUserEmailTextView;
    private TextView goToRegistrationTextView;
    private TextView goToLoginTextView;

    //private final MediaPlayer mp = MediaPlayer.create(DataCollectorActivity.this, R.raw.sound2);

    //queue for containing the fixed number of steps that has to be processed
    //TODO

    // For Step Detecting:
    private StepDetector simpleStepDetector;
    private static final int REQUEST_CODE = 212;

    //Firebase:
    private FirebaseStorage mFirestore;            // used to upload files
    private StorageReference mStorageReference;  // to storage
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();



    /*
    *
    *   OnCreate
    *
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collector);

        //Asking the user to enable WiFi
        CheckWiFiNetwork();

        //Asking the user to connect to WiFi or Mobile Data network
        //RequireInternetConnection();      // TODO :not doinig anything

        //FIREBASE INIT:
        mFirestore = FirebaseStorage.getInstance();
        mStorageReference = mFirestore.getReference();

        //SENSOR:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if( accelerometerSensor == null ){
            Toast.makeText(this, "The device has no com.example.jancsi_pc.playingwithsensors.Accelerometer !", Toast.LENGTH_SHORT).show();
            finish();
        }

        textViewStatus = findViewById(R.id.textViewStatus);
        textViewStatus.setText("Press START to start recording.");

        startButton = findViewById(R.id.buttonStart);
        stopButton  = findViewById(R.id.buttonStop);
        sendButton = findViewById(R.id.buttonSend);
        saveToFirebaseButton = findViewById(R.id.saveToFirebaseButton);
        loggedInUserEmailTextView = findViewById(R.id.showLoggedInUserEmailTextView);

        stopButton.setEnabled(false);
        sendButton.setEnabled(false);
        saveToFirebaseButton.setEnabled(false);

        accelerometerX = findViewById(R.id.textViewAX2);
        accelerometerY = findViewById(R.id.textViewAY2);
        accelerometerZ = findViewById(R.id.textViewAZ2);

        //goToRegistrationTextView = findViewById(R.id.);
        //goToLoginTextView = findViewById(R.id.goToLoginTextView);

        final DecimalFormat df = new DecimalFormat("0");
        df.setMaximumIntegerDigits(20);
        // 123...45E9 -> 123...459234
        //         ==            ====

        //Step Detecting:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);


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

                //ts = timeStamp;
                //accX = x;
                //accY = y;
                //accZ = z;

                //queueing
                //keeping the queue size fixed

                accelerometerX.setText("X: "+x);
                accelerometerY.setText("Y: "+y);
                accelerometerZ.setText("Z: "+z);


                if (isRecording) {
                    accArray.add(new Accelerometer(timeStamp, x, y, z, stepNumber));
                    recordCount++;
                    /*(STEPCOUNT)
                    stepArray.add(numSteps);
                    */
                    textViewStatus.setText("Recording: " + stepNumber + " steps made.");
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

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mediaPlayer.create(null,R.raw.start);
                //mediaPlayer.start();
                recordCount = 0;
                stepNumber = 0;
                sensorManager.registerListener(DataCollectorActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
                accArray.clear();
                isRecording = true;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                sendButton.setEnabled(false);
                saveToFirebaseButton.setEnabled(false);
                Log.d("ConnectionActivity_", "Start Rec.");
                //textViewStatus.setText("Recording ...");
            }
        });

        /*
        *
        *   Stop recording
        *
        */

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = false;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                sendButton.setEnabled(true);
                saveToFirebaseButton.setEnabled(true);
                sensorManager.unregisterListener(DataCollectorActivity.this);
                Log.d("ConnectionActivity", "Stop Rec. - Generating CMD");
                textViewStatus.setText("Calculating...");
                CMD = accArrayToString();
                CMD += ",end";
                Log.d("ConnectionActivity","CMD Generated.");
                textViewStatus.setText("Recorded: " + recordCount + " datapoints and " + stepNumber +" step cycles.");
            }
        });

        /*
        *
        *   Sending multiple records
        *   from Start to End
        *
        */

        //SEND   (MULTIPLE RECORDS)
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendButton.setEnabled(false);
                Toast.makeText(DataCollectorActivity.this,"freq1: " + Util.samplingFrequency(accArray) + "freq2: " + Util.samplingFrequency2(accArray),Toast.LENGTH_LONG).show();
                //TODO check if connected to wifi before attempting to send
                //extract features first TODO
                //ArrayList<byte[]> byteList = new FeatureExtractor(accArray).getByteList();

                // STOP button generates the CMD
                //sentTextView.setText( CMD );
                // Sending the array in multiple packages:
                accArrayGroupArrayToString();
                for(int i=0; i<accArrayStringGroups.size(); ++i) {
                    Log.i("accArrayString","aASG.get("+i+")= " + accArrayStringGroups.get(i) );
                    CMD = accArrayStringGroups.get(i);  //group of RECORDS_LIMIT_PER_PACKAGE records
                    //Prepare and Send
                    getIPandPort();
                    Socket_AsyncTask cmd_send_data = new Socket_AsyncTask();
                    cmd_send_data.execute();
                }
                Toast.makeText(DataCollectorActivity.this,"Data has been sent. " + Calendar.getInstance().getTime(), Toast.LENGTH_LONG).show();
            }
        });

        /*
         *
         *   Sending to FireStorage
         *   from Start to End
         *
         */

        saveToFirebaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DataCollectorActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                }
                //
                // Saving into .CSV file
                //
                Log.d(TAG,"Saving to .CSV");

                File root = android.os.Environment.getExternalStorageDirectory();
                File dir = new File (root.getAbsolutePath() /*+ "/accelerometer"*/);
                if(!dir.exists()) {
                    dir.mkdirs();
                }
                String randomIdName = UUID.randomUUID().toString();
                File file = new File(dir, randomIdName + ".csv");


                //Log.d("RecorderActivity", file.getAbsolutePath());

                //try {
                //    File root = new File(Environment.getExternalStorageDirectory(), "Aux");
                //    if (!root.exists()) {
                //        root.mkdirs();
                //    }
                //    File gpxfile = new File(root, "data.csv");
                //    Log.d(TAG, root.getAbsolutePath() );
                //    FileWriter writer = new FileWriter(gpxfile);
                //    writer.append("HELLO WORLD!");
                //    writer.flush();
                //    writer.close();
                //}catch (FileNotFoundException e){ Log.d(TAG, "***********FILE NOT FOUND*******"); e.printStackTrace(); }
                //catch ( Exception e){ e.printStackTrace(); }
                try {
                    FileOutputStream f = new FileOutputStream(file);
                    PrintWriter pw = new PrintWriter(f);
                    for( Accelerometer a : accArray){
                        pw.println( a.toString() );
                    }
                    pw.flush();
                    pw.close();
                    f.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.d(TAG, "******* File not found.");
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Log.d(TAG,"Saving CSV to FireStore...");
                //
                // Saving CSV to firestore
                //
                if (checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DataCollectorActivity.this, new String[]{Manifest.permission.INTERNET}, REQUEST_CODE);
                }

                Log.d(TAG ,"FILE PATH:" + file.getAbsolutePath());

                Uri path = Uri.fromFile( new File(file.getAbsolutePath()) );

                if( path != null){
                    final ProgressDialog progressDialog = new ProgressDialog(DataCollectorActivity.this);
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();
                    /*
                    *
                    *  Generate
                    *
                     */
                    StorageReference ref = mStorageReference.child("files/" + UUID.randomUUID().toString() + path.getLastPathSegment() );
                    ref.putFile(path)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progressDialog.dismiss();
                                    Toast.makeText(DataCollectorActivity.this, "Uploaded", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(DataCollectorActivity.this, "Upload Failed", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage("Uploaded " + (int)progress + "%" );
                                }
                            });
                }

                //file.delete();

                //
                // Updating JSON in the Firestore
                //

                //Query mQuery = mFirestore.collection("files");

                //CollectionReference files = mFirestor.collection("files");

                // TODO
                // TODO
                // TODO

            }
        });


    }// OnCreate

    private void RequireInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();



        while( !(activeNetworkInfo != null && activeNetworkInfo.isConnected()) ){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DataCollectorActivity.this);

            // set title
            alertDialogBuilder.setTitle("Wifi Settings");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Make you shure you are connected to the internet")
                    .setCancelable(false)
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Nothing (Retry)
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish(); //close the App
                        }
                    });
        };

    }

    private void CheckWiFiNetwork() {
        final WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if( ! mWifiManager.isWifiEnabled() ) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DataCollectorActivity.this);

            // set title
            alertDialogBuilder.setTitle("Wifi Settings");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Do you want to enable WIFI ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //enable wifi
                            mWifiManager.setWifiEnabled(true);
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //disable wifi
                            //mWifiManager.setWifiEnabled(false);
                            finish(); //close the App
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }
    }

    /*
    *
    * ArrayList<Accelerometer> accArray ==> String str
    *
    * output format:   "timestamp,x,y,z,currentStepCount,timestamp,x,y,z,currentStepCount,timestamp,x,y,z,timestamp,currentStepCount, ... ,end"
    *
    *
    */

    public String accArrayToString(){
        StringBuilder sb = new StringBuilder();
        int i;
        for( i=0; i< accArray.size()-1; ++i ){
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

    /*
    *
    * Same as accArrayToString just grouped in N groups
    * NO return value, the result is in accArrayStringGroups variable !
    * adds "end" to the end of the package-chain
    *
    */

    public void accArrayGroupArrayToString(){
        accArrayStringGroups.clear();
        StringBuilder sb = new StringBuilder();
        int i ;
        int c = 0;  // counter
        boolean limitReached = true;
        for( i=0; i < accArray.size(); ++i ){
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
            if( c == RECORDS_PER_PACKAGE_LIMIT ){
                accArrayStringGroups.add( sb.toString() );
                //str = "";
                sb.setLength(0);
                c = 0;
                limitReached = true;
                continue;
            }
            sb.append(",");
        }
        //If the last group has no exactly N elements then we have to add it on the end
        if( limitReached == false ){
            sb.append("end");
            accArrayStringGroups.add( sb.toString() );
        }
    }

    public void getIPandPort(){
        String iPandPort = IP_ADDRESS;
        Log.d("getIPandPort","IP String: " + iPandPort);
        String temp[] = iPandPort.split(":");
        wifiModuleIp = temp[0];
        wifiModulePort = Integer.parseInt(temp[1]);
        Log.d("getIPandPort","IP: " + wifiModuleIp);
        Log.d("getIPandPort", "Port: " + wifiModulePort);
    }

                                           // <String, String, TCPClient>            // TODO: Modify if needed
    public class Socket_AsyncTask extends AsyncTask<Void,Void,Void>{
        Socket socket;
        @Override
        protected Void doInBackground(Void... voids) {
            try{
                InetAddress inetAddress = InetAddress.getByName( DataCollectorActivity.wifiModuleIp );
                Log.i(TAG,"doInBackground: 1");
                Log.d(TAG,"doInBackground: 1");
                socket = new java.net.Socket( inetAddress, DataCollectorActivity.wifiModulePort );
                Log.d(TAG,"doInBackground: 2");
                DataOutputStream dataOutputStream  = new DataOutputStream(socket.getOutputStream() );
                Log.d(TAG,"doInBackground: 3");
                Log.i("SocketAsyncT","SENDING: " + CMD + " ("+ DataCollectorActivity.wifiModuleIp+" : "+ DataCollectorActivity.wifiModulePort+")");
                //DataOutputStream.writeBytes( CMD );
                byte byteArray[] = CMD.getBytes();
                Log.d(TAG,"doInBackground: 4");
                dataOutputStream.write(byteArray);
                Log.d(TAG,"doInBackground: 5");
                dataOutputStream.flush();
                Log.d(TAG,"doInBackground: 6");
                dataOutputStream.close();
                Log.d(TAG,"doInBackground: 7");
                socket.close();
            }catch( UnknownHostException e ){
                e.printStackTrace();
            }catch( IOException e ){
                e.printStackTrace();
            }
            return null;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if( ! Util.isSignedIn ) {
            Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
            Intent intent = new Intent(DataCollectorActivity.this, AuthenticationActivity.class);
            startActivity(intent);
        }
        loggedInUserEmailTextView.setText( Util.userEmail );
        //updateUI(currentUser);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(accelerometerEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(accelerometerEventListener);
    }

    //(STEPCOUNT)

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("TEST","*");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d("TEST","#");
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        //mp.start();
        Log.d("TEST"," + ");
        DataCollectorActivity.stepNumber++;
    }


}