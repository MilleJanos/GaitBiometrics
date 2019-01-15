package com.example.jancsi_pc.playingwithsensors;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.Utils.FirebaseUtil;
import com.example.jancsi_pc.playingwithsensors.Utils.UserDataObject;
import com.example.jancsi_pc.playingwithsensors.Utils.Util;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

/**
 * This Activity is responsible in editing the user`s personal data
 *
 * @author Fulop Timea
 */

public class EditUserActivity extends AppCompatActivity {

    private static ImageView editUserImageBig;
    private ImageView editUserImageSmall;

    private static EditText userNameEditText;

    private Button cancelButton;
    private Button saveButton;
    private Button backButton;

    private String imagepath=null;

    private String TAG = "EditUserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        userNameEditText = findViewById(R.id.user_name_edit_text);

        backButton = findViewById(R.id.edit_profile_backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cancelButton =findViewById(R.id.cancel_edit_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(EditUserActivity.this, UserProfileActivity.class);
                //startActivity(intent);
                finish();
            }
        });

        saveButton = findViewById(R.id.submit_edit_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = userNameEditText.getText().toString();
                Date date = new Date();
                UserDataObject udo = new UserDataObject(date.toString(),"-1","null", username);
                DocumentReference ref = FirebaseFirestore.getInstance()
                        .collection(FirebaseUtil.USER_DATA_KEY + "/")
                        .document(Util.mAuth.getUid() + "");
                Log.e(TAG,"DocumentReference ref = " + ref.toString() );
                FirebaseUtil.UploadObjectToFirebaseFirestore(EditUserActivity.this, udo, ref);
                finish();
            }
        });

        editUserImageBig = findViewById(R.id.edit_user_image_big);
        editUserImageSmall = findViewById(R.id.edit_user_image_small);

        editUserImageBig.setOnClickListener(uploadImageOnClickListener);
        editUserImageSmall.setOnClickListener(uploadImageOnClickListener);

    }

    private View.OnClickListener uploadImageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), 1);
            /*Context context = getApplicationContext();
            CharSequence text = "Hello toast!";
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();*/
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {
            //Bitmap photo = (Bitmap) data.getData().getPath();
            //Uri imagename=data.getData();
            Uri selectedImageUri = data.getData();
            //imagepath = getPath(selectedImageUri);
            //Bitmap bitmap=BitmapFactory.decodeFile(imagepath);

            editUserImageBig.setImageURI(selectedImageUri);
            /*if(bitmap != null) {
                editUserImageBig.setImageBitmap(bitmap);
            }*/

            //final File file = new File(Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + "/lastuserimage.jpg");
            //File auxFile = new File(selectedImageUri.getPath());
            //assertEquals(file.getAbsolutePath(), auxFile.getAbsolutePath());
            Log.i(TAG, "selectedImageUri = " + selectedImageUri);


            String destinationFilename = Util.internalFilesRoot.getAbsolutePath() + "/" + Util.customDIR + "last_user_profile_image.jpg";
            Log.i(TAG,"destinationFilename = " + destinationFilename );
            Log.i(TAG,"selectedImageUri.getPath() = " + selectedImageUri.getPath() );
            Log.i(TAG,"selectedImageUri.toString()  = " + selectedImageUri.toString() );
            //SaveUriToFile(selectedImageUri,destinationFilename);


            try {
                File file = new File(new URI(selectedImageUri.getPath()));

                FirebaseStorage storage = Util.mStorage;
                StorageReference ref  = storage.getReference();
                ref = ref.child( "TEST" + "/");

                FirebaseUtil.UploadFileToFirebaseStorage(EditUserActivity.this, file, ref);
            }catch (Exception e){
                Log.e(TAG,"***ERROR CREATING FILE FROM URI");
            }
        }
    }
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    void SaveUriToFile(Uri sourceUri, String destinationFilePath)
    {
        Log.i(TAG,">>>RUN>>>SaveUriToFile");
        String sourceFilename= sourceUri.getPath();
        // /*String */ destinationFilePath = Util.internalFilesRoot.getAbsolutePath() + Util.customDIR + File.separatorChar + "last_user_profile_image.jpg";

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(sourceFilename));
            bos = new BufferedOutputStream(new FileOutputStream(destinationFilePath, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while(bis.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG,"<<<FINISHED<<<SaveUriToFile");
    }

    /**
     *  This method download the data of the user from Firebase Firestore
     */
    public void DownloadUserDataFromFirebase(){
        // Name:


        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection(  FirebaseUtil.USER_DATA_KEY + "/")
                .document(Util.mAuth.getUid() + "");
        FirebaseUtil.DownloadUserDataObjectFromFirebaseFirestore_AND_SetTheResult(EditUserActivity.this, ref, 1);   // 1 = from EditUserActivity
        // Image:

        // Stats
    }

    /**
     * This method updates the User Interface.
     */
    public static void UpdateUserDataObject(){
        userNameEditText.setText( Util.mUserDataObject_Temp.userName);
        userNameEditText.setEnabled(true);
    }

    @Override
    protected void onStart(){
        super.onStart();
        userNameEditText.setEnabled(false);
        userNameEditText.setText("Loading...");
        DownloadUserDataFromFirebase();
    }

}
