package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;


/**
 * An class defining a path within a route
 */

public class Spot extends RouteElement{

    private final double mRadius;
    private final Location mLocation;

    /**
     * @param id The RouteElement's request ID
     * @param name The RouteElement's human readable name.
     * @param version The RouteElement's revision version.
     * @param radius The Radius an which the spot is active.
     * @param location The location of the spot.
     */
    public Spot(
            int id,
            String name,
            double version,
            double radius,
            Location location) {

        super(id,name,version);

        this.mRadius = radius;
        this.mLocation = location;

    }


}
