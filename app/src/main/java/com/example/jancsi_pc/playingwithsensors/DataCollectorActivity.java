package com.example.jancsi_pc.playingwithsensors;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.StepCounterPackage.StepDetector;
import com.example.jancsi_pc.playingwithsensors.StepCounterPackage.StepListener;
import com.example.jancsi_pc.playingwithsensors.Utils.Accelerometer;
import com.example.jancsi_pc.playingwithsensors.Utils.UserAndHisFile;
import com.example.jancsi_pc.playingwithsensors.Utils.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;


public class DataCollectorActivity extends AppCompatActivity implements SensorEventListener, StepListener{
    private final String TAG = "DataCollectorActivity";

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
    private ImageView logoutImageView;
    private TextView reportErrorTextView;

    Date mDate;
    private String mFileName;

    //private final MediaPlayer mp = MediaPlayer.create(DataCollectorActivity.this, R.raw.sound2);

    //queue for containing the fixed number of steps that has to be processed
    //TODO

    // For Step Detecting:
    private StepDetector simpleStepDetector;
    private static final int REQUEST_CODE = 212;

    // Firebase:
    private FirebaseStorage mFirestore;            // used to upload files
    private StorageReference mStorageReference;  // to storage
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DocumentReference mDocRef; // = FirebaseFirestore.getInstance().document("usersFiles/information");

    // Internal Files:
    private File rawdataUserFile;

    /*
     *
     *   OnCreate
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collector);

        Log.d(TAG, ">>>RUN>>>onCreate()");

        //
        // Internal Saving Location for ALL hidden files:
        //
        Util.internalFilesRoot = new File( getFilesDir().toString() );
        Log.i(TAG, "Util.internalFilesRoot.getAbsolutePath() = " + Util.internalFilesRoot.getAbsolutePath() );


        //
        // Internal files Path:
        //
        mDate = new Date();

        // Create folder if not exists:
        File myInternalFilesRoot;

        myInternalFilesRoot = new File( Util.internalFilesRoot.getAbsolutePath() /*+ customDIR*/ );
        if(!myInternalFilesRoot.exists()) {
            myInternalFilesRoot.mkdirs();
            Log.i(TAG,"Path not exists (" + myInternalFilesRoot.getAbsolutePath() + ") --> .mkdirs()");
        }

        // Creating user's raw data file path:
        Util.rawdata_user_path  = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/rawdata_" + mAuth.getUid() + ".csv";
        rawdataUserFile  = new File( Util.rawdata_user_path );
        Log.i(TAG,"PATH: Util.rawdata_user_path  = " + Util.rawdata_user_path  );

        // Creating user's raw data file (if not exists):
        if(!rawdataUserFile.exists()){
            try {
                rawdataUserFile.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG,"File can't be created: " + Util.rawdata_user_path);
            }
        }

        //FIREBASE INIT:
        mFirestore = FirebaseStorage.getInstance();
        mStorageReference = mFirestore.getReference();

        //SENSOR:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if( accelerometerSensor == null ){
            Toast.makeText(this, "The device has no com.example.jancsi_pc.playingwithsensors.Utils.Accelerometer !", Toast.LENGTH_SHORT).show();
            finish();
        }

        textViewStatus = findViewById(R.id.textViewStatus);
        textViewStatus.setText(R.string.startRecording);

        startButton = findViewById(R.id.buttonStart);
        stopButton  = findViewById(R.id.buttonStop);
        sendToServerButton = findViewById(R.id.buttonSend);
        saveToFirebaseButton = findViewById(R.id.saveToFirebaseButton);
        loggedInUserEmailTextView = findViewById(R.id.showLoggedInUserEmailTextView);

        logoutImageView = findViewById(R.id.logoutImageView);

        stopButton.setEnabled(false);
        sendToServerButton.setEnabled(false);
        saveToFirebaseButton.setEnabled(false);

        accelerometerX = findViewById(R.id.textViewAX2);
        accelerometerY = findViewById(R.id.textViewAY2);
        accelerometerZ = findViewById(R.id.textViewAZ2);

        //goToRegistrationTextView = findViewById(R.id.);
        //goToLoginTextView = findViewById(R.id.goToLoginTextView);

        reportErrorTextView = findViewById(R.id.errorReportTextView);
        reportErrorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>reportErrorTextViewClickListener");
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","abc@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Problem with authentication.");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, "");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
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

        if( NO_PYTHON_SERVER_YET ){
            ImageView pythonServerImageView = findViewById(R.id.pythonServerImageView);
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

                accelerometerX.setText(("X: "+x));
                accelerometerY.setText(("Y: "+y));
                accelerometerZ.setText(("Z: "+z));

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

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                Log.d(TAG, ">>>RUN>>>stopButtonClickListener");
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
                Log.d("ConnectionActivity","CMD Generated.");
                textViewStatus.setText(("Recorded: " + recordCount + " datapoints and " + stepNumber +" step cycles."));
            }
        });

        /*
         *
         *   Sending records to server
         *
         */
        sendToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>sendButtonClickListener");
                sendToServerButton.setEnabled(false);
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
         *   Sending to Firebase
         *   from Start to End
         *
         */

        saveToFirebaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>saveToFirebaseButtonClickListener");
                if (checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DataCollectorActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                }
                // Saving array into .CSV file (Local):
                savingAccArrayIntoCSV();

                // Saving CSV to FireBase Storage:
                savingCSVtoFireBaseStorage();

                // Updating JSON in the FireStore: (Collection->Documents->Collection->Documents->...)
                uploadJSONintoFireBaseFireStore();

                //
                // Saving into .CSV file
                //


                Log.d(TAG,"Saving CSV to FireStore...");
                //
                // Saving CSV to firestore
                //

                //
                // Updating JSON in the FireStore (Collection->Documents->Collection->Documents->...)
                //

            }
        });


        /*
         *
         *  Logout user:
         *
         */
        logoutImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Util.isSignedIn = false;
                Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
                Util.userEmail = "";
                startActivity( new Intent(DataCollectorActivity.this,AuthenticationActivity.class) );
            }
        });

    }// OnCreate


    // step: 1
    //region HELP
    /*
        savingAccArrayIntoCSV()
            | This method saves the accArray<Accelerometer> list
            | into .CSV file including header.
    */
    //endregion
    private void savingAccArrayIntoCSV(){
        Log.d(TAG,">>>RUN>>>savingAccArrayIntoCSV()");

        try {
            FileOutputStream f = new FileOutputStream(rawdataUserFile);
            PrintWriter pw = new PrintWriter(f);

            // Header:
            if(Util.rawDataHasHeader) {
                pw.println(Util.rawDataHeaderStr);
            }

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
        Log.d(TAG,"<<<FINISH<<<savingAccArrayIntoCSV()");
    }

    // step: 2
    //region HELP
    /*
        savingCSVtoFireBaseStorage()
            | This method uploads the .CSV file
            | to FireBase Storage.
    */
    //endregion
    private void savingCSVtoFireBaseStorage(){
        Log.d(TAG,">>>RUN>>>savingCSVtoFireBaseStorage()");
        if (checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DataCollectorActivity.this, new String[]{Manifest.permission.INTERNET}, REQUEST_CODE);
        }

        Uri path = Uri.fromFile( rawdataUserFile );

        if( path != null){
            //final ProgressDialog progressDialog = new ProgressDialog(DataCollectorActivity.this);
            //progressDialog.setTitle("Uploading...");
            //progressDialog.show();
            /*
             *
             *  Generate
             *
             */
            StorageReference ref = mStorageReference.child("files/" + path.getLastPathSegment() );
            ref.putFile(path)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //progressDialog.dismiss();
                            Log.i(TAG,"CSV Uploaded");
                            Toast.makeText(DataCollectorActivity.this, "CSV file has been saved to FireBase Storage!", Toast.LENGTH_LONG).show();
                            Log.d(TAG,"<<<FINISH<<<savingCSVtoFireBaseStorage() - onSuccess");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DataCollectorActivity.this, "Upload Failed", Toast.LENGTH_LONG).show();
                            Log.d(TAG,"<<<FINISH<<<savingCSVtoFireBaseStorage() - onFailure");
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            //progressDialog.setMessage("Uploaded " + (int)progress + "%" );
                        }
                    });
        }
    }

    // step: 3
    //region HELP
    /*
        uploadJSONintoFireBaseFireStore()
            | This method uploads the UserAndHisFile
            | object(JSON) into FireBase FireStore.
     */
    //endregion
    private void uploadJSONintoFireBaseFireStore(){
        Log.d(TAG,">>>RUN>>>uploadJSONintoFireBaseFireStore()");
        Util.deviceId = Settings.Secure.getString(DataCollectorActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);
        String randomId = UUID.randomUUID().toString();

        // Just to test Different device situations:
        //Random r = new Random();
        //deviceId += "_" + r.nextInt(100);

        // OLD:
        // mDocRef = FirebaseFirestore.getInstance().document("user_records/" + randomUserRecordID );
        // "user_records" / <userID> / <deviceID> / <randomId> / ...fields...
        mDocRef = FirebaseFirestore.getInstance()
                .collection("user_records_2/" )
                .document( mAuth.getUid() + "" )
                .collection( Util.deviceId )
                .document( randomId ) ;

        UserAndHisFile info = new UserAndHisFile(mDate.toString(), randomId);
        Log.d(TAG,"File: randomId" + randomId);

        mDocRef.set(info).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG,"Document has been saved to FireStore!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG,"Problem saving to FireStore!");
            }
        });
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
        Log.d(TAG, ">>>RUN>>>accArrayToString()");
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
        Log.d(TAG, ">>>RUN>>>accArrayGroupArrayToString()");
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
        if( !limitReached ){
            sb.append("end");
            accArrayStringGroups.add( sb.toString() );
        }
    }

    public void getIPandPort(){
        Log.d(TAG, ">>>RUN>>>getIPandPort()");
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
                socket = new Socket( inetAddress, DataCollectorActivity.wifiModulePort );
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
        Log.d(TAG, ">>>RUN>>>onStart()");
        super.onStart();

        Util.mPreferences = getSharedPreferences(Util.sharedPrefFile,MODE_PRIVATE);

    }

    @Override
    protected void onResume() {
        Log.d(TAG, ">>>RUN>>>onResume()");
        super.onResume();

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
        }
        loggedInUserEmailTextView.setText(Util.userEmail);

        sensorManager.registerListener(accelerometerEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

        // Show on screen model status
        if(Util.isSetUserModel) {
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
        }else{
            Log.d(TAG, "User Model is not set yet.");
        }
    }


    @Override
    protected void onPause() {
        Log.d(TAG, ">>>RUN>>>onPause()");
        super.onPause();

        Util.preferencesEditor = Util.mPreferences.edit();
        Util.preferencesEditor.putString(Util.LAST_LOGGED_IN_EMAIL_KEY, Util.userEmail );
        Util.preferencesEditor.putString(Util.LAST_LOGGED_IN_ID_KEY, mAuth.getUid() );
        Util.preferencesEditor.apply();

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
    }

    @Override
    public void step(long timeNs) {
        Log.d(TAG, ">>>RUN>>>step()");
        //mp.start();
        DataCollectorActivity.stepNumber++;
    }


}