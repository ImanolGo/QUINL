package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;
import java.util.HashMap;

/**
 * An class defining a Zone, which a certain kind of element and has it's own types.
 */

public class Zone extends RouteElement{

    public enum ZoneType {ZONE, ROOM, PATH};

    private final ZoneType mZoneType;
    private HashMap<Integer, Segment> mSegments;

    /**
     * @param id The RouteElement's request ID
     * @param name The RouteElement's human readable name.
     * @param version The RouteElement's revision version.
     * @param zoneType The Type in which the zone belong to.
     */
    public Zone(
            int id,
            String name,
            double version,
            ZoneType zoneType) {

        super(id,name,version);

        mSegments = new  HashMap<Integer, Segment>();
        mZoneType = zoneType;
    }

    public ZoneType getZoneType() {
        return mZoneType;
    }

    public void addSegment(Segment segment) {
        mSegments.put(segment.getId(), segment);
    }

    public boolean isInside(Location loc) {
        for (Segment segment : mSegments.values()) {
            if(segment.isInside(loc)){
                return true;
            }
        }
        return false;
    }
}
