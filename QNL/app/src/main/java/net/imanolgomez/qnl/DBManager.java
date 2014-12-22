package net.imanolgomez.qnl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.sql.SQLException;

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
        mDatabase = mHelper.getWritableDatabase();
    }

    public boolean insertRoute(Route route)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(mHelper.COLUMN_ROUTE_NAME, route.getName());
        contentValues.put(mHelper.COLUMN_ROUTE_ID, route.getId());
        contentValues.put(mHelper.COLUMN_ROUTE_VERSION, route.getVersion());

        mDatabase.insertWithOnConflict(mHelper.TABLE_ROUTES, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        return true;
    }

    public boolean insertSample(Sample sample)
    {
        Log.i(TAG, "insertSample");
        ContentValues contentValues = new ContentValues();

        contentValues.put(mHelper.COLUMN_SAMPLE_NAME, sample.getName());
        contentValues.put(mHelper.COLUMN_SAMPLE_ID, sample.getId());
        contentValues.put(mHelper.COLUMN_SAMPLE_VERSION, sample.getVersion());

        mDatabase.insertWithOnConflict(mHelper.TABLE_SAMPLES, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);
        return true;
    }

    private void createDatabaseFolder(){
        File folder = new File(DB_ABSOLUTE_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

}
