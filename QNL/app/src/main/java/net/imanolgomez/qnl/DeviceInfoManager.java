package net.imanolgomez.qnl;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by imanolgo on 12/12/14.
 */

public class DeviceInfoManager {

    public static String TAG = "DeviceInfoManager";

    private String mDeviceManufacturer;
    private String mDeviceModel;
    private String mAndroidVersion;
    private int mDeviceId;
    private String mDeviceUuid;
    private String mDeviceName;
    private String mImei;
    private String mSerial;
    private String mMacAddress;

    private static DeviceInfoManager sDeviceInfoManager;

    private Context mAppContext;


    private DeviceInfoManager(Context appContext) {
        mAppContext = appContext;
        this.initialize();
        this.logValues();

    }
    public static DeviceInfoManager get(Context c) {
        if (sDeviceInfoManager == null) {
            sDeviceInfoManager = new DeviceInfoManager(c.getApplicationContext());
        }
        return sDeviceInfoManager;
    }


    private void initialize(){

        Log.i(TAG, "initialize()");

        DBManager dbManager = DBManager.get(mAppContext);

        // Device manufacturer
        mDeviceManufacturer =  android.os.Build.MANUFACTURER;
        // Device model
        mDeviceModel = android.os.Build.MODEL ;
        // Android version
        mAndroidVersion = android.os.Build.VERSION.RELEASE;

        mSerial = Build.SERIAL;

        TelephonyManager tManager = (TelephonyManager)mAppContext.getSystemService(Context.TELEPHONY_SERVICE);
        mImei = tManager.getDeviceId();

        mDeviceUuid = Settings.Secure.getString(mAppContext.getContentResolver(), Settings.Secure.ANDROID_ID);

        mDeviceId = dbManager.getDeviceId();
        mDeviceName = dbManager.getDeviceName();

        WifiManager wifiManager = (WifiManager) mAppContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        mMacAddress = wInfo.getMacAddress();
    }

    private void logValues(){
        Log.i(TAG,"Device Name: " + mDeviceName);
        Log.i(TAG,"Device Model: " + mDeviceModel);
        Log.i(TAG,"Android Version: " + mAndroidVersion);
        Log.i(TAG,"Device ID: " + mDeviceId);
        Log.i(TAG,"Device UUID: " + mDeviceUuid);
        Log.i(TAG,"Device IMEI: " + mImei);
        Log.i(TAG,"Device Serial: " + mSerial);
        Log.i(TAG,"Device MAC Address: " + mMacAddress);
        Log.i(TAG,"Device Battery Life: " + Integer.toString((int)(getFloatBatteryLevel()*100)) + "%");
        Log.i(TAG,"Time: " + getTime());
        Log.i(TAG,"Network Status: " + isNetworkConnected());
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getBatteryLevel() {
        return Float.toString(getFloatBatteryLevel());
    }

    private float getFloatBatteryLevel() {
        Intent batteryIntent = mAppContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 0.5f;
        }


        return ((float)level / (float)scale);
        //return Integer.toString(iBatteryLevel);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mAppContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    public void setDeviceIdFromJson(String jsonStr){

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            int id = jsonObj.getInt("inserted");
            Log.i(TAG,"Read Device ID from JSON response: " + id);
            DeviceInfoManager deviceInfoManager = DeviceInfoManager.get(mAppContext);
            setDeviceId(id);
            setDeviceName("QNL_Device_" + id);
            Log.i(TAG,"New Device Name: " + mDeviceName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDeviceManufacturer() {
        return mDeviceManufacturer;
    }

    public String getDeviceModel() {
        return mDeviceModel;
    }

    public String getAndroidVersion() {
        return mAndroidVersion;
    }

    public int getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(int deviceId) {
        mDeviceId = deviceId;
    }

    public void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    public String getDeviceUuid() {
        return mDeviceUuid;
    }

    public String getImei() {
        return mImei;
    }

    public String getSerial() {
        return mSerial;
    }

    public String getMacAddress() {
        return mMacAddress;
    }

    public String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }

}
