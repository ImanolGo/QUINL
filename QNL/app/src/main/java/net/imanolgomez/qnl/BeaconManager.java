package net.imanolgomez.qnl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Representation of an Beacon
 */
class Beacon {

    public static int TIME_TO_LIVE_MS = 30000;

    String uuid;
    private int major;
    private int minor;
    private int rssi;
    private int txPower;
    private double accuracy;
    private int timeToLive;

    public Beacon(String uuid, int major, int minor, int rssi) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.rssi = rssi;
        this.txPower = -69;
        this.accuracy = calculateAccuracy();
        this.timeToLive = 0;
    }


    public void setRssi (int rssi) {
        this.rssi = rssi;
        this.accuracy = calculateAccuracy();
    }

    public void update(long elapsedTimeInMs){
        this.timeToLive -= (int) elapsedTimeInMs;
        if(this.timeToLive<0){
            this.timeToLive = 0;
        }

        //Log.i("Beacon", this.minor + " , time to live: " + this.timeToLive);
    }

    public boolean isAlive(){return timeToLive == 0;}

    public void setToSleep(){
        setTimeToLive(TIME_TO_LIVE_MS);
    }

    public void setTimeToLive(int timeToLiveInMs){
        this.timeToLive = timeToLiveInMs;
    }

    public int getTimeToLive(){
        return this.timeToLive;
    }

    public double getAccuracy(){
        return this.accuracy;
    }

    public int getMinor(){
        return this.minor;
    }

    public int getRssi(){
        return this.rssi;
    }

    public boolean equals (Beacon beacon) {
        return (beacon.major == this.major && beacon.minor == this.minor);
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
        long currentTime = System.nanoTime();

        Iterator<Integer> it = mBeacons.keySet().iterator();
        while (it.hasNext()) {
            Integer key = it.next();
            Beacon beacon = mBeacons.get(key);
            beacon.update((currentTime-mLastUpdateTime)/NANOSECONDS_PER_MILISECONDS);
            if (beacon.getTimeToLive()<=0) {
                it.remove();
            }
        }

        mLastUpdateTime = currentTime;

    }

    public void updateNearestBeacon(){
        mNearestBeacon = null;
        for (Beacon beacon : mBeacons.values()) {

            if(beacon.isAlive()){
                if(mNearestBeacon==null){
                    mNearestBeacon = beacon; //Get the first beacon as nearest
                    //Log.i(TAG,"Nearest Beacon: " + mNearestBeacon.getMinor() + ", " + mNearestBeacon.getAccuracy() + "m");
                }
                else if(beacon.getAccuracy()<=mNearestBeacon.getAccuracy()){
                    mNearestBeacon = beacon;
                    //Log.i(TAG,"Nearest Beacon: " + mNearestBeacon.getMinor() + ", " + mNearestBeacon.getAccuracy() + "m");
                }
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

    public void stop() {
        stopScanningForBeacons();
        mNearestBeacon = null;
        mBeacons.clear();
    }


    public void foundBeacon(Beacon nearestBeacon){
        //Log.i(TAG, "Beacon-> id: " + nearestBeacon.minor + ", accuracy: " + nearestBeacon.accuracy);
        Log.i(TAG,"FoundBeacon Beacon: " + nearestBeacon.getMinor() +
                ", " + nearestBeacon.getAccuracy() + "m");

        if(!mBeacons.containsKey(nearestBeacon.getMinor()))
        {
            mBeacons.put(nearestBeacon.getMinor(), nearestBeacon);
            updateNearestBeacon();
        }

    }


    public String getBeaconsListString(){

        if(mBeacons.isEmpty()){
            return "";
        }

        String beaconsList = "";
        Route currentRoute = RouteManager.get(mAppContext).getCurrentRoute();



        for (Beacon beacon : mBeacons.values()) {

            int spotId = beacon.getMinor();

            if(currentRoute!=null){
                Spot spot = currentRoute.getSpot(Integer.toString(beacon.getMinor()));

                if(spot!=null){
                    spotId = spot.getId();
                }
            }

            beaconsList = beaconsList + spotId + "," + beacon.getRssi() + ";";
        }

        return beaconsList;
    }

    public void setBeaconToSleep(int minor){
        Log.i(TAG,"setBeaconToSleep " +minor);

        for (Beacon beacon : mBeacons.values()) {

            if(beacon.getMinor()==minor) {
                Log.i(TAG,"Beacon to sleep: " + beacon.getMinor());
                beacon.setToSleep();
                updateNearestBeacon();
                return;
            }
        }
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
