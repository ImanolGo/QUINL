package net.imanolgomez.qnl;


import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

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

    private Spot mCurrentSpot;
    private Region mCurrentRegion;
    private Route mCurrentRoute;
    private DBManager mDBManager;
    private MapManager mMapManager;

    Context mAppContext;
    private HashMap<Integer, Route> mRoutes;

    private static RouteManager sRouteManager;

    private RouteManager(Context appContext) {
        mAppContext = appContext;
        this.initialize();
    }


    public static RouteManager get(Context c) {
        if (sRouteManager == null) {
            sRouteManager = new RouteManager(c.getApplicationContext());
        }
        return sRouteManager;
    }

    private void initialize(){
        initializeAttributes();
        startRetrievingRoutes();
    }

    private void initializeAttributes(){
        mRoutes = new HashMap<Integer, Route>();
        clearAttributes();
        mDBManager = DBManager.get(mAppContext);
        getMapManager();
    }

    public void startRetrievingRoutes()
    {
        new retrieveRoutes().execute();
        new retrieveSpots().execute();
        new retrieveRegions().execute();
        new updateDatabase().execute();
    }

    private void clearAttributes(){
        mRoutes.clear();
        mCurrentRegion = null;
        mCurrentRoute = null;
        mCurrentSpot = null;
    }

    public void stop(){
        mCurrentRegion = null;
        mCurrentRoute = null;
        mCurrentSpot = null;
    }

    private void getMapManager(){

        try{

            Activity activity = (Activity) mAppContext;

            mMapManager = MapManager.get(activity.getFragmentManager());

            // If using the Support lib.
            // return activity.getSupportFragmentManager();

        } catch (ClassCastException e) {
            Log.d(TAG, "Can't get the fragment manager with this");
        }

    }

    public void updateLocation(Location currentLocation){

        mCurrentSpot = null;
        mCurrentRegion = null;

        if(!updateCurrentRoute(currentLocation)){
            mCurrentRoute = null;
            searchForCurrentRoute(currentLocation);
        }

    }

    public boolean updateCurrentRoute(Location currentLocation){

        if(mCurrentRoute == null){
            return false;
        }

        BeaconManager beaconManager = BeaconManager.get(mAppContext);
        if(mCurrentRoute.isInsideSpot(beaconManager.getNearestBeacon())){
            mCurrentSpot = mCurrentRoute.getCurrentSpot();
            int beaconMinor = Integer.parseInt(mCurrentSpot.getUUID());
            beaconManager.setBeaconToSleep(beaconMinor);
            return true;
        }

        if(mCurrentRoute.isInside(currentLocation)){
            mCurrentRegion = mCurrentRoute.getCurrentRegion();
            return true;
        }

        return false;

    }

    private void searchForCurrentRoute(Location currentLocation){

        BeaconManager beaconManager = BeaconManager.get(mAppContext);
        for (Route route : mRoutes.values()) {
            if(route.isInsideSpot(beaconManager.getNearestBeacon())){
                mCurrentSpot = route.getCurrentSpot();
                mCurrentRoute = route;
                int beaconMinor = Integer.parseInt(mCurrentSpot.getUUID());
                beaconManager.setBeaconToSleep(beaconMinor);
                return;
            }
        }

        for (Route route : mRoutes.values()) {
            if(route.isInside(currentLocation)){
                mCurrentRegion = route.getCurrentRegion();
                mCurrentRoute = route;
                return;
            }
        }

    }

    private class retrieveRoutes extends AsyncTask<Void,Void,String> {
        String result;
        @Override
        protected String doInBackground(Void... params) {
            try {
                result = new ServerCommunicator(mAppContext).getUrl(ENDPOINT+ROUTE+"?versions=1");
                Log.i(TAG, "Retrieving routes data: " + result);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to send tracking data ", ioe);
            }
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            createRoutes(result);
        }

    }

    private class retrieveSpots extends AsyncTask<Void,Void,String> {
        String result;
        @Override
        protected String doInBackground(Void... params) {
            try {
                result = new ServerCommunicator(mAppContext).getUrl(ENDPOINT+SPOT+"?versions=1");
                Log.i(TAG, "Retrieving spots data: " + result);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed retrieving spots data:  ", ioe);
            }
            return result;
        }
        @Override
        protected void onPostExecute(String result) {
            createSpots(result);
        }

    }

    private class retrieveRegions extends AsyncTask<Void,Void,String> {
        String result;
        @Override
        protected String doInBackground(Void... params) {
            try {
                result = new ServerCommunicator(mAppContext).getUrl(ENDPOINT+REGION+"?versions=1");
                Log.i(TAG, "Retrieving region data: " + result);
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

    private class getRouteInfo extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = EMPTY_STRING;
            for (String url : urls) {
                try {
                    result = new ServerCommunicator(mAppContext).getUrl(url);
                    Log.i(TAG, "Retrieving route data from: " + url);
                    //Log.i(TAG, "Retrieving routes data: " + result);
                } catch (IOException ioe) {
                    Log.e(TAG, "Failed to retrieve route info data: " + url , ioe);
                }

            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            createSingleRoute(result);
        }
    }

    private class getSpotInfo extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = EMPTY_STRING;
            for (String url : urls) {
                try {
                    result = new ServerCommunicator(mAppContext).getUrl(url);
                    Log.i(TAG, "Retrieving spot data from: " + url);
                    //Log.i(TAG, "Retrieving spot data: " + result);
                } catch (IOException ioe) {
                    Log.e(TAG, "Failed to retrieve spot info data: " + url , ioe);
                }

            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            createSingleSpot(result);
        }
    }

    private class getRegionInfo extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = EMPTY_STRING;
            for (String url : urls) {
                try {
                    result = new ServerCommunicator(mAppContext).getUrl(url);
                    Log.i(TAG, "Retrieving region data from: " + url);
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

    private class updateDatabase extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            //To DO
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //To DO
        }

    }

    private void createRoutes(String routeInfo) {
        try {
            JSONObject reader = new JSONObject(routeInfo);
            Iterator iterator = reader.keys();
            while(iterator.hasNext()){
                String key = (String)iterator.next();
                double version = reader.getDouble(key);
                int id = Integer.parseInt(key);
                if(mDBManager.isRouteUpToDate(id,version)){
                    Route route = mDBManager.getRoute(id);
                    if (route != null) {
                        /*Log.i(TAG,"Route id: " + route.getId());
                        Log.i(TAG,"Route name: " + route.getName());
                        Log.i(TAG,"Route version: " + route.getVersion());*/
                        addRoute(route);
                    }
                }
                else{
                    String url = ENDPOINT+ROUTE+"?route=" + key;
                    new getRouteInfo().execute(url);
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSpots(String spotInfo) {
        try {
            JSONObject reader = new JSONObject(spotInfo);
            Iterator iterator = reader.keys();
            while(iterator.hasNext()){
                String key = (String)iterator.next();
                double version = reader.getDouble(key);
                int id = Integer.parseInt(key);
                if(mDBManager.isSpotUpToDate(id,version)){
                    Spot spot = mDBManager.getSpot(id);
                    if (spot != null) {
                        /*Log.i(TAG,"Spot id: " + spot.getId());
                        Log.i(TAG,"Spot name: " + spot.getName());
                        Log.i(TAG,"Spot version: " + spot.getVersion());*/
                        addSpot(spot);
                    }
                }
                else {
                    String url = ENDPOINT + SPOT + "?beacon=" + key;
                    new getSpotInfo().execute(url);
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
                double version = reader.getDouble(key);
                int id = Integer.parseInt(key);
                if(mDBManager.isRegionUpToDate(id,version)){
                    Region region = mDBManager.getRegion(id);
                    if (region != null) {
                        /*Log.i(TAG,"Region id: " + region.getId());
                        Log.i(TAG,"Region name: " + region.getName());
                        Log.i(TAG,"Region version: " + region.getVersion());*/
                        addRegion(region);
                    }
                }
                else {
                    mDBManager.deleteSectionsFromRegion(id);
                    String url = ENDPOINT + REGION + "?zone=" + key;
                    new getRegionInfo().execute(url);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSingleRoute(String singleRouteInfo) {
        Route route = Route.createRouteFromJson(singleRouteInfo);
        if (route != null) {
            /*Log.i(TAG,"Route id: " + route.getId());
            Log.i(TAG,"Route name: " + route.getName());
            Log.i(TAG,"Route version: " + route.getVersion());*/

            addRoute(route);
        }
    }

    private void createSingleRegion(String singleRegionInfo) {
        Region region = Region.createRegionFromJson(singleRegionInfo);
        if (region != null) {
            /*Log.i(TAG,"Region id: " + region.getId());
            Log.i(TAG,"Region name: " + region.getName());
            Log.i(TAG,"Region version: " + region.getVersion());
            Log.i(TAG,"Region sampleId: " + region.getSampleId());
            Log.i(TAG,"Region routeId: " + region.getRouteId());
            Log.i(TAG,"Region volume: " + region.getVolume());
            Log.i(TAG,"Region loop: " + region.isLooping());*/
            boolean sectionsCreated = region.createSectionsFromJson(singleRegionInfo, mDBManager,mMapManager);
            if(sectionsCreated){
                addRegion(region);
            }
        }
    }

    private void createSingleSpot(String singleSpotnInfo) {
        Spot spot = Spot.createSpotFromJson(singleSpotnInfo);
        if (spot != null) {
            /*Log.i(TAG,"Spot id: " + spot.getId());
            Log.i(TAG,"Spot name: " + spot.getName());
            Log.i(TAG,"Spot version: " + spot.getVersion());
            Log.i(TAG,"Spot sampleId: " + spot.getSampleId());
            Log.i(TAG,"Spot routeId: " + spot.getRouteId());
            Log.i(TAG,"Spot volume: " + spot.getVolume());
            Log.i(TAG,"Spot loop: " + spot.isLooping());*/
            addSpot(spot);
        }
    }

    private void addRoute(Route route) {
        if(route==null){
            return;
        }
        Log.i(TAG,"Added Route: " + route.getId() + ", " + route.getName());
        mRoutes.put(route.getId(), route);

        mDBManager.insertRoute(route);
    }

    private void addRegion(Region region){
        if(region==null){
            return;
        }

        if(mRoutes.containsKey(region.getRouteId())){
            Log.i(TAG,"Added Region: " + region.getId() + " to route: " + region.getRouteId());
            mRoutes.get(region.getRouteId()).addRegion(region);
            mDBManager.insertRegion(region);
        }

    }

    private void addSpot(Spot spot){
        if(spot==null){
            return;
        }

        if(mRoutes.containsKey(spot.getRouteId())){
            Log.i(TAG,"Added Spot: " + spot.getId() + " to route: " + spot.getRouteId());
            mRoutes.get(spot.getRouteId()).addSpot(spot);
            mDBManager.insertSpot(spot);
        }

    }

    public Region getCurrentRegion() {
        return mCurrentRegion;
    }

    public Spot getCurrentSpot() {
        return mCurrentSpot;
    }

    public Route getCurrentRoute() {
        return mCurrentRoute;
    }

    public Route getRouteFromId(int routeId){
        if(mRoutes.containsKey(routeId)){
            return mRoutes.get(routeId);
        }

        return null;
    }
}