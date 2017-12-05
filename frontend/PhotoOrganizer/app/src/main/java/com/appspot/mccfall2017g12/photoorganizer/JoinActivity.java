package com.appspot.mccfall2017g12.photoorganizer;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.appspot.mccfall2017g12.photoorganizer.http.http.GroupHttpClient;
import com.appspot.mccfall2017g12.photoorganizer.http.http.JoinGroupRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.Result;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.Response;

public class JoinActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView zXingScannerView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        mAuth =FirebaseAuth.getInstance();
        zXingScannerView = new ZXingScannerView(getApplicationContext());
        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
    }


    @Override
    protected void onPause() {
        super.onPause();
        zXingScannerView.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zXingScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {

        zXingScannerView.stopCamera();

        sendPost(result.getText(), JoinActivity.this);
    }



    public void sendPost(final String qr, final JoinActivity joinActivity) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final JoinActivity parent=joinActivity;
                try {
                    GroupHttpClient groupHttpClient = new GroupHttpClient();


                    JSONObject jsonParam = new JSONObject();
                    String[] splitStr = qr.split("\\s+");
                    if(splitStr.length == 2) {
                        JoinGroupRequest joinGroupRequest = new JoinGroupRequest();
                        joinGroupRequest.setGroup_id(splitStr[0]);
                        joinGroupRequest.setUser_id(mAuth.getCurrentUser().getUid());
                        joinGroupRequest.setToken(splitStr[1]);

                        try {
                            Response response = groupHttpClient.joinGroup(joinGroupRequest, "application/json", User.get().getIdtoken());
                            System.out.println(response.body().string());
                            if (response.code() == 400) {
                                parent.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(parent.getBaseContext(), "Bad request, try again!", Toast.LENGTH_LONG).show();
                                        JoinActivity.this.startActivity(new Intent(JoinActivity.this, GroupManagementActivity.class));
                                        finish();
                                    }
                                });

                            } else {
                                JoinActivity.this.startActivity(new Intent(JoinActivity.this, MainActivity.class));
                                finish();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                    else{
                        parent.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(parent.getBaseContext(), "Invalid QR, try again!", Toast.LENGTH_LONG).show();
                                JoinActivity.this.startActivity(new Intent(JoinActivity.this, GroupManagementActivity.class));
                                finish();
                            }
                        });
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}
