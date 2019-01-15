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
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
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

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Activity that handle the user model: download model, upload model, generate model.
 */

public class ModelUploaderActivity extends AppCompatActivity implements SensorEventListener, StepListener {
    private final String TAG = "ModelUploaderActivity";

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEventListener accelerometerEventListener;
    private Button startButton;
    private Button stopButton;
    private Button saveToFirebaseButton;
    public static String CMD = "0";
    private boolean isRecording = false;
    private ArrayList<Accelerometer> accArray = new ArrayList<>();
    private long recordCount = 0;
    public static int stepNumber = 0;
    private TextView textViewStatus;
    private TextView loggedInUserEmailTextView;
    private ImageView logoutImageView;
    private TextView reportErrorTextView;

    // For Step Detecting:
    private StepDetector simpleStepDetector;

    //Firebase:
    private FirebaseStorage mFirestore;            // used to upload files
    private StorageReference mStorageReference;  // to storage
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DocumentReference mDocRef; // = FirebaseFirestore.getInstance().document("usersFiles/information");

    private Date mDate;

    // local stored files:
    private File featureDummyFile;  // local stored dummy file from firebase
    private File rawdataUserFile;
    private File featureUserFile;   // only the path exists !
    private File modelUserFile;     // only the path exists !

    // for shared pres
    private CharSequence lastModelDate = "";


    /*
     *
     *   OnCreate
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, ">>>RUN>>>onCreate()");
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_uploader);

        Util.progressDialog = new ProgressDialog(ModelUploaderActivity.this);

        /*
         *
         *   Internal files Path:
         *
         */

        mDate = new Date();

        // Create folder if not exists:
        File myInternalFilesRoot;

        myInternalFilesRoot = new File(Util.internalFilesRoot.getAbsolutePath() /*+ customDIR*/);
        if (!myInternalFilesRoot.exists()) {
            myInternalFilesRoot.mkdirs();
            Log.i(TAG, "Path not exists (" + myInternalFilesRoot.getAbsolutePath() + ") --> .mkdirs()");
        }


        //endregion
        Util.feature_dummy_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_dummy.arff";
        Util.rawdata_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/rawdata_" + mAuth.getUid() + ".csv";
        Util.feature_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_" + mAuth.getUid() + ".arff";  // The date and time will be added before uploading the files
        Util.model_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/model_" + mAuth.getUid() + ".mdl";
        //region Print this 4 paths
        Log.i(TAG, "PATH: Util.feature_dummy_path = " + Util.feature_dummy_path);
        Log.i(TAG, "PATH: Util.rawdata_user_path  = " + Util.rawdata_user_path);
        Log.i(TAG, "PATH: Util.feature_user_path  = " + Util.feature_user_path);
        Log.i(TAG, "PATH: Util.model_user_path    = " + Util.model_user_path);
        //endregion

        // internal files as File type:
        featureDummyFile = new File(Util.feature_dummy_path);
        rawdataUserFile = new File(Util.rawdata_user_path);
        featureUserFile = new File(Util.feature_user_path);
        modelUserFile = new File(Util.model_user_path);


        if (!featureDummyFile.exists()) {
            try {
                featureDummyFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + Util.feature_dummy_path);
            }
        }
        if (!rawdataUserFile.exists()) {
            try {
                rawdataUserFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + Util.rawdata_user_path);
            }
        }
        if (!featureUserFile.exists()) {
            try {
                featureUserFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + Util.feature_user_path);
            }
        }
        if (!modelUserFile.exists()) {
            try {
                modelUserFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "File can't be created: " + Util.model_user_path);
            }
        }

        /*
         *
         *   Firebase Init
         *
         */
        mFirestore = FirebaseStorage.getInstance();
        mStorageReference = mFirestore.getReference();

        /*
         *
         *   Sensor
         *
         */
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometerSensor == null) {
            Toast.makeText(this, "The device has no Accelerometer !", Toast.LENGTH_SHORT).show();
            finish();
        }

        textViewStatus = findViewById(R.id.model_textViewStatus);
        textViewStatus.setText(R.string.startRecording);

        startButton = findViewById(R.id.model_buttonStart);
        stopButton = findViewById(R.id.model_buttonStop);
        saveToFirebaseButton = findViewById(R.id.model_saveToFirebaseButton);
        loggedInUserEmailTextView = findViewById(R.id.model_showLoggedInUserEmailTextView);

        logoutImageView = findViewById(R.id.model_logoutImageView);

        stopButton.setEnabled(false);
        saveToFirebaseButton.setEnabled(false);


        final DecimalFormat df = new DecimalFormat("0");
        df.setMaximumIntegerDigits(20);

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
                long timeStamp = event.timestamp;
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
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
            recordCount = 0;
            stepNumber = 0;
            sensorManager.registerListener(ModelUploaderActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
            accArray.clear();
            isRecording = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            saveToFirebaseButton.setEnabled(false);
            Log.d("ConnectionActivity_", "Start Rec.");
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
            saveToFirebaseButton.setEnabled(true);
            sensorManager.unregisterListener(ModelUploaderActivity.this);
            Log.d("ConnectionActivity", "Stop Rec. - Generating CMD");
            textViewStatus.setText(R.string.calculating);
            CMD = accArrayToString();
            CMD += ",end";
            Log.d("ConnectionActivity", "CMD Generated.");
            textViewStatus.setText(("Recorded: " + recordCount + " datapoints and " + stepNumber + " step cycles."));
        });

        /*
         *
         *   Sending to FireBase
         *   from Start to End
         *
         */

        saveToFirebaseButton.setOnClickListener(v -> {
            Log.d(TAG, ">>>RUN>>>saveToFirebaseButtonClickListener");

            Util.progressDialog = new ProgressDialog(ModelUploaderActivity.this, ProgressDialog.STYLE_SPINNER);
            Util.progressDialog.setTitle("Progress Dialog");
            Util.progressDialog.setMessage("Generating feature and model");
            Util.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            Util.progressDialog.show();

            try {
                if (checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ModelUploaderActivity.this, new String[]{Manifest.permission.INTERNET}, Util.REQUEST_CODE);
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
                FirebaseUtil.UploadFileToFirebaseStorage(ModelUploaderActivity.this, rawdataUserFile, ref);

                // Updating (JSON) Object in the FireStore: (Collection->Documents->Collection->Documents->...)
                String randomId = UUID.randomUUID().toString();
                String downloadUrl = ref.getDownloadUrl().toString();
                UserRecordObject info = new UserRecordObject(mDate.toString(), rawdataUserFile.getName(), downloadUrl);
                mDocRef = FirebaseFirestore.getInstance()
                        .collection(collectionName + "/")
                        .document(mAuth.getUid() + "")
                        .collection(Util.deviceId)
                        .document(randomId);
                FirebaseUtil.UploadObjectToFirebaseFirestore(ModelUploaderActivity.this, info, mDocRef);


                /*
                 * Model generating:
                 */

                DownloadDummyDataFromFireBaseStorage_and_GenerateModel();

            }
            catch (Exception e) {
                Util.progressDialog.dismiss();
                e.printStackTrace();
            }

            Date date = new Date();
            lastModelDate = DateFormat.format("yyyyMMdd_HHmmss", date.getTime());

        });


        /**
         * Logout user:
         */
        logoutImageView.setOnClickListener(v -> {
            mAuth.signOut();
            Util.isSignedIn = false;
            Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
            Util.userEmail = "";
            Util.validatedOnce = false;
            finish();
        });

    }// OnCreate


    /**
     * DownloadDummyDataFromFireBaseStorage_and_GenerateModel()
     * | This method downloads the
     * | dummy user data (.arff) from Firebase
     * | Storage
     * <p>
     * ModelGenerating()
     * | Generates the model
     * | for the current signed in user.
     * <p>
     * UploadModelToFireBaseStorage()
     * | Uploads the generated model
     * | to FireBase Storage.
     *
     * @author Mille Janos
     */
    private void DownloadDummyDataFromFireBaseStorage_and_GenerateModel() {
        Log.d(TAG, ">>>RUN>>>DownloadDummyDataFromFireBaseStorage_and_GenerateModel()");
        // Downloading Dummy Feature from FireBase Storage:
        // Dummy is always in features folder (not in features_debug)
        Util.mRef = Util.mStorage.getReference().child( /*featureFolder*/ FirebaseUtil.STORAGE_FEATURES_KEY + "/" + Util.firebaseDummyFileName);
        Log.i(TAG, "DUMMY: mRef = " + Util.mRef.toString());

        Log.d(TAG, "Downloading local dummy from FireBase Storage...");
        try {
            Util.mRef.getFile(featureDummyFile).addOnSuccessListener(taskSnapshot -> {
                Log.i(TAG, "Dummy feature found and downloaded: Local PATH: " + featureDummyFile.getAbsolutePath());
                try {
                    ModelBuilder();
                } catch (Exception e) {
                    // do nothing
                }
            }).addOnFailureListener(e -> {
                Log.i(TAG, "Dummy feature not found or internet problems; -> return;");
                e.printStackTrace();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error downloading dummy file!");
            e.printStackTrace();
        }
    }

    /**
     * Builds the model using the ModelBuilder library.
     *
     * @author Mille Janos
     */
    private void ModelBuilder() {
        Log.d(TAG, ">>RUN>>>ContinueModelGenerating()");

        Toast.makeText(ModelUploaderActivity.this, "- under development -", Toast.LENGTH_SHORT).show();
    }

    /**
     * Uploads the model to Firebase storage.
     *
     * @author Mille Janos
     */
    private void UploadModelToFireBaseStorage() {
        Log.d(TAG, ">>>RUN>>>uploadModeltoFireBaseStorage()");
        Util.progressDialog.dismiss();
        Util.progressDialog = new ProgressDialog(ModelUploaderActivity.this, ProgressDialog.STYLE_SPINNER);
        Util.progressDialog.setTitle("Model Generated");
        Util.progressDialog.setMessage("Uploading Model");
        Util.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        Util.progressDialog.show();

        Uri path = Uri.fromFile(modelUserFile);

        if (path != null) {
            Log.i(TAG, "Uploading Model...");
            String filesDir;
            if (Util.debugMode) {
                filesDir = FirebaseUtil.STORAGE_MODELS_DEBUG_KEY;
            } else {
                filesDir = FirebaseUtil.STORAGE_MODELS_KEY;
            }
            StorageReference ref = mStorageReference.child(filesDir + "/" + path.getLastPathSegment());
            ref.putFile(path)
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.i(TAG, "Model Uploaded");
                        Toast.makeText(ModelUploaderActivity.this, "Model uploaded.", Toast.LENGTH_LONG).show();
                        if (!renameIternalFiles_to_withoutDate()) { //return false if an error occured     // will be renamed back after uploads
                            Toast.makeText(ModelUploaderActivity.this, "ERROR (renamig file)", Toast.LENGTH_LONG).show();
                            //throw new MyFileRenameException("Error renaming file to \"..._<date>_<time>...\"");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "CSV upload Failed");
                        Toast.makeText(ModelUploaderActivity.this, "Model upload failed!", Toast.LENGTH_LONG).show();
                        if (!renameIternalFiles_to_withoutDate()) { //return false if an error occured     // will be renamed back after uploads
                            Toast.makeText(ModelUploaderActivity.this, "ERROR (renamig file)", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(taskSnapshot -> {
                    });
        }

        Log.i(TAG, "### Util.hasUserModel = true");
        Util.hasUserModel = true;
        finish();
    }

    /**
     * renameIternalFiles_withDate()
     * | Before upload add "_<date>_<time>" to the end of the file (and path)
     * | ( After reload rename it back! )
     * | return:
     * |   true - No errors
     * |   false - Error
     *
     * @return true if the operation finishes successfully
     *
     * @author Mille Janos
     */
    private boolean renameIternalFiles_to_withDate() {
        Log.d(TAG, ">>RUN>>renameIternalFiles_to_withDate()");
        File f = null;
        Util.rawdata_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/rawdata_" + mAuth.getUid() + "_" + Util.recordDateAndTimeFormatted + ".csv";
        Util.feature_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_" + mAuth.getUid() + "_" + Util.recordDateAndTimeFormatted + ".arff";
        Util.model_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/model_" + mAuth.getUid() + "_" + Util.recordDateAndTimeFormatted + ".mdl";

        try {
            f = new File(Util.rawdata_user_path);
            rawdataUserFile.renameTo(f);

            f = new File(Util.feature_user_path);
            featureUserFile.renameTo(f);

            f = new File(Util.model_user_path);
            modelUserFile.renameTo(f);
        } catch (Exception e) {
            Log.e(TAG, "renameIternalFiles_withDate() - CANNOT RENAME FILE TO: " + f.getAbsolutePath());
            e.printStackTrace();
            Log.d(TAG, "<<FINISHED<<renameIternalFiles_to_withDate() - ERROR");
            return false;
        }
        Log.d(TAG, "<<FINISHED<<renameIternalFiles_to_withDate() - OK");
        return true;
    }

    /**
     * renameIternalFiles_withDate()
     * | ( Before upload add "_<date>_<time>" to the end of the file (and path) )
     * | After reload rename it back!
     * | return:
     * |   true - No errors
     * |   false - Error
     *
     * @return true if the operation finishes successfully
     *
     * @author Mille Janos
     */
    private boolean renameIternalFiles_to_withoutDate() {
        Log.d(TAG, ">>RUN>>renameIternalFiles_to_withoutDate()");
        File f = null;
        Util.rawdata_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/rawdata_" + mAuth.getUid() + "_0_0" + ".csv";
        Util.feature_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_" + mAuth.getUid() + "_0_0" + ".arff";
        Util.model_user_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/model_" + mAuth.getUid() + "_0_0" + ".mdl";

        try {
            f = new File(Util.rawdata_user_path);
            rawdataUserFile.renameTo(f);

            f = new File(Util.feature_user_path);
            featureUserFile.renameTo(f);

            f = new File(Util.model_user_path);
            modelUserFile.renameTo(f);
        } catch (Exception e) {
            Log.e(TAG, "renameIternalFiles_withoutDate() - CANNOT RENAME FILE TO: " + f.getAbsolutePath());
            e.printStackTrace();
            Log.d(TAG, "<<FINISHED<<renameIternalFiles_to_withoutDate() - ERROR");
            return false;
        }
        Log.d(TAG, "<<FINISHED<<renameIternalFiles_to_withoutDate() - OK");
        return true;
    }

    /**
     * accArrayToString()
     * | ArrayList<Accelerometer> accArray ==> String str
     * |
     * | output format:   "timestamp,x,y,z,currentStepCount,timestamp,x,y,z,currentStepCount,timestamp,x,y,z,timestamp,currentStepCount, ... ,end"
     *
     * @return the custom string representation of accArray
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

    @Override
    public void onStart() {
        Log.d(TAG, ">>>RUN>>>onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, ">>>RUN>>>onResume()");
        super.onResume();

        if (Util.isFinished) {
            Log.d(TAG, " isFinished() = true");
            finish();
        }

        // Check if user is signed in (non-null) and update UI accordingly.
        if (!Util.isSignedIn) {
            Util.screenMode = Util.ScreenModeEnum.EMAIL_MODE;
            Intent intent = new Intent(ModelUploaderActivity.this, AuthenticationActivity.class);
            startActivity(intent);
        }
        loggedInUserEmailTextView.setText(Util.userEmail);

        sensorManager.registerListener(accelerometerEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);


    }

    @Override
    protected void onPause() {
        Log.d(TAG, ">>>RUN>>>onPause()");
        super.onPause();
        Log.d(TAG, "SAVE to Shared Pref" + lastModelDate.toString());
        Log.d(TAG, "SAVE to Shared Pref" + Util.userEmail);
        Log.d(TAG, "SAVE to Shared Pref" + mAuth.getUid());
        Util.mSharedPrefEditor.putString(Util.LAST_MODEL_DATE_KEY, lastModelDate.toString());
        Util.mSharedPrefEditor.putString(Util.LAST_MODEL_EMAIL_KEY, Util.userEmail);
        Util.mSharedPrefEditor.putString(Util.LAST_MODEL_ID_KEY, mAuth.getUid());
        Util.mSharedPrefEditor.apply();

        sensorManager.unregisterListener(accelerometerEventListener);
    }


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

    /**
     * Increases the stepNumber variable by 1.
     * @param timeNs non
     */
    @Override
    public void step(long timeNs) {
        Log.d(TAG, ">>>RUN>>>step()");
        //mp.start();
        ModelUploaderActivity.stepNumber++;
    }


}