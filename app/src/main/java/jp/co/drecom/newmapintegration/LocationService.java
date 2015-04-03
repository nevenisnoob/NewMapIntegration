package jp.co.drecom.newmapintegration;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.co.drecom.newmapintegration.utils.LocationDBHelper;
import jp.co.drecom.newmapintegration.utils.NewLog;


//mouse points to class name, then press control + enter
public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //foreground mode 30 second update
    //background mode: 60 second update




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

    private Intent mLocationIntent = new Intent(AppController.BROADCAST_SELF_ACTION);

    private String mFriendMailList;

    private WifiStatusUserSettingChangedReceiver mWifiReceiver;



    public LocationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWhetherLocationUpdate = true;
        mLastLatitude = 0;
        mLastLongitude = 0;

        mWifiReceiver = new WifiStatusUserSettingChangedReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentfilter.addAction(AppController.BROADCAST_UPDATE_INTERVAL);
        this.registerReceiver(mWifiReceiver, intentfilter);

//        updateOnlineStatusToServer(AppController.USER_MAIL, String.valueOf(1));

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
        mLocationRequest.setInterval(AppController.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(AppController.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //100
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); //102
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
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

        mLocationDBHelper.mLocationDB.close();
        NewLog.logD("service onDestroy");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        if (mWifiReceiver != null) {
            this.unregisterReceiver(mWifiReceiver);
        }
        super.onDestroy();


    }

    @Override
    public IBinder onBind(Intent intent) {
        //Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(Bundle bundle) {
        NewLog.logD("service googleApiClient connected");
        startLocationUpdates();
        //send the online status to server

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

        mFriendMailList = mLocationDBHelper.getFriendMailList();

        //TODO?
        //always send and receive data to/from server every 30 seconds.
        if (true) {
//            updateLocationDataToServer(AppController.USER_MAIL,
//                    String.valueOf(mCurrentLatitude),
//                    String.valueOf(mCurrentLongitude),
//                    String.valueOf(mCurrentUnixTime));

            updateLocationDataToServer(AppController.USER_MAIL,
                    mFriendMailList,
                    String.valueOf(mCurrentLatitude),
                    String.valueOf(mCurrentLongitude),
                    String.valueOf(mCurrentUnixTime));
        }

        if (whetherNeedUpdateLocation()) {
            rowID = mLocationDBHelper
                    .saveLocationDataToDB(mCurrentLatitude, mCurrentLongitude, mCurrentUnixTime);
            //send data to server

            sendBroadcast(mLocationIntent);
            NewLog.logD("DB inserted, row ID is " + rowID);
        }


        NewLog.logD("the current time is " + mCurrentTime);
        NewLog.logD("the current unix time is " + mCurrentUnixTime);
        NewLog.logD("service location changed" + mCurrentLocation);
        NewLog.logD("the current accuracy is " + mLocationRequest.getPriority());

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        NewLog.logD("onConnectionFailed");
    }



    private boolean whetherNeedUpdateLocation() {
        //the distance of two points (35.631260n 139.712820w), (35.631269n 139.712829w) is 1.3m
        if (Math.abs(mLastLatitude - mCurrentLatitude) < AppController.NORMAL_TOLERANCE &&
                Math.abs(mLastLongitude - mCurrentLongitude) < AppController.NORMAL_TOLERANCE) {
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

    //send the JSONArray data which got from server to broadcast receiver with different action.
    private void updateLocationDataCallback(String jsonData) {
        NewLog.logD("updateLocationDataCallback is called");
        Intent otherIntent = new Intent(AppController.BROADCAST_ELSE_ACTION);
        otherIntent.putExtra("jsonData", jsonData);
        sendBroadcast(otherIntent);
        //create the intent
        //call the sendBroadcast function.
    }

    private void updateLocationDataToServer (final String userMail, final String userLatitude,
                                             final String userLongitude, final String userUnixTime) {
        NewLog.logD("the data sent to server is " + userMail + " " + userLatitude + " "
                + userLongitude + " " + userUnixTime);
        String dataUpdateURL = getString(R.string.data_update_url);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, dataUpdateURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String jsonString = response.toString();
                        NewLog.logD("sign up response " + jsonString);
                        //receive the others' info: userMail, userLatitude, userLongitude
                        updateLocationDataCallback(jsonString);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NewLog.logD("Error is " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                //change the mail.
                params.put("user_email", userMail);
                params.put("user_latitude", userLatitude);
                params.put("user_longitude", userLongitude);
                params.put("user_unixtime", userUnixTime);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);


//        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
//                Request.Method.POST, dataUpdateURL, null,
//                new Response.Listener<JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        NewLog.logD("sign up response " + response.toString());
//                        //
//                        //receive the others' info: userMail, userLatitude, userLongitude
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                NewLog.logD("Error is " + error.getMessage());
//            }
//        }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                //
//                //change the mail.
//                params.put("user_email", userMail);
//                params.put("user_latitude", userLatitude);
//                params.put("user_longitude", userLongitude);
//                params.put("user_unixtime", userUnixTime);
//                return params;
//            }
//        };
//        AppController.getInstance().addToRequestQueue(jsonArrayRequest);
    }

    //only receive the friend location info by setting the friendMail[]
    private void updateLocationDataToServer (final String userMail, final String friendMailList,
                                             final String userLatitude,
                                             final String userLongitude, final String userUnixTime) {

        if (!AppController.SHARE_LOCATION) {
            return;
        }
        String dataUpdateURL = getString(R.string.data_update_url);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, dataUpdateURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String jsonString = response.toString();
                        NewLog.logD("sign up response " + jsonString);
                        //receive the others' info: userMail, userLatitude, userLongitude
                        updateLocationDataCallback(jsonString);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NewLog.logD("Error is " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                //change the mail.
                params.put("user_email", userMail);
                //fix the "own location info is not updated" issue.
                if (friendMailList != null) {
                    params.put("user_friend_mail", friendMailList);
                }

                params.put("user_latitude", userLatitude);
                params.put("user_longitude", userLongitude);
                params.put("user_unixtime", userUnixTime);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);

    }



    //***********************NetworkChangeReceiver**************************//
    public class WifiStatusUserSettingChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO

            if (intent.getAction().equalsIgnoreCase(AppController.BROADCAST_UPDATE_INTERVAL)) {
                if (mGoogleApiClient.isConnected()) {
                    mLocationRequest.setInterval(AppController.UPDATE_INTERVAL_IN_MILLISECONDS);
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, LocationService.this);
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                            mGoogleApiClient, mLocationRequest, LocationService.this);
                    NewLog.logD("location interval changed " + AppController.UPDATE_INTERVAL_IN_MILLISECONDS);
                }
            }

            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();
            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                NewLog.logD("Have Wifi Connection");
                if (mGoogleApiClient.isConnected()) {
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, LocationService.this);
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                            mGoogleApiClient, mLocationRequest, LocationService.this);
                    NewLog.logD("locationRequest changed to wifi");
                }
            }

            else {
                NewLog.logD("No Wifi Connection");
                if (mGoogleApiClient.isConnected()) {
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,LocationService.this);
                    LocationServices.FusedLocationApi.requestLocationUpdates(
                            mGoogleApiClient, mLocationRequest, LocationService.this);
                    NewLog.logD("locationRequest changed to no wifi");
                }
            }

        }
    }
    //***********************NetworkChangeReceiver**************************//

}
