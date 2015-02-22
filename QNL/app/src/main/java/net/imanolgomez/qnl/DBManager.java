package net.imanolgomez.qnl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;

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

    public boolean isRouteInDB(int id) {

        openReadDB();
        Cursor cursor = mDatabase.query(mHelper.TABLE_REGIONS,  null, // All columns
                mHelper.COLUMN_ID + " = ?", // limit to the given id
                new String[]{ String.valueOf(id) },
                null, // group by
                null, // order by
                null, // having
                "1"); // limit 1 row

        if(cursor!=null && cursor.getCount()>0){
            return true;
        }else{
            return false;
        }
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
            Log.i(TAG, "getDeviceId: " + cursor.getInt(0));
            return cursor.getInt(0);
        }else{
            return -1;
        }
    }


    private void createDatabaseFolder(){
        File folder = new File(DB_ABSOLUTE_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

}
