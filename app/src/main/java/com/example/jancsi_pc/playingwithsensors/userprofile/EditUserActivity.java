package com.example.jancsi_pc.playingwithsensors.userprofile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.jancsi_pc.playingwithsensors.R;
import com.example.jancsi_pc.playingwithsensors.utils.Util;
import com.example.jancsi_pc.playingwithsensors.utils.firebase.FirebaseUtil;
import com.example.jancsi_pc.playingwithsensors.utils.firebase.UserDataObject;
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

    /**
     * This method updates the User Interface.
     */
    public static void updateUserDataObject() {
        userNameEditText.setText(Util.mUserDataObject_Temp.userName);
        userNameEditText.setEnabled(true);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        Util.addToDebugActivityStackList(TAG);

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
                FirebaseUtil.uploadObjectToFirebaseFirestore(EditUserActivity.this, udo, ref);
                finish();
            }
        });

        editUserImageBig = findViewById(R.id.edit_user_image_big);
        editUserImageSmall = findViewById(R.id.edit_user_image_small);

        editUserImageBig.setOnClickListener(uploadImageOnClickListener);
        editUserImageSmall.setOnClickListener(uploadImageOnClickListener);

    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

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
            //saveUriToFile(selectedImageUri,destinationFilename);


            try {
                File file = new File(new URI(selectedImageUri.getPath()));

                FirebaseStorage storage = Util.mStorage;
                StorageReference ref  = storage.getReference();
                ref = ref.child( "TEST" + "/");

                FirebaseUtil.uploadFileToFirebaseStorage(EditUserActivity.this, file, ref);
            }catch (Exception e){
                Log.e(TAG,"***ERROR CREATING FILE FROM URI");
            }
        }
    }

    void saveUriToFile(Uri sourceUri, String destinationFilePath)
    {
        Log.i(TAG, ">>>RUN>>>saveUriToFile");
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
        Log.i(TAG, "<<<FINISHED<<<saveUriToFile");
    }

    /**
     *  This method download the data of the user from Firebase Firestore
     */
    public void downloadUserDataFromFirebase() {
        // Name:


        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection(  FirebaseUtil.USER_DATA_KEY + "/")
                .document(Util.mAuth.getUid() + "");
        FirebaseUtil.downloadUserDataObjectFromFirebaseFirestoreANDSetTheResult(EditUserActivity.this, ref, 1);   // 1 = from EditUserActivity
        // Image:

        // Stats
    }

    @Override
    protected void onStart(){
        super.onStart();
        userNameEditText.setEnabled(false);
        userNameEditText.setText("Loading...");
        downloadUserDataFromFirebase();
    }

    @Override
    public void onDestroy(){
        Util.removeFromDebugActivityStackList(TAG);
        super.onDestroy();
    }

}
