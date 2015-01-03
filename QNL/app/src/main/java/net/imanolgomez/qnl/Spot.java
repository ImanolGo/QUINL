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

    private final double mRadius;
    private final Location mLocation;

    protected static final String TAG_RADIUS = "radius";
    protected static final String TAG_LAT = "lat";
    protected static final String TAG_LON = "long";

    /**
     * @param basicElement The RouteElement's basic attributes.
     * @param radius The Radius an which the spot is active.
     * @param location The location of the spot.
     */
    public Spot(
            BasicElement basicElement,
            double radius,
            Location location) {

        super(basicElement);

        this.mRadius = radius;
        this.mLocation = location;
    }

    public double getRadius(){
        return mRadius;
    }

    public Location getLocation(){
        return mLocation;
    }

    public static Spot createSpotFromJson(String jsonStr) {

        try {
            JSONObject reader = new JSONObject(jsonStr);
            JSONObject spotJson = reader.getJSONObject("beacon");


            int id = spotJson.getInt(TAG_ID);
            int sampleId = spotJson.getInt(TAG_SAMPLE_ID);
            int routeId = spotJson.getInt(TAG_ROUTE_ID);
            double version = spotJson.getDouble(TAG_VERSION);
            String name = spotJson.getString(TAG_NAME);
            double volume = spotJson.getDouble(TAG_VOLUME);
            double radius = spotJson.getDouble(TAG_RADIUS);
            double lat = spotJson.getDouble(TAG_LAT);
            double lon = spotJson.getDouble(TAG_LON);
            Location location = new Location("");
            location.setLatitude(lat);
            location.setLongitude(lon);
            int intBoolean = spotJson.getInt(TAG_LOOP);
            boolean loop = true;
            if (intBoolean == 0) {
                loop = false;
            }

            BasicElement basicElement = new BasicElement(id, name, version);
            Spot spot = new Spot(basicElement, radius, location);
            spot.setLoop(loop);
            spot.setSampleId(sampleId);
            spot.setVolume(volume);
            spot.setRouteId(routeId);
            return spot;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
