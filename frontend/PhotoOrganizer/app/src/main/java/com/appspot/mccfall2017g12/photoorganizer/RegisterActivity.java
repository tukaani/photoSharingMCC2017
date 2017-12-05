package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mPasswordConfirm;
    private Button mSubmit;
    private static final String TAG = "RegisterActivity";
    private EditText mUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mPasswordConfirm = (EditText) findViewById(R.id.passwordconfirm);
        mSubmit = (Button) findViewById(R.id.submit);
        mUsername = (EditText) findViewById(R.id.username);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mPassword.getText().toString().length()>5) {

                    if (mPassword.getText().toString().equals(mPasswordConfirm.getText().toString()) && mEmail.getText() != null) {

                        mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            System.out.println("created");
                                            Log.d(TAG, "createUserWithEmail:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            mDatabase.child("users").child(user.getUid()).child("username").setValue(mUsername.getText().toString());
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            startActivity(intent);

                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "createUserWithEmail:failure", task.getException());

                                        }


                                    }
                                });

                    }
                }
                else{
                    mPassword.setText("");
                    mPassword.setHint("Password");
                    mPassword.setHintTextColor(Color.RED);
                    mPassword.requestFocus();
                    mPasswordConfirm.setText("");
                    mPasswordConfirm.setHint("Repeat password");
                    mPasswordConfirm.setHintTextColor(Color.RED);
                    Toast.makeText(RegisterActivity.this,"Password lenght must have more than 5 characters", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
