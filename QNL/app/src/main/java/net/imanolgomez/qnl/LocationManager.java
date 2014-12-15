package net.imanolgomez.qnl;


import android.content.Context;
import android.location.Location;
import android.util.Log;

/**
 * Created by imanolgo on 12/12/14.
 */
public class LocationManager {

    public static String TAG = "LocationManager";

    private Location mCurrentLocation;
    Region mCurrentRegion;
    private int mCurrentBeaconId = -1;

    Context mAppContext;
    RouteManager mRouteManager;
    SoundManager mSoundManager;

    private static LocationManager sLocationManager;

    private LocationManager(Context appContext) {
        mAppContext = appContext;
        mRouteManager =  RouteManager.get(mAppContext);
        mSoundManager = SoundManager.get(mAppContext);
    }

    public static LocationManager get(Context c) {
        if (sLocationManager == null) {
            sLocationManager = new LocationManager(c.getApplicationContext());
        }
        return sLocationManager;
    }


    public void updateLocation(Location currentLocation) {
        this.mCurrentLocation = currentLocation;
        Log.i(TAG, "updateLocation");
        updateRegion();

    }

    public void updateRegion() {

        mRouteManager.updateLocation(mCurrentLocation);

        if(regionHasChanged()){
            updateRegionId();
            updateSample();
        }

    }


    private boolean regionHasChanged(){
        return (mRouteManager.getCurrentRegion()!=mCurrentRegion);
    }

    private void updateRegionId(){
        mCurrentRegion = mRouteManager.getCurrentRegion();
    }

    private void updateSample(){
        if(mCurrentRegion==null){
            mSoundManager.playSample(-1);
            return;
        }

        mSoundManager.playSample(mCurrentRegion.getSampleId(), mCurrentRegion.isLooping());
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public Region getCurrentRegion() {
        return mCurrentRegion;
    }


    public int getCurrentBeaconId() {
        return mCurrentBeaconId;
    }
}
