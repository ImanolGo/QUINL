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

public class Route extends RouteElement{

    private Date mStartTime;
    private Date mEndTime;

    private HashMap<Integer, Zone> mZones;

    /**
     * @param id The RouteElement's request ID
     * @param name The RouteElement's human readable name.
     * @param version The RouteElement's revision version.
     */
    public Route(
            int id,
            String name,
            double version) {

        super(id,name,version);
        mZones =  new HashMap<Integer, Zone>();
    }

    public void addZone(Zone zone) {
        mZones.put(zone.getId(), zone);
    }

    public boolean isInside(Location loc) {
        for (Zone zone : mZones.values()) {
            if(zone.isInside(loc)){
                return true;
            }
        }
        return false;
    }
}
