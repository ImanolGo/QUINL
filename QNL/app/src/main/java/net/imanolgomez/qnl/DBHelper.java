package net.imanolgomez.qnl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by imanolgo on 22/12/14.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String TABLE_ROUTES = "routes";
    private static final String COLUMN_ROUTE_ID = "route_id";
    private static final String COLUMN_ROUTE_NAME = "route_name";
    private static final String COLUMN_ROUTE_VERSION= "route_version";

    private static final String TABLE_SECTIONS = "sections";
    private static final String COLUMN_SECTION_ID = "section_id";
    private static final String COLUMN_SECTION_LAT1 = "section_lat1";
    private static final String COLUMN_SECTION_LAT2 = "section_lat2";
    private static final String COLUMN_SECTION_LON1 = "section_lon1";
    private static final String COLUMN_SECTION_LON2 = "section_lon2";

    private static final String TABLE_REGIONS = "regions";
    private static final String COLUMN_REGION_ID = "region_id";
    private static final String COLUMN_REGION_NAME = "region_name";
    private static final String COLUMN_REGION_VERSION= "region_version";
    private static final String COLUMN_REGION_LOOP= "region_loop";
    private static final String COLUMN_REGION_VOLUME= "region_volume";
    private static final String COLUMN_REGION_TYPE= "region_type";

    private static final String TABLE_SAMPLES = "samples";
    private static final String COLUMN_SAMPLE_ID= "sample_id";
    private static final String COLUMN_SAMPLE_VERSION= "sample_version";
    private static final String COLUMN_SAMPLE_NAME= "sample_name";

    private static final String TABLE_BEACONS = "beacons";
    private static final String COLUMN_BEACON_ID = "beacon_id";
    private static final String COLUMN_BEACON_NAME = "beacon_name";
    private static final String COLUMN_BEACON_VERSION= "beacon_version";
    private static final String COLUMN_BEACON_LOOP= "beacon_loop";
    private static final String COLUMN_BEACON_VOLUME= "beacon_volume";
    private static final String COLUMN_BEACON_RADIUS= "beacon_radius";


    private static final String DATABASE_NAME = "qnl.db";
    private static final int DATABASE_VERSION = 1;

    // Regions table creation sql statement
    private static final String CREATE_REGIONS_TABLE = "create table "
            + TABLE_REGIONS + "("
            + COLUMN_REGION_ID + " integer primary key, "
            + COLUMN_REGION_NAME + " text, "
            + COLUMN_REGION_TYPE + " text, "
            + COLUMN_REGION_VERSION + " real, "
            + COLUMN_ROUTE_ID + " integer references run(" + COLUMN_ROUTE_ID + ")"
            + COLUMN_SAMPLE_ID + " integer references run(" + COLUMN_SAMPLE_ID + ")"
            + COLUMN_REGION_LOOP + " integer, "
            + COLUMN_REGION_VOLUME + " real);";

    // Routes table creation sql statement
    private static final String CREATE_ROUTES_TABLE = "create table "
            + TABLE_ROUTES + "("
            + COLUMN_ROUTE_ID + " integer primary key, "
            + COLUMN_ROUTE_NAME + " text, "
            + COLUMN_ROUTE_VERSION + " real);";

    // Sections table creation sql statement
    private static final String CREATE_SECTIONS_TABLE = "create table "
            + TABLE_SECTIONS + "("
            + COLUMN_SECTION_ID + " integer primary key, "
            + COLUMN_SECTION_LAT1 + " real, "
            + COLUMN_SECTION_LAT2 + " real, "
            + COLUMN_SECTION_LON1 + " real, "
            + COLUMN_SECTION_LON2 + " real);";

    // Beacons table creation sql statement
    private static final String CREATE_BEACONS_TABLE = "create table "
            + TABLE_BEACONS + "("
            + COLUMN_BEACON_ID + " integer primary key, "
            + COLUMN_BEACON_NAME + " text, "
            + COLUMN_BEACON_VERSION + " real, "
            + COLUMN_BEACON_ID + " integer references run(" + COLUMN_ROUTE_ID + ")"
            + COLUMN_SAMPLE_ID + " integer references run(" + COLUMN_SAMPLE_ID + ")"
            + COLUMN_BEACON_RADIUS + " real, "
            + COLUMN_BEACON_LOOP + " integer, "
            + COLUMN_BEACON_VOLUME + " real);";

    // Samples table creation sql statement
    private static final String CREATE_SAMPLES_TABLE = "create table "
            + TABLE_SAMPLES + "("
            + COLUMN_SAMPLE_ID + " integer primary key, "
            + COLUMN_SAMPLE_NAME + " text, "
            + COLUMN_SAMPLE_VERSION + " real);";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_ROUTES_TABLE);
        database.execSQL(CREATE_SAMPLES_TABLE);
        database.execSQL(CREATE_REGIONS_TABLE);
        database.execSQL(CREATE_BEACONS_TABLE);
        database.execSQL(CREATE_SECTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Implement schema changes and data massage here when upgrading

        Log.w(DBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        /*db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAMPLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BEACONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECTIONS);*/
        onCreate(db);
    }

}