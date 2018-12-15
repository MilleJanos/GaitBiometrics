package com.example.jancsi_pc.playingwithsensors;

//import ro.sapientia.gaitbiom.GaitModelBuilder;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.StepCounterPackage.StepDetector;
import com.example.jancsi_pc.playingwithsensors.StepCounterPackage.StepListener;
import com.example.jancsi_pc.playingwithsensors.Utils.Accelerometer;
import com.example.jancsi_pc.playingwithsensors.Utils.FirebaseUtil;
import com.example.jancsi_pc.playingwithsensors.Utils.MyFileRenameException;
import com.example.jancsi_pc.playingwithsensors.Utils.UserRecordObject;
import com.example.jancsi_pc.playingwithsensors.Utils.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import ro.sapientia.gaitbiom.GaitModelBuilder;
import ro.sapientia.gaitbiom.IGaitModelBuilder;
import ro.sapientia.gaitbiom.Main;
import weka.classifiers.Classifier;
import weka.core.Attribute;

//import ro.sapientia.gaitbiom.GaitModelBuilder;
//import ro.sapientia.gaitbiom.IGaitModelBuilder;


public class ModelUploaderActivity extends AppCompatActivity implements SensorEventListener, StepListener{
    private final String TAG = "ModelUploaderActivity";

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEventListener accelerometerEventListener;
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
    private TextView loggedInUserEmailTextView;
    private ImageView logoutImageView;
    private TextView reportErrorTextView;

    //private final MediaPlayer mp = MediaPlayer.create(ModelUploaderActivity.this, R.raw.sound2);

    //queue for containing the fixed number of steps that has to be processed
    //TODO

    // For Step Detecting:
    private StepDetector simpleStepDetector;
    private static final int REQUEST_CODE = 212;

    //Firebase:
    private FirebaseStorage mFirestore;            // used to upload files
    private StorageReference mStorageReference;  // to storage
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private DocumentReference mDocRef; // = FirebaseFirestore.getInstance().document("usersFiles/information");

    private Date mDate;
    private String mFileName;

    // local stored files:
    private File featureDummyFile;  // local stored dummy file from firebase
    private File rawdataUserFile;
    private File featureUserFile;   // only the path exists !
    private File modelUserFile;     // only the path exists !

    // for shared pres
    private CharSequence lastModelDate;

    // Progress:
    private ProgressDialog progressDialog;

    //Debug mode:
    Switch debugSwitch;

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

        progressDialog = new ProgressDialog(ModelUploaderActivity.this);

        /*
         *
         *   Internal files Path:
         *
         */

        mDate = new Date();

        // Create folder if not exists:
        File myInternalFilesRoot;

        myInternalFilesRoot = new File( Util.internalFilesRoot.getAbsolutePath() /*+ customDIR*/ );
        if(!myInternalFilesRoot.exists()) {
            myInternalFilesRoot.mkdirs();
            Log.i(TAG,"Path not exists (" + myInternalFilesRoot.getAbsolutePath() + ") --> .mkdirs()");
        }

        //region
        /*
            storing:
                INTERNAL STORAGE            FIREBASE STORAGE
                feature_dummy.arff          -
                deature_<userId>.arff       deature_<userId>_<date>_<time>.arff
                model_<userId>.mdl          model_<userId>_<date>_<time>.mdl
                rawdata_<userId>.csv        rawdata_<userId>_<date>_<time>.csv
        */
        //endregion
        Util.feature_dummy_path = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_dummy.arff" ;
        Util.rawdata_user_path  = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/rawdata_" + mAuth.getUid() + ".csv";
        Util.feature_user_path  = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_" + mAuth.getUid() + ".arff";  // The date and time will be added before uploading the files
        Util.model_user_path    = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/model_"   + mAuth.getUid() + ".mdl";
        //region Print this 4 paths
        Log.i(TAG,"PATH: Util.feature_dummy_path = " + Util.feature_dummy_path );
        Log.i(TAG,"PATH: Util.rawdata_user_path  = " + Util.rawdata_user_path  );
        Log.i(TAG,"PATH: Util.feature_user_path  = " + Util.feature_user_path  );
        Log.i(TAG,"PATH: Util.model_user_path    = " + Util.model_user_path    );
        //endregion

        // internal files as File type:
        featureDummyFile = new File( Util.feature_dummy_path );
        rawdataUserFile  = new File( Util.rawdata_user_path );
        featureUserFile  = new File( Util.feature_user_path );
        modelUserFile    = new File( Util.model_user_path );


        if(!featureDummyFile.exists()){
            try {
                featureDummyFile.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG,"File can't be created: " + Util.feature_dummy_path);
            }
        }
        if(!rawdataUserFile.exists()){
            try {
                rawdataUserFile.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG,"File can't be created: " + Util.rawdata_user_path);
            }
        }
        if(!featureUserFile.exists()){
            try {
                featureUserFile.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG,"File can't be created: " + Util.feature_user_path);
            }
        }
        if(!modelUserFile.exists()){
            try {
                modelUserFile.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG,"File can't be created: " + Util.model_user_path);
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

        if( accelerometerSensor == null ){
            Toast.makeText(this, "The device has no Accelerometer !", Toast.LENGTH_SHORT).show();
            finish();
        }

        textViewStatus = findViewById(R.id.model_textViewStatus);
        textViewStatus.setText(R.string.startRecording);

        startButton = findViewById(R.id.model_buttonStart);
        stopButton  = findViewById(R.id.model_buttonStop);
        saveToFirebaseButton = findViewById(R.id.model_saveToFirebaseButton);
        loggedInUserEmailTextView = findViewById(R.id.model_showLoggedInUserEmailTextView);

        logoutImageView = findViewById(R.id.model_logoutImageView);

        stopButton.setEnabled(false);
        saveToFirebaseButton.setEnabled(false);

        debugSwitch = findViewById(R.id.debugSwitch);


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

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>startButtonClickListener");
                //mediaPlayer.create(null,R.raw.start);
                //mediaPlayer.start();
                recordCount = 0;
                stepNumber = 0;
                sensorManager.registerListener(ModelUploaderActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
                accArray.clear();
                isRecording = true;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
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
                Util.recordDateAndTimeFormatted  = DateFormat.format("yyyyMMdd_HHmmss", mDate.getTime());
                isRecording = false;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                saveToFirebaseButton.setEnabled(true);
                sensorManager.unregisterListener(ModelUploaderActivity.this);
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
         *   Sending to FireBase
         *   from Start to End
         *
         */

        saveToFirebaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, ">>>RUN>>>saveToFirebaseButtonClickListener");

                progressDialog.setTitle("Progress Dialog");
                progressDialog.setMessage("Uploading");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();

                try {
                    //region Explanation
                    /*
                        We have to upload the files withDate then after upload
                        the files has to be renamed withoutDate to make sure
                        there will be no copy in the internal storage.
                     */
                    //endregion
                    if( ! renameIternalFiles_to_withDate() ){ //return false if an error occured     // will be renamed back after uploads
                        throw new MyFileRenameException("Error renaming file to \"..._<date>_<time>...\"");
                    }
                    if (checkCallingOrSelfPermission("android.permission.INTERNET") != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ModelUploaderActivity.this, new String[]{Manifest.permission.INTERNET}, Util.REQUEST_CODE);
                    }

                    // Change Debug DIR
                    String fileStorageName = "";
                    String collectionName = "";
                    if( Util.debugMode ){
                        fileStorageName = FirebaseUtil.STORAGE_FILES_DEBUG_KEY;
                        collectionName = FirebaseUtil.USER_RECORDS_DEBUG_KEY;
                    }else{
                        fileStorageName = FirebaseUtil.STORAGE_FILES_KEY;
                        collectionName = FirebaseUtil.USER_RECORDS_NEW_KEY;
                    }

                    // Saving array into .CSV file (Local):
                    Util.SaveAccArrayIntoCsvFile(accArray, rawdataUserFile);

                    // Saving CSV File to FireBase Storage:
                    StorageReference ref = mStorageReference.child( fileStorageName + "/" + rawdataUserFile.getName() );
                    FirebaseUtil.UploadFileToFirebaseStorage(ModelUploaderActivity.this, rawdataUserFile, ref);

                    // Updating (JSON) Object in the FireStore: (Collection->Documents->Collection->Documents->...)
                    String randomId = UUID.randomUUID().toString();
                    String downloadUrl = ref.getDownloadUrl().toString();
                    UserRecordObject info = new UserRecordObject(mDate.toString(), rawdataUserFile.getName(), downloadUrl);
                    mDocRef = FirebaseFirestore.getInstance()
                            .collection(collectionName + "/" )
                            .document( mAuth.getUid() + "" )
                            .collection( Util.deviceId )
                            .document( randomId ) ;
                    FirebaseUtil.UploadObjectToFirebaseFirestore(ModelUploaderActivity.this, info, mDocRef);

                    // solved in ContinueModelGenerating
                    //
                    // // TODO: VARJA BE OKET ES FUTTASSA LE EZT: !!!
                    // // Wait until these two async uploads finish !
                    // if (!renameIternalFiles_to_withoutDate()) { //return false if an error occured     // will be renamed back after uploads
                    //     Toast.makeText(ModelUploaderActivity.this,"ERROR (renamig file)",Toast.LENGTH_LONG).show();
                    //     throw new MyFileRenameException("Error renaming file to \"..._<date>_<time>...\"");
                    // }

                    /*
                     * Model generating:
                     */

                    DownloadDummyDataFromFireBaseStorage_and_GenerateModel();

                }catch( MyFileRenameException e ){progressDialog.dismiss();
                    progressDialog.dismiss();
                    Log.e(TAG,"ERROR (MyFileRenameError): File cannot be renamed !");
                    e.printStackTrace();
                }catch( Exception e ){
                    progressDialog.dismiss();
                    e.printStackTrace();
                }

                Date date = new Date();
                lastModelDate  = DateFormat.format("yyyyMMdd_HHmmss", date.getTime());
                
                // TODO: if( az utolso 4 fuggveny hibatlanul lefutott ) ==> ROLLBACK
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
                finish();
            }
        });

    }// OnCreate

    // step: 2,3 in FirebaseUtil

    // step: 4
    //region HELP
    /*
        DownloadDummyDataFromFireBaseStorage_and_GenerateModel()
            | This method downloads the
            | dummy user data (.arff) from Firebase
            | Storage

        ModelGenerating()
            | Generates the model
            | for the current signed in user.

        UploadModelToFireBaseStorage()
            | Uploads the generated model
            | to FireBase Storage.
    */
    //endregion
    private void DownloadDummyDataFromFireBaseStorage_and_GenerateModel() throws MyFileRenameException{
        Log.d(TAG,">>>RUN>>>DownloadDummyDataFromFireBaseStorage_and_GenerateModel()");
        // Dowloading Dummy Feature from FireBase Storage:
        String featureFolder;
        if( Util.debugMode ){
            featureFolder = FirebaseUtil.STORAGE_FEATURES_DEBUG_KEY;
        }else {
            featureFolder = FirebaseUtil.STORAGE_FEATURES_KEY;
        }
        Util.mRef = Util.mStorage.getReference().child( featureFolder + "/" + Util.firebaseDumyFileName );
        Log.i(TAG,"mRef = "  + Util.mRef.toString() );

        Log.d(TAG,"Downloading local dummy from FireBase Storage...");
        try {
            Util.mRef.getFile(featureDummyFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG, "Dummy feature found and downloaded: Local PATH: " + featureDummyFile.getAbsolutePath());
                    try {
                        ModelGenerating();
                    }catch (Exception e){
                        // do nothing
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG,"Dummy feature not found or internet problems; -> return;");
                    e.printStackTrace();
                    return;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error downloading dummy file!");
            e.printStackTrace();
            return;
        }
    }
    private void ModelGenerating() throws MyFileRenameException {
        Log.d(TAG,">>RUN>>>ContinueModelGenerating()");

        /*ModelBuilder*/



        //region *
        Log.i(TAG," |IN| String Util.rawdata_user_path [size:"+ new File(Util.rawdata_user_path).length() +"]= "   + Util.rawdata_user_path );
        Log.i(TAG," |IN| String Util.feature_user_path [size:"+ new File(Util.feature_user_path).length() +"]= " + Util.feature_user_path);
        //endregion
        // ModelBuilderMain.getFeatures(Util.rawdata_user_path, Util.feature_user_path.substring(0,Util.feature_user_path.length()-(".arff").length()) );  // getFeatures will add the ".arff" to the end of the file (and saves it)
        //String userId="";//TODO
        Main.createFeaturesFileFromRawFile(Util.rawdata_user_path, Util.feature_user_path.substring(0,Util.feature_user_path.length()-(".arff").length()),Util.mAuth.getUid() ); //TODO userID?
        //region *
        Log.i(TAG," |OUT| String Util.rawdata_user_path [size:"+ new File(Util.rawdata_user_path).length() +"]= "   + Util.rawdata_user_path );
        Log.i(TAG," |OUT| String Util.feature_user_path [size:"+ new File(Util.feature_user_path).length() +"]= " + Util.feature_user_path);
        //endregion

        //region *
        Log.i(TAG," |IN| String Util.feature_dummy_path [size:"+ new File(Util.feature_dummy_path).length() +"]= " + Util.feature_dummy_path);
        Log.i(TAG," |IN| String Util.feature_user_path [size:"+ new File(Util.feature_user_path).length() +"]= "  + Util.feature_user_path);
        //endregion
        // ModelBuilderMain.mergeArffFiles(Util.feature_dummy_path, Util.feature_user_path);
        Main.mergeEquallyArffFiles(Util.feature_dummy_path, Util.feature_user_path);
        //region *
        Log.i(TAG," |OUT| String Util.feature_dummy_path [size:"+ new File(Util.feature_dummy_path).length() +"]= " + Util.feature_dummy_path);
        Log.i(TAG," |OUT| String Util.feature_user_path [size:"+ new File(Util.feature_user_path).length() +"]= "  + Util.feature_user_path);
        //endregion
        try{
            //region *
            Log.i(TAG," |IN| String Util.feature_user_path [size:"+ new File(Util.feature_user_path).length() +"]= "  + Util.feature_user_path );
            Log.i(TAG," |IN| String Util.model_user_path = [size:"+ new File(Util.model_user_path).length() +"]" + Util.model_user_path);
            //endregion
            //ModelBuilderMain.CreateAndSaveModel(Util.feature_user_path, Util.model_user_path);
            IGaitModelBuilder builder = new GaitModelBuilder();
            Classifier classifier = builder.createModel(Util.feature_user_path);
            ArrayList<Attribute> attributes = builder.getAttributes(Util.feature_user_path);
            //region *
            Log.i(TAG," |OUT| String Util.feature_user_path [size:"+ new File(Util.feature_user_path).length() +"]= "  + Util.feature_user_path );
            Log.i(TAG," |OUT| String Util.model_user_path = [size:"+ new File(Util.model_user_path).length() +"]" + Util.model_user_path);
            //endregion
        }
        catch (Exception e){
            progressDialog.dismiss();
            Toast.makeText(ModelUploaderActivity.this,"Model generating failed!",Toast.LENGTH_LONG).show();
            Log.e(TAG,"ERROR: ModelBuilderMain.CreateAndSaveModel(Util.feature_user_path, Util.model_user_path)");
            e.printStackTrace();
        }
        UploadModelToFireBaseStorage();
        progressDialog.dismiss();




    }
    private void UploadModelToFireBaseStorage() throws MyFileRenameException {
        Log.d(TAG,">>>RUN>>>uploadModeltoFireBaseStorage()");

        Uri path = Uri.fromFile( modelUserFile );

        if( path != null){
            Log.i(TAG,"Uploading Model...");
            //final ProgressDialog progressDialog = new ProgressDialog(ModelUploaderActivity.this);
            //progressDialog.setTitle("Uploading...");
            //progressDialog.show();
            String filesDir;
            if( Util.debugMode ){
                filesDir = FirebaseUtil.STORAGE_MODELS_DEBUG_KEY;
            }else{
                filesDir = FirebaseUtil.STORAGE_MODELS_KEY;
            }
            StorageReference ref = mStorageReference.child( filesDir + "/" + path.getLastPathSegment() );
            ref.putFile(path)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //progressDialog.dismiss();
                            Log.i(TAG,"Model Uploaded");
                            Toast.makeText(ModelUploaderActivity.this, "Model uploaded.", Toast.LENGTH_LONG).show();
                            // Wait until these two async uploads finish !
                            if (!renameIternalFiles_to_withoutDate()) { //return false if an error occured     // will be renamed back after uploads
                                Toast.makeText(ModelUploaderActivity.this,"ERROR (renamig file)",Toast.LENGTH_LONG).show();
                                //throw new MyFileRenameException("Error renaming file to \"..._<date>_<time>...\"");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG,"CSV upload Failed");
                            Toast.makeText(ModelUploaderActivity.this, "Model upload failed!", Toast.LENGTH_LONG).show();
                            // Wait until these two async uploads finish !
                            if (!renameIternalFiles_to_withoutDate()) { //return false if an error occured     // will be renamed back after uploads
                                Toast.makeText(ModelUploaderActivity.this,"ERROR (renamig file)",Toast.LENGTH_LONG).show();
                                //throw new MyFileRenameException("Error renaming file to \"..._<date>_<time>...\"");
                            }
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            //progressDialog.setMessage("Uploaded " + (int)progress + "%" );
                        }
                    });
        }

        Log.i(TAG,"### Util.hasUserModel = true");
        Util.hasUserModel = true;
        finish();
    }

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
    private boolean renameIternalFiles_to_withDate(){
        Log.d(TAG,">>RUN>>renameIternalFiles_to_withDate()");
        File f = null;
        Util.rawdata_user_path  = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/rawdata_" + mAuth.getUid() + "_" + Util.recordDateAndTimeFormatted + ".csv";
        Util.feature_user_path  = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_" + mAuth.getUid() + "_" + Util.recordDateAndTimeFormatted + ".arff";
        Util.model_user_path    = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/model_"   + mAuth.getUid() + "_" + Util.recordDateAndTimeFormatted + ".mdl";

        try {
            f= new File( Util.rawdata_user_path );
            rawdataUserFile.renameTo(f);

            f= new File( Util.feature_user_path );
            featureUserFile.renameTo(f);

            f= new File( Util.model_user_path );
            modelUserFile.renameTo(f);
        }catch( Exception e ){
            Log.e(TAG,"renameIternalFiles_withDate() - CANNOT RENAME FILE TO: " + f.getAbsolutePath() );
            e.printStackTrace();
            Log.d(TAG,"<<FINISHED<<renameIternalFiles_to_withDate() - ERROR");
            return false;
        }
        Log.d(TAG,"<<FINISHED<<renameIternalFiles_to_withDate() - OK");
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
    private boolean renameIternalFiles_to_withoutDate(){
        Log.d(TAG,">>RUN>>renameIternalFiles_to_withoutDate()");
        File f = null;
        Util.rawdata_user_path  = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/rawdata_" + mAuth.getUid() + "_0_0" + ".csv";
        Util.feature_user_path  = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/feature_" + mAuth.getUid() + "_0_0" + ".arff";
        Util.model_user_path    = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/model_"   + mAuth.getUid() + "_0_0" + ".mdl";

        try {
            f= new File( Util.rawdata_user_path );
            rawdataUserFile.renameTo(f);

            f= new File( Util.feature_user_path );
            featureUserFile.renameTo(f);

            f= new File( Util.model_user_path );
            modelUserFile.renameTo(f);
        }catch( Exception e ){
            Log.e(TAG,"renameIternalFiles_withoutDate() - CANNOT RENAME FILE TO: " + f.getAbsolutePath() );
            e.printStackTrace();
            Log.d(TAG,"<<FINISHED<<renameIternalFiles_to_withoutDate() - ERROR");
            return false;
        }
        Log.d(TAG,"<<FINISHED<<renameIternalFiles_to_withoutDate() - OK");
        return true;
    }

    //region HELP
    /*
        accArrayToString()
            | ArrayList<Accelerometer> accArray ==> String str
            |
            | output format:   "timestamp,x,y,z,currentStepCount,timestamp,x,y,z,currentStepCount,timestamp,x,y,z,timestamp,currentStepCount, ... ,end"
     */
    //endregion
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
            //Util.isFinished = false;    // TODO: VIGYAZZ MERT ITT MEGSZAKITJA A LANCOLT KILEPEST, MIVEL EZ AZ UTOLSO ACTIVITY A STACKBEN, ugy(e nelkul) ujrainditaskor is kikapcsolt
            finish();
        }

        // Admin Mode:
        if( Util.isAdminLoggedIn ){
            debugSwitch.setChecked(false);
            debugSwitch.setVisibility(View.VISIBLE);
            debugSwitch = findViewById(R.id.debugSwitch);
            debugSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Util.debugMode = true;
                        Log.i(TAG, "Debug Mode -> ON");
                    } else {
                        Util.debugMode = false;
                        Log.i(TAG, "Debug Mode -> OFF");
                    }
                }
            });
        }else{
            debugSwitch.setChecked(false);
            debugSwitch.setVisibility(View.INVISIBLE);
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
        Log.d(TAG, "SAVE to Shared Pref" + lastModelDate.toString() );
        Log.d(TAG, "SAVE to Shared Pref" + Util.userEmail );
        Log.d(TAG, "SAVE to Shared Pref" + mAuth.getUid() );
        Util.preferencesEditor.putString(Util.LAST_MODEL_DATE_KEY, lastModelDate.toString() );
        Util.preferencesEditor.putString(Util.LAST_MODEL_EMAIL_KEY, Util.userEmail );
        Util.preferencesEditor.putString(Util.LAST_MODEL_ID_KEY, mAuth.getUid() );
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
        ModelUploaderActivity.stepNumber++;
    }


}