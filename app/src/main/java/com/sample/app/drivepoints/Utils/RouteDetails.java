package com.sample.app.drivepoints.Utils;

public class RouteDetails {
    String from_address, to_address;
    double route_distance;

    public RouteDetails() {
    }

    public RouteDetails(String from_address, String to_address, int route_distance) {
        this.from_address = from_address;
        this.to_address = to_address;
        this.route_distance = route_distance;
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

    public double getRoute_distance() {
        return route_distance;
    }

    public void setRoute_distance(double route_distance) {
        this.route_distance = route_distance;
    }
}
