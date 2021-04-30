package com.sample.app.drivepoints.Utils;

import com.google.android.gms.maps.model.LatLng;
import com.sample.app.drivepoints.Model.RouteDetails;

import java.util.ArrayList;

public class Shared {

    public static final String MESSENGER_INTENT_KEY = "msg-intent-key";
    public static ArrayList<LatLng> route_latlng = new ArrayList<LatLng>();
    public static ArrayList<RouteDetails> route_details = new ArrayList<>();

}
