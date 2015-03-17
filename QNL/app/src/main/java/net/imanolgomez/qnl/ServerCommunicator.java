package net.imanolgomez.qnl;

import android.content.Context;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by imanolgo on 12/12/14.
 */

public class ServerCommunicator {

    public static final String TAG = "ServerCommunicator";

    private static final String TRACKING_ENDPOINT = "http://www.o-a.info/qnl/lib/track.php";
    private static final String PHONE_ENDPOINT = "http://www.o-a.info/qnl/lib/phone.php";
    private static final String TIME = "time=";
    private static final String NAME = "name";
    private static final String REGISTER = "register";
    private static final String SERVICED = "serviced=";
    private static final String DEVICE_ID = "phone=";
    private static final String DEVICE_UUID = "uuid";
    private static final String BATTERY_LEVEL = "bat=";
    private static final String REGION = "region=";
    private static final String POSITION = "pos=";
    private static final String ACCURACY = "accuracy=";
    private static final String BEACON = "beacon=";
    private static final String RSSI = "rssi=";
    private static final String BEACONS = "beacons=";
    private static final String DEVICE_MANUFACTURER = "make";
    private static final String DEVICE_MODEL = "model";
    private static final String DEVICE_IMEI = "imei";
    private static final String DEVICE_SERIAL = "serial";
    private static final String DEVICE_MAC = "mac";
    private static final String EMPTY_STRING = "";

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

    private String makeGetRequest(String url) throws IOException {

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        // replace with your url

        HttpResponse response;
        try {
            response = client.execute(request);
            String responseStr = EntityUtils.toString(response.getEntity());
            Log.d(TAG, "Response of GET request" + responseStr);
            return responseStr;
            //return response.toString();
        } catch (ClientProtocolException e) {
            //  Auto-generated catch block
            e.printStackTrace();
            return EMPTY_STRING;
        } catch (IOException e) {
            //  Auto-generated catch block
            e.printStackTrace();
            return EMPTY_STRING;
        }

    }

    private String makePostRequest(String url,  List<NameValuePair> nameValuePair) throws IOException{

        HttpClient httpClient = new DefaultHttpClient();
        // replace with your url
        HttpPost httpPost = new HttpPost(url);


        //Encoding POST data
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            // log exception
            e.printStackTrace();
            return EMPTY_STRING;
        }

        //making POST request.
        try {
            HttpResponse response = httpClient.execute(httpPost);
            // write response to log
            String responseStr = EntityUtils.toString(response.getEntity());
            Log.d(TAG, "Http Post Response:" + responseStr);
            return responseStr;
        } catch (ClientProtocolException e) {
            // Log exception
            e.printStackTrace();
            return EMPTY_STRING;
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
            return EMPTY_STRING;
        }
    }

    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public String sendTrackingData() throws IOException {
        String trackingUrl = buildTrackingUrl();
        if (trackingUrl == null || trackingUrl.equals(EMPTY_STRING)){
            return null;
        }

        Log.d(TAG,"sendTrackingData-> " + trackingUrl);
        return makeGetRequest(trackingUrl);
    }

    public String registerDevice() throws IOException {
        String registrationUrl = PHONE_ENDPOINT;
        List<NameValuePair> nameValuePair = buildRegistrationParameters();
        //Log.d(TAG,registrationUrl);
        String response =  makePostRequest(registrationUrl, nameValuePair);
        setDeviceId(response);
        return response;
    }

    public String SendServicedMessage() throws IOException {
        String servicedMessageUrl = buildServicedMessageUrl();
        Log.d(TAG,"SendServicedMessage-> " + servicedMessageUrl);
        return makeGetRequest(servicedMessageUrl);
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
            return null;
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

        Log.d(TAG,"url: " + url);

        return url;
    }

    private List<NameValuePair> buildRegistrationParameters(){
        DeviceInfoManager deviceInfoManager = DeviceInfoManager.get(mAppContext);

        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
        nameValuePair.add(new BasicNameValuePair(REGISTER, "1"));
        nameValuePair.add(new BasicNameValuePair(NAME, deviceInfoManager.getDeviceName()));
        nameValuePair.add(new BasicNameValuePair(DEVICE_MANUFACTURER, deviceInfoManager.getDeviceManufacturer()));
        nameValuePair.add(new BasicNameValuePair(DEVICE_MODEL, deviceInfoManager.getDeviceModel()));
        nameValuePair.add(new BasicNameValuePair(DEVICE_IMEI, deviceInfoManager.getImei()));
        nameValuePair.add(new BasicNameValuePair(DEVICE_SERIAL, deviceInfoManager.getSerial()));
        nameValuePair.add(new BasicNameValuePair(DEVICE_MAC, deviceInfoManager.getMacAddress()));
        nameValuePair.add(new BasicNameValuePair(DEVICE_UUID, deviceInfoManager.getDeviceUuid()));

        return nameValuePair;
    }

    private String buildServicedMessageUrl(){
        DeviceInfoManager deviceInfoManager = DeviceInfoManager.get(mAppContext);

        String url = PHONE_ENDPOINT + "?" +
                SERVICED + deviceInfoManager.getDeviceId();

        return url;
    }

    private void setDeviceId(String jsonStr){

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            int id = jsonObj.getInt("inserted");
            Log.i(TAG,"Read Device ID from JSON response: " + id);
            DeviceInfoManager deviceInfoManager = DeviceInfoManager.get(mAppContext);
            deviceInfoManager.setDeviceId(id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
