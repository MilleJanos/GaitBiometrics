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
import com.example.jancsi_pc.playingwithsensors.Old.MainActivity;
import com.example.jancsi_pc.playingwithsensors.StepCounterPackage.StepDetector;
import com.example.jancsi_pc.playingwithsensors.StepCounterPackage.StepListener;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/* SEND PACKAGE FORMATS:
*
*    "timestamp,x,y,x,timestamp,x,y,x,timestamp,x,y,x,timestamp,x,y,x,timestamp,x,y,x, ... ,end"
*    *
*/

public class NewMainActivity extends AppCompatActivity /*(STEPCOUNT) implements SensorEventListener, StepListener */ {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEventListener accelerometerEventListener;

    private float ts;
    private float accX;
    private float accY;
    private float accZ;

    private Button sendButton;
    private EditText txtAddress;
    private TextView sentTextView;
    private Button startButton;
    private Button stopButton;
    private Button clearButton;

    private Socket myAppSocket = null;

    //PORT: 21567
    //                              "<ip>:<port>"
    private String IP_ADDRESS = "192.168.43.54:21456";

    public static String wifiModuleIp = "";
    public static int wifiModulePort = 0;

    public static String CMD = "0";

    private boolean isRecording = false;

    private ArrayList<Accelerometer> accArray = new ArrayList<>();
    private long recordCount = 0;
    /*(STEPCOUNT)
    private ArrayList<Integer> stepArray = new ArrayList<>();
    private int numSteps = 0;
    */
    private ArrayList<String> accArrayStringGroups = new ArrayList<>();
    private final int RECORDS_PER_PACKAGE_LIMIT = 10;

    public static int stepNumber=0;
    public static final int MAX_STEP_NUMBER=10;
    public static final int MIN_STEP_NUMBER=5;

    TextView textViewStatus;
    TextView currentTextView;
    TextView sendCurrentStatusTextView;
    Button sendCurrentButton;

    //MediaPlayer mediaPlayer;

    // For Step Detecting:
    private StepDetector simpleStepDetector;
    //private SensorManager sensorManager;
    //private Sensor accel;

    /*
    *
    *   OnCreate
    *
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);

        //SENSOR:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if( accelerometerSensor == null ){
            Toast.makeText(this, "The device has no com.example.jancsi_pc.playingwithsensors.Accelerometer !", Toast.LENGTH_SHORT).show();
            finish();
        }

        textViewStatus = (TextView) findViewById(R.id.statusTextView);
        currentTextView = (TextView) findViewById(R.id.currentTextView);
        sendButton = (Button) findViewById(R.id.sendButton);
        txtAddress = (EditText) findViewById(R.id.ipAddress);
        sentTextView = (TextView) findViewById(R.id.sentScrollView);
        startButton = findViewById( R.id.startButton );
        stopButton  = findViewById( R.id.stopButton  );
        clearButton  = findViewById( R.id.clearButton  );
        sendButton = (Button) findViewById( R.id.sendButton );
        stopButton.setEnabled(false);
        clearButton.setEnabled(false);
        sendButton.setEnabled(false);

        txtAddress.setText(IP_ADDRESS);

        final DecimalFormat df = new DecimalFormat("0");
        df.setMaximumIntegerDigits(20);
        // 123...45E9 -> 123...459234
        //         ==            ====
        /*(STEPDETECTOR)
        //Step Detecting:
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        sensorManager.registerListener(NewMainActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        */

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
                    /*(STEPCOUNT)
                    stepArray.add(numSteps);
                    recordCount++;
                    */
                    textViewStatus.setText("Recording: " + recordCount);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        /*
        *
        *   Start / Continue
        *   recording
        *
        */

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mediaPlayer.create(null,R.raw.start);
                //mediaPlayer.start();
                isRecording = true;
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                clearButton.setEnabled(false);
                sendButton.setEnabled(false);
                Log.d("ConnectionActivity_", "Start Rec.");
                //textViewStatus.setText("Recording ...");
            }
        });

        /*
        *
        *   Stop recording
        *
        */

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = false;
                startButton.setText("CONTINUE");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                clearButton.setEnabled(true);
                sendButton.setEnabled(true);
                Log.d("ConnectionActivity", "Stop Rec. - Generating CMD");
                textViewStatus.setText("Calculating...");
                CMD = accArrayToString();
                CMD += ",end";
                Log.d("ConnectionActivity","CMD Generated.");
                sentTextView.setText( CMD );
                textViewStatus.setText("Recorded: " + recordCount);
            }
        });

        /*
        *
        *   Clear recorded records
        *
        */

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setText("START");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                clearButton.setEnabled(false);
                sendButton.setEnabled(false);
                recordCount = 0;
                accArray.clear();
                Log.d("ConnectionActivity_", "Clear Rec.");
                sentTextView.setText("-cleared-");
                textViewStatus.setText("Press START to new record.");
            }
        });

        /*
        *
        *   Sending multiple records
        *   from Start to End
        *
        */

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


    }// OnCreate

    /*
    *
    * ArrayList<Accelerometer> accArray ==> String str
    *
    * output format:   "timestamp,x,y,z,currentStepCount,timestamp,x,y,z,currentStepCount,timestamp,x,y,z,timestamp,currentStepCount, ... ,end"
    *
    *
    */

    public String accArrayToString(){
        StringBuilder sb = new StringBuilder();
        int i;
        for( i=0; i< accArray.size()-1; ++i ){
            sb.append(accArray.get(i).getTimeStamp())
                .append(",")
                .append(accArray.get(i).getX())
                .append(",")
                .append(accArray.get(i).getY())
                .append(",")
                .append(accArray.get(i).getTimeStamp())
                .append(",");
                /*(STEPCOUNT).append(stepArray.get(i))
                .append(",");*/
        }
        sb.append(accArray.get(i).getTimeStamp())
            .append(",")
            .append(accArray.get(i).getX())
            .append(",")
            .append(accArray.get(i).getY())
            .append(",")
            .append(accArray.get(i).getTimeStamp());
            /*(STEPCOUNT).append(",")
            .append(stepArray.get(i)); */
        return sb.toString();
    }

    /*
    *
    * Same as accArrayToString just grouped in N groups
    * NO return value, the result is in accArrayStringGroups variable !
    * adds "end" to the end of the package-chain
    *
    */

    public void accArrayGroupArrayToString(){
        accArrayStringGroups.clear();
        //String str = "";
        StringBuilder sb = new StringBuilder();
        int i ;
        int c = 0;  // counter
        boolean limitReached = true;
        for( i=0; i < accArray.size(); ++i ){
            ++c;
            //str += accArray.get(i).getTimeStamp() +","+ accArray.get(i).getX() +","+ accArray.get(i).getY() +","+ accArray.get(i).getZ();
            sb.append(accArray.get(i).getTimeStamp())
                    .append(",")
                    .append(accArray.get(i).getX())
                    .append(",")
                    .append(accArray.get(i).getY())
                    .append(",")
                    .append(accArray.get(i).getZ());
                    /*(STEPCOUNT).append(",")
                    .append(stepArray.get(i)); */
            limitReached = false;
            if( c == RECORDS_PER_PACKAGE_LIMIT ){
                //accArrayStringGroups.add(str);
                accArrayStringGroups.add( sb.toString() );
                //str = "";
                sb.setLength(0);
                c = 0;
                limitReached = true;
                continue;
            }
            //str += ",";
            sb.append(",");
        }
        //If the last group has no exactly N elements then we have to add it on the end
        if( limitReached == false ){
            //str += "end";
            sb.append("end");
            //accArrayStringGroups.add(str);
            accArrayStringGroups.add( sb.toString() );
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

                                           // <String, String, TCPClient>            // TODO: Modify if needed
    public class Socket_AsyncTask extends AsyncTask<Void,Void,Void>{
        Socket socket;
        @Override
        protected Void doInBackground(Void... voids) {
            try{
                InetAddress inetAddress = InetAddress.getByName( NewMainActivity.wifiModuleIp );
                socket = new java.net.Socket( inetAddress, NewMainActivity.wifiModulePort );
                DataOutputStream dataOutputStream  = new DataOutputStream(socket.getOutputStream() );
                Log.i("SocketAsyncT","SENDING: " + CMD + " ("+ NewMainActivity.wifiModuleIp+" : "+ NewMainActivity.wifiModulePort+")");
                //DataOutputStream.writeBytes( CMD );
                byte byteArray[] = CMD.getBytes();
                dataOutputStream.write(byteArray);
                dataOutputStream.flush();
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
        sensorManager.registerListener(accelerometerEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }



    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(accelerometerEventListener);
    }
    /*(STEPCOUNT)
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        NewMainActivity.stepNumber++;
        //TvSteps.setText(TEXT_NUM_STEPS + numSteps);
    }
    */

}