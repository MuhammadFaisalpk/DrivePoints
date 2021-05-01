package com.sample.app.drivepoints.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sample.app.drivepoints.R;
import com.sample.app.drivepoints.Utils.InputValidation;

public class HelpCenterActivity extends AppCompatActivity {

    private ImageView back_img;
    private EditText name, email, subject, message;
    private LinearLayout submit_btn;
    private InputValidation inputValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_center);

        inputValidation = new InputValidation(HelpCenterActivity.this);

        back_img = findViewById(R.id.back_img);
        name = findViewById(R.id.name_id);
        email = findViewById(R.id.email_id);
        subject = findViewById(R.id.subject_id);
        message = findViewById(R.id.message_id);
        submit_btn = findViewById(R.id.submit_btn);


        back_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inputValidation.isInputEditTextFilled(name, "Enter Name")) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled(email, "Enter Your Email")) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled(subject, "Enter Subject")) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled(message, getString(R.string.write_message))) {
                    return;
                }
                Toast.makeText(HelpCenterActivity.this, "Sent!", Toast.LENGTH_SHORT).show();

            }
        });
    }
}