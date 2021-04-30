package com.sample.app.drivepoints.Model;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class RouteDetails implements Serializable {
    String from_address, to_address, route_tag, route_duration, route_distance, mongodb_id, response_sorted_id;
    LatLng from_latlng, to_latlng;

    public RouteDetails() {
    }

    public RouteDetails(String from_address, String to_address, String route_tag, String route_duration, String route_distance,
                        String mongodb_id, String response_sorted_id, LatLng from_latlng, LatLng to_latlng) {
        this.from_address = from_address;
        this.to_address = to_address;
        this.route_tag = route_tag;
        this.route_duration = route_duration;
        this.route_distance = route_distance;
        this.mongodb_id = mongodb_id;
        this.response_sorted_id = response_sorted_id;
        this.from_latlng = from_latlng;
        this.to_latlng = to_latlng;
    }

    public String getFrom_address() {
        return from_address;
    }

    public void setFrom_address(String from_address) {
        this.from_address = from_address;
    }

    public String getTo_address() {
        return to_address;
    }

    public void setTo_address(String to_address) {
        this.to_address = to_address;
    }

    public String getRoute_tag() {
        return route_tag;
    }

    public void setRoute_tag(String route_tag) {
        this.route_tag = route_tag;
    }

    public String getRoute_duration() {
        return route_duration;
    }

    public void setRoute_duration(String route_duration) {
        this.route_duration = route_duration;
    }

    public String getRoute_distance() {
        return route_distance;
    }

    public void setRoute_distance(String route_distance) {
        this.route_distance = route_distance;
    }

    public String getMongodb_id() {
        return mongodb_id;
    }

    public void setMongodb_id(String mongodb_id) {
        this.mongodb_id = mongodb_id;
    }

    public String getResponse_sorted_id() {
        return response_sorted_id;
    }

    public void setResponse_sorted_id(String response_sorted_id) {
        this.response_sorted_id = response_sorted_id;
    }

    public LatLng getFrom_latlng() {
        return from_latlng;
    }

    public void setFrom_latlng(LatLng from_latlng) {
        this.from_latlng = from_latlng;
    }

    public LatLng getTo_latlng() {
        return to_latlng;
    }

    public void setTo_latlng(LatLng to_latlng) {
        this.to_latlng = to_latlng;
    }
}
