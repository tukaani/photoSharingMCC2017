package com.appspot.mccfall2017g12.photoorganizer;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.Result;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

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


        sendPost(result.getText(), JoinActivity.this);
        zXingScannerView.stopCamera();
        JoinActivity.this.startActivity(new Intent(JoinActivity.this, MainActivity.class));
    }



    public void sendPost(final String qr, final JoinActivity joinActivity) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final JoinActivity parent=joinActivity;
                try {

                    URL url = new URL("http://10.100.23.218:8080/photoorganizer/api/v1.0/group/join");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Authorization", User.get().getIdtoken() );//User.get().getIdtoken());
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    String[] splitStr = qr.split("\\s+");
                    if(splitStr.length==2) {
                        jsonParam.put("group_id", splitStr[0]);
                        jsonParam.put("token", splitStr[1]);
                        jsonParam.put("user_id", mAuth.getCurrentUser().getUid());


                        Log.i("JSON", jsonParam.toString());
                        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                        os.writeBytes(jsonParam.toString());

                        os.flush();
                        os.close();
                        int responseCode = conn.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line + "\n");
                            }

                        }

                        if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                            parent.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(parent.getBaseContext(), "Bad request, try again!", Toast.LENGTH_LONG).show();
                                    JoinActivity.this.startActivity(new Intent(JoinActivity.this, GroupManagementActivity.class));
                                    finish();
                                }
                            });


                        }


                        conn.disconnect();
                    }
                    else{
                        parent.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(parent.getBaseContext(), "QR invalid, try again!", Toast.LENGTH_LONG).show();
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
