package com.powerranger.sow2.iamnotarobot;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private ImageView imageHeartLeft;
    private ImageView imageHeartRight;
    private RelativeLayout relativeLayout;
    private EditText editReceiverUsername;
    private Socket mSocket;
    private final String SOCKET_URL = "http://192.168.0.110:3001";
    private Intent intent;
    private Bundle bundle;
    private String receiverEmail;
    private String id;
    private String email;
    private String avatar;
    private String birthday;
    private String gender;

    private Boolean isReceivingRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Init();
    }

    private void Init() {

        intent = getIntent();
        bundle = intent.getBundleExtra("profile");

        if(bundle != null) {
            id = bundle.getString("id");
            avatar = bundle.getString("avatar");
            email = bundle.getString("email");
            birthday = bundle.getString("birthday");
            gender = bundle.getString("gender");
        }

        Toast.makeText(this, "Thong tin: " + email, Toast.LENGTH_SHORT).show();

        try {
            mSocket = IO.socket(SOCKET_URL);
        } catch (URISyntaxException e) {
            Toast.makeText(this, ""+ e, Toast.LENGTH_SHORT).show();
        }

        mSocket.on("broadcast match request", onBroadcastMatchRequest);
        mSocket.on("broadcast request accepted", onBroadcastRequestAccepted);

        mSocket.connect();

        editReceiverUsername = findViewById(R.id.edit_receiver_username);
        imageHeartLeft = findViewById(R.id.image_heart_left);
        imageHeartRight = findViewById(R.id.image_heart_right);
        relativeLayout = findViewById(R.id.relative);

        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        imageHeartLeft.setVisibility(View.INVISIBLE);
        imageHeartRight.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, lightSensor, sensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
                if(sensorEvent.values[0] < 1) {

//                    Animation animationRotale = AnimationUtils.loadAnimation(this, R.anim.rotate);
//                    Animation animationZoomOut = AnimationUtils.loadAnimation(this, R.anim.zoom_out);
//                    Animation animationZoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);

                    //imageHeartLeft.startAnimation(animationZoomOut);

                    if(isReceivingRequest) { //Neu dang nhan duoc loi moi ket ban =>> cho dong y!
                        showHeartRight();
                        acceptMatchRequest(email, receiverEmail);
                    }
                    else { // Thuc hien gui loi moi ket ban
                        showHeartLeft();

                        sendMatchRequest(email, editReceiverUsername.getText().toString());
                    }
                }
                else {

                    YoYo.with(Techniques.Tada)
                            .duration(1000)
                            .repeat(10)
                            .playOn(findViewById(R.id.image_heart_left));
                }
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        //mSocket.off("new message", onBroadcastMatchRequest);
    }

    private void sendMatchRequest(String sender, String receiver) {
        JsonObject json = new JsonObject();
        json.addProperty("sender", sender);
        json.addProperty("receiver", receiver);

        mSocket.emit("send match request", json);
    }

    private void acceptMatchRequest(String sender, String receiver) {
        JsonObject json = new JsonObject();
        json.addProperty("sender", sender);
        json.addProperty("receiver", receiver);

        mSocket.emit("accept request", json);
    }

    private void showHeartLeft() {
        imageHeartLeft.setVisibility(View.VISIBLE);

        YoYo.with(Techniques.ZoomInLeft)
                .duration(1000)
                .repeat(5)
                .playOn(imageHeartLeft);

        YoYo.with(Techniques.FadeIn)
                .duration(1000)
                .repeat(5)
                .playOn(imageHeartLeft);
    }

    private void showHeartRight() {
        imageHeartRight.setVisibility(View.VISIBLE);

        YoYo.with(Techniques.ZoomInLeft)
                .duration(1000)
                .repeat(5)
                .playOn(imageHeartRight);

        YoYo.with(Techniques.FadeIn)
                .duration(1000)
                .repeat(5)
                .playOn(imageHeartRight);
    }

    private Emitter.Listener onBroadcastMatchRequest = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    JSONObject data = (JSONObject) args[0];
                    Toast.makeText(MainActivity.this, "data: " + data, Toast.LENGTH_SHORT).show();
                    String sender;
                    String receiver;
                    try {
                        sender = data.getString("sender");
                        receiverEmail = sender;
                        receiver = data.getString("receiver");

                        if(receiver.equals(email)) {
                            Toast.makeText(MainActivity.this, "receiver: " + receiver, Toast.LENGTH_SHORT).show();
                            isReceivingRequest = true;

                            showHeartLeft();

                            Toast.makeText(MainActivity.this, "Co loi moi ket ban ne!",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Du lieu: " + data,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };

    private Emitter.Listener onBroadcastRequestAccepted = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    String sender;
                    String receiver;
                    try {
                        sender = data.getString("sender");
                        receiver = data.getString("receiver");

                        if(receiver.equals(email)) {
                            showHeartRight();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Du lieu: " + data,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };
}
