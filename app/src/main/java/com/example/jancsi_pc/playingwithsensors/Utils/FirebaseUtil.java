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
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class FirebaseUtil {

    public static final String USER_RECORDS_KEY_OLD = "user_records";
    public static final String USER_RECORDS_KEY_NEW = "user_records_2";


    //region HELP
    /*
            | This method uploads the file
            | to FireBase Storage where the refrence is set.
    */
    //endregion
    public static void UploadFileToFirebaseStorage(Activity activity, File file, StorageReference ref){
        String TAG = "Util";
        Log.d(TAG,">>>RUN>>>savingCSVtoFireBaseStorage()");

        Uri path = Uri.fromFile( file );

        if( path != null){
            //final ProgressDialog progressDialog = new ProgressDialog(DataCollectorActivity.this);
            //progressDialog.setTitle("Uploading...");
            //progressDialog.show();
            /*
             *
             *  Generate
             *
             */

            ref.putFile(path)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //progressDialog.dismiss();
                            Toast.makeText(activity, "File uploaded.", Toast.LENGTH_LONG).show();
                            Log.d(TAG,"<<<FINISH(async)<<<savingCSVtoFireBaseStorage() - onSuccess");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(activity, "File upload Failed!", Toast.LENGTH_LONG).show();
                            Log.d(TAG,"<<<FINISH(async)<<<savingCSVtoFireBaseStorage() - onFailure");
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
        Log.d(TAG,"(<<<FINISH<<<)savingCSVtoFireBaseStorage() - running task in background");
    }

    //region HELP
    /*
            | This method uploads the UserAndHisFile
            | object(JSON) into Firebase FireStore.
     */
    //endregion
    public static void UploadObjectToFirebaseFirestore(Activity activity, UserAndHisFile info, DocumentReference ref){
        String TAG = "Util";
        Log.d(TAG,">>>RUN>>>uploadJSONintoFireBaseFireStore()");

        ref.set(info).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(activity, "Object uploaded.",Toast.LENGTH_LONG).show();
                Log.d(TAG,"<<<FINISH(async)<<<uploadJSONintoFireBaseFireStore() - onSuccess");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, "Object upload failed!",Toast.LENGTH_LONG).show();
                Log.d(TAG,"<<<FINISH(async)<<<uploadJSONintoFireBaseFireStore() - onFailure");
            }
        });
        Log.d(TAG,"(<<<FINISH<<<)uploadJSONintoFireBaseFireStore() - running task in background");
    }



    // TODO ! save here all !



}
