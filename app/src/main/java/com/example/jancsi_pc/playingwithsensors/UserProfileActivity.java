package com.example.jancsi_pc.playingwithsensors;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
/**
 * This Activity show the data of the user downloaded from Firebase
 *
 * @author Fulop Timea
 */
public class UserProfileActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView userEmailTextView;
    private TextView numberOfSessionsTextView;
    private TextView numberOfRecordsTextView;
    private Button editUserProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userNameTextView = findViewById(R.id.userName);
        userEmailTextView = findViewById(R.id.userEmail);
        numberOfSessionsTextView = findViewById(R.id.numberOfSessions);
        numberOfRecordsTextView = findViewById(R.id.numberOfRecords);
        editUserProfileButton = findViewById(R.id.editUserProfileButton);

        editUserProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, EditUserActivity.class);
                startActivity(intent);
            }
        });


    }
}
