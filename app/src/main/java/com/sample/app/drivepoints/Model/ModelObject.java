package com.sample.app.drivepoints.Model;

import com.sample.app.drivepoints.R;

public enum ModelObject {

    SAFE_ROUTE(R.layout.safe_route_layout),
    TRACK_VEHICLE(R.layout.track_vehicle_layout),
    VEHICLE_INSURED(R.layout.vehicle_insured_layout),
    GET_REGISTERED(R.layout.get_registered_layout);

    private int mLayoutResId;

    ModelObject(int layoutResId) {
        mLayoutResId = layoutResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

}
