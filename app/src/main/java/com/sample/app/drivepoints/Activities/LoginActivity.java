package com.sample.app.drivepoints.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.sample.app.drivepoints.Model.RouteDetails;
import com.sample.app.drivepoints.R;
import com.sample.app.drivepoints.Utils.InputValidation;
import com.sample.app.drivepoints.Utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText Email, Password;
    private LinearLayout Login;
    private InputValidation inputValidation;
    private String MyPREFERENCES = "mypref";
    SharedPreferences sharedpreferences;
    String SIGNIN_URL_ADDRESS = "http://34.69.251.155:8000/api/v1/signin/";
    private ProgressDialog Loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        InitiatingViews();

    }

    private void InitiatingViews() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        inputValidation = new InputValidation(LoginActivity.this);

        Loading = new ProgressDialog(this);
        Loading.setCancelable(false);

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
                hideSoftKeyboard(LoginActivity.this);
                if (isNetworkAvailable()) {
                    CheckingSignin(email, password);
                } else {
                    NoConectionAlert();
                }

            }
        });

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void NoConectionAlert() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("No internet connection!");
        builder1.setCancelable(false);
        builder1.setNegativeButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void CheckingSignin(String email, String password) {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("email", email);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Loading.setMessage("Checking User Data.Please wait");
        showDialog();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, SIGNIN_URL_ADDRESS, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                String message = response.getString("message");
                                String name = response.getString("name");
                                String token = response.getString("token");
//                                boolean is_driver = response.getBoolean("is_driver");
//                                boolean is_employee = response.getBoolean("is_employee");
//                                boolean is_entrepreneur = response.getBoolean("is_entrepreneur");
//                                boolean is_private_user = response.getBoolean("is_private_user");

                                sharedpreferences.edit().putBoolean("firsttime", false).apply();
                                sharedpreferences.edit().putString("user_name", name).apply();
                                sharedpreferences.edit().putString("user_token", token).apply();
//                                if (is_driver) {
//                                    sharedpreferences.edit().putString("user_type", "Driver").apply();
//                                } else if (is_employee) {
//                                    sharedpreferences.edit().putString("user_type", "Employee").apply();
//                                } else if (is_entrepreneur) {
//                                    sharedpreferences.edit().putString("user_type", "Entrepreneur").apply();
//                                } else if (is_private_user) {
//                                    sharedpreferences.edit().putString("user_type", "Private User").apply();
//                                }
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                finish();
                                startActivity(intent);
                            } else {
                                String message = response.getString("message");
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception exception) {
                            exception.printStackTrace();
                            Toast.makeText(LoginActivity.this, "Some error occurred! Try again later.", Toast.LENGTH_LONG).show();
                            hideDialog();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error == null || error.networkResponse == null) {
                            return;
                        }
                        String body = null, msg = "";
                        JSONObject jsonObject;
                        //get status code here
                        final String statusCode = String.valueOf(error.networkResponse.statusCode);
                        //get response body and parse with appropriate encoding
                        try {
                            body = new String(error.networkResponse.data, "UTF-8");
                            jsonObject = new JSONObject(body);
                            msg = jsonObject.getString("message");
                        } catch (UnsupportedEncodingException | JSONException e) {
                            // exception
                        }

                        hideDialog();
                        Logger.addRecordToLog(LoginActivity.this.getLocalClassName() + "API Error: " + error);
                        Toast.makeText(LoginActivity.this, "" + msg, Toast.LENGTH_LONG).show();
                    }
                }) {

        };
        int socketTimeout = 30000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonObjectRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(jsonObjectRequest);
        jsonObjectRequest.setShouldCache(false);

    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    private void showDialog() {
        if (!Loading.isShowing())
            Loading.show();
    }

    private void hideDialog() {
        if (Loading.isShowing())
            Loading.dismiss();
    }
}