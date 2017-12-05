package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private Button mRegister;
    private EditText mEmail;
    private EditText mPassword;
    private Button mLogin;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private DatabaseReference mDatabase;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkPermissions();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mRegister = (Button) findViewById(R.id.register);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        mEmail = (EditText) findViewById(R.id.emailLogin);
        mPassword = (EditText) findViewById(R.id.passwordLogin);
        mLogin = (Button) findViewById(R.id.loginbutton);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mEmail.getText().toString().matches("") && !mPassword.getText().toString().matches("")) {
                    mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update User object
                                        User.set(task.getResult().getUser().getUid(), getApplicationContext());
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(LoginActivity.this, "Incorrect login, try again", Toast.LENGTH_SHORT).show();
                                        mPassword.setText("");
                                        mPassword.requestFocus();
                                    }

                                    // ...
                                }
                            });
                }
                else{
                    Toast.makeText(LoginActivity.this, "Fill both fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Button listeners



    }


    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
       if(currentUser != null) {
            User.set(currentUser.getUid(), getApplicationContext());
            startActivity(new Intent(LoginActivity.this, MainActivity.class));

        }
    }



    //TODO
    private void checkPermissions() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeExternalFile = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readExternalFile = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if(cameraPermission && readExternalFile & writeExternalFile)
                    {

                    } else {
                       checkPermissions();
                    }
                }
                break;
        }
    }
}
