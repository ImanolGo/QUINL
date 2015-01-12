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
    private int mCurrentBeaconId = -1;

    private Context mAppContext;
    private Region mCurrentRegion;
    private Spot mCurrentSpot;
    private RouteManager mRouteManager;
    private SoundManager mSoundManager;

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
        updateRoute();

    }

    public void updateRoute() {

        mRouteManager.updateLocation(mCurrentLocation);

        if(regionHasChanged()){
            Log.i(TAG, "REGION HAS CHANGED");
            updateSpot();
            updateRegion();
            updateSample();
        }
    }

    private boolean regionHasChanged(){

        if(mRouteManager.getCurrentSpot()!=mCurrentSpot){
            return true;
        }

        if(mRouteManager.getCurrentRegion()!=mCurrentRegion){
            return true;
        }

        return false;
    }

    private void updateRegion(){
        mCurrentRegion = mRouteManager.getCurrentRegion();
    }
    private void updateSpot(){
        mCurrentSpot = mRouteManager.getCurrentSpot();
    }

    private void updateSample(){

        if(mCurrentSpot!=null){
            mSoundManager.playSample(mCurrentSpot.getSampleId(), mCurrentSpot.isLooping());
            return;
        }

        if(mCurrentRegion!=null){
            mSoundManager.playSample(mCurrentRegion.getSampleId(), mCurrentRegion.isLooping());
            return;
        }

        mSoundManager.playSample(-1);
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public Region getCurrentRegion() {
        return mCurrentRegion;
    }

    public Route getCurrentRoute() {
        return mRouteManager.getCurrentRoute();
    }

    public Spot getCurrentSpot() {
        return mCurrentSpot;
    }

}
