package net.imanolgomez.qnl_androidlocation;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by imanolgo on 12/12/14.
 */

public class DeviceInfoManager {

    public static String TAG = "DeviceInfoManager";

    private String mDeviceModel;
    private String mAndroidVersion;
    private String mDeviceId;
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

        mDeviceName = "QnlDevice";

        // Device model
        mDeviceModel =  android.os.Build.MANUFACTURER + "_" + android.os.Build.MODEL ;
        // Android version
        mAndroidVersion = android.os.Build.VERSION.RELEASE;

        mSerial = Build.SERIAL;

        TelephonyManager tManager = (TelephonyManager)mAppContext.getSystemService(Context.TELEPHONY_SERVICE);
        mImei = tManager.getDeviceId();

        mDeviceId = Settings.Secure.getString(mAppContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        mDeviceId = "102";

        WifiManager wifiManager = (WifiManager) mAppContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        mMacAddress = wInfo.getMacAddress();
    }

    private void logValues(){
        Log.i(TAG,"Device Name: " + mDeviceName);
        Log.i(TAG,"Device Model: " + mDeviceModel);
        Log.i(TAG,"Android Version: " + mAndroidVersion);
        Log.i(TAG,"Device ID: " + mDeviceId);
        Log.i(TAG,"Device IMEI: " + mImei);
        Log.i(TAG,"Device Serial: " + mSerial);
        Log.i(TAG,"Device MAC Address: " + mMacAddress);
        Log.i(TAG,"Device Battery Life: " + Integer.toString((int)(getFloatBatteryLevel()*100)) + "%");
        Log.i(TAG,"Time: " + getTime());
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

    public String getDeviceModel() {
        return mDeviceModel;
    }

    public String getAndroidVersion() {
        return mAndroidVersion;
    }

    public String getDeviceId() {
        return mDeviceId;
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
