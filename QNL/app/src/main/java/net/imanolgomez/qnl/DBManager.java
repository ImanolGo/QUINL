package net.imanolgomez.qnl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by imanolgo on 22/12/14.
 */


public class DBManager {
    public static String TAG = "DBManager";
    public static final String DB_RELATIVE_PATH = "/QNL/Database/";
    public static final String DB_ABSOLUTE_PATH = Environment.getExternalStorageDirectory() + DB_RELATIVE_PATH;

    // Database fields
    private SQLiteDatabase mDatabase;
    private DBHelper mHelper;

    private static DBManager sDBManager;

    private Context mAppContext;


    private DBManager(Context appContext) {
        mAppContext = appContext;
        this.initialize();

    }
    public static DBManager get(Context c) {
        if (sDBManager == null) {
            sDBManager = new DBManager(c.getApplicationContext());
        }
        return sDBManager;
    }

    private void initialize(){
        Log.i(TAG, "Initialize");
        this.createDatabaseFolder();
        mHelper = new DBHelper(mAppContext);
    }

    private void openWriteDB() {
        mDatabase = mHelper.getWritableDatabase();
    }

    private void openReadDB() {
        mDatabase = mHelper.getReadableDatabase();
    }

    private void closeDB() { mDatabase.close(); }

    public boolean insertRoute(Route route)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(mHelper.COLUMN_NAME, route.getName());
        contentValues.put(mHelper.COLUMN_ID, route.getId());
        contentValues.put(mHelper.COLUMN_VERSION, route.getVersion());

        openWriteDB();
        mDatabase.insertWithOnConflict(mHelper.TABLE_ROUTES, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);

        return true;
    }

    public boolean insertSample(Sample sample)
    {
        Log.i(TAG, "insertSample");
        ContentValues contentValues = new ContentValues();

        contentValues.put(mHelper.COLUMN_NAME, sample.getName());
        contentValues.put(mHelper.COLUMN_ID, sample.getId());
        contentValues.put(mHelper.COLUMN_VERSION, sample.getVersion());
        contentValues.put(mHelper.COLUMN_URL, sample.getUrl());

        openWriteDB();
        mDatabase.insertWithOnConflict(mHelper.TABLE_SAMPLES, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);

        return true;
    }

    public boolean insertRegion(Region region)
    {
        Log.i(TAG, "insertRegion");
        ContentValues contentValues = new ContentValues();

        contentValues.put(mHelper.COLUMN_NAME, region.getName());
        contentValues.put(mHelper.COLUMN_ID, region.getId());
        contentValues.put(mHelper.COLUMN_VERSION, region.getVersion());
        contentValues.put(mHelper.COLUMN_SAMPLE_ID, region.getSampleId());
        contentValues.put(mHelper.COLUMN_ROUTE_ID, region.getRouteId());
        contentValues.put(mHelper.COLUMN_VOLUME, region.getVolume());
        contentValues.put(mHelper.COLUMN_REGION_TYPE, region.getRegionTypeString());
        contentValues.put(mHelper.COLUMN_LOOP, region.getLoopInt());

        openWriteDB();
        mDatabase.insertWithOnConflict(mHelper.TABLE_REGIONS, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);

        return true;
    }

    public boolean insertSpot(Spot spot)
    {

        Log.i(TAG, "insertSpot");
        ContentValues contentValues = new ContentValues();

        contentValues.put(mHelper.COLUMN_NAME, spot.getName());
        contentValues.put(mHelper.COLUMN_ID, spot.getId());
        contentValues.put(mHelper.COLUMN_UUID, spot.getUUID());
        contentValues.put(mHelper.COLUMN_VERSION, spot.getVersion());
        contentValues.put(mHelper.COLUMN_SAMPLE_ID, spot.getSampleId());
        contentValues.put(mHelper.COLUMN_ROUTE_ID, spot.getRouteId());
        contentValues.put(mHelper.COLUMN_VOLUME, spot.getVolume());
        contentValues.put(mHelper.COLUMN_RADIUS, spot.getRadius());
        contentValues.put(mHelper.COLUMN_LOOP, spot.getLoopInt());
        contentValues.put(mHelper.COLUMN_LAT, spot.getLocation().getLatitude());
        contentValues.put(mHelper.COLUMN_LON, spot.getLocation().getLongitude());

        openWriteDB();
        mDatabase.insertWithOnConflict(mHelper.TABLE_BEACONS, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);

        return true;
    }

    public boolean insertSection(Section section)
    {
        Log.i(TAG, "insertSection");
        ContentValues contentValues = new ContentValues();

        contentValues.put(mHelper.COLUMN_ID, section.getId());
        contentValues.put(mHelper.COLUMN_REGION_ID, section.getRegionId());
        contentValues.put(mHelper.COLUMN_LAT1, section.getLocation1().getLatitude());
        contentValues.put(mHelper.COLUMN_LON1, section.getLocation1().getLongitude());
        contentValues.put(mHelper.COLUMN_LAT2, section.getLocation2().getLatitude());
        contentValues.put(mHelper.COLUMN_LON2, section.getLocation2().getLongitude());

        openWriteDB();
        mDatabase.insertWithOnConflict(mHelper.TABLE_SECTIONS, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);

        return true;
    }

    public boolean isRouteUpToDate(int id, float version) {

        openReadDB();
        Cursor cursor = mDatabase.query(mHelper.TABLE_ROUTES, // a. table
                        new String[] {mHelper.COLUMN_ID, mHelper.COLUMN_NAME, mHelper.COLUMN_VERSION }, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit


        if(cursor!=null && cursor.getCount()>0){
            cursor.moveToFirst();
            float dbVersion = cursor.getFloat(2);
            //Log.i(TAG,"New Route -> Id = " + id +  ", version = " + dbVersion+  ", name = " + name);
            return dbVersion>=version;

        }else{
            return false;
        }
    }

    public Route getRoute(int id){

        openReadDB();
        Cursor cursor = mDatabase.query(mHelper.TABLE_ROUTES, // a. table
                new String[] {mHelper.COLUMN_ID, mHelper.COLUMN_NAME, mHelper.COLUMN_VERSION }, // b. column names
                " id = ?", // c. selections
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit


        if(cursor!=null && cursor.getCount()>0){


            cursor.moveToFirst();
            String name = cursor.getString(1);
            float version = cursor.getFloat(2);
            BasicElement basicElement = new BasicElement(id,name,version);
            return new Route(basicElement);

        }else{
            return null;
        }

    }

    public boolean isSampleUpToDate(int id, double version) {

        openReadDB();
        Cursor cursor = mDatabase.query(mHelper.TABLE_SAMPLES, // a. table
                new String[] {mHelper.COLUMN_ID, mHelper.COLUMN_NAME, mHelper.COLUMN_VERSION }, // b. column names
                " id = ?", // c. selections
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit


        if(cursor!=null && cursor.getCount()>0){
            cursor.moveToFirst();
            double dbVersion = cursor.getDouble(2);
            //Log.i(TAG,"New Route -> Id = " + id +  ", version = " + dbVersion+  ", name = " + name);
            return dbVersion>=version;

        }else{
            return false;
        }
    }

    public Sample getSample(int id){

        openReadDB();
        Cursor cursor = mDatabase.query(mHelper.TABLE_SAMPLES, // a. table
                new String[] {mHelper.COLUMN_ID, mHelper.COLUMN_NAME, mHelper.COLUMN_VERSION, mHelper.COLUMN_URL }, // b. column names
                " id = ?", // c. selections
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit


        Sample sample = null;

        if(cursor.moveToFirst()){
            String name = cursor.getString(1);
            float version = cursor.getFloat(2);
            String url = cursor.getString(3);
            BasicElement basicElement = new BasicElement(id,name,version);
            sample = new Sample(basicElement);
            sample.setUrl(url);
        }

       return sample;
    }

    public boolean isSpotUpToDate(int id, double version) {

        openReadDB();
        Cursor cursor = mDatabase.query(mHelper.TABLE_BEACONS, // a. table
                new String[] {mHelper.COLUMN_ID, mHelper.COLUMN_NAME, mHelper.COLUMN_VERSION }, // b. column names
                " id = ?", // c. selections
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit


        if(cursor!=null && cursor.getCount()>0){
            cursor.moveToFirst();
            double dbVersion = cursor.getDouble(2);
            //Log.i(TAG,"New Route -> Id = " + id +  ", version = " + dbVersion+  ", name = " + name);
            return dbVersion>=version;

        }else{
            return false;
        }
    }

    public Spot getSpot(int id){

        openReadDB();
        Cursor cursor = mDatabase.query(mHelper.TABLE_BEACONS, // a. table
                new String[] {mHelper.COLUMN_ID, mHelper.COLUMN_NAME, mHelper.COLUMN_UUID, mHelper.COLUMN_VERSION,
                        mHelper.COLUMN_ROUTE_ID, mHelper.COLUMN_SAMPLE_ID, mHelper.COLUMN_LOOP, mHelper.COLUMN_RADIUS,
                        mHelper.COLUMN_LAT, mHelper.COLUMN_LON, mHelper.COLUMN_VOLUME}, // b. column names
                " id = ?", // c. selections
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit


        if(cursor!=null && cursor.getCount()>0){
            cursor.moveToFirst();

            String name = cursor.getString(1);
            String uuid = cursor.getString(2);
            float version = cursor.getFloat(3);
            int routeId = cursor.getInt(4);
            int sampleId = cursor.getInt(5);
            int intBoolean  = cursor.getInt(6);
            boolean loop = intBoolean!=0;
            double radius =  cursor.getDouble(7);
            double lat =  cursor.getDouble(8);
            double lon =  cursor.getDouble(9);
            float volume = cursor.getFloat(10);
            Location location = new Location("");
            location.setLatitude(lat);
            location.setLongitude(lon);

            BasicElement basicElement = new BasicElement(id, name, version);
            Spot spot = new Spot(basicElement, uuid);
            spot.setLoop(loop); spot.setSampleId(sampleId); spot.setVolume(volume); spot.setRouteId(routeId);
            spot.setRadius(radius);spot.setLocation(location);

            return spot;

        }else{
            return null;
        }

    }

    public boolean isRegionUpToDate(int id, double version) {

        openReadDB();
        Cursor cursor = mDatabase.query(mHelper.TABLE_REGIONS, // a. table
                new String[] {mHelper.COLUMN_ID, mHelper.COLUMN_NAME, mHelper.COLUMN_VERSION }, // b. column names
                " id = ?", // c. selections
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit


        if(cursor!=null && cursor.getCount()>0){
            cursor.moveToFirst();
            double dbVersion = cursor.getDouble(2);
            //Log.i(TAG,"New Route -> Id = " + id +  ", version = " + dbVersion+  ", name = " + name);
            return dbVersion>=version;

        }else{
            return false;
        }
    }

    public Region getRegion(int id){

        openReadDB();
        Cursor cursor = mDatabase.query(mHelper.TABLE_REGIONS, // a. table
                new String[] {mHelper.COLUMN_ID, mHelper.COLUMN_NAME, mHelper.COLUMN_REGION_TYPE, mHelper.COLUMN_VERSION,
                mHelper.COLUMN_ROUTE_ID, mHelper.COLUMN_SAMPLE_ID,mHelper.COLUMN_LOOP,mHelper.COLUMN_VOLUME}, // b. column names
                " id = ?", // c. selections
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit


        Region region = null;
        if(cursor.moveToFirst()){
            String name = cursor.getString(1);
            Region.RegionType regionType = Region.getTypeFromString(cursor.getString(2));
            float version = cursor.getFloat(3);
            int routeId = cursor.getInt(4);
            int sampleId = cursor.getInt(5);
            int intBoolean  = cursor.getInt(6);
            boolean loop = intBoolean!=0;
            float volume = cursor.getFloat(7);

            BasicElement basicElement = new BasicElement(id,name,version);
            region = new Region(basicElement,regionType);
            region.setLoop(loop);region.setSampleId(sampleId);region.setVolume(volume); region.setRouteId(routeId);

            region = addSectionsToRegion(region);
        }

        return region;
    }

    public Region getRegion(Location loc) {

        openWriteDB();
        //SELECT id, region_id FROM sections WHERE lat1 <= 51.21096 AND lat2 >= 51.21096 AND lon1 <= 3.2263 AND lon2 >= 3.2263

        int regionId = 0;
        Region region = null;
        String selectQuery = "SELECT " + mHelper.COLUMN_REGION_ID + " FROM " + mHelper.TABLE_SECTIONS + " WHERE " +
                mHelper.COLUMN_LAT1 + " <= " + loc.getLatitude() + " AND " + mHelper.COLUMN_LAT2 + " >= " + loc.getLatitude() +
                mHelper.COLUMN_LON1 + " <= " + loc.getLongitude() + " AND " + mHelper.COLUMN_LON2 + " >= " + loc.getLongitude();
        Cursor cursor = mDatabase.rawQuery(selectQuery, null);
        if(cursor.moveToFirst()){

            do {

                regionId = cursor.getInt(1);
                region = getRegion(regionId);
                if(region!=null && region.getRegionType() == Region.RegionType.ROOM ){ //Found a Room!!
                    return region;
                }

            } while (cursor.moveToNext());

        }

        return region;
    }
    // Delete All Sections from a Specific Region
    public void deleteSectionsFromRegion(int regionId) {
        openWriteDB();
        mDatabase.delete(mHelper.TABLE_SECTIONS, mHelper.COLUMN_REGION_ID + " = ?", new String[] { String.valueOf(regionId) });
    }

    // Getting All Sections from a Specific Region
    public Region addSectionsToRegion(Region region) {

        openReadDB();
        Cursor cursor = mDatabase.query(mHelper.TABLE_SECTIONS, // a. table
                new String[] {mHelper.COLUMN_ID, mHelper.COLUMN_REGION_ID,
                        mHelper.COLUMN_LAT1, mHelper.COLUMN_LAT2,mHelper.COLUMN_LON1,mHelper.COLUMN_LON2}, // b. column names
                " region_id = ?", // c. selections
                new String[] { String.valueOf(region.getId()) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit


        if(cursor.moveToFirst()){

            do {

                int id = cursor.getInt(0);
                double lat1 = cursor.getDouble(2);
                double lat2 = cursor.getDouble(3);
                double lon1 = cursor.getDouble(4);
                double lon2 = cursor.getDouble(5);

                Location location1 = new Location("");
                location1.setLatitude(lat1);
                location1.setLongitude(lon1);

                Location location2 = new Location("");
                location2.setLatitude(lat2);
                location2.setLongitude(lon2);

                Section section = new Section(id,location1,location2, region.getId());
                region.addSection(section);

            } while (cursor.moveToNext());

        }

        return region;
    }

    public boolean registerDevice()
    {
        Log.i(TAG, "registerDevice");
        ContentValues contentValues = new ContentValues();
        DeviceInfoManager deviceInfoManager = DeviceInfoManager.get(mAppContext);

        contentValues.put(mHelper.COLUMN_ID, deviceInfoManager.getDeviceId());
        contentValues.put(mHelper.COLUMN_NAME, deviceInfoManager.getDeviceName());
        contentValues.put(mHelper.COLUMN_PHONE_UUID, deviceInfoManager.getDeviceUuid());
        contentValues.put(mHelper.COLUMN_MAKE, deviceInfoManager.getDeviceManufacturer());
        contentValues.put(mHelper.COLUMN_MODEL, deviceInfoManager.getDeviceModel());
        contentValues.put(mHelper.COLUMN_SERIAL, deviceInfoManager.getSerial());
        contentValues.put(mHelper.COLUMN_IMEI, deviceInfoManager.getImei());
        contentValues.put(mHelper.COLUMN_MAC, deviceInfoManager.getMacAddress());

        openWriteDB();
        mDatabase.insertWithOnConflict(mHelper.TABLE_PHONE, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);

        return true;
    }

    public boolean isDeviceRegistered() {

        if(getDeviceId()==-1){
            return false;
        }

        return true;
    }

    public int getDeviceId() {

        openReadDB();

        Cursor cursor = mDatabase.query(mHelper.TABLE_PHONE, // Only return column ID
                new String[] {mHelper.COLUMN_ID },
                null, null, null, null, null);


        if(cursor!=null && cursor.getCount()>0){
            cursor.moveToFirst();
            Log.d(TAG, "getDeviceId: " + cursor.getInt(0));
            return cursor.getInt(0);
        }else{
            return -1;
        }
    }

    public String getDeviceName() {

        openReadDB();

        Cursor cursor = mDatabase.query(mHelper.TABLE_PHONE, // Only return column ID
                new String[] {mHelper.COLUMN_NAME },
                null, null, null, null, null);


        if(cursor!=null && cursor.getCount()>0){
            cursor.moveToFirst();
            Log.d(TAG, "getDeviceName: " + cursor.getString(0));
            return cursor.getString(0);
        }else{
            return "QNL_Device_";
        }
    }


    private void createDatabaseFolder(){
        File folder = new File(DB_ABSOLUTE_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

}
