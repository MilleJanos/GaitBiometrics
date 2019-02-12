package com.example.jancsi_pc.playingwithsensors.activityes.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.jancsi_pc.playingwithsensors.activityes.other.SplashScreenActivity;
import com.example.jancsi_pc.playingwithsensors.utils.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        Util.mAuth = FirebaseAuth.getInstance();
        Util.mStorage = FirebaseStorage.getInstance();

        startActivity(new Intent(MainActivity.this, SplashScreenActivity.class));

    }

    @Override
    public void onResume() {
        super.onResume();
        // If close all Activities
        if (Util.isFinished) {
            Util.isFinished = false;    // to run the app if the app is restarted
            Log.d(TAG, " isFinished() = true");
            finish();
        }
    }

}
