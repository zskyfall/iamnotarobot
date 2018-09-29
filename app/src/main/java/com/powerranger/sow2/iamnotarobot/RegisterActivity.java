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
                String token = loginResult.getAccessToken().getToken();
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        getFacebookData(object);
                        Log.d("kiemtra", response.toString());
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,email,birthday,friends,gender");
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

    private void getFacebookData(JSONObject object) {
        try {
            String id = object.getString("id");
            URL profile_picture = new URL("https://graph.facebook.com/" +
                    id + "/picture?width=250&height=250");
            String email = object.getString("email");
            String birthday = object.getString("birthday");
            String friends = object.getJSONObject("friends").getJSONObject("summary").getString("total_count");
            String gender = object.getString("gender");

            goMainScreen(id, profile_picture.toString(), email, birthday, friends, gender);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
        }
    }

    private void goMainScreen(String id, String profile_picture, String email, String birthday,
                              String friends, String gender) {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putString("avatar", profile_picture);
        bundle.putString("email", email);
        bundle.putString("birthday", birthday);
        bundle.putString("friends", friends);
        bundle.putString("gender", gender);
        intent.putExtra("profile", bundle);
        startActivity(intent);
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
