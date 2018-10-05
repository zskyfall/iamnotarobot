package com.powerranger.sow2.iamnotarobot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.powerranger.sow2.iamnotarobot.configuration.API;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class RegisterActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Init();

    }

    private void Init() {
        callbackManager = CallbackManager.Factory.create();

        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday",
                "user_friends", "user_gender"));

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final String token = loginResult.getAccessToken().getToken();
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        User user = getFacebookUser(object, token);
                        if(user != null) {
                            login(user);
                        }

                        Log.d("kiemtra", response.toString());
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,email,birthday,friends,gender,name");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    private User getFacebookUser(JSONObject object, String token) {
        User user;
        try {
            String id = object.getString("id");
            URL profile_picture = new URL("https://graph.facebook.com/" +
                    id + "/picture?width=250&height=250");
            String avatar = profile_picture.toString();
            String name = object.getString("name");
            String email = object.getString("email");
            String birthday = object.getString("birthday");
            String friends = object.getJSONObject("friends").getJSONObject("summary").getString("total_count");
            String gender = object.getString("gender");

            user = new User(id, name, avatar, email, gender, birthday, token);
            return user;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
        }

        return null;
    }

    private void goMainScreen(User user) {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("id", user.getId());
        bundle.putString("avatar", user.getAvatar());
        bundle.putString("email", user.getEmail());
        bundle.putString("birthday", user.getBirthday());
        //bundle.putString("friends", friends);
        bundle.putString("gender", user.getGender());
        intent.putExtra("profile", bundle);
        startActivity(intent);
    }

    private void login(final User user) {
        if(user != null) {
            JsonObject json = new JsonObject();
            json.addProperty("email", user.getEmail());
            json.addProperty("avatar", user.getAvatar() );
            json.addProperty("name", user.getName());
            json.addProperty("birthday", user.getBirthday() );
            json.addProperty("gender",user.getGender() );
            json.addProperty("token", user.getToken());
            json.addProperty("fbId", user.getId());

            Ion.with(this)
                    .load(API.Server.LOGIN)
                    .setJsonObjectBody(json)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if(result != null) {
                                Toast.makeText(RegisterActivity.this, "" + result, Toast.LENGTH_SHORT).show();
                                goMainScreen(user);
                            }
                            else {
                                Toast.makeText(RegisterActivity.this, "ERROR: " + e, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
