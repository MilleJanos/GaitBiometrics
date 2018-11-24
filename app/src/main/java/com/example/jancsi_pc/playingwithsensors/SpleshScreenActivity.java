package com.example.jancsi_pc.playingwithsensors;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SpleshScreenActivity extends AppCompatActivity {

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
        Thread timer = new Thread(){
            public void run(){
                try{
                    sleep(2000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    startActivity(intent);
                    finish();
                }
            }
        };
        timer.start();
    }
}
