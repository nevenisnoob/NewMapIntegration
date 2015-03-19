package jp.co.drecom.newmapintegration;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    public Boolean mMoveMapCamera;
    private LatLng mCurrentLatLng;

    private Location mTempLocation;

    private Location mLastLocation;

    private PolylineOptions mFootPrint;





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

        buildGoogleApiClient();

        mGoogleApiClient.connect();

        //register a dynamic broadcaster.
        mLocationReceiver = new LocationReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(BROADCASTER_ACTION);
        getActivity().registerReceiver(mLocationReceiver, intentfilter);

        mFootPrint = new PolylineOptions();


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
        mCurrentLatLng = new LatLng(latitude, longitude);
        mFootPrint.add(mCurrentLatLng);
        mGoogleMap.addPolyline(mFootPrint);
        if (mMoveMapCamera) {

            mGoogleMap.animateCamera(
                    CameraUpdateFactory.newLatLng(mCurrentLatLng));
        }

    }
}
