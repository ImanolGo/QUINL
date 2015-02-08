package net.imanolgomez.qnl_androidlocation;

/**
 * Created by imanolgo on 14/12/14.
 */


import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * An class defining a Zone, which a certain kind of element and has it's own types.
 */

public class Region extends RouteElement {

    public enum RegionType {ZONE, ROOM, PATH};

    private final RegionType mRegionType;
    private HashMap<Integer, Section> mSections;

    /**
     * @param basicElement The RouteElement's basic attributes.
     * @param regionType The Type to which the Region belong to.
     */
    public Region(
            BasicElement basicElement,
            RegionType regionType) {

        super(basicElement);

        mSections = new  HashMap<Integer, Section>();
        mRegionType = regionType;
    }

    public RegionType getRegionType() {
        return mRegionType;
    }

    public String getRegionTypeString() {

        String regionTypeStr;

        switch (mRegionType) {
            case PATH:
                regionTypeStr = "path";
                break;
            case ZONE:
                regionTypeStr = "zone";
                break;
            case ROOM:
                regionTypeStr = "room";
                break;
            default:
                regionTypeStr = "zone";
                break;
        }

        return regionTypeStr;
    }

    private static RegionType getTypeFromString(String typeStr) {

        RegionType regionType;

        switch (typeStr.toLowerCase()) {
            case "path":
                regionType = RegionType.PATH;
                break;
            case "zone":
                regionType = RegionType.ZONE;
                break;
            case "room":
                regionType = RegionType.ROOM;
                break;
            default:
                regionType = RegionType.ZONE;
                break;
        }

        return regionType;
    }

    public void addSection(Section section) {
        if(section==null){
            return;
        }

        mSections.put(section.getId(), section);
        Log.i("createSegmentsFromJson", "Added section:" + section.getId() + " to region: " + getId());
    }

    public boolean isInside(Location loc) {
        for (Section section : mSections.values()) {
            if(section.isInside(loc)){
                //Log.i("Section", "Section found->" + section.getId() + " to region: " + getId());
                return true;
            }
        }
        return false;
    }

    public static Region createRegionFromJson(String jsonStr){

        try {
            JSONObject reader = new JSONObject(jsonStr);
            JSONObject zoneJson  = reader.getJSONObject("zone");

            int id = zoneJson.getInt(TAG_ID);
            int sampleId = zoneJson.getInt(TAG_SAMPLE_ID);
            int routeId = zoneJson.getInt(TAG_ROUTE_ID);
            double version = zoneJson.getDouble(TAG_VERSION);
            String name = zoneJson.getString(TAG_NAME);
            double volume = zoneJson.getDouble(TAG_VOLUME);
            int intBoolean = zoneJson.getInt(TAG_LOOP);
            boolean loop = true;
            if(intBoolean==0){
                loop = false;
            }
            RegionType regionType = getTypeFromString(zoneJson.getString(TAG_TYPE));

            BasicElement basicElement = new BasicElement(id,name,version);
            Region region = new Region(basicElement,regionType);
            region.setLoop(loop);region.setSampleId(sampleId);region.setVolume(volume); region.setRouteId(routeId);
            return region;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createSectionsFromJson(String jsonStr, DBManager dbManager, MapManager mapManager){

        try {
            JSONObject reader = new JSONObject(jsonStr);
            JSONObject regionsJson  = reader.getJSONObject("regions");
            //Log.i("createSegmentsFromJson", "Json names:" + regionsJson.names());
            //Log.i("createSegmentsFromJson", "Json names type:" + regionsJson.names().getClass().getName());
            //Log.i("createSegmentsFromJson", "Json regions length:" + regionsJson.length());

            JSONArray regionsNumbers = regionsJson.names();

            for (int i = 0; i < regionsJson.length(); i++) {
                String key = regionsNumbers.getString(i);
                //Log.i("createSegmentsFromJson", "Json regionsNumbers:" + key);
                JSONObject regionJson  = regionsJson.getJSONObject(key);
                Section section = createSectionFromJsonObject(regionJson);
                if(section!=null){
                    addSection(section);
                    if(dbManager!=null) {
                        dbManager.insertSection(section);
                    }
                    if(mapManager!=null) {
                        mapManager.addSection(section);
                    }

                }
                //Log.i("createSegmentsFromJson", "Single region names:" + regionJson.names());
            }



            // looping through All Contacts
            /*for (int i = 0; i < regionsJson.length(); i++) {
                JSONObject c = regionsJson.getJSONObject(i);
                Log.i("createSegmentsFromJson", "Json names:" + c.names());

                Section section = createSectionFromJsonObject(c);
                if(section!=null){
                    addSection(section);
                }
            }*/


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
