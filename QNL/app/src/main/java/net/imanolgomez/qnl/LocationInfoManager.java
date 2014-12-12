package net.imanolgomez.qnl;


import android.location.Location;

/**
 * Created by imanolgo on 12/12/14.
 */
public class LocationInfoManager {

    private Location mCurrentLocation;
    private int mRegionId;

    private static LocationInfoManager sLocationInfoManager;

    private LocationInfoManager() {
        this.initialize();
    }

    public static LocationInfoManager get() {
        if (sLocationInfoManager == null) {
            sLocationInfoManager = new LocationInfoManager();
        }
        return sLocationInfoManager;
    }

    private void initialize(){
        mRegionId = -1;
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public int getRegionId() {
        return mRegionId;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.mCurrentLocation = currentLocation;
    }

    public void setRegionId(int regionId) {
        this.mRegionId = regionId;
    }
}
