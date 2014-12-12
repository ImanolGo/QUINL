package net.imanolgomez.qnl;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by imanolgo on 12/12/14.
 */

public class DeviceInfoManager {

    private String mPhoneModel;
    private String mAndroidVersion;
    private String mDeviceId;
    private String mDeviceName;
    private Context mAppContext;

    public DeviceInfoManager(Context c) {
        mAppContext = c.getApplicationContext();
        this.initialize();
        this.logValues();

    }

    private void initialize(){

        mDeviceName = "QNL_Smartphone";

        // Device model
        mPhoneModel =  android.os.Build.MANUFACTURER + "_" + android.os.Build.MODEL ;
        // Android version
        mAndroidVersion = android.os.Build.VERSION.RELEASE;

        TelephonyManager tManager = (TelephonyManager)mAppContext.getSystemService(Context.TELEPHONY_SERVICE);
        mDeviceId = tManager.getDeviceId();
    }

    private void logValues(){
        Log.i("DeviceManager","Device Name: " + mDeviceName);
        Log.i("DeviceManager","Phone Model: " + mPhoneModel);
        Log.i("DeviceManager","Android Version: " + mAndroidVersion);
        Log.i("DeviceManager","Device ID: " + mDeviceId);
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
}
