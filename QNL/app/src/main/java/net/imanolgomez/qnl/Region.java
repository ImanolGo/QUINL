package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;
import java.util.HashMap;

/**
 * An class defining a Zone, which a certain kind of element and has it's own types.
 */

public class Region extends RouteElement {

    public enum RegionType {ZONE, ROOM, PATH};

    private final RegionType mRegionType;
    private HashMap<Integer, Section> mSegments;

    /**
     * @param basicElement The RouteElement's basic attributes.
     * @param regionType The Type to which the Region belong to.
     */
    public Region(
            BasicElement basicElement,
            RegionType regionType) {

        super(basicElement);

        mSegments = new  HashMap<Integer, Section>();
        mRegionType = regionType;
    }

    public RegionType getRegionType() {
        return mRegionType;
    }

    public void addSegment(Section section) {
        mSegments.put(section.getId(), section);
    }

    public boolean isInside(Location loc) {
        for (Section segment : mSegments.values()) {
            if(segment.isInside(loc)){
                return true;
            }
        }
        return false;
    }

}
