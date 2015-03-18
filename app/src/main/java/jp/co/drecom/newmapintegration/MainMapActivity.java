package jp.co.drecom.newmapintegration;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.Date;

import jp.co.drecom.newmapintegration.utils.NewLog;
import jp.co.drecom.newmapintegration.utils.NewToast;


public class MainMapActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private MapFragment mapFragment;

    private Intent mLocationServiceIntent;

    //for init location
    private GoogleApiClient mGoogleApiClient;


    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    protected final static String BROADCASTER_ACTION= "jp.co.drecom.newmapintegration.location";

    private GoogleMap googleMap;

    private Location mTempLocation;
    private Location mCurrentLocation;
    private LatLng mCurrentLatLng;

    private LocationRequest mLocationRequest;

    //whether the map camera should be updated.
    private Boolean mMoveMapCamera;

    private String mLastUpdateTime;

    private LocationReceiver mLocationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_map);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
//        mRequestLocationUpdates = false;
        mLastUpdateTime = "";
        mMoveMapCamera = true;

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        updateValuesFromBundle(savedInstanceState);

        //register a dynamic broadcaster.
        mLocationReceiver = new LocationReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(BROADCASTER_ACTION);
        registerReceiver(mLocationReceiver, intentfilter);

        buildGoogleApiClient();

        mLocationServiceIntent = new Intent(this, LocationService.class);
        startService(mLocationServiceIntent);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }



    private void updateValuesFromBundle(Bundle savedInstanceState) {
        NewLog.logD("updating values from bundle");
        if (savedInstanceState != null) {
//            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
//                mRequestLocationUpdates =
//                        savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
//            }
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
//            updateUI();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        NewLog.logD("onStart");
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        NewLog.logD("onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        NewLog.logD("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        NewLog.logD("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(mLocationServiceIntent);
        unregisterReceiver(mLocationReceiver);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        NewLog.logD("onTouchEvent");
        if (mMoveMapCamera) {
            mMoveMapCamera = false;
        }

        return super.dispatchTouchEvent(event);
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        NewLog.logD("onSaveInstanceState");
//        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        NewLog.logD("onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
//            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
//                mRequestLocationUpdates =
//                        savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
//            }
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
        }

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main_map, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //*****************************************************************************
    //location related


    private void updateUI(double latitude, double longitude) {
        //TODO
        //if screen is touched, or turned to background, stop updating UI
            //for UI updated
        if (mMoveMapCamera) {
            mCurrentLatLng = new LatLng(latitude, longitude);
            mapFragment.getMap().animateCamera(
                    CameraUpdateFactory.newLatLng(mCurrentLatLng));
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        NewLog.logD("onMapReady");
        this.googleMap = googleMap;
//        googleMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).title("Marker"));
//        googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMyLocationButtonClickListener(this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mTempLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mTempLocation != null) {
            //update UI
            mCurrentLatLng = new LatLng(mTempLocation.getLatitude(),
                    mTempLocation.getLongitude());
            //zoom range is 2.0-21.0
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng,15));

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

    @Override
    public boolean onMyLocationButtonClick() {
        NewLog.logD("MyLocationButton clicked");
        mMoveMapCamera = true;
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        NewLog.logD("map clicked");
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_map, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainMapActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    public class LocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //TODO
//            NewLog.logD("onReceived");
            updateUI(intent.getDoubleExtra("Latitude", 0.0),
                    intent.getDoubleExtra("Longitude", 0.0));
            //receive the location info, then update UI here
        }
    }

}
