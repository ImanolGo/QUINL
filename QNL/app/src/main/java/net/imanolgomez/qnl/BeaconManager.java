package net.imanolgomez.qnl;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;


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
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BeaconFoundCallback mCallback;

    private Context mAppContext;

    private HashMap<Integer, Beacon> mRoutes;

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
    }

    public void startScanningForBeacons(BeaconFoundCallback callback) {
        //Check to see if the device supports Bluetooth and that it's turned on
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mCallback = callback;
        }
    }

    public void stopScanningForBeacons() {
        //Check to see if the device supports Bluetooth and that it's turned on
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    public void foundBeacon(Beacon nearestBeacon){
        Log.i(TAG, "Beacon-> id: " + nearestBeacon.getId() + ", accuracy: " + nearestBeacon.getAccuracy() );
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

                BasicElement basicElement = new BasicElement(minor, "", 0);
                Beacon beacon = new Beacon(basicElement);
                beacon.setRssi(rssi);
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
