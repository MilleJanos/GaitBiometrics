package com.example.jancsi_pc.playingwithsensors.Activityes.Other;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jancsi_pc.playingwithsensors.Activityes.Main.DataCollectorActivity;
import com.example.jancsi_pc.playingwithsensors.R;
import com.example.jancsi_pc.playingwithsensors.Utils.Util;

// loading app activity

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SpleshScreenActivity";
    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splesh_screen);
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);
        Animation myAnimation = AnimationUtils.loadAnimation(this, R.anim.mytransition);
        textView.setAnimation(myAnimation);
        imageView.setAnimation(myAnimation);
        final Intent intent = new Intent(this, DataCollectorActivity.class);
        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    startActivity(intent);
                    finish();
                }
            }
        };
        timer.start();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, ">>>RUN>>>onResume()");
        super.onResume();

        if (Util.isFinished) {
            Log.d(TAG, " isFinished() = true");
            finish();
        }
    }
}
