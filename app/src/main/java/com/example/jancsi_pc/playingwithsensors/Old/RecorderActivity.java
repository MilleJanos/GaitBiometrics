package com.example.jancsi_pc.playingwithsensors;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/*
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
*/
public class RecorderActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor gyroscopemeterSensor;
    private SensorEventListener accelerometerEventListener;
    private SensorEventListener gyoroscopemeterEventListener;
    private static final int REQUEST_CODE = 212;

    private File root = android.os.Environment.getExternalStorageDirectory();
    private File dir = new File (root.getAbsolutePath() + "/accelerometer");

    private boolean isRecording = false;

    //private LineChart mChart;
    //private Thread thread;
    //private boolean plotData = true;

    private ArrayList<Accelerometer> accArray = new ArrayList<>();
    private long recordCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        ////////// Initialize file //////////
        dir.mkdirs();
        final File file = new File(dir, "data.csv");
        Log.d("RecorderActivity_", file.getAbsolutePath());

        ////////// Testing the sensors //////////
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopemeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if( accelerometerSensor == null ){
            Toast.makeText(this, "The device has no com.example.jancsi_pc.playingwithsensors.Accelerometer !", Toast.LENGTH_SHORT).show();
            finish();
        }

        if( gyroscopemeterSensor == null ){
            Toast.makeText( this, "this device has no Gyroscopemeter !", Toast.LENGTH_SHORT).show();
        }

        if (checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }

        //BACK:
        Button backButton = (Button)findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent( RecorderActivity.this, MainActivity.class ) );
            }
        });

        //mChart = (LineChart)findViewById(R.id.chart1);

        ////////// Defining the dataset //////////
        //LineChart lineChart = (LineChart) findViewById(R.id.chart);
        //ArrayList<String> labels = new ArrayList<String>();
        //labels.add()

        ////////// Event Listener for sensors //////////
        final TextView textViewResultStatus = findViewById( R.id.textViewRecordStatus );

        accelerometerEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                TextView textViewAX = findViewById(R.id.textViewAX);
                TextView textViewAY = findViewById(R.id.textViewAY);
                TextView textViewAZ = findViewById(R.id.textViewAZ);
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                textViewAX.setText("X: " + x);
                textViewAY.setText("Y: " + y);
                textViewAZ.setText("Z: " + z);
                if (isRecording) {
                    long timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                    accArray.add(new Accelerometer(timeStamp, x, y, z, 0));
                    recordCount++;
                    textViewResultStatus.setText("Recorded: " + recordCount);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        gyoroscopemeterEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                TextView textViewGX = findViewById( R.id.textViewGX );
                TextView textViewGY = findViewById( R.id.textViewGY );
                TextView textViewGZ = findViewById( R.id.textViewGZ );
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                textViewGX.setText("X: " + x);
                textViewGY.setText("Y: " + y);
                textViewGZ.setText("Z: " + z);
                // TODO if gyroscope is needed too
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        final TextView textViewRecordStatus = findViewById( R.id.textViewRecordStatus );
        final TextView textViewSaved = findViewById( R.id.textViewSaved );
        final Button startButton = findViewById( R.id.buttonStart );
        final Button stopButton  = findViewById( R.id.buttonStop  );
        final Button saveButton  = findViewById( R.id.buttonSave  );
        final Button clearButton = findViewById( R.id.buttonClear );
        stopButton.setEnabled(false);
        saveButton.setEnabled(false);
        clearButton.setEnabled(false);

        //Start recording
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = true;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                saveButton.setEnabled(false);
                clearButton.setEnabled(false);
                Log.d("RecorderActivity_", "Start Rec.");
                textViewRecordStatus.setText("Recording ...");
            }
        });

        //Stop recording
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = false;
                startButton.setText("CONTINUE");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                saveButton.setEnabled(true);
                clearButton.setEnabled(true);
                Log.d("RecorderActivity_", "Stop Rec.");
                textViewRecordStatus.setText("Recorded: " + recordCount);
            }
        });

        //Save records
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("RecorderActivity_", "Saving....");
                textViewRecordStatus.setText("Saving...");
                try {
                    FileOutputStream f = new FileOutputStream(file);
                    PrintWriter pw = new PrintWriter(f);
                    //pw.println("TimeStamp, Accelerometer X, Accelerometer Y, Accelerometer Z, Gyroscope X, Gyroscope Y, Gyroscope Z");
                    pw.println("TimeStamp, Accelerometer X, Accelerometer Y, Accelerometer Z");
                    File file = new File(dir, "data.csv");
                    Log.d("RecorderActivity_", file.getAbsolutePath());
                    for (Accelerometer e : accArray) {
                        pw.println( e.toString() );
                    }
                    pw.flush();
                    pw.close();
                    f.close();
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                    Log.d("RecorderActivity_", "Error 1");
                    isRecording = false;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Log.d("RecorderActivity_", "Error 2");
                    isRecording = false;
                }
                textViewSaved.setText( "Saved: " + recordCount );
                Log.d("RecorderActivity_", "Saved.");
                textViewRecordStatus.setText("Saved. ("+ recordCount +")");
            }
        });
        //Clear records
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setText("START");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                saveButton.setEnabled(false);
                clearButton.setEnabled(false);
                recordCount = 0;
                accArray.clear();
                textViewSaved.setText(" ");
                Log.d("RecorderActivity_", "Clear Rec.");
                textViewRecordStatus.setText("Press START to new record.");
            }
        });

    } //onCreate



    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(accelerometerEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(gyoroscopemeterEventListener, gyroscopemeterSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(accelerometerEventListener);
        sensorManager.unregisterListener(gyoroscopemeterEventListener);
    }
}
