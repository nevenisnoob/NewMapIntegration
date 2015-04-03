package jp.co.drecom.newmapintegration.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.wearable.DataEvent;

import java.sql.Statement;

import jp.co.drecom.newmapintegration.AppController;

/**
 * Created by huang_liangjin on 2015/03/19.
 * all operations about the DB
 * Maybe
 */
public class LocationDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final int DATABASE_VERSION_MARCH = 2;
    public static final int DATABASE_VERSION_APRIL = 3;
    public static final int DATABASE_VERSION_FOURTH = 4;
    public static final int DATABASE_VERSION_FIFTH = 5;
    public static final int DATABASE_VERSION_SIXTH = 6;
    public static final int DATABASE_VERSION_SEVENTH = 7;
    public static final int DATABASE_VERSION_EIGHTH = 8;
    public static final int DATABASE_VERSION_NINTH = 9;
    public static final int DATABASE_VERSION_TENTH = 10;
    public static final int DATABASE_VERSION_ELEVENTH = 11;

    public static final String DATABASE_NAME = "NewMapIntegration.db";
    //***********************MyLocationTable*****************************//
    public static final String MY_LOCATION_TABLE_NAME = "myLocationTable";
    public static final String MY_LOCATION_ID = "id";
    public static final String MY_LOCATION_TIME = "current_localtime";
    public static final String MY_LOCATION_LATITUDE = "current_latitude";
    public static final String MY_LOCATION_LONGITUDE = "current_longitude";
    public static final String CREATE_MY_LOCATION_TABLE =
            "create table " + MY_LOCATION_TABLE_NAME
                    + "(" + MY_LOCATION_ID + " integer primary key autoincrement, " +
                    MY_LOCATION_LATITUDE + " real, " +
                    MY_LOCATION_LONGITUDE + " real, " +
                    MY_LOCATION_TIME + " integer) ";
    public static final String DELETE_MY_LOCATION_TABLE = "drop table if exists "
            + MY_LOCATION_TABLE_NAME;
    private static String SELECT_LOCATION_DATA = "select "
            + MY_LOCATION_LATITUDE + ", "
            + MY_LOCATION_LONGITUDE + ", "
            + MY_LOCATION_TIME + " from "
            + MY_LOCATION_TABLE_NAME + " where "
            + MY_LOCATION_TIME + " between " + " ? and ?";

    //***********************MyLocationTable*****************************//

    //***********************MyAccountTable*****************************//
    public static final String MY_ACCOUNT_TABLE_NAME = "myAccountTable";
    public static final String MY_ACCOUNT_ID = "account_id";
    public static final String MY_ACCOUNT_MAIL = "account_mail";
    public static final String CREATE_MY_ACCOUNT_TABLE =
            "create table " + MY_ACCOUNT_TABLE_NAME
            + "(" + MY_ACCOUNT_ID + " integer, "
            + MY_ACCOUNT_MAIL + " text) ";
    public static final String DELETE_MY_ACCOUNT_TABLE = "drop table if exists "
            + MY_ACCOUNT_TABLE_NAME;
//    private static String UPDATE_USER_ID = "update " + MY_ACCOUNT_TABLE_NAME
//            + " set " + MY_ACCOUNT_ID + " = ? where " + MY_ACCOUNT_MAIL + " = " +
    //***********************MyAccountTable*****************************//

    //***********************myFriendTable*****************************//
    public static final String MY_FRIEND_TABLE_NAME = "myFriendTable";
    //change friend_id to _id due to the cursorAdapter
//    public static final String MY_FRIEND_ID = "friend_id";
    public static final String MY_FRIEND_ID = "_id";
    public static final String MY_FRIEND_SELECTED = "friend_selected";
    public static final String MY_FRIEND_MAIL = "friend_mail"; //must
    public static final String MY_FRIEND_NAME = "friend_name"; //could be null
    public static final String CREATE_MY_FRIEND_TABLE =
            "create table " + MY_FRIEND_TABLE_NAME
            + "(" + MY_FRIEND_ID + " integer primary key autoincrement, "
            + MY_FRIEND_SELECTED + " integer, "
            + MY_FRIEND_MAIL + " text, "
            + MY_FRIEND_NAME + " text) ";
    public static final String DELETE_MY_FRIEND_TABLE = "drop table if exists "
            + MY_FRIEND_TABLE_NAME;
    private static String SELECT_FRIEND_DATA = "select  "
            + MY_FRIEND_ID + ", "
            + MY_FRIEND_SELECTED + ", "
            + MY_FRIEND_MAIL + " from "
            + MY_FRIEND_TABLE_NAME;
    private static String SELECT_FRIEND_MAIL_DATA = "select "
            + MY_FRIEND_MAIL + " from "
            + MY_FRIEND_TABLE_NAME + " where "
            + MY_FRIEND_SELECTED + " == 1";
    //***********************MyFriendTable*****************************//

    //***********************MyMapSettingTable*****************************//
    public static final String MY_SETTING_TABLE_NAME = "mySettingTable";
    public static final String MY_SETTING_ID = "setting_id";
    public static final String MY_SETTING_KEY = "setting_key";
    public static final String MY_SETTING_VALUE = "setting_value";
    public static final String CREATE_MY_SETTING_TABLE =
            "create table " + MY_SETTING_TABLE_NAME
            + "(" + MY_SETTING_ID + " integer primary key autoincrement, "
            + MY_SETTING_KEY + " text, "
            + MY_SETTING_VALUE + " text)";
    public static final String INSERT_DEFAULT_SETTING_VALUE =
            "insert into " + MY_SETTING_TABLE_NAME
            + "(" + MY_SETTING_KEY + " , " + MY_SETTING_VALUE + ")"
            + "values ('key_show_footprint','1'), ('key_location_share','1'), ('key_update_interval','2')";
    public static final String DELETE_MY_SETTING_TABLE = "drop table if exists"
            + MY_SETTING_TABLE_NAME;
    public static final String GET_MY_SETTING_DATA = "select "
            + MY_SETTING_VALUE + " from "
            + MY_SETTING_TABLE_NAME + " where "
            + MY_SETTING_KEY + " = ?";
    public static final String MY_SETTING_KEY_FOOTPRINT = "key_show_footprint";
    public static final String MY_SETTING_KEY_SHARE = "key_location_share";
    public static final String MY_SETTING_KEY_INTERVAL = "key_update_interval";
    //***********************MyMapSettingTable*****************************//




    public SQLiteDatabase mLocationDB;

    public LocationDBHelper(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION_MARCH);
        super(context, DATABASE_NAME, null, DATABASE_VERSION_ELEVENTH);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MY_LOCATION_TABLE);
        db.execSQL(CREATE_MY_ACCOUNT_TABLE);
        onUpgrade(db, DATABASE_VERSION, DATABASE_VERSION_ELEVENTH);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL(DELETE_MY_LOCATION_TABLE);
//        db.execSQL(DELETE_MY_ACCOUNT_TABLE);
//        db.execSQL(DELETE_MY_FRIEND_TABLE);
//        onCreate(db);
        //for adding the group
        if (newVersion == DATABASE_VERSION_APRIL) {
            db.execSQL(CREATE_MY_FRIEND_TABLE);
        }
        if (newVersion >= DATABASE_VERSION_NINTH) {
//            db.execSQL(DELETE_MY_FRIEND_TABLE);
            db.execSQL(CREATE_MY_FRIEND_TABLE);
        }
        if (newVersion >= DATABASE_VERSION_TENTH) {
            db.execSQL(CREATE_MY_SETTING_TABLE);

        }
        if (newVersion >= DATABASE_VERSION_ELEVENTH) {
            db.execSQL(INSERT_DEFAULT_SETTING_VALUE);
        }

    }

    public long saveLocationDataToDB(double latitude, double longitude,long time) {
        ContentValues values = new ContentValues();

        values.put(MY_LOCATION_LATITUDE, latitude);
        values.put(MY_LOCATION_LONGITUDE, longitude);
        values.put(MY_LOCATION_TIME, time);
        return mLocationDB.insert(MY_LOCATION_TABLE_NAME,
                null, values);
    }

    public Cursor getLocationLog(long starTime, long endTime) {
        NewLog.logD(String.valueOf(starTime));
        NewLog.logD(String.valueOf(endTime));
        // where location_time is between
        return mLocationDB.rawQuery(SELECT_LOCATION_DATA,
                new String[] {String.valueOf(starTime), String.valueOf(endTime)});

//        return mLocationDB.rawQuery("select current_latitude, current_longitude, current_localtime from myLocationTable where id between 1 and 5", null);

    }

    public long saveAccountMailToDB (int userID, String userMail) {
        ContentValues values = new ContentValues();
        values.put(MY_ACCOUNT_ID, userID);
        values.put(MY_ACCOUNT_MAIL, userMail);
        AppController.USER_MAIL = userMail;
        return mLocationDB.insert(MY_ACCOUNT_TABLE_NAME, null, values);
    }

    public int updateAccountID (int userID, String userMail) {
        ContentValues values = new ContentValues();
        values.put(MY_ACCOUNT_ID, userID);
        String selection = MY_ACCOUNT_MAIL + " = ?";
        String[] selectionArgs = {userMail};
        return mLocationDB.update(MY_ACCOUNT_TABLE_NAME,
                values, selection, selectionArgs);

        //return the number of rows affected
    }

    public String getUserAccount () {
        String[] columns = {MY_ACCOUNT_MAIL};
        Cursor cursor = mLocationDB.query(
                MY_ACCOUNT_TABLE_NAME,
                columns,
                null,null,null,null,null);
        boolean isEof = cursor.moveToFirst();
        if (isEof) {
            String userMail = cursor.getString(0);
            NewLog.logD("login user mail is " + userMail);
            cursor.close();
            return userMail;
        }
        return null;

    }

    public Cursor getFriendList() {
        return mLocationDB.rawQuery(SELECT_FRIEND_DATA, null);
    }

    public String getFriendMailList() {
        String friendMailList = "(";
        Cursor cursor = mLocationDB.rawQuery(SELECT_FRIEND_MAIL_DATA, null);
        boolean isEof = cursor.moveToFirst();
        //isEof == false when cursor is null
        if (!isEof) {
            NewLog.logD("friendMailList is null");
            return null;
        }
        while (isEof) {
            friendMailList += ("'" + cursor.getString(0) + "',");
            isEof = cursor.moveToNext();
        }
        friendMailList = friendMailList.substring(0, friendMailList.length()-1);
        friendMailList += ")";
        NewLog.logD("FriendMailList is " + friendMailList);
        cursor.close();
        return friendMailList;
    }

    public long saveFriendInfo(String friendMail, String friendName) {
        ContentValues values = new ContentValues();
        //default: selected
        values.put(MY_FRIEND_SELECTED, 1);
        values.put(MY_FRIEND_MAIL, friendMail);
        values.put(MY_FRIEND_NAME, friendName);
        return mLocationDB.insert(MY_FRIEND_TABLE_NAME, null, values);
    }

    public int updateFriendSelected(int selected, String mail) {
        //todo
        ContentValues values = new ContentValues();
        values.put(MY_FRIEND_SELECTED, selected);
        String selection = MY_FRIEND_MAIL + " = ?";
        String[] selectionArgs = {mail};
        return mLocationDB.update(MY_FRIEND_TABLE_NAME,
                values, selection, selectionArgs);
    }

    public int deleteFriend(int friendId) {
        String selection = MY_FRIEND_ID + " = ? ";
        String[] selectionArgs = { String.valueOf(friendId) };
        return mLocationDB.delete(MY_FRIEND_TABLE_NAME, selection, selectionArgs);
    }

    public void updateSettingInfo(SQLiteDatabase db, String footprint, String shareLocation, String updateInterval) {
        //TODO

        db.execSQL("update mySettingTable set setting_value = '"
                + footprint + "' where setting_key = '"+ MY_SETTING_KEY_FOOTPRINT + "'");
        db.execSQL("update mySettingTable set setting_value = '"
                + shareLocation + "' where setting_key = '"+ MY_SETTING_KEY_SHARE + "'");
        db.execSQL("update mySettingTable set setting_value = '"
                + updateInterval + "' where setting_key = '"+ MY_SETTING_KEY_INTERVAL + "'");
    }

    public String getMySettingDataFootprint() {
        Cursor cursor =  mLocationDB.rawQuery(GET_MY_SETTING_DATA, new String[] {MY_SETTING_KEY_FOOTPRINT});
        String str = "0";
        boolean isEof = cursor.moveToFirst();
        if (isEof) {
            str = cursor.getString(0);
            NewLog.logD("footprint is " + str);
        }
        cursor.close();
        return str;

    }
    public String getMySettingDataShareLocation() {
        Cursor cursor =  mLocationDB.rawQuery(GET_MY_SETTING_DATA, new String[] {MY_SETTING_KEY_SHARE});
        String str = "0";
        boolean isEof = cursor.moveToFirst();
        if (isEof) {
            str = cursor.getString(0);
            NewLog.logD("shareLocation is " + str);
        }
        cursor.close();
        return str;
    }
    public String getMySettingDataInterval() {
        Cursor cursor =  mLocationDB.rawQuery(GET_MY_SETTING_DATA, new String[] {MY_SETTING_KEY_INTERVAL});
        String str = "1";
        boolean isEof = cursor.moveToFirst();
        if (isEof) {
            str = cursor.getString(0);
            NewLog.logD("DataInterval is " + str);
        }
        cursor.close();
        return str;
    }


}
