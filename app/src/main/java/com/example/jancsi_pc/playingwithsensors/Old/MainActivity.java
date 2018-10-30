package com.example.jancsi_pc.playingwithsensors;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;

public class MainActivity extends AppCompatActivity  {

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button recorderButton = (Button) findViewById(R.id.recorderButton);
        Button connectionButton = (Button) findViewById(R.id.connectionButton);
        Button stepcounterButton = (Button) findViewById(R.id.stepcounterButton);

        mContext = recorderButton.getContext();

        recorderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(  new Intent( MainActivity.this, RecorderActivity.class ) );
            }
        });

        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(  new Intent( MainActivity.this, ConnectionActivity.class ) );
            }
        });


        stepcounterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(  new Intent( MainActivity.this, StepCounterActivity.class ) );
            }
        });

    }



}
