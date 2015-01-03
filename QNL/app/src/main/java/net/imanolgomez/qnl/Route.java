package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;
import android.util.Log;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;


/**
 * An class defining a path within a route
 */

public class Route extends BasicElement {

    public static final String TAG = "Route";

    private Date mStartTime;
    private Date mEndTime;

    private HashMap<Integer, Region> mRegions;
    private HashMap<Integer, Region> mZones;
    private HashMap<Integer, Region> mPaths;
    private HashMap<Integer, Region> mRooms;
    private HashMap<Integer, Spot>   mSpots;

    private Region mCurrentRegion;

    /**
     * @param basicElement The BasicElement's attributes.
     */

    public Route(
            BasicElement basicElement) {

        super(basicElement.getId(), basicElement.getName(), basicElement.getVersion());
        initialize();
    }

    private void initialize(){
        initializeAttributes();
    }

    private void initializeAttributes(){
        mRegions =  new HashMap<Integer, Region>();
        mZones =  new HashMap<Integer, Region>();
        mPaths =  new HashMap<Integer, Region>();
        mRooms =  new HashMap<Integer, Region>();
        mSpots =  new HashMap<Integer, Spot>();
        mCurrentRegion = null;
    }

    public void addRegion(Region region) {
        if(region==null){
            return;
        }

        mRegions.put(region.getId(), region);
        addRegionHierarchically(region);
    }

    public void addSpot(Spot spot) {
        if(spot==null){
            return;
        }

        mSpots.put(spot.getId(), spot);
    }

    public boolean isInside(Location loc) {
        mCurrentRegion = null;

        for (Region region : mRooms.values()) {
            if(region.isInside(loc)){
                mCurrentRegion = region;
                return true;
            }
        }

        for (Region region : mZones.values()) {
            if(region.isInside(loc)){
                mCurrentRegion = region;
                return true;
            }
        }

        for (Region region : mPaths.values()) {
            if(region.isInside(loc)){
                mCurrentRegion = region;
                return true;
            }
        }

        return false;
    }

    public static Route createRouteFromJson(String jsonStr){

        try {
            JSONObject reader = new JSONObject(jsonStr);
            JSONObject zoneJson  = reader.getJSONObject("route");

            int id = zoneJson.getInt(TAG_ID);
            double version = zoneJson.getDouble(TAG_VERSION);
            String name = zoneJson.getString(TAG_NAME);

            BasicElement basicElement = new BasicElement(id,name,version);
            Route route = new Route(basicElement);
            return route;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addRegionHierarchically(Region region) {
        if(region==null){
            return;
        }

        switch (region.getRegionType()){

            case PATH: {
                mPaths.put(region.getId(), region);
                break;
            }

            case ROOM: {
                mRooms.put(region.getId(), region);
                break;
            }

            case ZONE: {
                mZones.put(region.getId(), region);
                break;
            }

            default: {
                mPaths.put(region.getId(), region);
            }


        }
    }

    public Region getCurrentRegion() {
        return mCurrentRegion;
    }
}
