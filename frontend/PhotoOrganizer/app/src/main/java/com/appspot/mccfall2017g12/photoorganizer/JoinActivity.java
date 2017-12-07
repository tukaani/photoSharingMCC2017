package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.appspot.mccfall2017g12.photoorganizer.http.GroupHttpClient;
import com.appspot.mccfall2017g12.photoorganizer.http.JoinGroupRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.Result;

import org.json.JSONObject;

import java.io.IOException;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import okhttp3.Response;

public class JoinActivity extends UserSensitiveActivity
        implements ZXingScannerView.ResultHandler {

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

        zXingScannerView.stopCamera();
        returnToMainActivity();
    }

    @Override
    protected boolean shouldGoOn() {
        return !getUser().isInGroup();
    }

    public void sendPost(final String qr, final JoinActivity joinActivity) {

        ThreadTools.EXECUTOR.execute(new Runnable() {
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
                            Response response = groupHttpClient.joinGroup(joinGroupRequest, "application/json", getUser().getIdtoken());
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
                                parent.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        JoinActivity.this.startActivity(new Intent(JoinActivity.this, MainActivity.class));
                                        finish();
                                    }
                                });
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
    }
}
