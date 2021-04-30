package com.sample.app.drivepoints.Model;

import java.io.Serializable;

public class RecentLocation implements Serializable {

    String source_address, destination_address, source_longlat, destination_longlat, route_selected;
    boolean is_favorite;

    public RecentLocation() {
    }

    public RecentLocation(String source_address, String destination_address, String source_longlat,
                          String destination_longlat, String route_selected, boolean is_favorite) {
        this.source_address = source_address;
        this.destination_address = destination_address;
        this.source_longlat = source_longlat;
        this.destination_longlat = destination_longlat;
        this.route_selected = route_selected;
        this.is_favorite = is_favorite;
    }

    public String getSource_address() {
        return source_address;
    }

    public void setSource_address(String source_address) {
        this.source_address = source_address;
    }

    public String getDestination_address() {
        return destination_address;
    }

    public void setDestination_address(String destination_address) {
        this.destination_address = destination_address;
    }

    public String getSource_longlat() {
        return source_longlat;
    }

    public void setSource_longlat(String source_longlat) {
        this.source_longlat = source_longlat;
    }

    public String getDestination_longlat() {
        return destination_longlat;
    }

    public void setDestination_longlat(String destination_longlat) {
        this.destination_longlat = destination_longlat;
    }

    public String getRoute_selected() {
        return route_selected;
    }

    public void setRoute_selected(String route_selected) {
        this.route_selected = route_selected;
    }

    public boolean isIs_favorite() {
        return is_favorite;
    }

    public void setIs_favorite(boolean is_favorite) {
        this.is_favorite = is_favorite;
    }
}
