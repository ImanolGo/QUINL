package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * An abstract class, providing the fundamental methods and member variables for each element of a Route
 */

public class RouteElement extends BasicElement{

    // member variables
    private int mSampleId;
    private int mRouteId;
    private double mVolume;
    private boolean mLoop;

    protected static final String TAG_NAME = "name";
    protected static final String TAG_VERSION = "version";
    protected static final String TAG_ID = "id";
    protected static final String TAG_LOOP = "loop";
    protected static final String TAG_SAMPLE_ID = "play";
    protected static final String TAG_VOLUME = "volume";
    protected static final String TAG_TYPE = "type";
    protected static final String TAG_ROUTE_ID = "route";
    protected static final String TAG_LAT_1 = "lat_1";
    protected static final String TAG_LAT_2 = "lat_2";
    protected static final String TAG_LON_1 = "long_1";
    protected static final String TAG_LON_2 = "long_2";

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

    public boolean isLoop() {
        return mLoop;
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

    /**
     * A single Section object, defined by its coordinates.
     */
    class Section {
        // Instance variables
        private final int mId;
        private final Location mLocationSW;
        private final Location mLocationNE;

        /**
         * @param sectionId The Section's request ID
         * @param locationSW Location of the Section's South-West coordinates.
         * @param locationNE Location of the Section's North-East coordinates.
         */
        public Section(
                int sectionId,
                Location locationSW,
                Location locationNE ) {

            // Set the instance fields from the constructor
            this.mId = sectionId;
            this.mLocationSW = locationSW;
            this.mLocationNE = locationNE;
        }

        // Instance field getters
        public int getId() {
            return mId;
        }

        public Location getLocationSW() {
            return mLocationSW;
        }

        public Location getLocationNE() {
            return mLocationNE;
        }

        public boolean isInside(Location loc) {
            return (loc.getLatitude()>= mLocationSW.getLatitude()&&loc.getLatitude()<=mLocationNE.getLatitude() &&
                    loc.getLongitude()>=mLocationSW.getLongitude()&&loc.getLongitude()<=mLocationNE.getLongitude());
        }
    }

    protected Section createSectionFromJsonObject(JSONObject c){

        try {
            int id = c.getInt(TAG_ID);
            double lat1 = c.getDouble(TAG_LAT_1);
            double lat2 = c.getDouble(TAG_LAT_2);
            double lon1 = c.getDouble(TAG_LON_1);
            double lon2 = c.getDouble(TAG_LON_2);

            Location locationSW = new Location("");
            locationSW.setLatitude(lat1);
            locationSW.setLongitude(lon1);

            Location locationNE = new Location("");
            locationSW.setLatitude(lat2);
            locationSW.setLongitude(lon2);

            Section section = new Section(id,locationSW,locationNE);
            return section;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
