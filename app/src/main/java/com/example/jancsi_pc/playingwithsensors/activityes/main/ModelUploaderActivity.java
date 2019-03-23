package com.example.jancsi_pc.playingwithsensors.activityes.main;

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
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
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
import com.example.jancsi_pc.playingwithsensors.utils.firebase.FirebaseUtil;
import com.example.jancsi_pc.playingwithsensors.utils.firebase.UserRecordObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import ro.sapientia.gaitbiom.GaitHelperFunctions;
import ro.sapientia.gaitbiom.GaitModelBuilder;
import ro.sapientia.gaitbiom.IGaitModelBuilder;
import weka.classifiers.Classifier;

/**
 * Activity that handle the user model: download model, upload model, generate model.
 *
 * @author MilleJanos
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
    private boolean doubleBackToExitPressedOnce = false;
    // For Step Detecting:
    private StepDetector simpleStepDetector;
    //Firebase:
    private FirebaseStorage mFirestore;            // used to upload files
    private StorageReference mStorageReference;  // to storage
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DocumentReference mDocRef; // = FirebaseFirestore.getInstance().document("usersFiles/information");
    private Date mDate;
    // for shared pres
    private CharSequence lastModelDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, ">>>RUN>>>onCreate()");
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_uploader);

        Util.addToDebugActivityStackList(TAG);

        Util.progressDialog = new ProgressDialog(ModelUploaderActivity.this);


        // Internal files Path:
        Util.initInternalFiles();

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
            mDate = new Date();
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

                // Get debug value from shared pref
                String debugModeStr = Util.mSharedPref.getString(Util.SETTING_DEBUG_MODE_KEY, null);
                if (debugModeStr == null) {                                 // If was not set yet(in shared pref)
                    Log.i(TAG, "debugModeStr= " + null);
                    Util.debugMode = false;
                } else {
                    Log.i(TAG, "debugModeStr= " + "\"" + debugModeStr + "\"");
                    int debugModeInt = Integer.parseInt(debugModeStr);
                    Log.i(TAG, "debugModeInt= " + debugModeInt);
                    Util.debugMode = debugModeInt == 1;
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
                Util.saveAccArrayIntoCsvFile(accArray, Util.rawdataUserFile);

                // Uploading CSV File to FireBase Storage:
                StorageReference ref = mStorageReference.child(fileStorageName + "/" + Util.rawdataUserFile.getName());
                FirebaseUtil.uploadFileToFirebaseStorage(ModelUploaderActivity.this, Util.rawdataUserFile, ref);

                // Updating (JSON) Object in the FireStore: (Collection->Documents->Collection->Documents->...)
                String randomId = UUID.randomUUID().toString();
                String downloadUrl = ref.getDownloadUrl().toString();
                UserRecordObject info = new UserRecordObject(mDate.toString(), Util.rawdataUserFile.getName(), downloadUrl);
                mDocRef = FirebaseFirestore.getInstance()
                        .collection(collectionName + "/")
                        .document(mAuth.getUid() + "")
                        .collection(Util.deviceId)
                        .document(randomId);
                FirebaseUtil.uploadObjectToFirebaseFirestore(ModelUploaderActivity.this, info, mDocRef);


                /*
                 * Model generating:
                 */

                downloadNegativeDataFromFireBaseStorage_and_GenerateModel();

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
     * downloadNegativeDataFromFireBaseStorage_and_GenerateModel()
     * | This method downloads the
     * | dummy user data (.arff) from Firebase
     * | Storage
     * <p>
     * ModelGenerating()
     * | Generates the model
     * | for the current signed in user.
     * <p>
     * uploadModelToFireBaseStorage()
     * | Uploads the generated model
     * | to FireBase Storage.
     *
     * @author Mille Janos
     */
    private void downloadNegativeDataFromFireBaseStorage_and_GenerateModel() {
        Log.d(TAG, ">>>RUN>>>downloadNegativeDataFromFireBaseStorage_and_GenerateModel()");
        // Downloading Dummy Feature from FireBase Storage:
        // Dummy is always in features folder (not in features_debug)
        Util.mRef = Util.mStorage.getReference().child( /*featureFolder*/ FirebaseUtil.STORAGE_FEATURES_KEY + "/" + Util.firebaseDummyFileName);
        Log.i(TAG, "DUMMY: mRef = " + Util.mRef.toString());

        Log.d(TAG, "Downloading local dummy from FireBase Storage...");
        try {
            Util.mRef.getFile(Util.featureDummyFile).addOnSuccessListener(taskSnapshot -> {
                Log.i(TAG, "Dummy feature found and downloaded: Local PATH: " + Util.featureDummyFile.getAbsolutePath());
                try {

                    modelBuilder();

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
     * Builds the model using the modelBuilder library.
     *
     * @author Mille Janos
     */
    private void modelBuilder() {
        Log.d(TAG,">>RUN>>>ContinueModelGenerating()");

        //region *
        Log.i(TAG," |IN| String Util.rawdata_user_path [size:"+ new File(Util.rawdata_user_path).length() +"]= "   + Util.rawdata_user_path );
        Log.i(TAG," |IN| String Util.feature_user_path [size:"+ new File(Util.feature_user_path).length() +"]= " + Util.feature_user_path);
        //endregion
        GaitHelperFunctions.createFeaturesFileFromRawFile(
                Util.rawdata_user_path,                                                                 // INPUT
                Util.feature_user_path.substring(0,Util.feature_user_path.length()-(".arff").length()), // OUTPUT       // getFeatures will add the ".arff" to the end of the file (and saves it)
                Util.mAuth.getUid() );                                                                  // INPUT
        //region *
        Log.i(TAG," |OUT| String Util.rawdata_user_path [size:"+ new File(Util.rawdata_user_path).length() +"]= "   + Util.rawdata_user_path );
        Log.i(TAG," |OUT| String Util.feature_user_path [size:"+ new File(Util.feature_user_path).length() +"]= " + Util.feature_user_path);
        //endregion
        //region *
        Log.i(TAG," |IN| String Util.feature_dummy_path [size:"+ new File(Util.feature_dummy_path).length() +"]= " + Util.feature_dummy_path);
        Log.i(TAG," |IN| String Util.feature_user_path [size:"+ new File(Util.feature_user_path).length() +"]= "  + Util.feature_user_path);
        //endregion
        GaitHelperFunctions.mergeEquallyArffFiles(
                Util.feature_dummy_path,    // INPUT
                Util.feature_user_path);    // INPUT & OUTPUT
        //region *
        Log.i(TAG," |OUT| String Util.feature_dummy_path [size:"+ new File(Util.feature_dummy_path).length() +"]= " + Util.feature_dummy_path);
        Log.i(TAG," |OUT| String Util.feature_user_path [size:"+ new File(Util.feature_user_path).length() +"]= "  + Util.feature_user_path);
        //endregion
        try{
            //region *
            Log.i(TAG," |IN| String Util.feature_user_path [size:"+ new File(Util.feature_user_path).length() +"]= "  + Util.feature_user_path );
            Log.i(TAG," |IN| String Util.model_user_path = [size:"+ new File(Util.model_user_path).length() +"]" + Util.model_user_path);
            //endregion
            IGaitModelBuilder builder = new GaitModelBuilder();
            //region **
            Log.d(TAG,">RUN>builder.createModel()");
            //endregion
            // Creates the model from feature
            Classifier classifier = builder.createModel(Util.feature_user_path);
            //region **
            Log.d(TAG,"<FINISH<builder.createModel()");
            //endregion
            // Save model to file
            //region **
            Log.d(TAG,">RUN>builder.saveModel()");
            //endregion
            ((GaitModelBuilder) builder).saveModel(classifier, Util.model_user_path );
            //region **
            Log.d(TAG,"<FINISHED<builder.saveModel()");
            //endregion
            //region *
            Log.i(TAG," |OUT| String Util.feature_user_path [size:"+ new File(Util.feature_user_path).length() +"]= "  + Util.feature_user_path );
            Log.i(TAG," |OUT| String Util.model_user_path = [size:"+ new File(Util.model_user_path).length() +"]" + Util.model_user_path);
            //endregion
        }
        catch (Exception e){
            Util.progressDialog.dismiss();
            Toast.makeText(ModelUploaderActivity.this,"Model generating failed!",Toast.LENGTH_LONG).show();
            Log.e(TAG,"ERROR: ModelBuilderMain.CreateAndSaveModel(Util.feature_user_path, Util.model_user_path)");
            e.printStackTrace();
        }


        //UploadModelToFireBaseStorage();


        Util.progressDialog.dismiss();

    }

    /**
     * Uploads the model to Firebase storage.
     *
     * @author Mille Janos
     */
    private void uploadModelToFireBaseStorage() {
        Log.d(TAG, ">>>RUN>>>uploadModeltoFireBaseStorage()");
        Util.progressDialog.dismiss();
        Util.progressDialog = new ProgressDialog(ModelUploaderActivity.this, ProgressDialog.STYLE_SPINNER);
        Util.progressDialog.setTitle("Model Generated");
        Util.progressDialog.setMessage("Uploading Model");
        Util.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        Util.progressDialog.show();

        Uri path = Uri.fromFile(Util.modelUserFile);

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
                        //if (!renameIternalFiles_to_withoutDate()) { //return false if an error occured     // will be renamed back after uploads
                        //    Toast.makeText(ModelUploaderActivity.this, "ERROR (renamig file)", Toast.LENGTH_LONG).show();
                        //    //throw new MyFileRenameException("Error renaming file to \"..._<date>_<time>...\"");
                        //}
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "CSV upload Failed");
                        Toast.makeText(ModelUploaderActivity.this, "Model upload failed!", Toast.LENGTH_LONG).show();
                        //if (!renameIternalFiles_to_withoutDate()) { //return false if an error occured     // will be renamed back after uploads
                        //    Toast.makeText(ModelUploaderActivity.this, "ERROR (renamig file)", Toast.LENGTH_LONG).show();
                        //}
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
            Util.rawdataUserFile.renameTo(f);

            f = new File(Util.feature_user_path);
            Util.featureUserFile.renameTo(f);

            f = new File(Util.model_user_path);
            Util.modelUserFile.renameTo(f);
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
            Util.rawdataUserFile.renameTo(f);

            f = new File(Util.feature_user_path);
            Util.featureUserFile.renameTo(f);

            f = new File(Util.model_user_path);
            Util.modelUserFile.renameTo(f);
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
    public void onDestroy(){
        Util.removeFromDebugActivityStackList(TAG);
        super.onDestroy();
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