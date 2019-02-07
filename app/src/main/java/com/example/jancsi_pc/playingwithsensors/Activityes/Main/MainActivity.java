package com.example.jancsi_pc.playingwithsensors.Activityes.Main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.jancsi_pc.playingwithsensors.Activityes.Other.SplashScreenActivity;
import com.example.jancsi_pc.playingwithsensors.R;
import com.example.jancsi_pc.playingwithsensors.Utils.Util;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        startActivity( new Intent(MainActivity.this, SplashScreenActivity.class) );

    }

    @Override
    public void onResume(){
        super.onResume();
        // If close all Activities
        if (Util.isFinished) {
            Util.isFinished = false;    // to run the app if the app is restarted
            Log.d(TAG, " isFinished() = true");
            finish();
        }
    }

}
