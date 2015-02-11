package net.imanolgomez.qnl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Representation of an Beacon
 */
class Beacon {
    String uuid;
    private int major;
    private int minor;
    private int rssi;
    private int txPower;
    private double accuracy;

    public Beacon(String uuid, int major, int minor, int rssi) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.txPower = -59;
        this.accuracy = calculateAccuracy();
    }


    public void setRssi (int rssi) {
        this.rssi = rssi;
        this.accuracy = calculateAccuracy();
    }

    public double getAccuracy(){
        return this.accuracy;
    }

    public int getMinor(){
        return this.minor;
    }

    public boolean equals (Beacon beacon) {
        if (beacon.major == this.major && beacon.minor == this.minor) {
            return true;
        }

        return false;
    }

    public void setAccuracy(int _accuracy){
        this.accuracy = _accuracy;
    }

    public boolean isNear(double distance){
        return (distance <= this.accuracy);
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
    void onNearestBeaconChanged(Beacon nearestBeacon);
}

/**
 * Created by imanolgo on 30/12/14.
 */

public class BeaconManager {

    public static String TAG = "BeaconManager";
    private static BeaconManager sBeaconManager;

    final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    final static String GELO_UUID = "11E44F094EC4407E9203CF57A50FBCE0";
    public static final int NANOSECONDS_PER_MILISECONDS = 1000000;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BeaconFoundCallback mCallback;

    private Beacon mNearestBeacon;
    private long mLastUpdateTime;

    private Context mAppContext;

    private HashMap<Integer, Beacon> mBeacons;

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

        mBluetoothManager = (BluetoothManager) mAppContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager != null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }

        mLastUpdateTime = System.nanoTime();
        mBeacons = new HashMap<Integer, Beacon>();
    }

    public void startScanningForBeacons(BeaconFoundCallback callback) {
        //Check to see if the device supports Bluetooth and that it's turned on
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mCallback = callback;
        }
    }

    public void update() {
        updateNearestBeacon();
        mBeacons.clear();
    }

    public void updateNearestBeacon(){
        mNearestBeacon = null;
        for (Beacon beacon : mBeacons.values()) {
            if(mNearestBeacon==null){
                mNearestBeacon = beacon; //Get the first beacon as nearest
            }
            if(beacon.getAccuracy()<=mNearestBeacon.getAccuracy()){
                mNearestBeacon = beacon;
                Log.i(TAG,"Found Beacon: " + mNearestBeacon.getMinor() +
                       ", " + mNearestBeacon.getAccuracy() + "m");
            }
        }
    }

    public Beacon getNearestBeacon(){
        return mNearestBeacon;
    }

    public void stopScanningForBeacons() {
        //Check to see if the device supports Bluetooth and that it's turned on
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }


    public void foundBeacon(Beacon nearestBeacon){
        //Log.i(TAG, "Beacon-> id: " + nearestBeacon.minor + ", accuracy: " + nearestBeacon.accuracy);
        //Log.i(TAG,"FoundBeacon Beacon: " + nearestBeacon.getMinor());
        mBeacons.put(nearestBeacon.getMinor(), nearestBeacon);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //For readability we convert the bytes of the UUID into hex
            String UUIDHex = convertBytesToHex(Arrays.copyOfRange(scanRecord, 9, 25));

            if (UUIDHex.equals(GELO_UUID)) {
                //Bytes 25 and 26 of the advertisement packet represent the major value
                int major = (scanRecord[25] << 8)
                        | (scanRecord[26] << 0);

                //Bytes 27 and 28 of the advertisement packet represent the minor value
                int minor = ((scanRecord[27] & 0xFF) << 8)
                        | (scanRecord[28] & 0xFF);

                Beacon beacon = new Beacon(UUIDHex, major, minor, rssi);
                mCallback.onNearestBeaconChanged(beacon);
            }
        }
    };

    private static String convertBytesToHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for ( int i = 0; i < bytes.length; i++ ) {
            int v = bytes[i] & 0xFF;
            hex[i * 2] = HEX_ARRAY[v >>> 4];
            hex[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hex);
    }
}
