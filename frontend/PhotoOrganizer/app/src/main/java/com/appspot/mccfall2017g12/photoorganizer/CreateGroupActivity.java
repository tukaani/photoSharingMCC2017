package com.appspot.mccfall2017g12.photoorganizer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.appspot.mccfall2017g12.photoorganizer.http.CreateGroupRequest;
import com.appspot.mccfall2017g12.photoorganizer.http.Endpoints;
import com.appspot.mccfall2017g12.photoorganizer.http.SimpleGroupClient;

/**
 * Created by Ilkka on 28.11.2017.
 */

public class CreateGroupActivity extends UserSensitiveActivity {

    EditText nameEditText;
    EditText durationEditText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstaceState){
        super.onCreate(savedInstaceState);
        setContentView(R.layout.activity_create_group);

        nameEditText = findViewById(R.id.GroupName);
        durationEditText = findViewById(R.id.GroupDuration);
        button = findViewById(R.id.create_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEditText.getText().toString();
                int duration = Integer.valueOf(durationEditText.getText().toString());

                CreateGroupRequest request = new CreateGroupRequest();
                request.setAuthor(getUser().getUserId());
                request.setGroup_name(name);
                request.setValidity(duration);
                SimpleGroupClient.post(Endpoints.CREATE, request, getUser().getIdtoken(),
                        new SimpleGroupClient.Callback() {
                    @Override
                    public void onSuccess() {
                        returnToMainActivity();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    @Override
    protected boolean shouldGoOn() {
        return !getUser().isInGroup();
    }

}
