package jp.co.drecom.newmapintegration.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Statement;

/**
 * Created by huang_liangjin on 2015/03/19.
 * all operations about the DB
 * Maybe
 */
public class LocationDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NewMapIntegration.db";
    //***********************MyLocationTable*****************************//
    public static final String MY_LOCATION_TABLE_NAME = "myLocationTable";
    public static final String MY_LOCATION_ID = "id";
    public static final String MY_LOCATION_TIME = "current_time";
    public static final String MY_LOCATION_LATITUDE = "current_latitude";
    public static final String MY_LOCATION_LONGITUDE = "current_longitude";
    public static final String CREATE_MY_LOCATION_TABLE =
            "create table " + MY_LOCATION_TABLE_NAME
                    + "(" + MY_LOCATION_ID + " integer primary key autoincrement, " +
                    MY_LOCATION_TIME + " text, " +
                    MY_LOCATION_LATITUDE + " real, " +
                    MY_LOCATION_LONGITUDE + " real)";
    public static final String DELETE_MY_LOCATION_TABLE = "drop table if exists "
            + MY_LOCATION_TABLE_NAME;
    //***********************MyLocationTable*****************************//

    public LocationDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MY_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_MY_LOCATION_TABLE);
        onCreate(db);
    }
}
