package net.imanolgomez.qnl;

import android.content.Context;

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

    private static final String ENDPOINT = "http://o-a.info/qitnl/track.php";
    private static final String TIME = "time=";
    private static final String DEVICE_ID = "phone=";
    private static final String BATTERY_LEVEL = "bat=";
    private static final String REGION = "region=";
    private static final String POSITION = "pos=";
    private static final String ACCURACY = "accuracy=";

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
        //Log.i(TAG,trackingUrl);
        return getUrl(trackingUrl);
    }

    private String buildTrackingUrl(){
        DeviceInfoManager deviceInfoManager = DeviceInfoManager.get(mAppContext);
        LocationManager locationManager = LocationManager.get();

        String url = ENDPOINT + "?" +
                TIME + deviceInfoManager.getTime() + "&" +
                DEVICE_ID + deviceInfoManager.getDeviceId() + "&" +
                BATTERY_LEVEL + deviceInfoManager.getBatteryLevel() + "&" +
                REGION + locationManager.getCurrentRegionId() + "&" +
                POSITION + locationManager.getCurrentLocation().getLatitude() + "," +
                locationManager.getCurrentLocation().getLongitude() + "&" +
                ACCURACY  + locationManager.getCurrentLocation().getAccuracy();

        return url;
    }

}
