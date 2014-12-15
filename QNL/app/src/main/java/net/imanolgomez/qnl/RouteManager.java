package net.imanolgomez.qnl;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by imanolgo on 14/12/14.
 */
public class RouteManager {

    public static final String TAG = "RouteManager";
    private static final String EMPTY_STRING = "";
    private static final String ENDPOINT = "http://www.o-a.info/qnl/lib/";
    private static final String ROUTE = "route.php";
    private static final String REGION = "zone.php";
    private static final String SPOT = "beacon.php";

    private static final String TAG_NAME = "name";
    private static final String TAG_VERSION = "version";
    private static final String TAG_ID = "id";

    Context mAppContext;
    private HashMap<Integer, Route> mRoutes;

    private static RouteManager sRouteManager;

    private RouteManager(Context appContext) {
        mAppContext = appContext;
        mRoutes = new HashMap<Integer, Route>();
        this.initialize();
    }


    public static RouteManager get(Context c) {
        if (sRouteManager == null) {
            sRouteManager = new RouteManager(c.getApplicationContext());
        }
        return sRouteManager;
    }

    private void initialize(){
        //Set the tracking data
        new retrieveRoutes().execute();
        new retrieveRegions().execute();
    }

    private class retrieveRoutes extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String result = new ServerCommunicator(mAppContext).getUrl(ENDPOINT+ROUTE+"?list=1");
                Log.i(TAG, "Retrieving routes data: " + result);
                createRoutes(result);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to send tracking data ", ioe);
            }
            return null;
        }

    }

    private class retrieveRegions extends AsyncTask<Void,Void,String> {
        String result;
        @Override
        protected String doInBackground(Void... params) {
            try {
                result = new ServerCommunicator(mAppContext).getUrl(ENDPOINT+ROUTE+"?list=1");
                //Log.i(TAG, "Retrieving routes data: " + result);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to send tracking data ", ioe);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            createRegions(result);
        }

    }

    private class getRegionInfo extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = EMPTY_STRING;
            for (String url : urls) {
                try {
                    result = new ServerCommunicator(mAppContext).getUrl(url);
                    //Log.i(TAG, "Retrieving routes data: " + result);
                } catch (IOException ioe) {
                    Log.e(TAG, "Failed to retrieve data: " + url , ioe);
                }

            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            createSingleRegion(result);
        }

    }

    private void createRoutes(String routeInfo) {
        try {
            JSONObject reader = new JSONObject(routeInfo);
            Iterator iterator = reader.keys();
            while(iterator.hasNext()){
                String key = (String)iterator.next();
                if(!key.equals("list of routes")){
                    int id = Integer.parseInt(key);
                    double version = 1.0;
                    String name = reader.getString(key);
                    BasicElement basicElement = new BasicElement(id,name,version);
                    Route route = new Route(basicElement);
                    addRoute(route);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createRegions(String regionInfo) {
        try {
            JSONObject reader = new JSONObject(regionInfo);
            Iterator iterator = reader.keys();
            while(iterator.hasNext()){
                String key = (String)iterator.next();
                if(!key.equals("list of zones")){
                    String url = ENDPOINT+ROUTE+"?zone=" + key;
                    new getRegionInfo().execute(url);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void createSingleRegion(String singleRegionInfo) {
        Region region = Region.createRegionFromJson(singleRegionInfo);
        if (region != null) {
            region.createSegmentsFromJson(singleRegionInfo);
            addRegion(region);
        }
    }

    private void addRoute(Route route) {
        if(route==null){
            return;
        }

        mRoutes.put(route.getId(), route);
    }

    private void addRegion(Region region){
        if(region==null){
            return;
        }

        if(mRoutes.containsKey(region.getRouteId())){
            mRoutes.get(region.getRouteId()).addRegion(region);
        }

    }

}