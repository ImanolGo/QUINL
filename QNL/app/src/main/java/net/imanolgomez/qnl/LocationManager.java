package net.imanolgomez.qnl;


import android.location.Location;

/**
 * Created by imanolgo on 12/12/14.
 */
public class LocationManager {

    private Location mCurrentLocation;
    private int mCurrentRegionId = -1;
    private int mCurrentRouteId = -1;
    private int mCurrentBeaconId = -1;

    private static LocationManager sLocationManager;

    private LocationManager() {}

    public static LocationManager get() {
        if (sLocationManager == null) {
            sLocationManager = new LocationManager();
        }
        return sLocationManager;
    }


    public void updateLocation(Location currentLocation) {
        this.mCurrentLocation = currentLocation;
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public int getCurrentRegionId() {
        return mCurrentRegionId;
    }

    public int getCurrentRouteId() {
        return mCurrentRouteId;
    }

    public int getCurrentBeaconId() {
        return mCurrentBeaconId;
    }
}
