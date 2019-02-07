package com.example.jancsi_pc.playingwithsensors.UserProfile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jancsi_pc.playingwithsensors.R;
import com.example.jancsi_pc.playingwithsensors.Utils.Firebase.FirebaseUtil;
import com.example.jancsi_pc.playingwithsensors.Utils.Firebase.UserDataObject;
import com.example.jancsi_pc.playingwithsensors.Utils.Util;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This Activity show the data of the user downloaded from Firebase
 *
 * @author Fulop Timea
 */
public class UserProfileActivity extends AppCompatActivity {

    private static UserDataObject userDataObject = null;

    private static TextView userNameTextView;
    private static TextView userEmailTextView;
    private static TextView numberOfSessionsTextView;
    private static TextView numberOfRecordsTextView;
    private static Button editUserProfileButton;
    private static Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        backButton = findViewById(R.id.user_profile_backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        userNameTextView = findViewById(R.id.userName);
        userEmailTextView = findViewById(R.id.userEmail);
        userEmailTextView.setText( "Email: " + Util.userEmail );
        numberOfSessionsTextView = findViewById(R.id.numberOfSessions);
        numberOfSessionsTextView.setText( "Number of sesions: " + 0 );
        numberOfRecordsTextView = findViewById(R.id.numberOfRecords);
        numberOfRecordsTextView.setText( "Number of records: " + 0 );
        editUserProfileButton = findViewById(R.id.editUserProfileButton);

        editUserProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, EditUserActivity.class);
                startActivity(intent);
            }
        });

        Util.mSharedPref = getSharedPreferences(Util.sharedPrefFile, MODE_PRIVATE);
        Util.mSharedPrefEditor = Util.mSharedPref.edit();
    }

    /**
     *  This method download the data of the user from Firebase Firestore
     */
    public void DownloadUserDataFromFirebase(){
        // Name:


        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection(  FirebaseUtil.USER_DATA_KEY + "/")
                .document(Util.mAuth.getUid() + "");
        FirebaseUtil.DownloadUserDataObjectFromFirebaseFirestore_AND_SetTheResult(UserProfileActivity.this, ref, 0); // 0 = from UserProfileActivity
        // Image:

        // Stats:
    }

    /**
     * This method updates the User Interface.
     */
    public static void UpdateUserDataObject(){
        userDataObject = Util.mUserDataObject_Temp;
        userNameTextView.setText( userDataObject.userName );
        userNameTextView.setEnabled(true);
        //
        // Set username into Shared Pref
        //
        Util.mSharedPrefEditor.putString(Util.LAST_LOGGED_IN_USER_NAME_KEY, ( userDataObject.userName ));
        // Set more options goes here !
        Util.mSharedPrefEditor.apply();
    }

    @Override
    protected void onStart(){
        super.onStart();
        DownloadUserDataFromFirebase();
    }

    @Override
    protected void onResume(){
        super.onResume();
        userNameTextView.setEnabled(false);
        userNameTextView.setText("Loading...");
        DownloadUserDataFromFirebase();


    }

}

