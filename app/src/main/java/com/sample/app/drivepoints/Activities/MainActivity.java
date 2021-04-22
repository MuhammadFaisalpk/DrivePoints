package com.sample.app.drivepoints.Activities;

import android.app.ProgressDialog;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.sample.app.drivepoints.R;
import com.sample.app.drivepoints.Utils.RouteDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleMap map;
    private CameraPosition cameraPosition;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
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

    String ROUTES_URL = "http://34.121.66.173:8000/api/v1/m/get/route/";
    private ProgressDialog Loading;
    SupportMapFragment mapFragment;
    AutocompleteSupportFragment start_autocomplete, end_autocomplete;
    private LinearLayout search_layout;
    private TextView destination_text;
    private RelativeLayout My_location, menu_click, search_screen;
    private ImageView search_back;
    JSONObject jsonObject;
    List<Double> r1_lat = new ArrayList<Double>();
    List<Double> r1_lng = new ArrayList<Double>();
    List<Double> r2_lat = new ArrayList<Double>();
    List<Double> r2_lng = new ArrayList<Double>();
    List<Double> r3_lat = new ArrayList<Double>();
    List<Double> r3_lng = new ArrayList<Double>();
    ArrayList<RouteDetails> r1_details = new ArrayList<>();
    ArrayList<RouteDetails> r2_details = new ArrayList<>();
    ArrayList<RouteDetails> r3_details = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_main);

        initiatingViews();
//        getLocationPermission();
        addParameters();
        FetchingRoutes();
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
//        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();
    }

    private void initiatingViews() {
        jsonObject = new JSONObject();

        My_location = findViewById(R.id.my_location);
        search_layout = findViewById(R.id.search_layout);
        destination_text = findViewById(R.id.destination_text);
        menu_click = findViewById(R.id.menu_click);
        search_screen = findViewById(R.id.search_screen);
        search_back = findViewById(R.id.search_back);

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
            }
        });
        search_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search_screen.getVisibility() == View.VISIBLE) {
                    search_screen.setVisibility(View.GONE);
                }
            }
        });
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
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        String Route_tag = polyline.getTag().toString();
        if (Route_tag.equalsIgnoreCase("1")) {
            RoutesDialog(1, r1_details);
        }
        if (Route_tag.equalsIgnoreCase("2")) {
            RoutesDialog(2, r2_details);
        }
        if (Route_tag.equalsIgnoreCase("3")) {
            RoutesDialog(3, r3_details);
        }
    }

    public void addParameters() {

        try {
            jsonObject.put("source_address", "Starbucks Alameda Street, Los Angeles, CA, USA");
            jsonObject.put("destination_address", "ArcLight Cinemas - Hollywood Sunset Boulevard, Central LA, Los Angeles, CA, USA");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void FetchingRoutes() {
        Loading.setMessage("Checking for safe routes.Please wait");
        showDialog();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, ROUTES_URL, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");

                            JSONObject data = response.getJSONObject("data");
                            String from_address = data.getString("from_address");
                            String to_address = data.getString("to_address");
                            int no_of_routes = data.getInt("no_of_routes");
                            double average_route_risk_score = data.getDouble("average_route_risk_score");

                            JSONObject r1 = data.getJSONObject("r1");
                            String r1_safe_route = r1.getString("safe_route");
                            String r1_route_risk_score = r1.getString("route_risk_score");
                            double r1_route_distance = r1.getDouble("route_distance");

                            JSONObject r2 = data.getJSONObject("r2");
                            String r2_safe_route = r2.getString("safe_route");
                            String r2_route_risk_score = r2.getString("route_risk_score");
                            double r2_route_distance = r2.getDouble("route_distance");

                            JSONObject r3 = data.getJSONObject("r3");
                            String r3_safe_route = r3.getString("safe_route");
                            String r3_route_risk_score = r3.getString("route_risk_score");
                            double r3_route_distance = r3.getDouble("route_distance");

                            RouteDetails r1Details = new RouteDetails();
                            r1Details.setFrom_address(from_address);
                            r1Details.setTo_address(to_address);
                            r1Details.setRoute_distance(r1_route_distance);
                            RouteDetails r2Details = new RouteDetails();
                            r2Details.setFrom_address(from_address);
                            r2Details.setTo_address(to_address);
                            r2Details.setRoute_distance(r2_route_distance);
                            RouteDetails r3Details = new RouteDetails();
                            r3Details.setFrom_address(from_address);
                            r3Details.setTo_address(to_address);
                            r3Details.setRoute_distance(r3_route_distance);
                            r1_details.add(r1Details);
                            r2_details.add(r2Details);
                            r3_details.add(r3Details);

                            JSONArray r1_array = r1.getJSONArray("route");
                            JSONArray r2_array = r2.getJSONArray("route");
                            JSONArray r3_array = r3.getJSONArray("route");

                            for (int i = 0; i < r1_array.length(); i++) {
                                String mystring = r1_array.get(i).toString();
                                JSONArray jsonArray = new JSONArray(mystring);
                                String latitude = jsonArray.getString(0);
                                String longitude = jsonArray.getString(1);
                                double lat = Double.parseDouble(latitude);
                                double lng = Double.parseDouble(longitude);

                                r1_lat.add(lat);
                                r1_lng.add(lng);
                            }
                            for (int i = 0; i < r2_array.length(); i++) {
                                String mystring = r2_array.get(i).toString();
                                JSONArray jsonArray = new JSONArray(mystring);
                                String latitude = jsonArray.getString(0);
                                String longitude = jsonArray.getString(1);
                                double lat = Double.parseDouble(latitude);
                                double lng = Double.parseDouble(longitude);

                                r2_lat.add(lat);
                                r2_lng.add(lng);
                            }
                            for (int i = 0; i < r3_array.length(); i++) {
                                String mystring = r3_array.get(i).toString();
                                JSONArray jsonArray = new JSONArray(mystring);
                                String latitude = jsonArray.getString(0);
                                String longitude = jsonArray.getString(1);
                                double lat = Double.parseDouble(latitude);
                                double lng = Double.parseDouble(longitude);

                                r3_lat.add(lat);
                                r3_lng.add(lng);
                            }
                            setLocationPolyline();
                            hideDialog();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideDialog();
                        Toast.makeText(MainActivity.this, "Some error occurred! Try again later", Toast.LENGTH_LONG).show();

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMGJkMzQzYTktMGFmMC00NTJjLWE3NGYtZjcyOWM2Y2VlMDAwIiwidXNlcm5hbWUiOiJjZW9AdHdpdHRlci5jb20iLCJleHAiOjE2MTk4NTk0NzAsImVtYWlsIjoiY2VvQHR3aXR0ZXIuY29tIn0.ysPbOUuEXuh701TTbT_oxMamvBHiZRF39lmE5_wvtlI");

                return headers;
            }
        };
        int socketTimeout = 30000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonObjectRequest.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(jsonObjectRequest);
        jsonObjectRequest.setShouldCache(false);

    }


    private void setLocationPolyline() {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(r1_lat.get(0),
                r1_lng.get(0)), DEFAULT_ZOOM));
        map.addMarker(new MarkerOptions()
                .position(new LatLng(r1_lat.get(0),
                        r1_lng.get(0)))
                .title("Start Point")
                .icon(bitmapDescriptorFromVector(R.drawable.r1_start_icon)));
        map.addMarker(new MarkerOptions()
                .position(new LatLng(r1_lat.get(r1_lat.size() - 1),
                        r1_lng.get(r1_lng.size() - 1)))
                .title("End Point")
                .icon(bitmapDescriptorFromVector(R.drawable.r1_start_icon)));
        PolylineOptions options1 = new PolylineOptions().width(10).color(getResources().getColor(R.color.r1_color)).geodesic(true);
        PolylineOptions options2 = new PolylineOptions().width(10).color(getResources().getColor(R.color.r2_color)).geodesic(true);
        PolylineOptions options3 = new PolylineOptions().width(10).color(getResources().getColor(R.color.r3_color)).geodesic(true);

        Polyline polyline1 = null, polyline2 = null, polyline3 = null;

        for (int i = 0; i < r1_lat.size() - 1; i++) {
            options1.add(new LatLng(r1_lat.get(i), r1_lng.get(i)));
        }
        for (int i = 0; i < r2_lat.size() - 1; i++) {
            options2.add(new LatLng(r2_lat.get(i), r2_lng.get(i)));
        }
        for (int i = 0; i < r3_lat.size() - 1; i++) {
            options3.add(new LatLng(r3_lat.get(i), r3_lng.get(i)));
        }

        polyline1 = map.addPolyline(options1);
        polyline2 = map.addPolyline(options2);
        polyline3 = map.addPolyline(options3);

        polyline1.setTag("1");
        polyline2.setTag("2");
        polyline3.setTag("3");
        polyline1.setClickable(true);
        polyline2.setClickable(true);
        polyline3.setClickable(true);
        // [END maps_poly_activity_add_polyline]
        // [START_EXCLUDE silent]
        // Store a data object with the polyline, used here to indicate an arbitrary type.

//
//        stylePolyline(polyline1);
//        stylePolyline(polyline2);
//        stylePolyline(polyline3);

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

        start_autocomplete.setHint(getString(R.string.search_for_location));
        end_autocomplete.setHint(getString(R.string.search_for_location));
//        autocompleteFragment.setLocationRestriction(RectangularBounds.newInstance(
//                new LatLng(34.7006096, 19.2477876),
//                new LatLng(41.7488862, 29.7296986))); //Greece bounds
        start_autocomplete.setCountry("am");
        end_autocomplete.setCountry("am");


        // Specify the types of place data to return.
        start_autocomplete.setPlaceFields(Arrays.asList(Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS));
        start_autocomplete.setTypeFilter(TypeFilter.ADDRESS);
        end_autocomplete.setPlaceFields(Arrays.asList(Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS));
        end_autocomplete.setTypeFilter(TypeFilter.ADDRESS);


        // Set up a PlaceSelectionListener to handle the response.
        start_autocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (place.getAddressComponents().asList().get(0).getTypes().get(0).equalsIgnoreCase("route")) {
                    String location = place.getAddress();

                } else { //If user does not choose a specific place.
                    Toast.makeText(MainActivity.this, "choose an address", Toast.LENGTH_SHORT).show();

                }

                Log.i(TAG, "Place: " + place.getAddressComponents().asList().get(0).getTypes().get(0) + ", " + place.getId() + ", " + place.getAddress());
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MainActivity.this, "An error occurred: " + status, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "An error occurred: " + status);
            }
        });    // Set up a PlaceSelectionListener to handle the response.
        end_autocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (place.getAddressComponents().asList().get(0).getTypes().get(0).equalsIgnoreCase("route")) {
                    String location = place.getAddress();

                } else { //If user does not choose a specific place.
                    Toast.makeText(MainActivity.this, "choose an address", Toast.LENGTH_SHORT).show();

                }

                Log.i(TAG, "Place: " + place.getAddressComponents().asList().get(0).getTypes().get(0) + ", " + place.getId() + ", " + place.getAddress());
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MainActivity.this, "An error occurred: " + status, Toast.LENGTH_SHORT).show();
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
                map.setMyLocationEnabled(false);
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
                                    String current_address = addresses_list.get(0).getAddressLine(0);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

//                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                        new LatLng(lastKnownLocation.getLatitude(),
//                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//                                map.addMarker(new MarkerOptions()
//                                        .position(new LatLng(lastKnownLocation.getLatitude(),
//                                                lastKnownLocation.getLongitude()))
//                                        .title("My Location"));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
//                            map.moveCamera(CameraUpdateFactory
//                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void stylePolyline(Polyline polyline) {
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "A":
                // Use a custom bitmap as the cap at the start of the line.
                polyline.setStartCap(new RoundCap());
                polyline.setEndCap(new RoundCap());
                polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
                polyline.setColor(getResources().getColor(R.color.black));
                polyline.setJointType(JointType.ROUND);

//                polyline.setStartCap(
//                        new CustomCap(
//                                BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10));
                break;
            case "B":
                // Use a round cap at the start of the line.
                polyline.setStartCap(new RoundCap());
                polyline.setStartCap(new RoundCap());
                polyline.setEndCap(new RoundCap());
                polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
                polyline.setColor(getResources().getColor(R.color.colorAccent));
                polyline.setJointType(JointType.ROUND);
                break;
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

    private void RoutesDialog(int route_tag, ArrayList<RouteDetails> route_details) {
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
        TextView go_to_map = (TextView) dialogView.findViewById(R.id.go_to_map);
        ImageView close_dialog = (ImageView) dialogView.findViewById(R.id.close_dialog);
        ImageView previous_route = (ImageView) dialogView.findViewById(R.id.previous_route);
        ImageView next_route = (ImageView) dialogView.findViewById(R.id.next_route);

        route_name.setText("Route # " + route_tag + " Details");
        from_location.setText(route_details.get(0).getFrom_address());
        to_location.setText(route_details.get(0).getTo_address());
        mileage_text.setText(String.valueOf(route_details.get(0).getRoute_distance()) + " miles");

        previous_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (route_tag == 3) {
                    RoutesDialog(2, r2_details);
                } else if (route_tag == 2) {
                    RoutesDialog(1, r1_details);
                }
            }
        });
        next_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (route_tag == 1) {
                    RoutesDialog(2, r2_details);
                } else if (route_tag == 2) {
                    RoutesDialog(3, r3_details);
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

        routesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            routesDialog.show();
        } catch (WindowManager.BadTokenException e) {
            //use a log message
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
