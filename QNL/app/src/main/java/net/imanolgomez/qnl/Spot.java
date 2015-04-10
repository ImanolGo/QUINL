package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;

import org.json.JSONObject;


/**
 * An class defining a path within a route
 */

public class Spot extends RouteElement {

    private double mRadius;
    private double mDeviation;
    private double mCurrentDeviation;
    private Location mLocation;
    private final String mUUID;

    private enum RangeType {IMMEDIATE, NEAR, FAR};
    RangeType mRangeType;

    protected static final double IMMEDIATE_LIMIT = 0.5;
    protected static final double NEAR_LIMIT = 5.0;
    protected static final double FAR_LIMIT = 10.0;

    protected static final String TAG_RADIUS = "radius";
    protected static final String TAG_LAT = "lat";
    protected static final String TAG_LON = "long";
    protected static final String TAG_UUID = "uuid";

    /**
     * @param basicElement The RouteElement's basic attributes.
     * @param uuid The unique identifier from the beacon
     */
    public Spot(
            BasicElement basicElement,
            String uuid) {

        super(basicElement);
        this.mUUID = uuid;
        this.initialize();
    }

    public void initialize(){
        mRadius = 5;
        mCurrentDeviation = 0;
        this.updateProximityValues();
    }

    public void updateProximityValues(){

        if(mRadius <= FAR_LIMIT){
            mRangeType = RangeType.FAR;
            this.mDeviation = 10;
        }

        if(mRadius <= NEAR_LIMIT){
            mRangeType = RangeType.NEAR;
            this.mDeviation = 2;
        }

        if(mRadius <= IMMEDIATE_LIMIT){
            mRangeType = RangeType.IMMEDIATE;
            this.mDeviation = 1;
        }
    }

    public void setDeviation(boolean addDeviation){
        if(addDeviation){
            mCurrentDeviation = mDeviation;
        }
        else{
            mCurrentDeviation = 0;
        }
    }

    public double getCurrentDeviation(){
        return mCurrentDeviation;
    }

    public void setRadius(double radius){
        updateProximityValues();
        this.mRadius = radius;
    }

    public void setLocation(Location location){
        this.mLocation = location;
    }

    public double getRadius(){
        return mRadius;
    }

    public Location getLocation(){
        return mLocation;
    }

    public String getUUID(){
        return mUUID;
    }

    public static Spot createSpotFromJson(String jsonStr) {

        try {
            JSONObject reader = new JSONObject(jsonStr);
            JSONObject spotJson = reader.getJSONObject("beacon");

            int id = spotJson.getInt(TAG_ID);
            int sampleId = spotJson.getInt(TAG_SAMPLE_ID);
            int routeId = spotJson.getInt(TAG_ROUTE_ID);
            float version = (float) spotJson.getDouble(TAG_VERSION);
            String name = spotJson.getString(TAG_NAME);
            float volume = (float) spotJson.getDouble(TAG_VOLUME);
            double radius = spotJson.getDouble(TAG_RADIUS);
            double lat = spotJson.getDouble(TAG_LAT);
            double lon = spotJson.getDouble(TAG_LON);
            String uuid = spotJson.getString(TAG_UUID);
            Location location = new Location("");
            location.setLatitude(lat);
            location.setLongitude(lon);
            int intBoolean = spotJson.getInt(TAG_LOOP);
            boolean loop = true;
            if (intBoolean == 0) {
                loop = false;
            }

            BasicElement basicElement = new BasicElement(id, name, version);
            Spot spot = new Spot(basicElement, uuid);
            spot.setLoop(loop);
            spot.setSampleId(sampleId);
            spot.setVolume(volume);
            spot.setRouteId(routeId);
            spot.setRadius(radius);
            spot.setLocation(location);
            return spot;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
