package net.imanolgomez.qnl;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private static final long MIN_TIME_BW_UPDATES = 3000 * 1; // 3 second
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
    long BLUETOOTH_SCAN_PERIOD = 300;
    long BLUETOOTH_SCAN_INTERVAL = 500;
    BeaconFoundCallback mBeaconCallback;
    private Handler mBluetoothHandler;
    private Timer mBluetoothTimer;

    // Send Tracking
    long TRACKING_SEND_INTERVAL = 5000;
    private Timer mTrackingTimer;

    // Broadcast Communications
    public static final String ON_UPDATE_LOCATION = "onUpdateLocation";
    public static final String ON_POWER_CONNECTED = "onPowerConnected";
    public static final String ON_POWER_DISCONNECTED = "onPowerDisconnected";

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
        unregisterReceiver(mReceiver);
        Log.d(TAG, "onDestroy");
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(action.equals(Intent.ACTION_POWER_CONNECTED)){
                updateServicedMessage();
                stopUsingGPS();
                stopBluetoothTimer();
                stopManagers();
                retrieveData();
            }
            else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)){
                Log.i(TAG, "ACTION_POWER_DISCONNECTED");
                Intent intentSend = new Intent(ON_POWER_DISCONNECTED);
                sendBroadcast(intentSend);
                startUsingGPS();
                startBluetoothTimer();
            }
        }
    };

    private void initialize(){

        Log.i(TAG, "Initialize");
        this.initializeManagers();
        this.initializeLocationUpdates();
        this.initializeBluetooth();
        this.initializeReceiver();
        this.registerDevice();
        this.startTrackingTimer();
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
                        updateLocation();
                        //scanBeacons(false);
                    }
                });
            }
        };

        startBluetoothTimer();

    }

    protected void startTrackingTimer(){

        if(mTrackingTimer != null) {
            mTrackingTimer.cancel();
            mTrackingTimer = null;
        }

        mTrackingTimer = new Timer();
        // schedule task
        mTrackingTimer.scheduleAtFixedRate(new UpdateSendingTask(), 0, TRACKING_SEND_INTERVAL);
    }

    protected void startBluetoothTimer()
    {
        stopBluetoothTimer();
        mBluetoothTimer = new Timer();
        // schedule task
        mBluetoothTimer.scheduleAtFixedRate(new UpdateBeaconsTask(), 0, BLUETOOTH_SCAN_INTERVAL);
    }

    protected void stopBluetoothTimer()
    {
        if(mBluetoothTimer != null) {
            mBluetoothTimer.cancel();
            mBluetoothTimer = null;
        }
    }

    protected void initializeReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(mReceiver, filter);
    }

    protected void retrieveData()
    {
        mRouteManager.startRetrievingRoutes();
        mSoundManager.startRetrievingSamples();
    }

    protected void stopManagers()
    {
        mSoundManager.stop();
        mBeaconManager.stop();
        mRouteManager.stop();
        mQnlLocationManager.stop();
        communicateOnUpdate();
    }

    @Override
    public void onLocationChanged(Location location) {

        if(location==null){
            return;
        }

        Log.i(TAG, "onLocationChanged");
        Log.i(TAG, "lat-> " +  location.getLatitude() + ", lon-> " +  location.getLongitude() );

        mCurrentLocation = location;
        updateLocation();

    }

    public void updateLocation() {
        mQnlLocationManager.updateLocation(mCurrentLocation);
        communicateOnUpdate();
        //sendTrackingData();
    }

    private void sendTrackingData(){
        new SendTrackingData().execute();
    }

    private class SendTrackingData extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.d(TAG, "SendTrackingData");
                String result = new ServerCommunicator(mAppContext).sendTrackingData();
                if (result == null || result.equals("")){
                    Log.e(TAG, "Failed to get tracking data");
                }
                else{
                    Log.d(TAG, "Fetched contents from tracking data: " + result);
                }

            } catch (IOException ioe) {
                Log.e(TAG, "Failed to send tracking data ", ioe);
            }
            return null;
        }
    }

    private void registerDevice()
    {
        if(mDBManager.isDeviceRegistered()){
            Log.i(TAG, "Device registered");
            mDeviceInfoManager.setDeviceId(mDBManager.getDeviceId());
            updateServicedMessage();
        }
        else{
            Log.i(TAG, "Register Device");
            new RegisteringDevice().execute();
        }
    }

    private class RegisteringDevice extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String result = new ServerCommunicator(mAppContext).registerDevice();
                if (result == null || result.equals("")){
                    Log.e(TAG, "Failed to get to register device");
                }
                else{
                    mDBManager.registerDevice();
                    Log.i(TAG, "Device registered!!");
                }

            } catch (IOException ioe) {
                Log.e(TAG, "Failed to send register device ", ioe);
            }
            return null;
        }
    }

    private class SendingServicedMessage extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String result = new ServerCommunicator(mAppContext).SendServicedMessage();
                if (result == null || result.equals("")){
                    Log.e(TAG, "Failed to send service device ");
                }
                else{
                    Log.i(TAG, "Send Serviced Message: " + result);
                }

            } catch (IOException ioe) {
                Log.e(TAG, "Failed to send service device ", ioe);
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
        sendBroadcast(intent);
    }

    private void updateServicedMessage() {
        Log.i(TAG, "ACTION_POWER_CONNECTED");
        new SendingServicedMessage().execute();
        Intent intentSend = new Intent(ON_POWER_CONNECTED);
        sendBroadcast(intentSend);
    }

    class UpdateBeaconsTask extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mBluetoothHandler.post(new Runnable() {

                @Override
                public void run() {
                    Log.i(TAG, "Scan Beacons");
                    mBeaconManager.update();
                    //updateLocation();
                    scanBeacons(true);
                }

            });
        }
    }

    class UpdateSendingTask extends TimerTask {
        @Override
        public void run() {
            sendTrackingData();
        }
    }



    private void initializeLocationUpdates(){
        Log.i(TAG, "Initialize Location Updates");
        mLocationManager = (LocationManager) mAppContext.getSystemService(mAppContext.LOCATION_SERVICE);
        startUsingGPS();
    }

    /**
     * Start using GPS listener
     * Calling this function will start using GPS in your app
     * */
    public void startUsingGPS(){
        try {
            // getting GPS status
            mIsGPSEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!mIsGPSEnabled) {
                // no GPS provider is enabled
                mCanGetLocation = false;
            }
            else
            {
                mCanGetLocation = true;

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(mLocationManager != null){
            mLocationManager.removeUpdates(QnlService.this);
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
