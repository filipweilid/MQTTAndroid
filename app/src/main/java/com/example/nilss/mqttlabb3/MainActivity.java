package com.example.nilss.mqttlabb3;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements BeaconConsumer{
    private static final String TAG = "MainActivity";
    private static final String BeaconID = "88888888-8888-8888-8888-888888888888";
    //private String serverAndPort = "tcp://m21.cloudmqtt.com:18431";
    private String serverAndPort = "tcp://m20.cloudmqtt.com:13789";
    private MqttAndroidClient client;
    private Button publishBtn;
    private Button subBtn;
    private EditText etMessage, etTopic;
    private TextView tvMessage;
    private RecyclerView recyclerView;
    private ListAdapter rvAdapter;
    //private String username = "splbrgpc";
    //private String password = "Ef3r5zQAS7k4";
    private String username = "cadobfeg";
    private String password = "GadV6ZHExG7T";
    private BeaconManager beaconManager;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final String lamptopic = "Lamp_status";
    private List<Lampobject> lamps = new ArrayList(); //info of all lamps
    BeaconTimer timerLamp1;
    BeaconTimer timerLamp2;
    BeaconTimer timerLamp3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        initiateLamps();
        recyclerView = findViewById(R.id.rvView);
        rvAdapter = new ListAdapter(this, lamps, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new MyItemDecoration(this));
        recyclerView.setAdapter(rvAdapter);
        publishBtn = findViewById(R.id.publishBtn);
        subBtn = findViewById(R.id.subBtn);
        etMessage = findViewById(R.id.etMessage);
        etTopic = findViewById(R.id.etTopic);
        publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish();
            }
        });
        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe();
            }
        });
        connect(username, password);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
    }

    private void initiateLamps(){
        Lampobject lamp1 = new Lampobject("Lamp1","5","10","20","false");
        Lampobject lamp2 = new Lampobject("Lamp2","100","20","50","false");
        Lampobject lamp3 = new Lampobject("Lamp3","30","30","30","false");
        Lampobject lamp4 = new Lampobject("GroupConfig", "0", "0", "0", "true");
        lamps.add(lamp1);
        lamps.add(lamp2);
        lamps.add(lamp3);
        lamps.add(lamp4);
    }

    public String configureAll(){
        String message = "";
        for(int i = 0;i < rvAdapter.getContent().size()-1; i ++){
            message += rvAdapter.getContent().get(i).toString();
        }
        return message;
    }

    public void configureOne(int id){
        String message = "";
        message += rvAdapter.getContent().get(id-1).toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    private void connect(String userName, String password){
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(getApplicationContext(), serverAndPort,
                        clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setKeepAliveInterval(0);
        options.setUserName(userName);
        options.setPassword(password.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            Log.d(TAG, "onCreate: " + e);
        }
    }

    public void publish() {
        String message = configureAll();
        String topic = "Lamp_set";
        Log.v(TAG, message);
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            client.publish(topic, message.getBytes(),1,true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribe() {
        //String topic = etTopic.getText().toString();
        String topic = lamptopic;
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Subscription successful");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Log.v(TAG, message.toString());
                try {
                    messageDecodes(message.toString()); //Decode message from broker
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void messageDecodes(String message) throws JSONException {
        String[] lamplist;
        lamplist = message.split("\\*");

        for(int i = 0; i <3; i ++) {
            if(lamps.get(i).getInRange()){
                String[] lamp = lamplist[i].split(",");
                Log.v(TAG, i + lamp[0] +" " + lamp[1] + " " +lamp[2] + " " + lamp[3]);
                lamps.get(i).setOnoff(lamp[0]);
                lamps.get(i).setBri(lamp[1]);
                lamps.get(i).setHue(lamp[2]);
                lamps.get(i).setSat(lamp[3]);
            }
        }
        rvAdapter.setContent(lamps);
        Toast.makeText(this, "Message recieved from broker",
                Toast.LENGTH_LONG).show();
    }

    public void onDestroy(){
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                Iterator iter = beacons.iterator();
                //ConstraintLayout constraintLayout = findViewById(R.id.container);
                while(iter.hasNext()){
                    Beacon beacon = (Beacon) iter.next();
                    Log.v(TAG, beacon.getId1().toString());
                    if(beacon.getId1().toString().equals(BeaconID)) {
                        Log.i(TAG, "The first beacon I see is about "+beacon.getDistance()+" meters away." + beacon.getId1());
                        if(beacon.getId2().toString().equals("0")) {
                            //constraintLayout.setBackgroundColor(Color.RED);
                            rvAdapter.getContent().get(0).setInRange(true);
                            if(timerLamp1==null){
                                timerLamp1 = new BeaconTimer("0");
                                timerLamp1.startTimer();
                            }else{
                                timerLamp1.terminate();
                                timerLamp1 = new BeaconTimer("0");
                                timerLamp1.startTimer();
                            }
                        }
                        if(beacon.getId2().toString().equals("1")) {
                            //constraintLayout.setBackgroundColor(Color.GREEN);
                            rvAdapter.getContent().get(1).setInRange(true);
                            if(timerLamp2==null){
                                timerLamp2 = new BeaconTimer("1");
                                timerLamp2.startTimer();
                            }else{
                                timerLamp2.terminate();
                                timerLamp2 = new BeaconTimer("1");
                                timerLamp2.startTimer();
                            }
                        }
                        if(beacon.getId2().toString().equals("2")) {
                            //constraintLayout.setBackgroundColor(Color.BLUE);
                            rvAdapter.getContent().get(2).setInRange(true);
                            if(timerLamp3==null){
                                timerLamp3 = new BeaconTimer("2");
                                timerLamp3.startTimer();
                            }else{
                                timerLamp3.terminate();
                                timerLamp3 = new BeaconTimer("2");
                                timerLamp3.startTimer();
                            }
                        }
                        rvAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }

    private class BeaconTimer extends TimerTask{
        Timer timer;
        String identifier;

        public BeaconTimer(String identifier){
            this.timer = new Timer();
            this.identifier = identifier;
        }

        public void terminate(){
            timer.cancel();
            timer.purge();
        }

        public void startTimer(){
            timer.schedule(this, 1000*10);
            Log.v(TAG, "STARTING TIMER");
        }
        @Override
        public void run() {
            rvAdapter.getContent().get(Integer.parseInt(identifier)).setInRange(false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "DISABLE LAMP");
                    rvAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}
