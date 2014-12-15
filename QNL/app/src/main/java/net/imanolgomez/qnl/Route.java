package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;
import android.util.Log;

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
        mCurrentRegion = null;
    }

    public void addRegion(Region region) {
        if(region==null){
            return;
        }

        mRegions.put(region.getId(), region);
        addRegionhHierarchically(region);
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

    private void addRegionhHierarchically(Region region) {
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
