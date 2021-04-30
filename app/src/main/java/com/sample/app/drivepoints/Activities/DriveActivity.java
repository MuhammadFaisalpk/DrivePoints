package com.sample.app.drivepoints.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.sample.app.drivepoints.Model.RouteDetails;
import com.sample.app.drivepoints.R;
import com.sample.app.drivepoints.Utils.LocationUpdatesService;
import com.sample.app.drivepoints.Utils.Shared;
import com.sample.app.drivepoints.interfaces.IBaseGpsListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DriveActivity extends AppCompatActivity implements OnMapReadyCallback, IBaseGpsListener {

    private GoogleMap map;
    SupportMapFragment mapFragment;
    ArrayList<LatLng> route_latlng = new ArrayList<LatLng>();
    ArrayList<RouteDetails> route_details = new ArrayList<>();
    String start_time, end_time;
    private String MyPREFERENCES = "mypref", User_Token, User_Name, User_Type;
    SharedPreferences sharedpreferences;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private PlacesClient placesClient;
    private Location lastKnownLocation = null;
    private CameraPosition cameraPosition;
    private static final float DEFAULT_ZOOM = 18F;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    boolean isStartLatLng = true;
    double startLat = 0, startLng = 0, endLat = 0, endLng = 0;
    private LinearLayout finish_btn;
    AlertDialog trafficDialog, takeactionDialog, finishDialog;

    //Current Location Components
    private static final String TAG = DriveActivity.class.getSimpleName();
    private IncomingMessageHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_drive);

        initiatingViews();
        gettingUserDetails();
        gettingRouteData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        this.map.getUiSettings().setZoomControlsEnabled(true);

        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

//                TextView snippet = infoWindow.findViewById(R.id.snippet);
//                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
        updateLocationUI();
        getDeviceLocation();
        setLocationPolyline();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        if (location != null) {

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.setMyLocationEnabled(true);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14F);
            map.animateCamera(cameraUpdate);

            if (isStartLatLng) {
                startLat = location.getLatitude();
                startLng = location.getLongitude();
                isStartLatLng = false;
            }

            endLat = location.getLatitude();
            endLng = location.getLongitude();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler = null;
    }

    private void initiatingViews() {
        finish_btn = findViewById(R.id.finish_btn);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mHandler = new IncomingMessageHandler();

        //For Starting Service
//        Intent startServiceIntent = new Intent(DriveActivity.this, LocationUpdatesService.class);
//        Messenger messengerIncoming = new Messenger(mHandler);
//        startServiceIntent.putExtra(Shared.MESSENGER_INTENT_KEY, messengerIncoming);
//        startService(startServiceIntent);

        //For Ending Service
//        Intent stopServiceIntent = new Intent(DriveActivity.this, LocationUpdatesService.class);
//        Messenger stopmessenger = new Messenger(mHandler);
//        startServiceIntent.putExtra("STOP_SERVICE", stopmessenger);
//        startService(stopServiceIntent);
    }

    class IncomingMessageHandler extends Handler {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage..." + msg.toString());

            super.handleMessage(msg);

            switch (msg.what) {
                case LocationUpdatesService.LOCATION_MESSAGE:
                    try {
                        String current_address;
                        double latitude, longitude;

                        Location location = (Location) msg.obj;
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder(DriveActivity.this, Locale.getDefault());


                        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                        current_address = addresses.get(0).getAddressLine(0);
                        Toast.makeText(DriveActivity.this, "LAT :  " + latitude + "\nLNG : " + longitude + "\nAddress : " + current_address, Toast.LENGTH_SHORT).show();
//                        locationMsg.setText("LAT :  " + latitude + "\nLNG : " + longitude + "\n\n" + current_address + "\n\n" + location.toString() + " \n\n\nLast updated- " + currentDateTimeString);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
    }

    private void gettingUserDetails() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        User_Name = sharedpreferences.getString("user_name", null);
        User_Type = sharedpreferences.getString("user_type", null);
        User_Token = sharedpreferences.getString("user_token", null);
    }

    private void gettingRouteData() {
        route_latlng = Shared.route_latlng;
        route_details = Shared.route_details;
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            Geocoder geocoder;
                            List<Address> addresses_list;
                            geocoder = new Geocoder(DriveActivity.this, Locale.getDefault());

                            try {
                                addresses_list = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                String current_address = addresses_list.get(0).getAddressLine(0);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//                            map.addMarker(new MarkerOptions()
//                                    .position(new LatLng(lastKnownLocation.getLatitude(),
//                                            lastKnownLocation.getLongitude()))
//                                    .title("My Location"));
                        }
                    } else {
                        map.animateCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });
        } catch (
                SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void setLocationPolyline() {
//        map.clear();

//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(route_latlng.get(0), DEFAULT_ZOOM));

        map.addMarker(new MarkerOptions()
                .position(route_latlng.get(0))
                .title("Start Point" + "\n" + "\n" + route_details.get(0).getFrom_address()));

        map.addMarker(new MarkerOptions()
                .position(route_latlng.get(route_latlng.size() - 1))
                .title("End Point" + "\n" + "\n" + route_details.get(0).getTo_address()));

        PolylineOptions options1 = new PolylineOptions().width(5).color(getResources().getColor(R.color.r1_color)).geodesic(true);

        Polyline polyline1 = null;

        for (int i = 0; i < route_latlng.size() - 1; i++) {
            options1.add(route_latlng.get(i));
        }

        polyline1 = map.addPolyline(options1);

        polyline1.setTag("Current Route");
    }

    private void TrafficAlertDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DriveActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.traffic_popup, null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
        trafficDialog = dialogBuilder.create();
        trafficDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView name_text = (TextView) dialogView.findViewById(R.id.name_text);
        TextView got_it = (TextView) dialogView.findViewById(R.id.got_it);
        ImageView close_dialog = (ImageView) dialogView.findViewById(R.id.close_dialog);

        name_text.setText("Hi," + User_Name + "!");

        close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficDialog.dismiss();
            }
        });
        got_it.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficDialog.dismiss();
            }
        });

        trafficDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            trafficDialog.show();
        } catch (WindowManager.BadTokenException e) {
            //use a log message
        }
    }

    private void TakeActionDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DriveActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.action_popup, null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
        takeactionDialog = dialogBuilder.create();
        takeactionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView change_route = (TextView) dialogView.findViewById(R.id.change_route);
        TextView call_assistance = (TextView) dialogView.findViewById(R.id.call_assistance);
        ImageView close_dialog = (ImageView) dialogView.findViewById(R.id.close_dialog);


        close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeactionDialog.dismiss();
            }
        });
        change_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        call_assistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        takeactionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            takeactionDialog.show();
        } catch (WindowManager.BadTokenException e) {
            //use a log message
        }
    }

    private void FinishRideDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DriveActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.finish_ride_popup, null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
        finishDialog = dialogBuilder.create();
        finishDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView close_dialog = (ImageView) dialogView.findViewById(R.id.close_dialog);
        TextView from_location = (TextView) dialogView.findViewById(R.id.from_location);
        TextView to_location = (TextView) dialogView.findViewById(R.id.to_location);
        TextView mileage_text = (TextView) dialogView.findViewById(R.id.mileage_text);
        TextView time_estimate_text = (TextView) dialogView.findViewById(R.id.time_estimate_text);
        TextView download = (TextView) dialogView.findViewById(R.id.download);
        TextView dashboard = (TextView) dialogView.findViewById(R.id.dashboard);
        TextView email = (TextView) dialogView.findViewById(R.id.email);
        TextView share = (TextView) dialogView.findViewById(R.id.share);

        from_location.setText(route_details.get(0).getFrom_address());
        to_location.setText(route_details.get(0).getTo_address());
        mileage_text.setText(route_details.get(0).getRoute_distance());
        time_estimate_text.setText(route_details.get(0).getRoute_duration());

        close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finishDialog.dismiss();
            }
        });
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DriveActivity.this, "Download", Toast.LENGTH_SHORT).show();
            }
        });
        dashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DriveActivity.this, "Dashboard", Toast.LENGTH_SHORT).show();
            }
        });
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DriveActivity.this, "Email", Toast.LENGTH_SHORT).show();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DriveActivity.this, "Share", Toast.LENGTH_SHORT).show();
            }
        });

        finishDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            finishDialog.show();
        } catch (WindowManager.BadTokenException e) {
            //use a log message
        }
    }

    private void RatingDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DriveActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.rating_popup, null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
        finishDialog = dialogBuilder.create();
        finishDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView close_dialog = (ImageView) dialogView.findViewById(R.id.close_dialog);
        TextView from_location = (TextView) dialogView.findViewById(R.id.from_location);
        TextView to_location = (TextView) dialogView.findViewById(R.id.to_location);
        TextView mileage_text = (TextView) dialogView.findViewById(R.id.mileage_text);
        TextView time_estimate_text = (TextView) dialogView.findViewById(R.id.time_estimate_text);
        TextView download = (TextView) dialogView.findViewById(R.id.download);
        TextView dashboard = (TextView) dialogView.findViewById(R.id.dashboard);
        TextView email = (TextView) dialogView.findViewById(R.id.email);
        TextView share = (TextView) dialogView.findViewById(R.id.share);

        from_location.setText(route_details.get(0).getFrom_address());
        to_location.setText(route_details.get(0).getTo_address());
        mileage_text.setText(route_details.get(0).getRoute_distance());
        time_estimate_text.setText(route_details.get(0).getRoute_duration());

        close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finishDialog.dismiss();
            }
        });
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DriveActivity.this, "Download", Toast.LENGTH_SHORT).show();
            }
        });
        dashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DriveActivity.this, "Dashboard", Toast.LENGTH_SHORT).show();
            }
        });
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DriveActivity.this, "Email", Toast.LENGTH_SHORT).show();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DriveActivity.this, "Share", Toast.LENGTH_SHORT).show();
            }
        });

        finishDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            finishDialog.show();
        } catch (WindowManager.BadTokenException e) {
            //use a log message
        }
    }


}