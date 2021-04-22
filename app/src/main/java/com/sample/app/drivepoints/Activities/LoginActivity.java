package com.sample.app.drivepoints.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sample.app.drivepoints.R;
import com.sample.app.drivepoints.Utils.InputValidation;

public class LoginActivity extends AppCompatActivity {

    private EditText Email, Password;
    private LinearLayout Login;
    private InputValidation inputValidation;
    private String MyPREFERENCES = "mypref";
    SharedPreferences sharedpreferences;
    boolean firsttime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        InitiatingViews();

    }

    private void InitiatingViews() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        inputValidation = new InputValidation(LoginActivity.this);

        Email = findViewById(R.id.email_id);
        Password = findViewById(R.id.password_id);
        Login = findViewById(R.id.login_btn);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Email.getText().toString().trim();
                String password = Password.getText().toString().trim();

                if (!inputValidation.isInputEditTextFilled(Email, getString(R.string.error_message_email))) {
                    return;
                }
                if (!inputValidation.isInputEditTextEmail(Email, getString(R.string.error_valid_email))) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled(Password, getString(R.string.error_message_password))) {
                    return;
                }
                sharedpreferences.edit().putBoolean("firsttime", false).apply();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
            }
        });

    }
}