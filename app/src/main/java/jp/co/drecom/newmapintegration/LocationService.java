package jp.co.drecom.newmapintegration;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

import jp.co.drecom.newmapintegration.utils.LocationDBHelper;
import jp.co.drecom.newmapintegration.utils.NewLog;

//mouse points to class name, then press control + enter
public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //foreground mode 30 second update
    //background mode: 60 second update
    private static long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;

    private static final long UPDATE_INTERVAL_STEP = 30000;

    private static long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private final static String BROADCASTER_ACTION= "jp.co.drecom.newmapintegration.location";


    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private LocationDBHelper mLocationDBHelper;


    private Location mCurrentLocation;
    private double mCurrentLatitude;
    private double mCurrentLongitude;
    //if |mLastLatitude - mCurrentLatitude| < 0.000005 &&
    //   |mLastLongitude - mCurrentLongitude| < 0.000005
    //then do not write the current location to DB.
    private double mLastLatitude;
    private double mLastLongitude;

    private String mCurrentTime;
    private long mCurrentUnixTime;

    private Boolean mWhetherLocationUpdate;

    //for test
    private long rowID;

    private Intent mLocationIntent = new Intent(BROADCASTER_ACTION);



    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWhetherLocationUpdate = true;
        mLastLatitude = 0;
        mLastLongitude = 0;


        NewLog.logD("service onCreate");


    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private synchronized void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private synchronized void initLocationDB() {
        mLocationDBHelper = new LocationDBHelper(getBaseContext());
        mLocationDBHelper.mLocationDB = mLocationDBHelper.getWritableDatabase();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NewLog.logD("service onStartCommand");

        buildGoogleApiClient();
        createLocationRequest();
        initLocationDB();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationDBHelper.mLocationDB.close();
        NewLog.logD("service onDestroy");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(Bundle bundle) {
        NewLog.logD("service googleApiClient connected");
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void removeLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        NewLog.logD("service googleApiClient suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        //TODO
        //add current time
        mLastLatitude = mCurrentLatitude;
        mLastLongitude = mCurrentLongitude;

        mCurrentLocation = location;
        mCurrentLatitude = location.getLatitude();
        mCurrentLongitude = location.getLongitude();
        mLocationIntent.putExtra("Latitude", mCurrentLatitude);
        mLocationIntent.putExtra("Longitude", mCurrentLongitude);
        mCurrentTime = DateFormat.getDateTimeInstance().format(new Date());
        mCurrentUnixTime = (System.currentTimeMillis() / 1000L);



        //TODO

        if (whetherNeedUpdateLocation()) {
            rowID = mLocationDBHelper
                    .saveDataToDB(mCurrentLatitude, mCurrentLongitude, mCurrentUnixTime);
            sendBroadcast(mLocationIntent);
            NewLog.logD("DB inserted, row ID is " + rowID);
        }

        NewLog.logD("the current time is " + mCurrentTime);
        NewLog.logD("the current unix time is " + mCurrentUnixTime);
        NewLog.logD("service location changed" + mCurrentLocation);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        NewLog.logD("onConnectionFailed");
    }



    private boolean whetherNeedUpdateLocation() {
        //the distance of two points (35.631260n 139.712820w), (35.631269n 139.712829w) is 1.3m
        if (Math.abs(mLastLatitude - mCurrentLatitude) < 0.000009 &&
                Math.abs(mLastLongitude - mCurrentLongitude) < 0.000009) {
            //self-adaption
//            UPDATE_INTERVAL_IN_MILLISECONDS += UPDATE_INTERVAL_STEP;
//            NewLog.logD("update time interval is " + UPDATE_INTERVAL_IN_MILLISECONDS);
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
//            LocationServices.FusedLocationApi.requestLocationUpdates(
//                    mGoogleApiClient, mLocationRequest, this);
            return false;

        }
        return true;
    }

}
