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
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 60000;

    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private final static String BROADCASTER_ACTION= "jp.co.drecom.newmapintegration.location";


    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private LocationDBHelper mLocationDBHelper;
    private SQLiteDatabase mLocationDB;

    private Location mCurrentLocation;
    private double mCurrentLatitude;
    private double mCurrentLongitude;

    private String mCurrentTime;

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
        mLocationDB = mLocationDBHelper.getWritableDatabase();
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
        mLocationDB.close();
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
        mCurrentLocation = location;
        mCurrentLatitude = location.getLatitude();
        mCurrentLongitude = location.getLongitude();
        mLocationIntent.putExtra("Latitude", mCurrentLatitude);
        mLocationIntent.putExtra("Longitude", mCurrentLongitude);
        mCurrentTime = DateFormat.getDateTimeInstance().format(new Date());
        //TODO

        rowID = saveDataToDB(mCurrentTime, mCurrentLatitude, mCurrentLongitude);
        NewLog.logD("DB inserted, row ID is " + rowID);

        sendBroadcast(mLocationIntent);
        NewLog.logD("service location changed" + mCurrentLocation);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        NewLog.logD("onConnectionFailed");
    }

    private long saveDataToDB(String time, double latitude, double longitude) {
        ContentValues values = new ContentValues();
        values.put(mLocationDBHelper.MY_LOCATION_TIME, time);
        values.put(mLocationDBHelper.MY_LOCATION_LATITUDE, latitude);
        values.put(mLocationDBHelper.MY_LOCATION_LONGITUDE, longitude);
        return mLocationDB.insert(mLocationDBHelper.MY_LOCATION_TABLE_NAME,
                null, values);
    }

}
