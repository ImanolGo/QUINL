package net.imanolgomez.qnl;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by imanolgo on 12/12/14.
 */

public class DeviceInfoManager {

    public static String TAG = "DeviceInfoManager";

    private String mPhoneModel;
    private String mAndroidVersion;
    private String mDeviceId;
    private String mDeviceName;

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

        mDeviceName = "QNL_Smartphone";

        // Device model
        mPhoneModel =  android.os.Build.MANUFACTURER + "_" + android.os.Build.MODEL ;
        // Android version
        mAndroidVersion = android.os.Build.VERSION.RELEASE;

        TelephonyManager tManager = (TelephonyManager)mAppContext.getSystemService(Context.TELEPHONY_SERVICE);
        mDeviceId = tManager.getDeviceId();
        mDeviceId = "102";
    }

    private void logValues(){
        Log.i(TAG,"Device Name: " + mDeviceName);
        Log.i(TAG,"Phone Model: " + mPhoneModel);
        Log.i(TAG,"Android Version: " + mAndroidVersion);
        Log.i(TAG,"Device ID: " + mDeviceId);
        Log.i(TAG,"Device Battery Life: " + Integer.toString((int)getFloatBatteryLevel()*100) + "%");
        Log.i(TAG,"Time: " + getTime());
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

    public String getPhoneModel() {
        return mPhoneModel;
    }

    public String getAndroidVersion() {
        return mAndroidVersion;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date());
    }

}
