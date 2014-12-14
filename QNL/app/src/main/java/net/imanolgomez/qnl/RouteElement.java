package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;

/**
 * An abstract class, providing the fundamental methods and member variables for each element of a Route
 */

public class RouteElement {

    // Instance variables
    private  int mId;
    private  String mName;
    private  double mVersion;

    /**
     * @param id The RouteElement's request ID
     * @param name The RouteElement's human readable name.
     * @param version The RouteElement's revision version.
     */
    public RouteElement(
            int id,
            String name,
            double version) {

        // Set the instance fields from the constructor
        this.mId = id;
        this.mName = name;
        this.mVersion = version;
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public double getVersion() {
        return mVersion;
    }

    public void setId(int id){
        this.mId = id;
    }

    public void setName(String name){
        this.mName = name;
    }

    public void setVersion(double version){
        this.mVersion = version;
    }

    /**
     * A single Segment object, defined by its coordinates.
     */
     class Segment {
        // Instance variables
        private final int mId;
        private final Location mLocationSW;
        private final Location mLocationNE;

        /**
         * @param segmentId The Segments's request ID
         * @param locationSW Location of the Segments's South-West coordinates.
         * @param locationNE Location of the Segments's North-East coordinates.
         */
        public Segment(
                int segmentId,
                Location locationSW,
                Location locationNE ) {

            // Set the instance fields from the constructor
            this.mId = segmentId;
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

}
