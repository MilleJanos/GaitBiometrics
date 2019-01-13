package com.example.jancsi_pc.playingwithsensors;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SettingsActivity extends AppCompatActivity {

    Button settingsSaveButton;
    Button settingsCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FindViewsById();

        //TODO: Load data from shared pref

        //TODO click listener(save) -> Save to shared pref

        //TODO click listener(cancel) -> Close Activity

    }


    private void FindViewsById(){
        settingsCancelButton =  findViewById(R.id.settingsSaveButton);
        settingsCancelButton = findViewById(R.id.settingsCancelButton);
    }


}
