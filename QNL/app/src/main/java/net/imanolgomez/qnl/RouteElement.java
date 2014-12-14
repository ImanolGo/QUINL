package net.imanolgomez.qnl;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;

/**
 * An abstract class, providing the fundamental methods and member variables for each element of a Route
 */

public class RouteElement extends BasicElement{

    // member variables
    private int mSampleId;
    private double mVolume;
    private boolean mLoop;

    /**
     * @param basicElement The BasicElement's attributes.
     */

    public RouteElement(
            BasicElement basicElement) {

        super(basicElement.getId(), basicElement.getName(), basicElement.getVersion());

        mSampleId = -1;
        mVolume = 1.0;
        mLoop = false;
    }

    public int getSampleId() {
        return mSampleId;
    }

    public double getVolume() {
        return mVolume;
    }

    public boolean isLoop() {
        return mLoop;
    }

    public void setSampleId(int sampleId) {
        this.mSampleId = sampleId;
    }

    public void setVolume(double volume) {
        this.mVolume = volume;
    }

    public void setLoop(boolean loop) {
        this.mLoop = loop;
    }
}
