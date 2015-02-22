package net.imanolgomez.qnl;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by imanolgo on 12/12/14.
 */

public class ServerCommunicator {

    public static final String TAG = "ServerCommunicator";

    private static final String TRACKING_ENDPOINT = "http://www.o-a.info/qnl/lib/track.php";
    private static final String REGISTRATION_ENDPOINT = "http://www.o-a.info/qnl/lib/phone.php?register=1";
    private static final String TIME = "time=";
    private static final String NAME = "name=";
    private static final String DEVICE_ID = "phone=";
    private static final String DEVICE_UUID = "uuid=";
    private static final String BATTERY_LEVEL = "bat=";
    private static final String REGION = "region=";
    private static final String POSITION = "pos=";
    private static final String ACCURACY = "accuracy=";
    private static final String BEACON = "beacon=";
    private static final String RSSI = "rssi=";
    private static final String BEACONS = "beacons=";
    private static final String DEVICE_MANUFACTURER = "make=";
    private static final String DEVICE_MODEL = "model=";
    private static final String DEVICE_IMEI = "imei=";
    private static final String DEVICE_SERIAL = "serial=";
    private static final String DEVICE_MAC = "mac=";

    private Context mAppContext;

    public ServerCommunicator(Context c) {
        mAppContext = c.getApplicationContext();
    }


    byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public String sendTrackingData() throws IOException {
        String trackingUrl = buildTrackingUrl();
        //Log.d(TAG,trackingUrl);
        return getUrl(trackingUrl);
    }

    public String registerDevice() throws IOException {
        String registrationUrl = buildRegistrationUrl();
        //Log.d(TAG,registrationUrl);
        return getUrl(registrationUrl);
    }

    private String buildTrackingUrl(){
        DeviceInfoManager deviceInfoManager = DeviceInfoManager.get(mAppContext);
        QnlLocationManager qnlLocationManager = QnlLocationManager.get(mAppContext);
        BeaconManager beaconManager = BeaconManager.get(mAppContext);

        int currentRegionId = -1;
        if(qnlLocationManager.getCurrentRegion()!=null){
            currentRegionId = qnlLocationManager.getCurrentRegion().getId();
        }

        if(qnlLocationManager.getCurrentLocation() == null){
            return "";
        }

        int currentSpotId = -1;
        int rssi = 0;
        if(qnlLocationManager.getCurrentSpot()!=null){
            currentSpotId = qnlLocationManager.getCurrentSpot().getId();
            if(beaconManager.getNearestBeacon()!=null){
                rssi = beaconManager.getNearestBeacon().getRssi();
            }
        }



        String url = TRACKING_ENDPOINT + "?" +
                TIME + deviceInfoManager.getTime() + "&" +
                DEVICE_ID + deviceInfoManager.getDeviceId() + "&" +
                BATTERY_LEVEL + deviceInfoManager.getBatteryLevel() + "&" +
                REGION + currentRegionId + "&" +
                POSITION + qnlLocationManager.getCurrentLocation().getLatitude() + "," +
                qnlLocationManager.getCurrentLocation().getLongitude() + "&" +
                ACCURACY  + qnlLocationManager.getCurrentLocation().getAccuracy() + "&" +
                BEACON  + currentSpotId + "&" +
                RSSI  + rssi + "&" +
                BEACONS  + beaconManager.getBeaconsListString();

        Log.d("ServerCommunicator","url: " + url);

        return url;
    }

    private String buildRegistrationUrl(){
        DeviceInfoManager deviceInfoManager = DeviceInfoManager.get(mAppContext);

        String url = REGISTRATION_ENDPOINT + "&" +
                NAME + deviceInfoManager.getDeviceName()+ "&" +
                DEVICE_ID + deviceInfoManager.getDeviceId() + "&" +
                DEVICE_UUID + deviceInfoManager.getDeviceUuid() + "&" +
                DEVICE_MANUFACTURER + deviceInfoManager.getDeviceManufacturer() + "&" +
                DEVICE_MODEL + deviceInfoManager.getDeviceModel() + "&" +
                DEVICE_IMEI + deviceInfoManager.getImei() + "&" +
                DEVICE_SERIAL + deviceInfoManager.getSerial() + "&" +
                DEVICE_MAC + deviceInfoManager.getMacAddress();

        return url;
    }

}
