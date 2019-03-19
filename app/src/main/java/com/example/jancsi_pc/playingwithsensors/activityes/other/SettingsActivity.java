package com.example.jancsi_pc.playingwithsensors.activityes.other;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.jancsi_pc.playingwithsensors.R;
import com.example.jancsi_pc.playingwithsensors.utils.Util;

/**
 *  This Activity is used to manage application settings.
 *
 * @author MilleJanos
 */

public class SettingsActivity extends AppCompatActivity {

    private String TAG = "SettingsActivity";
    private Button settingsSaveButton;
    private Button settingsCancelButton;
    private Switch settingsDebugSwitch;

    private View.OnClickListener saveButtonClickListener = v -> {
        saveSettings(); // Save Settings to shared pref
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Util.addToDebugActivityStackList(TAG);

        /*
         * Initialize views
         */
        findViewsById();
        /*
         * Load data from shared pref
         */
        loadSettings();

        /*
         * Set switch state
         */

        if(Util.debugMode){
            settingsDebugSwitch.setChecked(true);
        }else{
            settingsDebugSwitch.setChecked(false);
        }

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

        Button settingsLoadButton = findViewById(R.id.settingsLoadButton);
        settingsLoadButton.setVisibility(View.GONE);
        settingsLoadButton.setOnClickListener(v -> {
            loadSettings();
            Toast.makeText(SettingsActivity.this, (Util.debugMode) ? "TRUE" : "FALSE", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public void onDestroy(){
        Util.removeFromDebugActivityStackList(TAG);
        super.onDestroy();
    }

    /**
     * Saves the app settings to shared preferences
     */
    private void saveSettings() {
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

    /**
     * Loads the app settings from shared preferences
     */
    public void loadSettings() {
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

    private View.OnClickListener cancelButtonClickListener = v -> finish();

    /**
     * Finds the views used by SettingsActivity
     */
    private void findViewsById() {
        settingsSaveButton = findViewById(R.id.settingsSaveButton);
        settingsCancelButton = findViewById(R.id.settingsCancelButton);
        settingsDebugSwitch = findViewById(R.id.settingsDebugSwitch);
    }


}
