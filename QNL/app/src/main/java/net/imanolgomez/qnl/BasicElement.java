package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;

/**
 * An abstract class, providing the fundamental methods and member variables for each element of a Route
 */

public class BasicElement {

    // Instance variables
    private  int mId;
    private  String mName;
    private  double mVersion;

    /**
     * @param id The RouteElement's request ID
     * @param name The RouteElement's human readable name.
     * @param version The RouteElement's revision version.
     */
    public BasicElement(
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

    public void setVersion(double version){
        this.mVersion = version;
    }

}
