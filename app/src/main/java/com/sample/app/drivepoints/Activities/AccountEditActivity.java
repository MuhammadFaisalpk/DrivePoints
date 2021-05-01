package com.sample.app.drivepoints.Activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sample.app.drivepoints.R;

public class AccountEditActivity extends AppCompatActivity {

    private TextView name, type, rating, first_name,
            last_name, email, password, contact, vehicle_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_edit);

        name = findViewById(R.id.fullname);
        type = findViewById(R.id.type);
        rating = findViewById(R.id.rating);
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        contact = findViewById(R.id.contact);
        vehicle_number = findViewById(R.id.vehicle_number);
    }
}