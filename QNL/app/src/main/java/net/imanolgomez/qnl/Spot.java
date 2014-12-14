package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;


/**
 * An class defining a path within a route
 */

public class Spot extends RouteElement {

    private final double mRadius;
    private final Location mLocation;

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


}
