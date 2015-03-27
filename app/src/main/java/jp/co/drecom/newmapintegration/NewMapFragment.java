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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

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
        GoogleMap.OnInfoWindowClickListener {

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

    private double mLastLatitude;
    private double mLastLongitude;

    private Location mLastLocation;

    //for performance improvement
//    private PolylineOptions mFootPrint;
    private List<PolylineOptions> mFootPrint;
    private PolylineOptions mRealTimeFootPrint;

    private LocationDBHelper mLocationDBHelper;

    private long mStartUnixTime;
    private long mEndUnixTime;

    private Marker mSelfMarker;
    private MarkerOptions mSelfMarkerOptions;
    private Marker[] mOthersMarker;
    private MarkerOptions[] mOthersMarkerOptions;

//    private Marker tempMarker;
//    private MarkerOptions tempMarkerOptions;




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
        //default time interval yesterday - current
        mStartUnixTime = (System.currentTimeMillis() / 1000) - 86400;
        mEndUnixTime = mStartUnixTime + 86400;

        buildGoogleApiClient();

        mGoogleApiClient.connect();

        //register a dynamic broadcaster.
        mLocationReceiver = new LocationReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(AppController.BROADCAST_SELF_ACTION);
        intentfilter.addAction(AppController.BROADCAST_ELSE_ACTION);
        getActivity().registerReceiver(mLocationReceiver, intentfilter);

        //performance improvement
//        mFootPrint = new PolylineOptions();
        mRealTimeFootPrint = new PolylineOptions().color(Color.RED);

        mFootPrint = new ArrayList<PolylineOptions>();
//        mFootPrint.geodesic(true);
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
        drawFootPrint(mStartUnixTime, mEndUnixTime);
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
//        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setOnMapClickListener(this);
        mGoogleMap.setOnMyLocationButtonClickListener(this);
        mGoogleMap.setBuildingsEnabled(true);
        mGoogleMap.setOnCameraChangeListener(this);
        mGoogleMap.setOnMarkerClickListener(this);


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
//        return false;
        NewLog.logD("marker is clicked " + marker.getTitle());
        if (marker.getTitle().equalsIgnoreCase("ME")) {
            mMoveMapCamera = true;
        }
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
        NewLog.logD("the start time is " + mStartUnixTime + " and the end time is " + mEndUnixTime);
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

//            LatLng otherPosition = new LatLng(mLastLatitude, mLastLongitude);
//            tempMarkerOptions = new MarkerOptions();
//            tempMarkerOptions.position(otherPosition);
//            tempMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//            tempMarker = mGoogleMap.addMarker(tempMarkerOptions);

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void getDateData(long startTime, long endTime) {
        mStartUnixTime = startTime;
        mEndUnixTime = endTime;
        NewLog.logD("the NewMapFraagment.getDateData function is called");
        drawFootPrint(mStartUnixTime, mEndUnixTime);

    }


    public class LocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO
            if (intent.getAction().equalsIgnoreCase(AppController.BROADCAST_SELF_ACTION)) {
                NewLog.logD("mMoveMapCamera == " + mMoveMapCamera);
                double latitude = intent.getDoubleExtra("Latitude", 0.0);
                double longitude = intent.getDoubleExtra("Longitude", 0.0);
                updateUI(latitude,longitude);
                updateSelfLocation(latitude, longitude);
                //receive the location info, then update UI here
            } else if (intent.getAction().equalsIgnoreCase(AppController.BROADCAST_ELSE_ACTION)) {
                updateOthersLocation(intent.getStringExtra("jsonData"));
            }
        }
    }

    private void updateSelfLocation(double latitude, double longitude) {
        if (mSelfMarkerOptions == null) {
            mSelfMarkerOptions = new MarkerOptions();
            LatLng selfPosition = new LatLng(latitude, longitude);
            mSelfMarkerOptions.position(selfPosition);
            mSelfMarkerOptions.title("ME");
            mSelfMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mSelfMarker = mGoogleMap.addMarker(mSelfMarkerOptions);
        } else {
            LatLng selfPosition = new LatLng(latitude, longitude);
            mSelfMarkerOptions.position(selfPosition);
            mSelfMarker.setPosition(selfPosition);
        }
    }

    private void updateOthersLocation(String jsonData) {
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            NewLog.logD("JSONArray count is " + jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                NewLog.logD("other info is " + jsonObject.getString("other_mail"));
                NewLog.logD("other info is " + jsonObject.getString("other_latitude"));
                NewLog.logD("other info is " + jsonObject.getString("other_longitude"));
            }

            if (mOthersMarkerOptions == null) {
                NewLog.logD("the first time for location share");
                mOthersMarkerOptions = new MarkerOptions[jsonArray.length()];
                mOthersMarker = new Marker[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String otherMail = jsonObject.getString("other_mail");
                    double otherLatitude = Double.valueOf(jsonObject.getString("other_latitude"));
                    double otherLongitude = Double.valueOf(jsonObject.getString("other_longitude"));
                    LatLng otherPosition = new LatLng(otherLatitude, otherLongitude);
                    mOthersMarkerOptions[i] = new MarkerOptions()
                            .position(otherPosition)
                            .title(otherMail)
//                            .snippet(jsonObject.getString("other_mail"))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    mOthersMarker[i] = mGoogleMap.addMarker(mOthersMarkerOptions[i]);
                }
                //add marker to map
            } else {
                NewLog.logD("others location information updated");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String otherMail = jsonObject.getString("other_mail");
                    double otherLatitude = Double.valueOf(jsonObject.getString("other_latitude"));
                    double otherLongitude = Double.valueOf(jsonObject.getString("other_longitude"));
                    if (mOthersMarker[i].getTitle().equalsIgnoreCase(otherMail)) {
                        NewLog.logD("others email is " + otherMail);
                        LatLng otherPosition = new LatLng(otherLatitude, otherLongitude);
                        mOthersMarkerOptions[i].position(otherPosition);
                        mOthersMarker[i].setPosition(otherPosition);
                    }

                }
                //marker.setPosition
            }


        } catch (JSONException e) {
            NewLog.logD("sth is wrong when transferring string to json");
        }
    }



    private void updateUI(double latitude, double longitude) {
        //TODO
        //if screen is touched, or turned to background, stop updating UI
        //for UI updated


        if (whetherNeedUpdateLocation(latitude,longitude)) {
            mCurrentLatLng = new LatLng(latitude, longitude);
            mRealTimeFootPrint.add(mCurrentLatLng);
            mGoogleMap.addPolyline(mRealTimeFootPrint);
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

        if (Math.abs(mLastLatitude - latitude) < AppController.NORMAL_TOLERANCE &&
                Math.abs(mLastLongitude - longitude) < AppController.NORMAL_TOLERANCE) {
            return false;
        }
        return true;
    }

    //clean the map then redraw the polyline, marker
    private void drawFootPrint(long startTime, long endTime) {
        if (mFootPrint == null || mGoogleMap == null) {
            return;
        }
        mGoogleMap.clear();

        //clean log for date pick
        for (int i = 0; i < mFootPrint.size(); i++) {
            mFootPrint.remove(i);
        }
        mGoogleMap.addPolyline(mRealTimeFootPrint);

        if (mOthersMarkerOptions != null) {
            for (int i = 0; i < mOthersMarkerOptions.length; i++) {
                mOthersMarker[i] = mGoogleMap.addMarker(mOthersMarkerOptions[i]);
            }
        }

        if (mSelfMarkerOptions != null) {
            mSelfMarker = mGoogleMap.addMarker(mSelfMarkerOptions);
        }

        //comment out for a moment
        mLocationDBHelper.mLocationDB = mLocationDBHelper.getReadableDatabase();
        Cursor cursor = mLocationDBHelper.getLocationLog(startTime, endTime);
        double latitude, longitude;
        LatLng location;
        boolean isEof = cursor.moveToFirst();
        int locationAmount = cursor.getCount();
        int polyLineOptionCount = locationAmount/AppController.MAX_SPOT_PER_POLYLINE + 1;
        int countForPolyLineOption = 0;
        int countForPolyLine = 0;
        mFootPrint.add(new PolylineOptions());
        NewLog.logD("the total data of today is " + cursor.getCount());
        //for performance improvement
        while (isEof) {
            if (countForPolyLine == AppController.MAX_SPOT_PER_POLYLINE) {
                countForPolyLine = 0;
                mFootPrint.add(new PolylineOptions());
                countForPolyLineOption++;
            }
            latitude = cursor.getDouble(0);
            longitude = cursor.getDouble(1);

            NewLog.logD("the data from db is " + latitude + ", " + longitude
                        + ", time is " + cursor.getLong(2));
            location = new LatLng(latitude, longitude);
            mFootPrint.get(countForPolyLineOption).add(location);
            isEof = cursor.moveToNext();
            countForPolyLine++;
        }
        for (int i = 0; i < mFootPrint.size(); i++) {
            mGoogleMap.addPolyline(mFootPrint.get(i));
        }
//        mGoogleMap.addPolyline(mFootPrint);
        cursor.close();
        mLocationDBHelper.mLocationDB.close();
    }

    //marker move animation
    private void moveMarkerWithAnimation(Marker marker, double preLatitude, double preLongitude,
                                    double curLatitude, double curLongitude) {
    }

}
