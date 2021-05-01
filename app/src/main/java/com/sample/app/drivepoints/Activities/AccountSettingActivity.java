package com.sample.app.drivepoints.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sample.app.drivepoints.R;

public class AccountSettingActivity extends AppCompatActivity {

    private ImageView back_img;

    private TextView name, type, rating, first_name,
            last_name, email, password, contact, vehicle_number;
    private String MyPREFERENCES = "mypref", User_Token, User_Name, User_Type;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        back_img = findViewById(R.id.back_img);
        name = findViewById(R.id.fullname);
        type = findViewById(R.id.type);
        rating = findViewById(R.id.rating);
        first_name = findViewById(R.id.first_name);
        last_name = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        contact = findViewById(R.id.contact);
        vehicle_number = findViewById(R.id.vehicle_number);

        gettingUserDetails();

        back_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void gettingUserDetails() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        User_Name = sharedpreferences.getString("user_name", null);
        User_Type = sharedpreferences.getString("user_type", null);
        User_Token = sharedpreferences.getString("user_token", null);

        name.setText(User_Name);
        type.setText(User_Type);
    }
}