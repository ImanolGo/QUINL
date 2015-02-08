package net.imanolgomez.qnl_androidlocation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

/**
 * Created by imanolgo on 22/12/14.
 */

public class DBHelper extends SQLiteOpenHelper {

    // Logcat tag
    public static String TAG = "DBHelper";

    // Database
    private static final String DATABASE_NAME = "qnl.db";
    private static final int DATABASE_VERSION = 1;

    // table names
    public static final String TABLE_ROUTES = "routes";
    public static final String TABLE_SECTIONS = "sections";
    public static final String TABLE_REGIONS = "regions";
    public static final String TABLE_SAMPLES = "samples";
    public static final String TABLE_BEACONS = "beacons";

    // Common column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_VERSION = "version";
    public static final String COLUMN_NAME = "name";

    // Shared column ID names
    public static final String COLUMN_ROUTE_ID = "route_id";
    public static final String COLUMN_REGION_ID = "region_id";
    public static final String COLUMN_SAMPLE_ID= "sample_id";

    // Coordinates column names
    public static final String COLUMN_LAT1 = "lat1";
    public static final String COLUMN_LAT2 = "lat2";
    public static final String COLUMN_LON1 = "lon1";
    public static final String COLUMN_LON2 = "lon2";

    // Region column names
    public static final String COLUMN_LOOP= "loop";
    public static final String COLUMN_VOLUME= "volume";
    public static final String COLUMN_REGION_TYPE= "region_type";

    // Spot column names
    public static final String COLUMN_UUID =  "uuid";
    public static final String COLUMN_RADIUS= "radius";
    public static final String COLUMN_LAT =  "latitude";
    public static final String COLUMN_LON =  "longitude";


    // Regions table creation sql statement
    private static final String CREATE_REGIONS_TABLE = "create table "
            + TABLE_REGIONS + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_NAME + " text, "
            + COLUMN_REGION_TYPE + " text, "
            + COLUMN_VERSION + " real, "
            + COLUMN_ROUTE_ID + " integer, "
            + COLUMN_SAMPLE_ID + " integer, "
            + COLUMN_LOOP + " integer, "
            + COLUMN_VOLUME + " real);";

    // Routes table creation sql statement
    private static final String CREATE_ROUTES_TABLE = "create table "
            + TABLE_ROUTES + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_NAME + " text, "
            + COLUMN_VERSION + " real);";

    // Sections table creation sql statement
    private static final String CREATE_SECTIONS_TABLE = "create table "
            + TABLE_SECTIONS + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_REGION_ID + " integer, "
            + COLUMN_LAT1 + " real, "
            + COLUMN_LAT2 + " real, "
            + COLUMN_LON1 + " real, "
            + COLUMN_LON2 + " real);";

    // Beacons table creation sql statement
    private static final String CREATE_BEACONS_TABLE = "create table "
            + TABLE_BEACONS + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_NAME + " text, "
            + COLUMN_UUID + " text, "
            + COLUMN_VERSION + " real, "
            + COLUMN_ROUTE_ID + " integer, "
            + COLUMN_SAMPLE_ID + " integer, "
            + COLUMN_LOOP + " integer, "
            + COLUMN_RADIUS + " real, "
            + COLUMN_LAT + " real, "
            + COLUMN_LON + " real, "
            + COLUMN_VOLUME + " real);";

    // Samples table creation sql statement
    private static final String CREATE_SAMPLES_TABLE = "create table "
            + TABLE_SAMPLES + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_NAME + " text, "
            + COLUMN_VERSION + " real);";


    public DBHelper(Context context) {

        super(context, DBManager.DB_ABSOLUTE_PATH + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.i(TAG, "CREATE_ROUTES_TABLE");
        database.execSQL(CREATE_ROUTES_TABLE);
        Log.i(TAG, "CREATE_SAMPLES_TABLE");
        database.execSQL(CREATE_SAMPLES_TABLE);
        Log.i(TAG, "CREATE_REGIONS_TABLE");
        database.execSQL(CREATE_REGIONS_TABLE);
        Log.i(TAG, "CREATE_BEACONS_TABLE");
        database.execSQL(CREATE_BEACONS_TABLE);
        Log.i(TAG, "CREATE_SECTIONS_TABLE");
        database.execSQL(CREATE_SECTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Implement schema changes and data massage here when upgrading

        Log.w(DBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAMPLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BEACONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECTIONS);
        onCreate(db);
    }

}