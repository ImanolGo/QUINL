package net.imanolgomez.qnl;

import android.app.FragmentManager;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by imanolgo on 29/12/14.
 */
public class MapManager {

    public static String TAG = "LocationManager";

    private static MapManager sMapManager;

    // Handles Google Maps Api
    private GoogleMap mGoogleMap;

    private FragmentManager mFragmentManager;

    private MapManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
       this.initialize();
    }

    public static MapManager get(FragmentManager fragmentManager) {
        if (sMapManager == null) {
            sMapManager = new MapManager(fragmentManager);
        }
        return sMapManager;
    }

    private void initialize(){
        mGoogleMap = ((MapFragment) mFragmentManager.findFragmentById(R.id.map))
                .getMap();

        mGoogleMap.setMyLocationEnabled(true); // false to disable
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    public void updateLocation(Location location){
        // Set Center of Google Maps
        LatLng CURRENT_LOCATION = new LatLng(location.getLatitude(), location.getLongitude());
        // Move the camera instantly to current location
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(CURRENT_LOCATION));
    }
}
