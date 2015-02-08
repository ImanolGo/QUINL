package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;

import org.json.JSONObject;

/**
 * A single Section object, defined by its coordinates.
 */
class Section {
    // Instance variables
    private final int mId;
    private final int mRegionId;
    private final Location mLocation1;
    private final Location mLocation2;

    /**
     * @param sectionId The Section's request ID
     * @param location1 Location of the Section's South-West coordinates.
     * @param location2 Location of the Section's North-East coordinates.
     * @param regionId The region's Id to whom it belongs.
     */
    public Section(
            int sectionId,
            Location location1,
            Location location2,
            int regionId) {

        // Set the instance fields from the constructor
        this.mId = sectionId;
        this.mLocation1 = location1;
        this.mLocation2 = location2;
        this.mRegionId = regionId;
    }

    // Instance field getters
    public int getId() {
        return mId;
    }

    public int getRegionId() {
        return mRegionId;
    }

    public Location getLocation1() {
        return mLocation1;
    }

    public Location getLocation2() {
        return mLocation2;
    }

    public boolean isInside(Location loc) {
        return (loc.getLatitude()>= mLocation1.getLatitude()&&loc.getLatitude()<=mLocation2.getLatitude() &&
                loc.getLongitude()>= mLocation1.getLongitude()&&loc.getLongitude()<=mLocation2.getLongitude());
    }
}

/**
 * An abstract class, providing the fundamental methods and member variables for each element of a Route
 */

public class RouteElement extends BasicElement{

    // member variables
    private int mSampleId;
    private int mRouteId;
    private double mVolume;
    private boolean mLoop;

    protected static final String TAG_LOOP = "loop";
    protected static final String TAG_SAMPLE_ID = "play";
    protected static final String TAG_VOLUME = "volume";
    protected static final String TAG_TYPE = "type";
    protected static final String TAG_ROUTE_ID = "route";
    protected static final String TAG_LAT_1 = "long_1";
    protected static final String TAG_LAT_2 = "long_2";
    protected static final String TAG_LON_1 = "lat_1";
    protected static final String TAG_LON_2 = "lat_2";

    /**
     * @param basicElement The BasicElement's attributes.
     */

    public RouteElement(
            BasicElement basicElement) {

        super(basicElement.getId(), basicElement.getName(), basicElement.getVersion());

        mSampleId = -1;
        mVolume = 1.0;
        mLoop = false;
    }

    public int getRouteId() {
        return mRouteId;
    }

    public int getSampleId() {
        return mSampleId;
    }

    public double getVolume() {
        return mVolume;
    }

    public boolean isLooping() {
        return mLoop;
    }

    public int getLoopInt() {
        if(mLoop){
            return 1;
        }
        else{
            return 0;
        }
    }

    public void setSampleId(int sampleId) {
        this.mSampleId = sampleId;
    }

    public void setVolume(double volume) {
        this.mVolume = volume;
    }

    public void setLoop(boolean loop) {
        this.mLoop = loop;
    }

    public void setRouteId(int routeId) {
        this.mRouteId = routeId;
    }

    protected Section createSectionFromJsonObject(JSONObject c){

        try {
            int id = c.getInt(TAG_ID);
            double lat1 = c.getDouble(TAG_LAT_1);
            double lat2 = c.getDouble(TAG_LAT_2);
            double lon1 = c.getDouble(TAG_LON_1);
            double lon2 = c.getDouble(TAG_LON_2);

            Location location1 = new Location("");
            location1.setLatitude(lat1);
            location1.setLongitude(lon1);

            Location location2 = new Location("");
            location2.setLatitude(lat2);
            location2.setLongitude(lon2);

            Section section = new Section(id,location1,location2, getId());
            return section;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
