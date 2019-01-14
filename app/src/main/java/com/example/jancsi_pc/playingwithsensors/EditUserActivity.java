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
import android.widget.ImageView;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.Utils.Util;

import java.io.File;

public class EditUserActivity extends AppCompatActivity {

    private ImageView editUserImageBig;
    private ImageView editUserImageSmall;

    private Button cancelButton;

    private String imagepath=null;

    private String TAG = "EditUserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        cancelButton =findViewById(R.id.cancel_edit_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditUserActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });

        editUserImageBig = findViewById(R.id.edit_user_image_big);
        editUserImageSmall = findViewById(R.id.edit_user_image_small);

        editUserImageBig.setOnClickListener(uploadImageOnClickListener);
        editUserImageSmall.setOnClickListener(uploadImageOnClickListener);

        //TODO download name, email, steps, sessions, records

        //TODO set name, email, steps, sessions, records

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
            Log.e(TAG, "selectedImageUri = " + selectedImageUri);
        }
    }
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

}
