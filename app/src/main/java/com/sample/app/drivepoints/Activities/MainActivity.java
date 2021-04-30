package com.sample.app.drivepoints.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.sample.app.drivepoints.Model.RecentLocation;
import com.sample.app.drivepoints.R;
import com.sample.app.drivepoints.Model.RouteDetails;
import com.sample.app.drivepoints.Utils.Logger;
import com.sample.app.drivepoints.Utils.Shared;
import com.sample.app.drivepoints.interfaces.IBaseGpsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.sample.app.drivepoints.Activities.MainActivity.hideSoftKeyboard;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener, GoogleMap.OnMapClickListener, IBaseGpsListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final float DEFAULT_ZOOM = 13F;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    AlertDialog routesDialog;

    String URL_ADDING_RECENT_LOCATIONS = "http://34.69.251.155:8000/api/v1/m/create-task/";
    String URL_FETCHING_RECENT_LOCATIONS = "http://34.69.251.155:8000/api/v1/route/travel/history/";
    String URL_ROUTES_LATLNG = "http://34.69.251.155:8000/api/v1/m/get/route-longlat/";
    private ProgressDialog Loading;
    SupportMapFragment mapFragment;
    private TextView destination_text, r1_text, r2_text, r3_text;
    private RelativeLayout My_location;
    private ImageView favourite_id;
    ArrayList<LatLng> r1_latlng = new ArrayList<LatLng>();
    ArrayList<LatLng> r2_latlng = new ArrayList<LatLng>();
    ArrayList<LatLng> r3_latlng = new ArrayList<LatLng>();
    ArrayList<RouteDetails> r1_details = new ArrayList<>();
    ArrayList<RouteDetails> r2_details = new ArrayList<>();
    ArrayList<RouteDetails> r3_details = new ArrayList<>();
    int newRoute_tag = 0, map_markers = 0;
    String from_address = "",
            to_address = "";
    LatLng Test_from = null, Test_to = null;
    String start_time, end_time, current_address;

    //For Fetching User Data
    private String MyPREFERENCES = "mypref", User_Token, User_Name, User_Type;
    SharedPreferences sharedpreferences;

    //For Search Destination
    AutocompleteSupportFragment start_autocomplete, end_autocomplete;
    LinearLayout search_btn, destination_btn, recent_layout;
    private RelativeLayout search_layout, search_screen;
    private ImageView search_back;
    private ListView recent_listview;

    //For Main Drawer
    private RelativeLayout menu_click, main_drawer;
    private ImageView close_drawer, user_profile;
    private TextView name_id, type_id, rating_id, travel_history, settings, road_assistance,
            apply_insurance, help_center, terms_condition;
    private LinearLayout logout_btn;

    //Recent Locations
    boolean re_routed = false, is_favorite = false, is_expired = false;
    ArrayList<RecentLocation> recentLocations = new ArrayList<>();
    ArrayList<RecentLocation> favouriteLocations = new ArrayList<>();

    RecentAdapter recentAdapter;
    RecentAdapter favouriteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_main);

        initiatingViews();
        initiatingDrawerViews();
        gettingUserDetails();
        FetchingRecentLocations();
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
    public void onMapReady(GoogleMap map) {
        this.map = map;
        this.map.getUiSettings().setZoomControlsEnabled(true);
//        this.map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        this.map.setOnMapClickListener(this);
//        this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(31.51073,
//                -96.4247), 3));
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

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
        initGooglePlacesApi();
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        map_markers++;
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        if (map_markers < 3) {
            if (map_markers == 1) {
                String adress = "";
                try {
                    Test_from = new LatLng(latLng.latitude, latLng.longitude);
                    List<Address> listaddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                    if (listaddress != null && listaddress.size() > 0) {
                        if (listaddress.get(0).getThoroughfare() != null) {

                            if (listaddress.get(0).getSubThoroughfare() != null) {
                                adress += listaddress.get(0).getSubThoroughfare() + "";

                            }
                            adress += listaddress.get(0).getThoroughfare();
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                from_address = adress;
                map.addMarker(new MarkerOptions().position(latLng).title(from_address));

            } else if (map_markers == 2) {
                String adress = "";
                try {
                    List<Address> listaddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    Test_to = new LatLng(latLng.latitude, latLng.longitude);

                    if (listaddress != null && listaddress.size() > 0) {
                        if (listaddress.get(0).getThoroughfare() != null) {

                            if (listaddress.get(0).getSubThoroughfare() != null) {
                                adress += listaddress.get(0).getSubThoroughfare() + "";

                            }
                            adress += listaddress.get(0).getThoroughfare();
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                to_address = adress;
                map.addMarker(new MarkerOptions().position(latLng).title(to_address));
            }
            if (Test_from != null && Test_to != null) {
                FetchingRoutesbyLatLng(Test_from, Test_to);
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            try {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                map.setMyLocationEnabled(true);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14F);
                map.animateCamera(cameraUpdate);

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(this, Locale.getDefault());


                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                current_address = addresses.get(0).getAddressLine(0);
                start_autocomplete.setHint(current_address);

                Test_from = latLng;
                from_address = current_address;
            } catch (IOException e) {
                e.printStackTrace();
            }

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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
        getDeviceLocation();
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        String Route_tag = polyline.getTag().toString();
        newRoute_tag = Integer.parseInt(Route_tag);
        if (Route_tag.equalsIgnoreCase("1")) {
            RoutesDialog(r1_details);
        }
        if (Route_tag.equalsIgnoreCase("2")) {
            RoutesDialog(r2_details);
        }
        if (Route_tag.equalsIgnoreCase("3")) {
            RoutesDialog(r3_details);
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (search_screen.getVisibility() == View.VISIBLE) {
            search_screen.setVisibility(View.GONE);
        } else {
            finish();
        }
    }

    private void initiatingViews() {
        My_location = findViewById(R.id.my_location);
        search_layout = findViewById(R.id.search_layout);
        search_btn = findViewById(R.id.search_btn);
        destination_btn = findViewById(R.id.destination_btn);
        recent_layout = findViewById(R.id.recent_layout);
        recent_listview = findViewById(R.id.recent_listview);
        destination_text = findViewById(R.id.destination_text);
        search_screen = findViewById(R.id.search_screen);
        search_back = findViewById(R.id.search_back);
        favourite_id = findViewById(R.id.favourite_id);
        r1_text = findViewById(R.id.r1_text);
        r2_text = findViewById(R.id.r2_text);
        r3_text = findViewById(R.id.r3_text);

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Loading = new ProgressDialog(this);
        Loading.setCancelable(false);

        My_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLocationUI();
                getDeviceLocation();
            }
        });
        search_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_screen.setVisibility(View.VISIBLE);
                menu_click.setVisibility(View.GONE);
            }
        });
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Test_to == null) {
                    search_screen.setVisibility(View.VISIBLE);
                    menu_click.setVisibility(View.GONE);
                } else if (Test_to != null) {
                    menu_click.setVisibility(View.VISIBLE);
                    FetchingRoutesbyLatLng(Test_from, Test_to);
                }
            }
        });
        search_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search_screen.getVisibility() == View.VISIBLE) {
                    search_screen.setVisibility(View.GONE);
                    menu_click.setVisibility(View.VISIBLE);
                }
            }
        });

        destination_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search_screen.getVisibility() == View.VISIBLE) {
                    search_screen.setVisibility(View.GONE);
                    menu_click.setVisibility(View.VISIBLE);
                }
                if (!to_address.equalsIgnoreCase("")) {
                    destination_text.setText(to_address);
                }
                favourite_id.setVisibility(View.VISIBLE);
                start_time = route_point_time();

                hideSoftKeyboard(MainActivity.this);

                if (Test_from.equals(Test_to)) {
                    Toast.makeText(MainActivity.this, "Current and destination location can't be same.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Test_to == null) {
                    Toast.makeText(MainActivity.this, "No Destination selected.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Test_from != null && Test_to != null) {
                    FetchingRoutesbyLatLng(Test_from, Test_to);
                }
//                FetchingRoutesbyAddress(from_address, to_address, Test_from, Test_to);
            }
        });
        favourite_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!is_favorite) {
                    is_favorite = true;
                    favourite_id.setImageResource(R.drawable.ic_favourite_icon);
                } else {
                    is_favorite = false;
                    favourite_id.setImageResource(R.drawable.ic_unfavourite_icon);
                }
            }
        });
        r1_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newRoute_tag = 1;
                RoutesDialog(r1_details);
            }
        });
        r2_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newRoute_tag = 2;
                RoutesDialog(r2_details);
            }
        });
        r3_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newRoute_tag = 3;
                RoutesDialog(r3_details);
            }
        });
    }

    private void initiatingDrawerViews() {
        menu_click = findViewById(R.id.menu_click);
        main_drawer = findViewById(R.id.main_drawer);
        close_drawer = findViewById(R.id.close_drawer);
        user_profile = findViewById(R.id.user_profile);
        name_id = findViewById(R.id.name_id);
        type_id = findViewById(R.id.type_id);
        rating_id = findViewById(R.id.rating_id);
        travel_history = findViewById(R.id.travel_history);
        settings = findViewById(R.id.settings);
        road_assistance = findViewById(R.id.road_assistance);
        apply_insurance = findViewById(R.id.apply_insurance);
        help_center = findViewById(R.id.help_center);
        terms_condition = findViewById(R.id.terms_condition);
        logout_btn = findViewById(R.id.logout_btn);

        menu_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main_drawer.getVisibility() == View.GONE) {
                    main_drawer.setVisibility(View.VISIBLE);
                }
            }
        });
        close_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (main_drawer.getVisibility() == View.VISIBLE) {
                    main_drawer.setVisibility(View.GONE);
                }
            }
        });

        travel_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Travel History", Toast.LENGTH_SHORT).show();
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Settings", Toast.LENGTH_SHORT).show();
            }
        });
        road_assistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Road Side Assistance", Toast.LENGTH_SHORT).show();
            }
        });
        apply_insurance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Apply For Insurance", Toast.LENGTH_SHORT).show();
            }
        });
        help_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Help Center", Toast.LENGTH_SHORT).show();
            }
        });
        terms_condition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Terms & Condition", Toast.LENGTH_SHORT).show();
            }
        });
        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedpreferences.edit().putBoolean("firsttime", true).apply();
                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                MainActivity.this.finish();
            }
        });
    }

    private void gettingUserDetails() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        User_Name = sharedpreferences.getString("user_name", null);
        User_Type = sharedpreferences.getString("user_type", null);
        User_Token = sharedpreferences.getString("user_token", null);

        name_id.setText(User_Name);
        type_id.setText(User_Type);
    }

    private void initGooglePlacesApi() {
        // Initialize Places.
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(getApplicationContext());

        // Initialize the AutocompleteSupportFragment.
        start_autocomplete = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.start_autocomplete);
        end_autocomplete = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.end_autocomplete);

        start_autocomplete.setHint(current_address);
        end_autocomplete.setHint(getString(R.string.search_for_location));

//        start_autocomplete.setCountry("US");
//        end_autocomplete.setCountry("US");


        // Specify the types of place data to return.
        start_autocomplete.setPlaceFields(Arrays.asList(Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG));
//        start_autocomplete.setTypeFilter(TypeFilter.ADDRESS);
        end_autocomplete.setPlaceFields(Arrays.asList(Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG));
//        end_autocomplete.setTypeFilter(TypeFilter.ADDRESS);


        // Set up a PlaceSelectionListener to handle the response.
        start_autocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                String location = place.getAddress();
                Test_from = place.getLatLng();
                from_address = location;
                start_autocomplete.setHint(location);

                Log.i(TAG, "Place: " + place.getAddressComponents().asList().get(0).getTypes().get(0) + ", " + place.getId() + ", " + place.getAddress());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        // Set up a PlaceSelectionListener to handle the response.
        end_autocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                String location = place.getAddress();
                to_address = location;
                Test_to = place.getLatLng();
                end_autocomplete.setHint(location);

                Log.i(TAG, "Place: " + place.getAddressComponents().asList().get(0).getTypes().get(0) + ", " + place.getId() + ", " + place.getAddress());
            }

            @Override
            public void onError(Status status) {
                end_autocomplete.setHint(getString(R.string.search_for_location));
                destination_text.setText(getString(R.string.enter_the_destination));
                Test_to = null;
                to_address = "";
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
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
                                geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                                try {
                                    addresses_list = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                    current_address = addresses_list.get(0).getAddressLine(0);
                                    start_autocomplete.setHint(current_address);
                                    Test_from = new LatLng(addresses_list.get(0).getLatitude(), addresses_list.get(0).getLongitude());
                                    from_address = current_address;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//                                map.addMarker(new MarkerOptions()
//                                        .position(new LatLng(lastKnownLocation.getLatitude(),
//                                                lastKnownLocation.getLongitude()))
//                                        .title("My Location"));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.animateCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void FetchingRoutesbyLatLng(LatLng test_from, LatLng test_to) {
        r1_latlng.clear();
        r2_latlng.clear();
        r3_latlng.clear();
        r1_details.clear();
        r2_details.clear();
        r3_details.clear();

        JSONObject jsonObject = new JSONObject();
        String stringFromLatLng = test_from.latitude + "," + test_from.longitude;
        String stringToLatLng = test_to.latitude + "," + test_to.longitude;
        try {
            jsonObject.put("from_point", stringFromLatLng);
            jsonObject.put("to_point", stringToLatLng);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        test_from = null;
        test_to = null;

        Loading.setMessage("Checking for safe routes.Please wait");
        showDialog();

        LatLng finalTest_from = test_from;
        LatLng finalTest_to = test_to;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL_ROUTES_LATLNG, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                String mongodb_id = response.getString("mongodb_id");
                                String response_sorted_id = response.getString("response_sorted_id");

                                JSONObject data = response.getJSONObject("data");
                                int no_of_routes = data.getInt("no_of_routes");

                                JSONObject r1 = null;
                                r1 = data.getJSONObject("r1");
                                int r1_length = r1.length();

                                if (r1_length != 0) {
                                    String r1_safe_route = r1.getString("safe_route");
                                    String r1_route_risk_score = r1.getString("route_risk_score");
                                    double r1_route_distance = r1.getDouble("route_distance");
                                    double r1_route_duration = r1.getDouble("route_duration");

                                    double r1_seconds = r1_route_duration;
                                    double r1_hours = 0;
                                    double r1_mints = 0;
                                    r1_hours = (r1_seconds % 86400) / 3600;
                                    r1_mints = ((r1_seconds % 86400) % 3600) / 60;

                                    long r1_fhour = Math.round(r1_hours);
                                    long r1_fmint = Math.round(r1_mints);

                                    String r1_distance = String.valueOf(r1_route_distance) + " miles";
                                    String r1_duration = r1_fhour + " hrs " + r1_fmint + " mints";

                                    RouteDetails r1Details = new RouteDetails();
                                    r1Details.setFrom_address(from_address);
                                    r1Details.setTo_address(to_address);
                                    r1Details.setFrom_latlng(finalTest_from);
                                    r1Details.setTo_latlng(finalTest_to);
                                    r1Details.setRoute_tag("Route # 1 Details");
                                    r1Details.setRoute_distance(r1_distance);
                                    r1Details.setRoute_duration(r1_duration);
                                    r1Details.setMongodb_id(mongodb_id);
                                    r1Details.setResponse_sorted_id(response_sorted_id);
                                    r1_details.add(r1Details);

                                    JSONArray r1_array = r1.getJSONArray("route");

                                    for (int i = 0; i < r1_array.length(); i++) {
                                        String mystring = r1_array.get(i).toString();
                                        JSONArray jsonArray = new JSONArray(mystring);
                                        String latitude = jsonArray.getString(0);
                                        String longitude = jsonArray.getString(1);
                                        double lat = Double.parseDouble(latitude);
                                        double lng = Double.parseDouble(longitude);

                                        r1_latlng.add(new LatLng(lat, lng));
                                    }
                                }

                                JSONObject r2 = null;
                                r2 = data.getJSONObject("r2");
                                int r2_length = r2.length();

                                if (r2_length != 0) {
                                    String r2_safe_route = r2.getString("safe_route");
                                    String r2_route_risk_score = r2.getString("route_risk_score");
                                    double r2_route_distance = r2.getDouble("route_distance");
                                    double r2_route_duration = r2.getDouble("route_duration");

                                    double r2_seconds = r2_route_duration;
                                    double r2_hours = 0;
                                    double r2_mints = 0;
                                    r2_hours = (r2_seconds % 86400) / 3600;
                                    r2_mints = ((r2_seconds % 86400) % 3600) / 60;

                                    long r2_fhour = Math.round(r2_hours);
                                    long r2_fmint = Math.round(r2_mints);

                                    String r2_distance = String.valueOf(r2_route_distance) + " miles";
                                    String r2_duration = r2_fhour + " hrs " + r2_fmint + " mints";

                                    RouteDetails r2Details = new RouteDetails();
                                    r2Details.setFrom_address(from_address);
                                    r2Details.setTo_address(to_address);
                                    r2Details.setFrom_latlng(finalTest_from);
                                    r2Details.setTo_latlng(finalTest_to);
                                    r2Details.setRoute_tag("Route # 2 Details");
                                    r2Details.setRoute_distance(r2_distance);
                                    r2Details.setRoute_duration(r2_duration);
                                    r2Details.setMongodb_id(mongodb_id);
                                    r2Details.setResponse_sorted_id(response_sorted_id);
                                    r2_details.add(r2Details);

                                    JSONArray r2_array = r2.getJSONArray("route");
                                    for (int i = 0; i < r2_array.length(); i++) {
                                        String mystring = r2_array.get(i).toString();
                                        JSONArray jsonArray = new JSONArray(mystring);
                                        String latitude = jsonArray.getString(0);
                                        String longitude = jsonArray.getString(1);
                                        double lat = Double.parseDouble(latitude);
                                        double lng = Double.parseDouble(longitude);

                                        r2_latlng.add(new LatLng(lat, lng));
                                    }
                                }

                                JSONObject r3 = null;
                                r3 = data.getJSONObject("r3");
                                int r3_length = r3.length();

                                if (r3_length != 0) {
                                    String r3_safe_route = r3.getString("safe_route");
                                    String r3_route_risk_score = r3.getString("route_risk_score");
                                    double r3_route_distance = r3.getDouble("route_distance");
                                    double r3_route_duration = r3.getDouble("route_duration");

                                    double r3_seconds = r3_route_duration;
                                    double r3_hours = 0;
                                    double r3_mints = 0;
                                    r3_hours = (r3_seconds % 86400) / 3600;
                                    r3_mints = ((r3_seconds % 86400) % 3600) / 60;

                                    long r3_fhour = Math.round(r3_hours);
                                    long r3_fmint = Math.round(r3_mints);

                                    String r3_distance = String.valueOf(r3_route_distance) + " miles";
                                    String r3_duration = r3_fhour + " hrs " + r3_fmint + " mints";

                                    RouteDetails r3Details = new RouteDetails();
                                    r3Details.setFrom_address(from_address);
                                    r3Details.setTo_address(to_address);
                                    r3Details.setFrom_latlng(finalTest_from);
                                    r3Details.setTo_latlng(finalTest_to);
                                    r3Details.setRoute_tag("Route # 3 Details");
                                    r3Details.setRoute_distance(r3_distance);
                                    r3Details.setRoute_duration(r3_duration);
                                    r3Details.setMongodb_id(mongodb_id);
                                    r3Details.setResponse_sorted_id(response_sorted_id);

                                    r3_details.add(r3Details);

                                    JSONArray r3_array = r3.getJSONArray("route");

                                    for (int i = 0; i < r3_array.length(); i++) {
                                        String mystring = r3_array.get(i).toString();
                                        JSONArray jsonArray = new JSONArray(mystring);
                                        String latitude = jsonArray.getString(0);
                                        String longitude = jsonArray.getString(1);
                                        double lat = Double.parseDouble(latitude);
                                        double lng = Double.parseDouble(longitude);

                                        r3_latlng.add(new LatLng(lat, lng));
                                    }
                                }
                                end_time = route_point_time();

                                long time = Calculatingtime(start_time, end_time);

                                Toast.makeText(MainActivity.this, "Time elapsed : " + time + " sec", Toast.LENGTH_SHORT).show();
                                setLocationPolyline();
                                hideDialog();

                            } else if (!success) {
                                menu_click.setVisibility(View.VISIBLE);
                                map.clear();
                                //No response from both APIs
                                Toast.makeText(MainActivity.this, "No routes found.", Toast.LENGTH_SHORT).show();
                                hideDialog();
                            }
                        } catch (Exception exception) {
                            Logger.addRecordToLog(MainActivity.this.getLocalClassName() + "Crash Error: " + exception);

                            map.clear();
                            exception.printStackTrace();
                            Toast.makeText(MainActivity.this, "Some error occurred! Try again later", Toast.LENGTH_LONG).show();
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
                        Logger.addRecordToLog(MainActivity.this.getLocalClassName() + "API Error: " + error);
                        Toast.makeText(MainActivity.this, "" + msg, Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + User_Token);

                return headers;
            }
        };
        int socketTimeout = 60000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonObjectRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsonObjectRequest);
        jsonObjectRequest.setShouldCache(false);

    }

    private void setLocationPolyline() {
        PolylineOptions options1 = new PolylineOptions().width(15).color(getResources().getColor(R.color.r1_color)).geodesic(true);
        PolylineOptions options2 = new PolylineOptions().width(10).color(getResources().getColor(R.color.r2_color)).geodesic(true);
        PolylineOptions options3 = new PolylineOptions().width(5).color(getResources().getColor(R.color.r3_color)).geodesic(true);

        Polyline polyline1 = null, polyline2 = null, polyline3 = null;

        map.clear();

        if (r1_latlng.size() != 0) {
            r1_text.setVisibility(View.VISIBLE);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(r1_latlng.get(0), DEFAULT_ZOOM));

            map.addMarker(new MarkerOptions()
                    .position(r1_latlng.get(0))
                    .title("Start Point"));

            map.addMarker(new MarkerOptions()
                    .position(r1_latlng.get(r1_latlng.size() - 1))
                    .title("End Point"));

            for (int i = 0; i < r1_latlng.size() - 1; i++) {
                options1.add(r1_latlng.get(i));
            }
        } else {
            r1_text.setVisibility(View.GONE);
        }
        if (r2_latlng.size() != 0) {
            r2_text.setVisibility(View.VISIBLE);

            for (int i = 0; i < r2_latlng.size() - 1; i++) {
                options2.add(r2_latlng.get(i));
            }
        } else {
            r2_text.setVisibility(View.GONE);
        }
        if (r3_latlng.size() != 0) {
            r3_text.setVisibility(View.VISIBLE);

            for (int i = 0; i < r3_latlng.size() - 1; i++) {
                options3.add(r3_latlng.get(i));
            }
        } else {
            r3_text.setVisibility(View.GONE);
        }

//        map.addMarker(new MarkerOptions()
//                .position(r1_latlng.get(0))
//                .title(from_address)
//                .icon(bitmapDescriptorFromVector(R.drawable.r1_start_icon)));

        polyline1 = map.addPolyline(options1);
        polyline2 = map.addPolyline(options2);
        polyline3 = map.addPolyline(options3);

        polyline1.setTag("1");
        polyline2.setTag("2");
        polyline3.setTag("3");
        polyline1.setClickable(true);
        polyline2.setClickable(true);
        polyline3.setClickable(true);

        map.setOnPolylineClickListener(this);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(MainActivity.this, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void RoutesDialog(ArrayList<RouteDetails> route_details) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.routes_popup, null);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
        routesDialog = dialogBuilder.create();
        routesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView route_name = (TextView) dialogView.findViewById(R.id.route_name);
        TextView from_location = (TextView) dialogView.findViewById(R.id.from_location);
        TextView to_location = (TextView) dialogView.findViewById(R.id.to_location);
        TextView mileage_text = (TextView) dialogView.findViewById(R.id.mileage_text);
        TextView time_estimate_text = (TextView) dialogView.findViewById(R.id.time_estimate_text);
        TextView go_to_map = (TextView) dialogView.findViewById(R.id.go_to_map);
        TextView start_drive = (TextView) dialogView.findViewById(R.id.start_drive);
        ImageView close_dialog = (ImageView) dialogView.findViewById(R.id.close_dialog);
        ImageView previous_route = (ImageView) dialogView.findViewById(R.id.previous_route);
        ImageView next_route = (ImageView) dialogView.findViewById(R.id.next_route);


        route_name.setText(route_details.get(0).getRoute_tag());
        from_location.setText(route_details.get(0).getFrom_address());
        to_location.setText(route_details.get(0).getTo_address());
        mileage_text.setText(route_details.get(0).getRoute_distance());
        time_estimate_text.setText(route_details.get(0).getRoute_duration());

        previous_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newRoute_tag == 3) {
                    newRoute_tag = 2;

                    route_name.setText(r2_details.get(0).getRoute_tag());
                    mileage_text.setText(r2_details.get(0).getRoute_distance());
                    time_estimate_text.setText(r2_details.get(0).getRoute_duration());

                } else if (newRoute_tag == 2) {
                    newRoute_tag = 1;

                    route_name.setText(r1_details.get(0).getRoute_tag());
                    mileage_text.setText(r1_details.get(0).getRoute_distance());
                    time_estimate_text.setText(r1_details.get(0).getRoute_duration());

                } else if (newRoute_tag == 1) {
                    if (r3_details.size() == 0) {
                        newRoute_tag = 2;

                        route_name.setText(r2_details.get(0).getRoute_tag());
                        mileage_text.setText(r2_details.get(0).getRoute_distance());
                        time_estimate_text.setText(r2_details.get(0).getRoute_duration());
                    } else {
                        newRoute_tag = 3;

                        route_name.setText(r3_details.get(0).getRoute_tag());
                        mileage_text.setText(r3_details.get(0).getRoute_distance());
                        time_estimate_text.setText(r3_details.get(0).getRoute_duration());
                    }
                }
            }
        });
        next_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newRoute_tag == 1) {
                    newRoute_tag = 2;

                    route_name.setText(r2_details.get(0).getRoute_tag());
                    mileage_text.setText(r2_details.get(0).getRoute_distance());
                    time_estimate_text.setText(r2_details.get(0).getRoute_duration());
                } else if (newRoute_tag == 2) {
                    if (r3_details.size() == 0) {
                        newRoute_tag = 1;

                        route_name.setText(r1_details.get(0).getRoute_tag());
                        mileage_text.setText(r1_details.get(0).getRoute_distance());
                        time_estimate_text.setText(r1_details.get(0).getRoute_duration());
                    } else {
                        newRoute_tag = 3;

                        route_name.setText(r3_details.get(0).getRoute_tag());
                        mileage_text.setText(r3_details.get(0).getRoute_distance());
                        time_estimate_text.setText(r3_details.get(0).getRoute_duration());
                    }
                } else if (newRoute_tag == 3) {
                    newRoute_tag = 1;

                    route_name.setText(r1_details.get(0).getRoute_tag());
                    mileage_text.setText(r1_details.get(0).getRoute_distance());
                    time_estimate_text.setText(r1_details.get(0).getRoute_duration());

                }
            }
        });
        go_to_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                routesDialog.dismiss();
            }
        });
        close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                routesDialog.dismiss();
            }
        });
        start_drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String start_date = "", currentTime = "", source_address = "", destination_address = "",
                        mongodb_id = "", mongodb_sorted_id = "", route_selected = "";

                Shared.route_latlng.clear();
                Shared.route_details.clear();

                Calendar calendar = Calendar.getInstance();
                int cyear = calendar.get(Calendar.YEAR);
                int cmonth = calendar.get(Calendar.MONTH) + 1;
                int cday = calendar.get(Calendar.DAY_OF_MONTH);

                start_date = cyear + "-" + cmonth + "-" + cday;
                currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                if (newRoute_tag == 1) {
                    Shared.route_latlng = r1_latlng;
                    Shared.route_details = r1_details;

                    source_address = r1_details.get(0).getFrom_address();
                    destination_address = r1_details.get(0).getTo_address();
                    mongodb_id = r1_details.get(0).getMongodb_id();
                    mongodb_sorted_id = r1_details.get(0).getResponse_sorted_id();
                    route_selected = "r1";

                    AddingRecentLocations(start_date, currentTime, source_address, destination_address, mongodb_id,
                            mongodb_sorted_id, route_selected, re_routed, is_favorite, is_expired);

                } else if (newRoute_tag == 2) {
                    Shared.route_latlng = r2_latlng;
                    Shared.route_details = r2_details;

                    source_address = r2_details.get(0).getFrom_address();
                    destination_address = r2_details.get(0).getTo_address();
                    mongodb_id = r2_details.get(0).getMongodb_id();
                    mongodb_sorted_id = r2_details.get(0).getResponse_sorted_id();
                    route_selected = "r2";

                    AddingRecentLocations(start_date, currentTime, source_address, destination_address, mongodb_id, mongodb_sorted_id, route_selected, re_routed, is_favorite, is_expired);
                } else if (newRoute_tag == 3) {
                    Shared.route_latlng = r3_latlng;
                    Shared.route_details = r3_details;

                    source_address = r3_details.get(0).getFrom_address();
                    destination_address = r3_details.get(0).getTo_address();
                    mongodb_id = r3_details.get(0).getMongodb_id();
                    mongodb_sorted_id = r3_details.get(0).getResponse_sorted_id();
                    route_selected = "r3";

                    AddingRecentLocations(start_date, currentTime, source_address, destination_address, mongodb_id, mongodb_sorted_id, route_selected, re_routed, is_favorite, is_expired);
                }
            }
        });

        routesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            routesDialog.show();
        } catch (WindowManager.BadTokenException e) {
            //use a log message
        }
    }

    private void AddingRecentLocations(String start_date, String currentTime, String source_address, String destination_address,
                                       String mongodb_id, String mongodb_sorted_id, String route_selected, boolean re_routed,
                                       boolean is_favorite, boolean is_expired) {
        JSONObject jsonObject = new JSONObject();
        String stringFromLatLng = Test_from.latitude + "," + Test_from.longitude;
        String stringToLatLng = Test_to.latitude + "," + Test_to.longitude;
        try {
            jsonObject.put("start_date", start_date);
            jsonObject.put("start_time", currentTime);
            jsonObject.put("source_address", source_address);
            jsonObject.put("destination_address", destination_address);
            jsonObject.put("source_longlat", stringFromLatLng);
            jsonObject.put("destination_longlat", stringToLatLng);
            jsonObject.put("task_status", 0);
            jsonObject.put("mongodb_id", mongodb_id);
            jsonObject.put("mongodb_sorted_id", mongodb_sorted_id);
            jsonObject.put("route_selected", route_selected);
            jsonObject.put("re_routed", re_routed);
            jsonObject.put("is_favorite", is_favorite);
            jsonObject.put("is_expired", is_expired);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Loading.setMessage("Adding Recent Location.Please wait");
        showDialog();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL_ADDING_RECENT_LOCATIONS, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                String Msg = response.getString("message");
                                hideDialog();

                                Intent intent = new Intent(MainActivity.this, DriveActivity.class);
                                startActivity(intent);

                                Toast.makeText(MainActivity.this, "" + Msg, Toast.LENGTH_SHORT).show();

                            } else {
                                hideDialog();

                                Intent intent = new Intent(MainActivity.this, DriveActivity.class);
                                startActivity(intent);
                            }
                        } catch (Exception exception) {
                            Logger.addRecordToLog(MainActivity.this.getLocalClassName() + "Crash Error: " + exception);
                            exception.printStackTrace();
                            Toast.makeText(MainActivity.this, "Some error occurred! Try again later", Toast.LENGTH_LONG).show();
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
                        Logger.addRecordToLog(MainActivity.this.getLocalClassName() + "API Error: " + error);
                        Toast.makeText(MainActivity.this, "" + msg, Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + User_Token);

                return headers;
            }
        };
        int socketTimeout = 60000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonObjectRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsonObjectRequest);
        jsonObjectRequest.setShouldCache(false);

    }

    private void FetchingRecentLocations() {
        JSONObject jsonObject = new JSONObject();

        Loading.setMessage("Fetching Recent Location.Please wait");
        showDialog();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_FETCHING_RECENT_LOCATIONS, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                String message = response.getString("message");
                                JSONArray data = response.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {

                                    String source_address = data.getJSONObject(i).getString("source_address");
                                    String destination_address = data.getJSONObject(i).getString("destination_address");
                                    String source_longlat = data.getJSONObject(i).getString("source_longlat");
                                    String destination_longlat = data.getJSONObject(i).getString("destination_longlat");
                                    String route_selected = data.getJSONObject(i).getString("route_selected");
                                    boolean is_favorite = data.getJSONObject(i).getBoolean("is_favorite");

                                    RecentLocation recentLocation = new RecentLocation();
                                    recentLocation.setSource_address(source_address);
                                    recentLocation.setDestination_address(destination_address);
                                    recentLocation.setSource_longlat(source_longlat);
                                    recentLocation.setDestination_longlat(destination_longlat);
                                    recentLocation.setRoute_selected(route_selected);
                                    recentLocation.setIs_favorite(is_favorite);

                                    recentLocations.add(recentLocation);
                                }
                                if (recentLocations.size() == 0) {
                                    recent_layout.setVisibility(View.GONE);
                                } else {
                                    recent_layout.setVisibility(View.VISIBLE);
                                    recentAdapter = new RecentAdapter(MainActivity.this, recentLocations);
                                    // setting list adapter
                                    recent_listview.setAdapter(recentAdapter);
                                }
                                hideDialog();
                            } else {
                                hideDialog();
                            }
                        } catch (Exception exception) {
                            Logger.addRecordToLog(MainActivity.this.getLocalClassName() + "Crash Error: " + exception);
                            exception.printStackTrace();
                            Toast.makeText(MainActivity.this, "Some error occurred! Try again later", Toast.LENGTH_LONG).show();
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
                        Logger.addRecordToLog(MainActivity.this.getLocalClassName() + "API Error: " + error);
                        Toast.makeText(MainActivity.this, "" + msg, Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + User_Token);

                return headers;
            }
        };
        int socketTimeout = 60000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonObjectRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsonObjectRequest);
        jsonObjectRequest.setShouldCache(false);

    }

    private static String route_point_time() {
        SimpleDateFormat s = new SimpleDateFormat("hhmmss");
        String format = s.format(new Date());
        return format;
    }

    private long Calculatingtime(String start_time, String end_time) {

        Date d1_start = null, d2_start = null;
        SimpleDateFormat format = new SimpleDateFormat("hhmmss");
        try {
            d1_start = format.parse(start_time);
            d2_start = format.parse(end_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long diff = 0;
        if (d2_start != null && d1_start != null) {
            diff = d2_start.getTime() - d1_start.getTime();
        }
        long diffSeconds = diff / 1000;
        long diffMinutes = diff / (60 * 1000);
        String diff_min = null;
        if (diffMinutes < 10) {
            diff_min = "0" + String.valueOf(diffMinutes);
        }
        long diffHours = diff / (60 * 60 * 1000);

        diffSeconds = diffSeconds % 60;

        return diffSeconds;
    }

    private void showDialog() {
        if (!Loading.isShowing())
            Loading.show();
    }

    private void hideDialog() {
        if (Loading.isShowing())
            Loading.dismiss();
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

    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(MainActivity.this);
        List<Address> address;
        LatLng latLng = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return latLng;
    }

    class RecentAdapter extends BaseAdapter {
        ArrayList<RecentLocation> arrayList;
        Context context;

        public RecentAdapter(Context context, ArrayList<RecentLocation> arrayList) {
            this.arrayList = arrayList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            convertView = null;  //in the get view and comments the else part of

            RecentLocation dietDetailsModel = arrayList.get(position);
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                convertView = layoutInflater.inflate(R.layout.list_item, null);

                ImageView favourite_img = (ImageView) convertView.findViewById(R.id.favourite_img);
                TextView source_name = (TextView) convertView.findViewById(R.id.source_name);
                TextView destination_name = (TextView) convertView.findViewById(R.id.destination_name);

                boolean favourite_check = dietDetailsModel.isIs_favorite();
                if (favourite_check) {
                    favourite_img.setImageResource(R.drawable.ic_favourite_icon);
                } else {
                    favourite_img.setImageResource(R.drawable.ic_unfavourite_icon);
                }
                source_name.setText(dietDetailsModel.getSource_address());
                destination_name.setText(dietDetailsModel.getDestination_address());

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String from_string = null, to_string = null;
                        String[] from_Val = null, to_Val = null;
                        from_address = dietDetailsModel.getSource_address();
                        to_address = dietDetailsModel.getDestination_address();
                        is_favorite = dietDetailsModel.isIs_favorite();

                        from_string = dietDetailsModel.getSource_longlat();
                        to_string = dietDetailsModel.getDestination_longlat();
                        if (from_string != null) {
                            from_Val = from_string.split(",");
                            double from_lat = Double.parseDouble(from_Val[0]);
                            double from_long = Double.parseDouble(from_Val[1]);

                            Test_from = new LatLng(from_lat, from_long);
                        } else if (!from_address.equalsIgnoreCase("")) {
                            Test_from = getLocationFromAddress(from_address);
                        }
                        if (to_string != null) {
                            to_Val = to_string.split(",");
                            double to_lat = Double.parseDouble(to_Val[0]);
                            double to_long = Double.parseDouble(to_Val[1]);

                            Test_to = new LatLng(to_lat, to_long);
                        } else if (!to_address.equalsIgnoreCase("")) {
                            Test_to = getLocationFromAddress(to_address);
                        }

                        if (search_screen.getVisibility() == View.VISIBLE) {
                            search_screen.setVisibility(View.GONE);
                            menu_click.setVisibility(View.VISIBLE);
                        }
                        if (!to_address.equalsIgnoreCase("")) {
                            destination_text.setText(to_address);
                        }
                        if (is_favorite) {
                            favourite_id.setImageResource(R.drawable.ic_favourite_icon);
                        } else {
                            favourite_id.setImageResource(R.drawable.ic_unfavourite_icon);
                        }
                        favourite_id.setVisibility(View.VISIBLE);
                        start_time = route_point_time();

                        hideSoftKeyboard(MainActivity.this);

                        if (Test_from.equals(Test_to)) {
                            Toast.makeText(MainActivity.this, "Current and destination location can't be same.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (Test_to == null) {
                            Toast.makeText(MainActivity.this, "No Destination selected.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (Test_from != null && Test_to != null) {
                            FetchingRoutesbyLatLng(Test_from, Test_to);
                        }
                    }
                });
            }
            return convertView;
        }
    }

}

