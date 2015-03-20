package jp.co.drecom.newmapintegration;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import jp.co.drecom.newmapintegration.utils.LocationDBHelper;
import jp.co.drecom.newmapintegration.utils.NewLog;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewMapFragment extends MapFragment implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnCameraChangeListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.InfoWindowAdapter,
        GoogleMap.OnInfoWindowClickListener{

    protected final static String BROADCASTER_ACTION= "jp.co.drecom.newmapintegration.location";

    public GoogleMap mGoogleMap;

    private LocationReceiver mLocationReceiver;

    //for init location
    private GoogleApiClient mGoogleApiClient;

    //because fragment couldn't receive touch event
    //while I need to get the touch event to control the map's camera view
    //so I make this flag to be public.
    //not good design, but...
    public boolean mMoveMapCamera;
    private LatLng mCurrentLatLng;

    private Location mTempLocation;

    //if |mLastLatitude - mCurrentLatitude| < 0.000005 &&
    //   |mLastLongitude - mCurrentLongitude| < 0.000005
    //then do not write the current location to DB.
    private double mLastLatitude;
    private double mLastLongitude;

    private Location mLastLocation;

    private PolylineOptions mFootPrint;

    private LocationDBHelper mLocationDBHelper;





    public NewMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        NewLog.logD("NewMapFragment.onAttach");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        NewLog.logD("NewMapFragment.onCreate");
        super.onCreate(savedInstanceState);

        mMoveMapCamera = true;
        mLastLatitude = 0;
        mLastLongitude = 0;

        buildGoogleApiClient();

        mGoogleApiClient.connect();

        //register a dynamic broadcaster.
        mLocationReceiver = new LocationReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(BROADCASTER_ACTION);
        getActivity().registerReceiver(mLocationReceiver, intentfilter);

        mFootPrint = new PolylineOptions();
        mFootPrint.geodesic(true);
//        mFootPrint.width(2);
//        mFootPrint.color(Color.YELLOW);
        initLocationDB();
    }

    private void initLocationDB() {
        mLocationDBHelper = new LocationDBHelper(getActivity());
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        NewLog.logD("NewMapFragment.onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        NewLog.logD("NewMapFragment.onResume");
        drawFootPrint(0, 1426840536);
    }

    @Override
    public void onPause() {
        super.onPause();
        NewLog.logD("NewMapFragment.onPause");

    }

    @Override
    public void onStop() {
        super.onStop();
        NewLog.logD("NewMapFragment.onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mLocationReceiver);
        NewLog.logD("NewMapFragment.onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        NewLog.logD("NewMapFragment.onDetach");
    }

    //Activity onCreate is over.
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getMapAsync(this);
        if (getMap() != null) {
            mGoogleMap = getMap();
        }
        NewLog.logD("NewMapFragment.onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        NewLog.logD("NewMapFragment.onStart");
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        NewLog.logD("onCameraChanged");
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        NewLog.logD("onMapClick");
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        NewLog.logD("onMapReady");
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setOnMapClickListener(this);
        mGoogleMap.setOnMyLocationButtonClickListener(this);
        mGoogleMap.setBuildingsEnabled(true);
        mGoogleMap.setOnCameraChangeListener(this);


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        NewLog.logD("MyLocationButton clicked");
        mMoveMapCamera = true;
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mTempLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mTempLocation != null) {
            //update UI
            mCurrentLatLng = new LatLng(mTempLocation.getLatitude(),
                    mTempLocation.getLongitude());
            mLastLatitude = mTempLocation.getLatitude();
            mLastLongitude = mTempLocation.getLongitude();
            //zoom range is 2.0-21.0
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng,15));

            mGoogleApiClient.disconnect();

            NewLog.logD("mGoogleApiClient onConnect - disconnect");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    public class LocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO
            NewLog.logD("mMoveMapCamera == " + mMoveMapCamera);
            updateUI(intent.getDoubleExtra("Latitude", 0.0),
                    intent.getDoubleExtra("Longitude", 0.0));
            //receive the location info, then update UI here
        }
    }

    private void updateUI(double latitude, double longitude) {
        //TODO
        //if screen is touched, or turned to background, stop updating UI
        //for UI updated
        if (whetherNeedUpdateLocation(latitude,longitude)) {
            mCurrentLatLng = new LatLng(latitude, longitude);
            mFootPrint.add(mCurrentLatLng);
            mGoogleMap.addPolyline(mFootPrint);
        }

        if (mMoveMapCamera) {
            mGoogleMap.animateCamera(
                    CameraUpdateFactory.newLatLng(mCurrentLatLng));
        }
        mLastLatitude = latitude;
        mLastLongitude = longitude;

    }

    private boolean whetherNeedUpdateLocation(double latitude, double longitude) {
        //the distance of two points (35.631260n 139.712820w), (35.631269n 139.712829w) is 1.3m
        if (Math.abs(mLastLatitude - latitude) < 0.000009 &&
                Math.abs(mLastLongitude - longitude) < 0.000009) {
            return false;
        }
        return true;
    }

    private void drawFootPrint(long startTime, long endTime) {
        if (mFootPrint == null || mGoogleMap == null) {
            return;
        }

        mLocationDBHelper.mLocationDB = mLocationDBHelper.getReadableDatabase();
        Cursor cursor = mLocationDBHelper.getLocationLog(startTime, endTime);
        double latitude, longitude;
        LatLng location;
        boolean isEof = cursor.moveToFirst();
        NewLog.logD("the total data of today is " + cursor.getCount());
        while (isEof) {
            latitude = cursor.getDouble(0);
            longitude = cursor.getDouble(1);
//            int tempTime = cursor.getInt(2);
//            long tempTime2 = cursor.getLong(2);
//            double tempTime3 = cursor.getFloat(2);
//            String tempTime4 = cursor.getString(2);


            NewLog.logD("the data from db is "+latitude + ", " + longitude
                    + ", time is "+ cursor.getLong(2));
            location = new LatLng(latitude, longitude);
            mFootPrint.add(location);
            mGoogleMap.addPolyline(mFootPrint);
            isEof = cursor.moveToNext();
        }
        cursor.close();
        mLocationDBHelper.mLocationDB.close();
    }
}
