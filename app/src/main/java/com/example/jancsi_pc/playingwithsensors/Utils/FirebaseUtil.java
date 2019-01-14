package com.example.jancsi_pc.playingwithsensors.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.ListDataFromFirebaseActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

public class FirebaseUtil {

    private FirebaseUtil() {
    }

    // FireStore (Beta database)
    public static final String USER_RECORDS_OLD_KEY = "user_records";
    public static final String USER_RECORDS_NEW_KEY = "user_records_2";
    public static final String USER_RECORDS_DEBUG_KEY = "user_records_debug";
    /* <user_id> */
    /* <device_id> */
    /* <random_id> */
    public static final String DATE_KEY = "date";                   // they will be used more
    public static final String FILE_ID_KEY = "fileId";              // often in UserRecordObject
    public static final String DOWNLOAD_URL_KEY = "downloadUrl";    // class

    public static final String USER_DATA_KEY = "user_data";
    /* <user_id> */
    public static final String USER_DATE_KEY = "date";                   // they will be used more
    public static final String USER_FILE_ID_KEY = "fileId";              // often in UserRecordObject
    public static final String USER_DOWNLOAD_URL_KEY = "downloadUrl";    // class

    // Storage (Files)
    public static final String STORAGE_FEATURES_KEY = "features";
    public static final String STORAGE_FILES_KEY = "files";
    public static final String STORAGE_FILES_METADATA_KEY = "files_metadata";
    public static final String STORAGE_MODELS_KEY = "models";
    public static final String STORAGE_FEATURES_DEBUG_KEY = "features_debug";
    public static final String STORAGE_FILES_DEBUG_KEY = "files_debug";
    public static final String STORAGE_MODELS_DEBUG_KEY = "models_debug";

    public static boolean fileUploadFunctionFinished = false;
    public static boolean objectUploadFunctionFinished = false;

    /**
     * This method uploads the file to FireBase Storage where the refrence is set.
     *
     * @param activity the activity context where the method will display progress messages
     * @param file     the File that will be uploaded
     * @param ref      the StorageReference where the file will be uploaded
     */
    public static void UploadFileToFirebaseStorage(Activity activity, File file, StorageReference ref) {
        String TAG = "FirebaseUtil";
        Log.d(TAG, ">>>RUN>>>UploadFileToFirebaseStorage()");

        fileUploadFunctionFinished = false;

        Uri path = Uri.fromFile(file);
        StorageTask task = null;

        if (path != null) {
            //final ProgressDialog progressDialog = new ProgressDialog(DataCollectorActivity.this);
            //progressDialog.setTitle("Uploading...");
            //progressDialog.show();
            /*
             *
             *  Generate
             *
             */

            task = ref.putFile(path)
                    .addOnSuccessListener(taskSnapshot -> {
                        //progressDialog.dismiss();
                        Util.progressDialog.dismiss();
                        Toast.makeText(activity, "File uploaded.", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "<<<FINISH(async)<<<UploadFileToFirebaseStorage - onSuccess");
                        fileUploadFunctionFinished = true;
                    })
                    .addOnFailureListener(e -> {
                        Util.progressDialog.dismiss();
                        Toast.makeText(activity, "File upload Failed!", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "<<<FINISH(async)<<<UploadFileToFirebaseStorage - onFailure");
                        fileUploadFunctionFinished = true;
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            //progressDialog.setMessage("Uploaded " + (int)progress + "%" );
                        }
                    });

        } else {
            Log.e(TAG, "ERROR: path = null");
            fileUploadFunctionFinished = true;
        }
        Log.d(TAG, "(<<<FINISH<<<)UploadFileToFirebaseStorage() - running task in background");
        /*
        if( task.isSuccessful() ){
            Log.d(TAG,"SUCCESS");
        }else {
            Log.d(TAG,"FAILURE");
        }
        */


    }

    /**
     * This method uploads the UserRecordObject object(JSON) into Firebase FireStore.
     *
     * @param activity the activity context where the method will display progress messages
     * @param info     the object that describes the required JSON object
     * @param ref      the StorageReference where the file will be uploaded
     */
    public static void UploadObjectToFirebaseFirestore(Activity activity, UserRecordObject info, DocumentReference ref) {
        String TAG = "FirebaseUtil";
        Log.d(TAG, ">>>RUN>>>UploadObjectToFirebaseFirestore()");

        objectUploadFunctionFinished = false;

        ref.set(info).addOnSuccessListener(aVoid -> {
            Util.progressDialog.dismiss();
            Toast.makeText(activity, "Object uploaded.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "<<<FINISH(async)<<<UploadObjectToFirebaseFirestore() - onSuccess");
            objectUploadFunctionFinished = true;
        }).addOnFailureListener(e -> {
            Util.progressDialog.dismiss();
            Toast.makeText(activity, "Object upload failed!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "<<<FINISH(async)<<<UploadObjectToFirebaseFirestore() - onFailure");
            objectUploadFunctionFinished = true;
        });
        Log.d(TAG, "(<<<FINISH<<<)UploadObjectToFirebaseFirestore() - running task in background");
    }

    /**
     * This method downloads a file from Firebase FireStore.
     *
     * @param activity        the activity context where the method will display progress messaged
     * @param downloadFromRef the StorageReference where the file will be downloaded from
     * @param saveToThisFile  the file that will contain the downloaded data
     */
    public static void DownloadFileFromFirebaseStorage(Activity activity, StorageReference downloadFromRef, File saveToThisFile) {
        String TAG = "FirebaseUtil";
        Log.d(TAG, ">>>RUN>>>DownloadFileFromFirebaseStorage()");

        //Util.mRef = Util.mStorage.getReference().child( /*featureFolder*/ FirebaseUtil.STORAGE_FEATURES_KEY + "/" + Util.firebaseDummyFileName );

        try {
            downloadFromRef.getFile(saveToThisFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "<<<FINISHED<<<(async)DownloadFileFromFirebaseStorage() - onSuccess");
                    Log.i(TAG, "File feature found and downloaded to: Local PATH: " + saveToThisFile.getAbsolutePath());
                    Toast.makeText(activity, "File downloaded.", Toast.LENGTH_LONG).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "<<<FINISHED<<<(async)DownloadFileFromFirebaseStorage() - onFailure");
                    Log.i(TAG, "File not found or internet problems; -> return;");
                    Toast.makeText(activity, "Download failed!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error downloading file!");
            e.printStackTrace();
            return;
        }
        Log.d(TAG, "(<<<FINISHED<<<)DownloadFileFromFirebaseStorage()");
    }

    /**
     * This method downloads a file from Firebase FireStore and runs the GaitModelBuilder to display
     * the classifiers result in percents.
     *
     * @param activity        the activity context where the method will display progress messages
     * @param downloadFromRef the StorageReference where the file will be downloaded from
     * @param saveToThisFile  the file that will contain the downloaded data
     */
    public static void DownloadFileFromFirebaseStorage_AND_CheckUserInPercentage(Activity activity, StorageReference downloadFromRef, File saveToThisFile) {
        String TAG = "FirebaseUtil";
        Log.d(TAG, ">>>RUN>>>DownloadFileFromFirebaseStorage()_AND_CheckUserInPercentage");

        //Util.mRef = Util.mStorage.getReference().child( /*featureFolder*/ FirebaseUtil.STORAGE_FEATURES_KEY + "/" + Util.firebaseDummyFileName );

        try {
            downloadFromRef.getFile(saveToThisFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "<<<FINISHED<<<(async)DownloadFileFromFirebaseStorage_AND_CheckUserInPercentage() - onSuccess");
                    Log.i(TAG, "File feature found and downloaded to: Local PATH: " + saveToThisFile.getAbsolutePath());
                    Toast.makeText(activity, "File downloaded.", Toast.LENGTH_LONG).show();

                    // Check user:
                    double percentage = Util.CheckUserInPercentage(
                            activity,
                            Util.rawdata_user_path,
                            Util.feature_user_path,
                            Util.feature_dummy_path,
                            saveToThisFile.getAbsolutePath(),
                            Util.mAuth.getUid());

                    // Show result

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                    builder1.setTitle("Gait Validation");
                    if (percentage != -1) {
                        // 0.8511111111 * 100 = 85.011111111
                        // ==> "85" ==> 85
                        String resultStr = ((percentage * 100) + "").substring(0, 2);
                        builder1.setMessage("Result: " + Integer.parseInt(resultStr) + "%");
                    } else {
                        builder1.setMessage("Result: ERROR");
                    }
                    builder1.setCancelable(true);
                    builder1.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog1, int id) {
                            dialog1.cancel();
                        }
                    });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "<<<FINISHED<<<(async)DownloadFileFromFirebaseStorage_AND_CheckUserInPercentage() - onFailure");
                    Log.i(TAG, "File not found or internet problems; -> return;");
                    Toast.makeText(activity, "Download failed!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error downloading file!");
            e.printStackTrace();
            return;
        }
        Log.d(TAG, "(<<<FINISHED<<<)DownloadFileFromFirebaseStorage_AND_CheckUserInPercentage()");
    }


    // TODO ! save here all !


    /**
     * A constant that contains the name of the Firebase/Firestore collection where user statistics
     * are stored
     *
     * @author Krisztian-Miklos Nemeth
     */
    public static final String FIRESTORE_STATS_NODE = "user_stats";

    /**
     * Function that uploads the current user's statistics in Firebase/Firestore
     *
     * @param steps the number of steps recorded in the current session; this value will be
     *              incremented to the previous value
     * @author Krisztian-Miklos Nemeth
     */
    public static void updateStatsInFirestore(int steps) {  //TODO test properly
        //query record
        DocumentReference docRef = FirebaseFirestore.getInstance()
                .collection(FIRESTORE_STATS_NODE + "/")
                .document(Util.mAuth.getUid());
        docRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("updateStatsInFirestore:", task.getResult().toString());
                        //getting existing records
                        UserStatsObject statsObject = task.getResult().toObject(UserStatsObject.class);
                        if (statsObject == null) {
                            return;
                        }
                        Log.d("RESULTED OBJECT:", statsObject.toString());
                        //updating records
                        if (statsObject.isNewSession(System.currentTimeMillis() / 1000)) {
                            statsObject.setLast_session(System.currentTimeMillis() / 1000);
                            statsObject.incrementSessions();
                        }
                        statsObject.addDevice(Util.deviceId);
                        statsObject.incrementFiles();
                        statsObject.incrementSteps(steps);

                        docRef.set(statsObject);
                    } else { //handle failure
                        //it means the user is new and does not have stats yet => creating stats
                        FirebaseUtil.createStatsInFirestore(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        updateStatsInFirestore(steps);
                    }
                });
    }

    /**
     * Function that creates an empty statistics document for a new user in Firebase/Firestore
     *
     * @param email the current user's email address
     * @author Krisztian-Miklos Nemeth
     */
    public static void createStatsInFirestore(String email) {  //TODO test properly
        FirebaseFirestore.getInstance()
                .collection(FIRESTORE_STATS_NODE + "/")
                .document(Util.mAuth.getUid())
                .set(new UserStatsObject(new ArrayList<String>(), email, 0, System.currentTimeMillis() / 1000, 0, 0));
    }

    /**
     * Function that query the given user's statistics from Firebase/Firestore
     *
     * @param userId the ID of the user whose data needs to be returned
     * @return a FirebaseUserData object containing the resulting data,
     * or null if the user does not exist
     * @author Krisztian-Miklos Nemeth
     */
    public static FirebaseUserData queryUserData(String userId) {  //TODO test properly
        //invoking a function already implemented in kotlin :D
        return ListDataFromFirebaseActivity.Companion.queryOneUsersDataFromFireStore(userId);
    }
}
