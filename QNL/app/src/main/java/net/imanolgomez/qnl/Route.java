package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;

import java.util.Date;
import java.util.HashMap;


/**
 * An class defining a path within a route
 */

public class Route extends BasicElement {

    private Date mStartTime;
    private Date mEndTime;

    private HashMap<Integer, Region> mRegions;

    /**
     * @param basicElement The BasicElement's attributes.
     */

    public Route(
            BasicElement basicElement) {

        super(basicElement.getId(), basicElement.getName(), basicElement.getVersion());
        mRegions =  new HashMap<Integer, Region>();
    }

    public void addRegion(Region region) {
        if(region==null){
            return;
        }

        mRegions.put(region.getId(), region);
    }

    public boolean isInside(Location loc) {
        for (Region region : mRegions.values()) {
            if(region.isInside(loc)){
                return true;
            }
        }
        return false;
    }
}
