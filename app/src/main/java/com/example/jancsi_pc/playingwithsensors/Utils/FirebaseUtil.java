package com.example.jancsi_pc.playingwithsensors.Utils;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URL;

public class FirebaseUtil {

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


    // Storage (Files)
    public static final String STORAGE_FEATURES_KEY = "features";
    public static final String STORAGE_FILES_KEY = "files";
    public static final String STORAGE_FILES_METADATA_KEY = "files_metadata";
    public static final String STORAGE_MODELS_KEY = "models";
    public static final String STORAGE_FEATURES_DEBUG_KEY = "features_debug";
    public static final String STORAGE_FILES_DEBUG_KEY = "files_debug";
    public static final String STORAGE_MODELS_DEBUG_KEY = "models_debug";

    public static boolean fileUploadFunctionFinished = false;
    public static boolean objectUploadDunctionFinished = false;

    //region HELP
    /*
            | This method uploads the file
            | to FireBase Storage where the refrence is set.
    */
    //endregion
    public static void UploadFileToFirebaseStorage(Activity activity, File file, StorageReference ref){
        String TAG = "Util";
        Log.d(TAG,">>>RUN>>>UploadFileToFirebaseStorage()");

        fileUploadFunctionFinished = false;

        Uri path = Uri.fromFile( file );
        StorageTask task = null;

        if( path != null){
            //final ProgressDialog progressDialog = new ProgressDialog(DataCollectorActivity.this);
            //progressDialog.setTitle("Uploading...");
            //progressDialog.show();
            /*
             *
             *  Generate
             *
             */

            task = ref.putFile(path)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //progressDialog.dismiss();
                            Util.progressDialog.dismiss();
                            Toast.makeText(activity, "File uploaded.", Toast.LENGTH_LONG).show();
                            Log.d(TAG,"<<<FINISH(async)<<<UploadFileToFirebaseStorage - onSuccess");
                            fileUploadFunctionFinished = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Util.progressDialog.dismiss();
                            Toast.makeText(activity, "File upload Failed!", Toast.LENGTH_LONG).show();
                            Log.d(TAG,"<<<FINISH(async)<<<UploadFileToFirebaseStorage - onFailure");
                            fileUploadFunctionFinished = true;
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            //progressDialog.setMessage("Uploaded " + (int)progress + "%" );
                        }
                    });

        }else{
            Log.e(TAG,"ERROR: path = null");
            fileUploadFunctionFinished = true;
        }
        Log.d(TAG,"(<<<FINISH<<<)UploadFileToFirebaseStorage() - running task in background");
        /*
        if( task.isSuccessful() ){
            Log.d(TAG,"SUCCESS");
        }else {
            Log.d(TAG,"FAILURE");
        }
        */


    }

    //region HELP
    /*
            | This method uploads the UserRecordObject
            | object(JSON) into Firebase FireStore.
     */
    //endregion
    public static void UploadObjectToFirebaseFirestore(Activity activity, UserRecordObject info, DocumentReference ref){
        String TAG = "Util";
        Log.d(TAG,">>>RUN>>>UploadObjectToFirebaseFirestore()");

        objectUploadDunctionFinished = false;

        ref.set(info).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Util.progressDialog.dismiss();
                Toast.makeText(activity, "Object uploaded.",Toast.LENGTH_LONG).show();
                Log.d(TAG,"<<<FINISH(async)<<<UploadObjectToFirebaseFirestore() - onSuccess");
                objectUploadDunctionFinished = true;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Util.progressDialog.dismiss();
                Toast.makeText(activity, "Object upload failed!",Toast.LENGTH_LONG).show();
                Log.d(TAG,"<<<FINISH(async)<<<UploadObjectToFirebaseFirestore() - onFailure");
                objectUploadDunctionFinished = true;
            }
        });
        Log.d(TAG,"(<<<FINISH<<<)UploadObjectToFirebaseFirestore() - running task in background");
    }



    // TODO ! save here all !



}
