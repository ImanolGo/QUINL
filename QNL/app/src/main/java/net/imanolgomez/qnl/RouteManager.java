package net.imanolgomez.qnl;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by imanolgo on 14/12/14.
 */
public class RouteManager {

    public static final String TAG = "RouteManager";

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

    }

    private void addRoute(Route route) {
        mRoutes.put(route.getId(), route);
    }

    private class SendTrackingData extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String result = new ServerCommunicator(mAppContext).sendTrackingData();
                //Log.i(TAG, "Fetched contents from tracking data: " + result);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to send tracking data ", ioe);
            }
            return null;
        }
    }

}