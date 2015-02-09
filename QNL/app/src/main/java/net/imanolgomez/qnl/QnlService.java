package net.imanolgomez.qnl;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by imanolgo on 07/02/15.
 */
public class QnlService extends Service implements LocationListener {

    // QNL Manager own members
    public static String TAG = "QnlService";
    private Context mAppContext;

    // Location updates members

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 3; // 3 second
    // flag for GPS status
    boolean mIsGPSEnabled = false;
    // flag for network status
    boolean mIsNetworkEnabled = false;
    // flag for GPS status
    boolean mCanGetLocation = false;
    // Declaring a Location Manager
    protected LocationManager mLocationManager;
    // Current Location
    protected Location mCurrentLocation;

    //Handled Managers
    private DeviceInfoManager mDeviceInfoManager;
    private QnlLocationManager mQnlLocationManager;
    private SoundManager mSoundManager;
    private DBManager mDBManager;
    private RouteManager mRouteManager;
    private BeaconManager mBeaconManager;

    // BeaconManager utilities
    long BLUETOOTH_SCAN_PERIOD = 1000;
    long BLUETOOTH_SCAN_INTERVAL = 2000;
    BeaconFoundCallback mBeaconCallback;
    private Handler mBluetoothHandler;
    private Handler mBluetoothTaskHandler;
    private Timer mBluetoothTimer;

    // Service Communications
    public static final String ON_UPDATE_LOCATION = "onUpdateLocation";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mAppContext = getApplicationContext();
        this.initialize();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void initialize(){

        Log.i(TAG, "Initialize");
        this.initializeLocationUpdates();
        this.initializeManagers();
        this.initializeBluetooth();
        this.registerDevice();
    }

    protected void initializeManagers(){
        Log.i(TAG, "Initialize Managers");
        mDeviceInfoManager = DeviceInfoManager.get(mAppContext);
        mDBManager = DBManager.get(mAppContext);
        mSoundManager = SoundManager.get(mAppContext);
        mRouteManager =  RouteManager.get(mAppContext);
        mBeaconManager = BeaconManager.get(mAppContext);
        mQnlLocationManager = QnlLocationManager.get(mAppContext);
    }

    protected void initializeBluetooth(){

        mBluetoothHandler = new Handler(Looper.getMainLooper());

        mBeaconCallback = new BeaconFoundCallback()
        {
            @Override
            public void onNearestBeaconChanged(final Beacon nearestBeacon) {
                mBluetoothHandler.post(new Runnable() {
                    public void run() {
                        mBeaconManager.foundBeacon(nearestBeacon);
                        //scanBeacons(false);
                    }
                });
            }
        };


        mBluetoothTaskHandler = new Handler(); // run on another Thread to avoid crash
        if(mBluetoothTimer != null) {
            mBluetoothTimer.cancel();
        } else {
            // recreate new
            mBluetoothTimer = new Timer();
        }
        // schedule task
        mBluetoothTimer.scheduleAtFixedRate(new UpdateBeaconsTask(), 0, BLUETOOTH_SCAN_INTERVAL);

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        Log.i(TAG, "lat-> " +  location.getLatitude() + ", lon-> " +  location.getLongitude() );

        updateManagers(location);
        communicateOnUpdate();
        sendTrackingData();

    }

    private void updateManagers(Location location){
        //scanBeacons(true);
        mBeaconManager.update();
        mQnlLocationManager.updateLocation(location);
        //mBeaconManager.update();
    }

    private void sendTrackingData(){
        new SendTrackingData().execute();
    }

    private class SendTrackingData extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String result = new ServerCommunicator(mAppContext).sendTrackingData();
                //Log.i(TAG, "Fetched contents from tracking data: " + result);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to send tracking data ", ioe);
            }
            return null;
        }
    }

    private void registerDevice(){ new RegisteringDevice().execute(); }

    private class RegisteringDevice extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String result = new ServerCommunicator(mAppContext).registerDevice();
                Log.i(TAG, "Device registered!!");
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to send register phone ", ioe);
            }
            return null;
        }
    }

    private void scanBeacons(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mBluetoothHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBeaconManager.stopScanningForBeacons();
                }
            }, BLUETOOTH_SCAN_PERIOD);
            mBeaconManager.startScanningForBeacons(mBeaconCallback);
        } else {
            mBeaconManager.stopScanningForBeacons();
        }
    }

    private void communicateOnUpdate() {
        Log.i(TAG, "communicateOnUpdate");
        Intent intent = new Intent(ON_UPDATE_LOCATION);
        Log.i(TAG, "sendBroadcast");
        sendBroadcast(intent);
    }

    class UpdateBeaconsTask extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mBluetoothHandler.post(new Runnable() {

                @Override
                public void run() {
                    //Log.i(TAG, "Scan Beacons");
                    scanBeacons(true);
                }

            });
        }
    }

    private void initializeLocationUpdates(){

        Log.i(TAG, "Initialize Location Updates");

        mLocationManager = (LocationManager) mAppContext
                .getSystemService(mAppContext.LOCATION_SERVICE);

        try {
            // getting GPS status
            mIsGPSEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            mIsNetworkEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!mIsGPSEnabled && !mIsNetworkEnabled) {
                // no network provider is enabled
            } else {
                mCanGetLocation = true;
                if (mIsNetworkEnabled) {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this);
                    Log.i(TAG, "Network");
                    if (mLocationManager != null) {
                        mCurrentLocation = mLocationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (mIsGPSEnabled) {
                    if (mCurrentLocation == null) {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                this);
                        Log.i(TAG, "GPS Enabled");
                        if (mLocationManager != null) {
                            mCurrentLocation = mLocationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
