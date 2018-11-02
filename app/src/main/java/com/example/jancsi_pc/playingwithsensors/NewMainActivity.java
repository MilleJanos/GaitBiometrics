package com.example.jancsi_pc.playingwithsensors;

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

import com.example.jancsi_pc.playingwithsensors.Old.ConnectionActivity;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/* SEND PACKAGE FORMATS:

One record:
    "timestamp,x,y,x,end"
Miltiple records:
    "timestamp,x,y,x,timestamp,x,y,x,timestamp,x,y,x,timestamp,x,y,x,timestamp,x,y,x, ... ,end"          - 20 record / package  (just 1 ",end")
Open command:
    "open"
*/

public class NewMainActivity extends AppCompatActivity {

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
    //myPcHotspot: 192.168.137.139
    //
    //PORT: 21567
    //      "<ipw4>:<port>" !
    String IP_ADDRESS = "192.168.137.102:21456";  //IP address + port

    public static String wifiModuleIp = "";
    public static int wifiModulePort = 0;

    public static String CMD = "0";

    private boolean isRecording = false;

    private ArrayList<Accelerometer> accArray = new ArrayList<>();
    private long recordCount = 0;

    private ArrayList<String> accArrayStringGroups = new ArrayList<>();
    private final int RECORDS_PER_PACKAGE_LIMIT = 1;

    public static int stepNumber=0;
    public static final int MAX_STEP_NUMBER=10;
    public static final int MIN_STEP_NUMBER=5;

    TextView textViewStatus;
    TextView currentTextView;
    TextView sendCurrentStatusTextView;
    Button sendCurrentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_activity);

        //SENSOR:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if( accelerometerSensor == null ){
            Toast.makeText(this, "The device has no com.example.jancsi_pc.playingwithsensors.Accelerometer !", Toast.LENGTH_SHORT).show();
            finish();
        }

        textViewStatus = (TextView) findViewById(R.id.statusTextView);
        currentTextView = (TextView) findViewById(R.id.currentTextView);
        sendCurrentStatusTextView = (TextView) findViewById(R.id.sendCurrentStatusTextView);
        sendCurrentButton= (Button) findViewById(R.id.sendCurrentButton);

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

        //TODO
        final DecimalFormat df = new DecimalFormat("0");
        //df.setMaximumFractionDigits(20);
        df.setMaximumIntegerDigits(20);
        // 123...45E9 -> 123...459234
        //         ==

        accelerometerEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if(stepNumber>MAX_STEP_NUMBER){ //only N steps allowed
                    stopButton.callOnClick();
                }
                if(stepNumber>MIN_STEP_NUMBER && !stopButton.isEnabled()){ //at least M steps
                    //stopButton.setEnabled(true);
                }
                long timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                ts = timeStamp;
                accX = x;
                accY = y;
                accZ = z;
                currentTextView.setText("TimeStamp: " + df.format(ts) + "\nX: " + accX + "\nY: " + accY + "\nZ: " + accZ +"\nStep:" + stepNumber );
                if (isRecording) {
                    accArray.add(new Accelerometer(timeStamp, x, y, z, stepNumber));
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
                textViewStatus.setText("Calculating...");
                CMD = accArrayToString();
                CMD += ",end";
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


        //SEND   (MULTIPLE RECORDS)
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IP_ADDRESS = txtAddress.getText().toString();
                // STOP button generates the CMD
                //sentTextView.setText( CMD );
                // Sending the array in multiple packages:
                accArrayGroupArrayToString();
                for(int i=0; i<accArrayStringGroups.size(); ++i) {
                    Log.i("accArrayString","aASG.get("+i+")= " + accArrayStringGroups.get(i) );
                    CMD = accArrayStringGroups.get(i);  //group of RECORDS_LIMIT_PER_PACKAGE records
                    //Prepare and Send
                    getIPandPort();
                    Socket_AsyncTask cmd_send_data = new Socket_AsyncTask();
                    cmd_send_data.execute();
                }
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
                sentTextView.setText( "Sent: 1 package with content of:\nopen");
            }
        });

    }


    // ===============================
    // =====  END OF: ON CREATE  =====
    // ===============================





    // ArrayList<Accelerometer> accArray ==> String str
    //
    // output format:   "timestamp,x,y,x,timestamp,x,y,x,timestamp,x,y,x,timestamp,x,y,x,timestamp,x,y,x, ... ,end"
    public String accArrayToString(){
        String str = "";
        int i;
        for( i=0; i< accArray.size()-1; ++i ){
            str += accArray.get(i).getTimeStamp() +","+ accArray.get(i).getX() +","+ accArray.get(i).getY() +","+ accArray.get(i).getZ() +",";
        }
        str += accArray.get(i).getTimeStamp() +","+ accArray.get(i).getX() +","+ accArray.get(i).getY() +","+ accArray.get(i).getZ();
        return str;
    }


    // Same as accArrayToString just grouped in N groups
    // NO return value, the result is in accArrayStringGroups variable !
    // adds "end" to the end of the package-chain
    public void accArrayGroupArrayToString(){
        accArrayStringGroups.clear();
        String str = "";
        int i ;
        int c = 0;  // counter
        boolean limitReached = true;
        for( i=0; i < accArray.size(); ++i ){
            ++c;
            str += accArray.get(i).getTimeStamp() +","+ accArray.get(i).getX() +","+ accArray.get(i).getY() +","+ accArray.get(i).getZ();
            limitReached = false;
            if( c == RECORDS_PER_PACKAGE_LIMIT ){
                accArrayStringGroups.add(str);
                str = "";
                c = 0;
                limitReached = true;
                continue;
            }
            str += ",";
        }
        if( limitReached == false ){                //If the last group has no exactly N elements then we have to add it on the end
            str += "end";
            accArrayStringGroups.add(str);
        }
    }


    public void getIPandPort(){
        String iPandPort = txtAddress.getText().toString();
        Log.d("getIPandPort","IP String: " + iPandPort);
        String temp[] = iPandPort.split(":");
        wifiModuleIp = temp[0];
        wifiModulePort = Integer.parseInt(temp[1]);
        Log.d("getIPandPort","IP: " + wifiModuleIp);
        Log.d("getIPandPort", "Port: " + wifiModulePort);
    }

    // <String, String, TCPClient>            // TODO simple TCP client and C++ server on Raspberry Pi
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
        }    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(accelerometerEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);    //SENSOR_DELAY_FASTEST
    }



    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(accelerometerEventListener);
    }
}

