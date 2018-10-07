package com.powerranger.sow2.iamnotarobot;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.powerranger.sow2.iamnotarobot.configuration.API;
import com.powerranger.sow2.iamnotarobot.interfaces.SearchItemClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener, SearchItemClickListener {
    private SensorManager sensorManager;
    private Sensor lightSensor; //cam bien anh sang
    private Sensor mAccelerometer; //cam bien gia toc
    private ShakeDetector mShakeDetector;

    private ImageView imageHeartLeft;
    private ImageView imageHeartRight;
    private ImageView imageMyAvatar;
    private ImageView imageCrushAvatar;
    private TextView textMyName;
    private TextView textCrushName;

    private RelativeLayout relativeMainContent;
    private RelativeLayout relativeLayout;
    private Socket mSocket;
    private final String SOCKET_URL = API.Server.SOCKET_URL;
    private Intent intent;
    private Bundle bundle;
    private String receiverEmail;
    private String id;
    private String email;
    private String avatar;
    private String birthday;
    private String gender;
    private String name;
    private String crushEmail;

    private Boolean isReceivingRequest = false;

    int notificationID;

    private MaterialSearchView searchView;
    private ArrayList<User> arrayListUser = new ArrayList<>();
    private SearchAdapter searchAdapter;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    Toolbar mToolbar;

    //NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Init();
        InitSearch();
        createNotification();
        searchEventHandle();
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
            name = bundle.getString("name");
        }

        //Toast.makeText(this, "Thong tin: " + email, Toast.LENGTH_SHORT).show();

        try {
            mSocket = IO.socket(SOCKET_URL);
        } catch (URISyntaxException e) {
            Toast.makeText(this, ""+ e, Toast.LENGTH_SHORT).show();
        }

        mSocket.on("broadcast match request", onBroadcastMatchRequest);
        mSocket.on("broadcast request accepted", onBroadcastRequestAccepted);

        mSocket.connect();

        imageHeartLeft = findViewById(R.id.image_heart_left);
        imageHeartRight = findViewById(R.id.image_heart_right);
        imageMyAvatar = findViewById(R.id.image_my_avatar);
        imageCrushAvatar = findViewById(R.id.image_crush_avatar);
        textMyName = findViewById(R.id.text_my_name);
        textCrushName = findViewById(R.id.text_crush_name);

        Glide.with(this).load(avatar).into(imageMyAvatar);
        textMyName.setText(name);

        relativeLayout = findViewById(R.id.relative);
        relativeMainContent = findViewById(R.id.relative_main_content);

        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {

                handleShakeEvent(count);
            }
        });

        imageHeartLeft.setVisibility(View.INVISIBLE);
        imageHeartRight.setVisibility(View.INVISIBLE);

    }

    private void InitSearch() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        searchView = findViewById(R.id.search_view);
        searchView.setVoiceSearch(true); //or false
        recyclerView = findViewById(R.id.recycler_search_result);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        searchAdapter = new SearchAdapter(this, arrayListUser);
        searchAdapter.addClickListenter(this);
        recyclerView.setAdapter(searchAdapter);
    }

    private void searchEventHandle() {
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                showSearchList();
                if(newText.length() > 0) {
                    newText = newText.toLowerCase();
                    String search_url = API.Server.SEARCH;

                    JsonObject json = new JsonObject();
                    json.addProperty("query", newText);
                    Ion.with(getApplicationContext())
                            .load(search_url)
                            .setJsonObjectBody(json)
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    ArrayList<User> newList = new ArrayList<>();
                                    if(result != null) {

                                        JsonArray arrayUsers = result.getAsJsonArray("users");
                                        for (int i = 0; i < arrayUsers.size(); i++) {
                                            JsonObject objUser = arrayUsers.get(i).getAsJsonObject();

                                            JsonElement eleEmail = objUser.get("email");
                                            JsonElement eleName = objUser.get("name");
                                            JsonElement eleAvatar = objUser.get("avatar");
                                            JsonElement eleBirthday = objUser.get("birthday");
                                            JsonElement eleGender = objUser.get("gender");
                                            JsonElement eleFbId = objUser.get("fbId");
                                            JsonElement eleToken = objUser.get("token");

                                            String email = eleEmail.getAsString();
                                            String name = eleName.getAsString();
                                            String avatar = eleAvatar.getAsString();
                                            String birthday = eleBirthday.getAsString();
                                            String gender = eleGender.getAsString();
                                            String fbId = eleFbId.getAsString();
                                            String token = eleToken.getAsString();

                                            User user = new User(fbId, name, avatar, email, gender, birthday, token);
                                            newList.add(user);

                                        }
                                        searchAdapter.setFilter(newList);
                                    }
                                    else {
                                        //Toast.makeText(SearchActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    return true;
                }
                else {
                    ArrayList<User> emptyList = new ArrayList<>();
                    searchAdapter.setFilter(emptyList);
                    return false;
                }
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                hideMainContent();
            }

            @Override
            public void onSearchViewClosed() {
                showMainContent();
            }
        });

    }

    private void handleShakeEvent(int count) {
        Toast.makeText(this, "lac : " + count, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        sensorManager.unregisterListener(mShakeDetector);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
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
                        sendMatchRequest(email, crushEmail);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    private void sendMatchRequest(String sender, String receiver) {

        if(receiver != null && !receiver.equals("")) {
            JsonObject json = new JsonObject();
            json.addProperty("sender", sender);
            json.addProperty("sender_name", name);
            json.addProperty("sender_avatar", avatar);
            json.addProperty("receiver", receiver);

            mSocket.emit("send match request", json);
            showHeartLeft();
        }
        else {
            Toast.makeText(this, "Chọn Người Nhận Trước!", Toast.LENGTH_SHORT).show();
        }
    }

    private void acceptMatchRequest(String sender, String receiver) {
        JsonObject json = new JsonObject();
        json.addProperty("sender", sender);
        json.addProperty("receiver", receiver);
        showHeartLeft();
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

    private void hideHeartLeft() {
        imageHeartLeft.setVisibility(View.GONE);
    }

    private void hideHeartRight() {
        imageHeartRight.setVisibility(View.GONE);
    }

    private void showSearchList() {
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void hideSearchList() {
        recyclerView.setVisibility(View.GONE);
    }

    private void showMainContent() {
        relativeMainContent.setVisibility(View.VISIBLE);
    }

    private void hideMainContent() {
        relativeMainContent.setVisibility(View.GONE);
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
                    String sender_name;
                    String sender_avatar;
                    String receiver;
                    try {
                        sender = data.getString("sender");
                        sender_avatar = data.getString("sender_avatar");
                        sender_name = data.getString("sender_name");
                        receiverEmail = sender;
                        receiver = data.getString("receiver");

                        if(receiver.equals(email)) {
                            Toast.makeText(MainActivity.this, "receiver: " + receiver, Toast.LENGTH_SHORT).show();
                            isReceivingRequest = true;
                            textCrushName.setText(sender_name);
                            Glide.with(getApplicationContext()).load(sender_avatar).into(imageCrushAvatar);
                            showHeartRight();

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

    private void createNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.heartleft)
                .setContentTitle("Thong bao")
                .setContentText("Co loi moi ket ban!");

        Intent resulIntent = new Intent(this, RegisterActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resulIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

//        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        builder.setSound(uri);

        Uri notificationSound = Uri.parse("android.resource://"
        + getPackageName() + "/" + R.raw.gaugau);
        builder.setSound(notificationSound);

        notificationID = 113;
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(notificationID, builder.build());
    }

    @Override
    public void onItemClicked(String avatar, String name, String email) {
        hideSearchList();
        showMainContent();
        hideHeartLeft();
        hideHeartRight();
        searchView.closeSearch();
        searchView.clearFocus();
        imageCrushAvatar.setVisibility(View.VISIBLE);
        textCrushName.setVisibility(View.VISIBLE);
        crushEmail = email;
        Glide.with(this).load(avatar).into(imageCrushAvatar);
        textCrushName.setText(name);

    }
}
