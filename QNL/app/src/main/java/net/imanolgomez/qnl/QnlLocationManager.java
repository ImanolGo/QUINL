package net.imanolgomez.qnl;


import android.content.Context;
import android.location.Location;
import android.util.Log;

/**
 * Created by imanolgo on 12/12/14.
 */
public class QnlLocationManager {

    public static String TAG = "LocationManager";

    private Location mCurrentLocation = new Location("FirstLocation");

    private int mCurrentBeaconId = -1;

    private Context mAppContext;
    private Region mCurrentRegion;
    private Spot mCurrentSpot;

    private static QnlLocationManager sQnlLocationManager;

    private QnlLocationManager(Context appContext) {
        mAppContext = appContext;
        this.initialize();
    }

    public static QnlLocationManager get(Context c) {
        if (sQnlLocationManager == null) {
            sQnlLocationManager = new QnlLocationManager(c.getApplicationContext());
        }
        return sQnlLocationManager;
    }

    private void initialize(){
        Log.i(TAG, "initialize");
        //this.initializeLocationManager();
    }

    public void updateLocation(Location currentLocation) {
        this.mCurrentLocation = currentLocation;
        Log.i(TAG, "updateLocation");
        updateRoute();

    }

    public void stop(){
        mCurrentRegion = null;
        mCurrentSpot = null;
    }

    public void updateRoute() {

        if(!isRouteUpdatable()){
            return;
        }

        RouteManager routeManager = RouteManager.get(mAppContext);
        routeManager.updateLocation(mCurrentLocation);

        if(regionHasChanged()){
            Log.i(TAG, "REGION HAS CHANGED");
            updateSpot();
            updateRegion();
            updateSample();
        }
    }

    private boolean regionHasChanged(){

        RouteManager routeManager = RouteManager.get(mAppContext);

        if(routeManager.getCurrentSpot()!=mCurrentSpot){
            return true;
        }


        if(routeManager.getCurrentRegion()!=mCurrentRegion){
            return true;
        }

        return false;
    }

    private boolean isRouteUpdatable(){

        SoundManager soundManager = SoundManager.get(mAppContext);

        if(soundManager.getCurrentSample()==null){
            return true;
        }

        if(soundManager.isSampleLooping()){
            return true;
        }

        return false;
    }

    private void updateRegion(){
        mCurrentRegion = RouteManager.get(mAppContext).getCurrentRegion();
    }
    private void updateSpot(){

        Spot spot = RouteManager.get(mAppContext).getCurrentSpot();

        if(spot == null && mCurrentSpot!=null){
            mCurrentSpot.setDeviation(false);
        }

        if(spot != null && mCurrentSpot!=spot){
            spot.setDeviation(true);
        }

        mCurrentSpot = spot;
    }

    private void updateSample(){

        SoundManager soundManager = SoundManager.get(mAppContext);
        if(mCurrentSpot!=null){
            soundManager.playSample(mCurrentSpot.getSampleId(), mCurrentSpot.getVolume(), mCurrentSpot.isLooping());
            return;
        }

        if(mCurrentRegion!=null){
            soundManager.playSample(mCurrentRegion.getSampleId(), mCurrentRegion.getVolume(), mCurrentRegion.isLooping());
            return;
        }

        soundManager.playSample(-1, 0.0f);
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public Region getCurrentRegion() {
        return mCurrentRegion;
    }

    public Route getCurrentRoute() {
        return RouteManager.get(mAppContext).getCurrentRoute();
    }

    public Spot getCurrentSpot() {
        return mCurrentSpot;
    }

}
