package com.example.jancsi_pc.playingwithsensors;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.Utils.Util;

public class SettingsActivity extends AppCompatActivity {

    private String TAG = "SettingsActivity";
    private Button settingsSaveButton;
    private Button settingsCancelButton;
    private Switch settingsDebugSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        /*
         * Initialize views
         */
        FindViewsById();
        /*
         * Load data from shared pref
         */
        LoadSettings();

        Log.i(TAG, "|||||||| Read Shared Pref: " + (Util.debugMode ? "TRUE" : "FALSE"));
        /*
         * Set Click listeners
         */
        settingsSaveButton.setOnClickListener(saveButtonClickListener);
        settingsCancelButton.setOnClickListener(cancelButtonClickListener);
        settingsDebugSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {
                Util.debugMode = true;
                Log.i(TAG, "Debug Mode -> ON");
            } else {
                Util.debugMode = false;
                Log.i(TAG, "Debug Mode -> OFF");
            }
        });

        //TODO: DELETE THIS DEBUG(LOAD) BUTTON
        Button settingsLoadButton = findViewById(R.id.settingsLoadButton);
        settingsLoadButton.setVisibility(View.GONE);
        settingsLoadButton.setOnClickListener(v -> {
            LoadSettings();
            Toast.makeText(SettingsActivity.this, (Util.debugMode) ? "TRUE" : "FALSE", Toast.LENGTH_SHORT).show();
        });

    }

    private void SaveSettings() {
        /*
         * Get fresh shared preferences
         */
        Util.mSharedPref = getSharedPreferences(Util.sharedPrefFile, MODE_PRIVATE);
        Util.mSharedPrefEditor = Util.mSharedPref.edit();
        /*
         * Put string in shared preferences
         */
        Util.mSharedPrefEditor.putString(Util.SETTING_DEBUG_MODE_KEY, (Util.debugMode ? "1" : "0"));
        // Set more options goes here !
        Util.mSharedPrefEditor.apply();
    }

    private void LoadSettings() {
        /*
         * Get fresh shared preferences
         */
        Util.mSharedPref = getSharedPreferences(Util.sharedPrefFile, MODE_PRIVATE);
        Util.mSharedPrefEditor = Util.mSharedPref.edit();
        /*
         * Read: SETTING_DEBUG_MODE_KEY
         */
        String debugModeStr = Util.mSharedPref.getString(Util.SETTING_DEBUG_MODE_KEY, null);
        if (debugModeStr == null) {                                 // If was not set yet(in shared pref)
            Log.i(TAG, "debugModeStr= " + null);
            Util.debugMode = false;
        } else {
            Log.i(TAG, "debugModeStr= " + "\"" + debugModeStr + "\"");
            int debugModeInt = Integer.parseInt(debugModeStr);
            Log.i(TAG, "debugModeInt= " + debugModeInt);
            Util.debugMode = debugModeInt == 1;
        }
        // Read more options goes here !
    }


    private View.OnClickListener saveButtonClickListener = v -> {
        SaveSettings(); // Save Settings to shared pref
    };

    private View.OnClickListener cancelButtonClickListener = v -> finish();

    private void FindViewsById() {
        settingsSaveButton = findViewById(R.id.settingsSaveButton);
        settingsCancelButton = findViewById(R.id.settingsCancelButton);
        settingsDebugSwitch = findViewById(R.id.settingsDebugSwitch);
    }


}
