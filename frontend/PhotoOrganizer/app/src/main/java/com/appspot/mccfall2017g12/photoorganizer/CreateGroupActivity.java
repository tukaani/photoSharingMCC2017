package com.appspot.mccfall2017g12.photoorganizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * Created by Ilkka on 28.11.2017.
 */

public class CreateGroupActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    Button time_button;
    int year, month, day, hour, minute;
    int year_selected, month_selected, day_selected, hour_selected, minute_selected;

    @Override
    protected void onCreate(Bundle savedInstaceState){
        super.onCreate(savedInstaceState);
        setContentView(R.layout.activity_create_group);

        time_button = (Button) findViewById(R.id.time_button);

        time_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //find the current date and use as default value and launch datepicker
                Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateGroupActivity.this,
                        CreateGroupActivity.this, year,month,day);
                datePickerDialog.show();
            }
        });
    }

    public void onDateSet (DatePicker datePicker, int y, int m, int d){
        year_selected = y;
        month_selected = m + 1; //months are marked 0-11
        day_selected = d;
        //find the current time, set as default and launch timepicker
        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(CreateGroupActivity.this,
                CreateGroupActivity.this, hour, minute, android.text.format.DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    public void onTimeSet (TimePicker timePicker, int h, int m){
        hour_selected = h;
        minute_selected = m;
        //Do stuff with the final time
        Context context = getApplicationContext();
        Toast.makeText(context, String.valueOf(year_selected) + String.valueOf(month_selected) +
                String.valueOf(day_selected) + String.valueOf(hour_selected) + String.valueOf(minute_selected), Toast.LENGTH_SHORT).show();
    }

}
