package com.sample.app.drivepoints.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sample.app.drivepoints.R;
import com.sample.app.drivepoints.Utils.InputValidation;

public class ApplyInsuranceActivity extends AppCompatActivity {

    private ImageView back_img;
    private EditText firstname, lastname, email, password, phone, vehicleno;
    private LinearLayout apply_btn;
    private InputValidation inputValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_insurance);

        inputValidation = new InputValidation(ApplyInsuranceActivity.this);


        back_img = findViewById(R.id.back_img);
        firstname = findViewById(R.id.firstname_id);
        lastname = findViewById(R.id.lastname_id);
        email = findViewById(R.id.email_id);
        password = findViewById(R.id.password_id);
        phone = findViewById(R.id.phone_id);
        vehicleno = findViewById(R.id.vehicleno_id);
        apply_btn = findViewById(R.id.apply_btn);


        back_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        apply_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inputValidation.isInputEditTextFilled(firstname, "Enter First Name")) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled(lastname, "Enter Last Name")) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled(email, "Enter Your Email")) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled(password, "Enter Your Password")) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled(phone, "Enter Your Phone")) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled(vehicleno, "Enter Your Number")) {
                    return;
                }
                Toast.makeText(ApplyInsuranceActivity.this, "Applied!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}