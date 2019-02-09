package com.example.jancsi_pc.playingwithsensors.Activityes.Other;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jancsi_pc.playingwithsensors.Activityes.Main.DataCollectorActivity;
import com.example.jancsi_pc.playingwithsensors.R;

// loading app activity

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SpleshScreenActivity";
    private final long ANIMATION_DURATION = 500; //miliseconds
    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splesh_screen);
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);

        // Animate icon
        handleAnimationAppSplashLogoIntro();
        // Animate text
        handleAnimationAppSplashTextIntro();

        //Animation myAnimation = AnimationUtils.loadAnimation(this, R.anim.mytransition);
        //textView.setAnimation(myAnimation);
        //imageView.setAnimation(myAnimation);
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

//    @Override
//    protected void onResume() {
//        Log.d(TAG, ">>>RUN>>>onResume()");
//        super.onResume();
//    }
//    // If close all Activityes
//    if (Util.isFinished) {
//        Log.d(TAG, " isFinished() = true");
//    finish();
//    }


    private void handleAnimationAppSplashLogoIntro(){
        float distanceY = TypedValue.applyDimension(         // dip to pixels
                TypedValue.COMPLEX_UNIT_DIP, 45,
                getResources().getDisplayMetrics()
        );

        // Scale
        Animation scaleAnimation = new ScaleAnimation(
                3f, 1f, // Start and end values for the X axis scaling
                3f, 1f, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        scaleAnimation.setFillAfter(true); // Needed to keep the result of the animation
        scaleAnimation.setDuration(ANIMATION_DURATION);

        // Alpha
        Animation alphaAnimShow = new AlphaAnimation(0f, 1f);
        alphaAnimShow.setDuration(ANIMATION_DURATION);
        alphaAnimShow.setStartOffset(0);
        alphaAnimShow.setFillAfter(true);

        // Animation Set:
        AnimationSet as = new AnimationSet(false);
        as.addAnimation(scaleAnimation);
        as.addAnimation(alphaAnimShow);

        // Start Animations:
        imageView.setAnimation(as);
    }

    private void handleAnimationAppSplashTextIntro(){
        float distanceY = TypedValue.applyDimension(         // dip to pixels
                TypedValue.COMPLEX_UNIT_DIP, 200,
                getResources().getDisplayMetrics()
        );
        // Translate:
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, distanceY, 0);
        translateAnimation.setDuration(ANIMATION_DURATION);

        // Alpha
        Animation alphaAnimShow = new AlphaAnimation(0f, 1f);
        alphaAnimShow.setDuration(ANIMATION_DURATION);
        alphaAnimShow.setStartOffset(0);
        alphaAnimShow.setFillAfter(true);

        // Animation Set: (Translate+Alpha)
        AnimationSet as = new AnimationSet(false);
        as.addAnimation(translateAnimation);
        as.addAnimation(alphaAnimShow);

        // Start Animations:
        textView.setAnimation(as);
    }

}
