package net.imanolgomez.qnl;

import android.app.FragmentManager;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

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
        mGoogleMap.moveCamera(CameraUpdateFactory.zoomTo(15.0f));
    }

    public void updateLocation(Location location){
        // Set Center of Google Maps
        LatLng CURRENT_LOCATION = new LatLng(location.getLatitude(), location.getLongitude());
        // Move the camera instantly to current location
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(CURRENT_LOCATION));
    }

    public void addSection(Section section){
        // Instantiates a new Polygon object and adds points to define a rectangle
        PolygonOptions sectionOptions = new PolygonOptions()
                .add(new LatLng(section.getLocation1().getLatitude(), section.getLocation1().getLongitude()),
                        new LatLng(section.getLocation1().getLatitude(), section.getLocation2().getLongitude()),
                        new LatLng(section.getLocation2().getLatitude(), section.getLocation2().getLongitude()),
                        new LatLng(section.getLocation2().getLatitude(), section.getLocation1().getLongitude()));

        // Get back the mutable Polygon
        Polygon polygon = mGoogleMap.addPolygon(sectionOptions);
    }
}
