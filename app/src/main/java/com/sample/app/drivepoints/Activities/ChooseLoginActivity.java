package com.sample.app.drivepoints.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.sample.app.drivepoints.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Arrays;

public class ChooseLoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private LinearLayout Signup_btn, Login_btn, GooglesignInBtn, FBsignInBtn;
    public static final int SIGN_IN_CODE_GOOGLE = 157;
    CallbackManager mFacebookCallbackManager;
    private GoogleApiClient googleApiClient;
    AccessToken accessToken = AccessToken.getCurrentAccessToken();
    boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
    LoginManager mLoginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login);

        initView();

    }

    private void initView() {
        Signup_btn = findViewById(R.id.signup_btn);
        Login_btn = findViewById(R.id.login_btn);
        GooglesignInBtn = (LinearLayout) findViewById(R.id.googlesignin_btn);
        FBsignInBtn = (LinearLayout) findViewById(R.id.fbsignin_btn);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        GooglesignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, SIGN_IN_CODE_GOOGLE);
            }
        });
        FBsignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AccessToken.getCurrentAccessToken() != null) {
                    mLoginManager.logOut();
                } else {
                    mLoginManager.logInWithReadPermissions(ChooseLoginActivity.this, Arrays.asList("email", "user_birthday", "public_profile"));
                }
            }
        });
        Signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(ChooseLoginActivity.this, SignUpActivity.class);
//                startActivity(intent);
            }
        });

        setupFacebookStuff();
    }

    private void setupFacebookStuff() {

        // This should normally be on your application class
        FacebookSdk.sdkInitialize(getApplicationContext());

        mLoginManager = LoginManager.getInstance();
        mFacebookCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getApplicationContext(), "FB Sign in", Toast.LENGTH_LONG).show();

                // App code
                //loginResult.getAccessToken();
                //loginResult.getRecentlyDeniedPermissions()
                //loginResult.getRecentlyGrantedPermissions()
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
//                boolean loggedOut = AccessToken.getCurrentAccessToken() == null;
//                Toast.makeText(ChooseLogin.this, "Logged in!", Toast.LENGTH_SHORT).show();
//                if (!loggedOut) {
//                    Picasso.with(ChooseLogin.this).load(Profile.getCurrentProfile().getProfilePictureUri(100, 100)).into(imageView);
//                    Log.d("TAG", "Username is: " + Profile.getCurrentProfile().getName());

                //Using Graph API
                getUserProfile(AccessToken.getCurrentAccessToken());
//                }

            }

            @Override
            public void onCancel() {
                Toast.makeText(ChooseLoginActivity.this, "The login was canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(ChooseLoginActivity.this, "There was an error in the login", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void printHashKey(Context context) {
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                final String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i("AppLog", "key:" + hashKey + "=");
            }
        } catch (Exception e) {
            Log.e("AppLog", "error:", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isLoggedIn) {
            getUserProfile(AccessToken.getCurrentAccessToken());

            startActivity(new Intent(ChooseLoginActivity.this, MainActivity.class));
            finish();
        }
        verifyGoogleAccount();
    }

    private void verifyGoogleAccount() {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            if (result.isSuccess()) {
                Intent intent = new Intent(ChooseLoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_CODE_GOOGLE) {//Google
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            Toast.makeText(getApplicationContext(), "Google Sign in", Toast.LENGTH_LONG).show();
            GoogleSignInAccount account = result.getSignInAccount();
            String Name = account.getDisplayName();
            String email = account.getEmail();
            String userid = account.getId();
//            userName.setText(account.getDisplayName());
//            userEmail.setText(account.getEmail());
//            userId.setText(account.getId());
            gotoProfile();
        } else {
            Log.d("error", "" + result);

            Toast.makeText(getApplicationContext(), "Sign in cancel " + result, Toast.LENGTH_LONG).show();
        }
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d("TAG", object.toString());
                        try {
                            String first_name = object.getString("first_name");
                            String last_name = object.getString("last_name");
                            String email = object.getString("email");
                            String id = object.getString("id");
                            String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

//                            txtUsername.setText("First Name: " + first_name + "\nLast Name: " + last_name);
//                            txtEmail.setText(email);
//                            Picasso.with(ChooseLogin.this).load(image_url).into(imageView);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void gotoProfile() {
        Intent intent = new Intent(ChooseLoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}