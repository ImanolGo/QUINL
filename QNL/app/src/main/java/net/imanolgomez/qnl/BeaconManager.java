package net.imanolgomez.qnl;

import android.content.Context;
import android.util.Log;

/**
 * Representation of an iBeacon
 */
class iBeacon {
    String uuid;
    int major;
    int minor;
    int rssi;
    int txPower;
    double accuracy;

    public iBeacon (String uuid, int major, int minor, int rssi) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.txPower = -65;
        this.accuracy = calculateAccuracy();
    }

    public void updateRSSI (int rssi) {
        this.rssi = rssi;
        this.accuracy = calculateAccuracy();
    }

    public boolean equals (iBeacon beacon) {
        if (beacon.major == this.major && beacon.minor == this.minor) {
            return true;
        }

        return false;
    }

    private double calculateAccuracy() {
        if (this.rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = this.rssi*1.0/this.txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }
}

interface BeaconFoundCallback {
    void onNearestBeaconChanged(iBeacon nearestBeacon);
}

/**
 * Created by imanolgo on 30/12/14.
 */
public class BeaconManager {

    public static String TAG = "BeaconManager";
    private static BeaconManager sBeaconManager;

    private Context mAppContext;


    private BeaconManager(Context appContext) {
        mAppContext = appContext;
        this.initialize();

    }
    public static BeaconManager get(Context c) {
        if (sBeaconManager == null) {
            sBeaconManager = new BeaconManager(c.getApplicationContext());
        }
        return sBeaconManager;
    }

    private void initialize(){
        Log.i(TAG, "Initialize");
    }
}
