package com.sample.app.drivepoints.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.sample.app.drivepoints.R;

public class SplashActivity extends AppCompatActivity {

    private String MyPREFERENCES = "mypref";
    SharedPreferences sharedpreferences;
    boolean firsttime = false, introcheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        InitiatingViews();
    }

    private void InitiatingViews() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        firsttime = sharedpreferences.getBoolean("firsttime", true);
        introcheck = sharedpreferences.getBoolean("introcheck", true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!firsttime) {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                } else if (!introcheck) {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    finish();
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(SplashActivity.this, IntroActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        }, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FullScreencall();
    }

    public void FullScreencall() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
