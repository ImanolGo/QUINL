package net.imanolgomez.qnl;


import java.util.HashMap;

/**
 * Created by imanolgo on 14/12/14.
 */
public class RouteManager {

    private HashMap<Integer, Route> mRoutes;

    private static RouteManager sRouteManager;

    private RouteManager() {
        this.initialize();
    }

    public static RouteManager get() {
        if (sRouteManager == null) {
            sRouteManager = new RouteManager();
        }
        return sRouteManager;
    }

    private void initialize(){

    }

    private void addRoute(Route route) {
        mRoutes.put(route.getId(), route);
    }

}