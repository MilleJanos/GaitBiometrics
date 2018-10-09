package com.example.jancsi_pc.playingwithsensors;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ConnectionActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEventListener accelerometerEventListener;

    float ts;
    float accX;
    float accY;
    float accZ;

    Button sendButton;
    Button openButton;
    EditText txtAddress;
    TextView sentTextView;

    Socket myAppSocket = null;


    //Orange4G: 192.168.1.101:21567
    //SapiInternet: 10.0.92.29:21567
    //KRS: 192.168.43.37
    //PORT: 21567
    String IP_ADDRESS = "192.168.43.37:21567";  //PI address + port

    public static String wifiModuleIp = "";
    public static int wifiModulePort = 0;

    public static String CMD = "0";



    private boolean isRecording = false;

    private ArrayList<Accelerometer> accArray = new ArrayList<>();
    private long recordCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_activiry);

        //SENSOR:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if( accelerometerSensor == null ){
            Toast.makeText(this, "The device has no com.example.jancsi_pc.playingwithsensors.Accelerometer !", Toast.LENGTH_SHORT).show();
            finish();
        }

        final TextView textViewStatus = (TextView) findViewById(R.id.statusTextView);
        final TextView currentTextView = (TextView) findViewById(R.id.currentTextView);
        final TextView sendCurrentStatusTextView = (TextView) findViewById(R.id.sendCurrentStatusTextView);
        final Button sendCurrentButton= (Button) findViewById(R.id.sendCurrentButton);

        sendButton = (Button) findViewById(R.id.sendButton);
        openButton = (Button) findViewById(R.id.openButton);
        txtAddress = (EditText) findViewById(R.id.ipAddress);
        sentTextView = (TextView) findViewById(R.id.sentScrollView);

        txtAddress.setText(IP_ADDRESS );

        final Button startButton = findViewById( R.id.startButton );
        final Button stopButton  = findViewById( R.id.stopButton  );
        final Button clearButton  = findViewById( R.id.clearButton  );
        sendButton = (Button) findViewById( R.id.sendButton );
        openButton = (Button) findViewById( R.id.openButton );
        stopButton.setEnabled(false);
        clearButton.setEnabled(false);
        sendButton.setEnabled(false);
        openButton.setEnabled(false);


        accelerometerEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                long timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                ts = timeStamp;
                accX = x;
                accY = y;
                accZ = z;
                currentTextView.setText("TimeStamp: " + ts + "\nX: " + accX + "\nY: " + accY + "\nZ: " + accZ);
                if (isRecording) {
                    accArray.add(new Accelerometer(timeStamp, x, y, z));
                    recordCount++;
                    textViewStatus.setText("Recording: " + recordCount);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        //START / CONTINUE  RECORDING
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = true;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                clearButton.setEnabled(false);
                sendButton.setEnabled(false);
                openButton.setEnabled(false);
                Log.d("ConnectionActivity_", "Start Rec.");
                //textViewStatus.setText("Recording ...");
            }
        });


        //STOP RECORDING
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = false;
                startButton.setText("CONTINUE");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                clearButton.setEnabled(true);
                sendButton.setEnabled(true);
                openButton.setEnabled(true);
                Log.d("ConnectionActivity", "Stop Rec. - Generating CMD");
                CMD = accArrayToString();
                Log.d("ConnectionActivity","CMD Generated.");
                sentTextView.setText( CMD );
                textViewStatus.setText("Recorded: " + recordCount);
            }
        });


        //CLEAR RECORDS
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setText("START");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                clearButton.setEnabled(false);
                sendButton.setEnabled(false);
                openButton.setEnabled(false);
                recordCount = 0;
                accArray.clear();
                Log.d("ConnectionActivity_", "Clear Rec.");
                sentTextView.setText("-cleared-");
                textViewStatus.setText("Press START to new record.");
            }
        });


        //SEND
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // STOP button generates the CMD
                sentTextView.setText( CMD );
                //Prepare and Send
                getIPandPort();
                Socket_AsyncTask cmd_send_data = new Socket_AsyncTask();
                cmd_send_data.execute();
            }
        });


        //OPEN BUTTON
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Prepare and Send
                getIPandPort();
                CMD = "open";
                sendButton.setEnabled(false);
                sentTextView.setText( CMD );
                Socket_AsyncTask cmd_send_data = new Socket_AsyncTask();
                cmd_send_data.execute();
            }
        });


        //SEND CURRENT ACCELEROMETER STATUS
        sendCurrentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SendCurrent","SendCurrent: START");
                CMD = "TimeStamp: " + ts*1000 + "\nX: " + accX + "\nY: " + accY + "\nZ: " + accZ;
                Log.i("SenDCurrent","Message(CMD): " + CMD );
                sendCurrentStatusTextView.setText( CMD );
                //Prepare and Send
                getIPandPort();
                Socket_AsyncTask cmd_send_data = new Socket_AsyncTask();
                cmd_send_data.execute();
                Log.i("SendCurrent","SendCurrent: END");
            }
        });


        //BACK BUTTON
        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ConnectionActivity.this, MainActivity.class));
            }
        });

    }



    // ===============================
    // =====  END OF: ON CREATE  =====
    // ===============================


    public void getIPandPort(){
        String iPandPort = txtAddress.getText().toString();
        Log.d("getIPandPort","IP String: " + iPandPort);
        String temp[] = iPandPort.split(":");
        wifiModuleIp = temp[0];
        wifiModulePort = Integer.parseInt(temp[1]);
        Log.d("getIPandPort","IP: " + wifiModuleIp);
        Log.d("getIPandPort", "Port: " + wifiModulePort);
    }


    // ArrayList<Accelerometer> accArray ==> String str
    public String accArrayToString(){
        String str = "";
        int i;
        for( i=0; i< accArray.size()-1; i++ ){
            str += accArray.get(i).getTimeStamp() +","+ accArray.get(i).getX() +","+ accArray.get(i).getY() +","+ accArray.get(i).getZ() +",";
        }
        str += accArray.get(i).getTimeStamp() +","+ accArray.get(i).getX() +","+ accArray.get(i).getY() +","+ accArray.get(i).getZ();
        return str;
    }


    public class Socket_AsyncTask extends AsyncTask<Void,Void,Void>{
        Socket socket;
        @Override
        protected Void doInBackground(Void... voids) {
            try{
                InetAddress inetAddress = InetAddress.getByName( ConnectionActivity.wifiModuleIp );
                socket = new java.net.Socket( inetAddress,ConnectionActivity.wifiModulePort );
                DataOutputStream dataOutputStream  = new DataOutputStream(socket.getOutputStream() );
                Log.i("SocketAsyncT","SENDING: " + CMD + " ("+ConnectionActivity.wifiModuleIp+" : "+ConnectionActivity.wifiModulePort+")");
                dataOutputStream.writeBytes( CMD );
                dataOutputStream.close();
                socket.close();
            }catch( UnknownHostException e ){
                e.printStackTrace();
            }catch( IOException e ){
                e.printStackTrace();
            }
            return null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(accelerometerEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }



    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(accelerometerEventListener);
    }


}
