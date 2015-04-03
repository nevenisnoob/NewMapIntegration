package jp.co.drecom.newmapintegration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import jp.co.drecom.newmapintegration.utils.LocationDBHelper;
import jp.co.drecom.newmapintegration.utils.NewLog;
import jp.co.drecom.newmapintegration.utils.NewToast;


public class MainMapActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        DatePickingFragment.OnDatePickListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    private static final int SIGN_UP_DIALOG = 1;
    private static final int ADD_FRIEND_DIALOG = 2;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private NewMapFragment mapFragment;
    private DatePickingFragment datePickingFragment;
    private FriendListFragment friendListFragment;
    private MapSettingFragment mapSettingFragment;

    private Intent mLocationServiceIntent;

    private LocationDBHelper mDBHelper;




    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";



    private Location mCurrentLocation;

    //whether the map camera should be updated.

    private String mLastUpdateTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_map);

        mDBHelper = new LocationDBHelper(this);



        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
//        mTitle = getTitle();
        mTitle = getUserAccountInfo();
        getMapSettingFootPrint();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
//        mRequestLocationUpdates = false;
        mLastUpdateTime = "";

        mapFragment = (NewMapFragment) getFragmentManager().findFragmentById(R.id.map);


        updateValuesFromBundle(savedInstanceState);

        mLocationServiceIntent = new Intent(this, LocationService.class);
        startService(mLocationServiceIntent);
    }

    private void getMapSettingFootPrint() {
        mDBHelper.mLocationDB = mDBHelper.getReadableDatabase();
        String showFootprint = mDBHelper.getMySettingDataFootprint();
        if (showFootprint.equalsIgnoreCase("1")) {
            AppController.SHOW_FOOT_PRINT = true;
        } else {
            AppController.SHOW_FOOT_PRINT = false;
        }
    }

    private String getUserAccountInfo() {
        mDBHelper.mLocationDB = mDBHelper.getReadableDatabase();
        String userAccount = mDBHelper.getUserAccount();
        mDBHelper.close();
        if (userAccount == null) {
            return "Please Sign Up";
        } else {
            AppController.USER_MAIL = userAccount;
            return userAccount;
        }

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

    private void updateOnlineStatusToServer (final String userMail, final String onlineStatus) {

        String onlineStatusURL = getString(R.string.online_status_url);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, onlineStatusURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String responseString = response.toString();
                        NewLog.logD("online status update response " + responseString);

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
                params.put("user_online", onlineStatus);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);
        NewLog.logD("service destroyed, updateOnlineStatusToServer is called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if you want update the location log,
        //just comment the function.
        stopService(mLocationServiceIntent);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
//        NewLog.logD("onTouchEvent");
        if (mapFragment.mMoveMapCamera) {
            mapFragment.mMoveMapCamera = false;
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
        switch (position) {
            case 0:
                if (datePickingFragment != null) {
                    fragmentManager.beginTransaction()
                            .remove(datePickingFragment).commit();
                }
                if (friendListFragment != null) {
                    fragmentManager.beginTransaction()
                            .remove(friendListFragment).commit();
                }
                if (mapSettingFragment != null) {
                    fragmentManager.beginTransaction().remove(mapSettingFragment).commit();
                }
//                if (mapFragment != null) {
//                    //refresh mapFragment
//                    NewLog.logD("map refreshed");
////                    mapFragment.refreshMapAfterSetting();
//
//                }

                break;
            case 1:
                if (datePickingFragment == null) {
                    datePickingFragment = new DatePickingFragment();
//                    fragmentManager.beginTransaction()
//                            .add(R.id.container, datePickingFragment).commit();
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.container, datePickingFragment,
                                datePickingFragment.FRAGMENT_TAG).commit();

                break;
            //map settings: such as update interval, accuracy
            case 2:
                if (friendListFragment == null) {
                    friendListFragment = new FriendListFragment();
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.container, friendListFragment,
                                friendListFragment.FRAGMENT_TAG).commit();

                break;
            //email sign up, email and user_ID
            //send email to server, then get user_ID.
            //save user_ID and email to DB
            case 3:
                if (mapSettingFragment == null) {
                    mapSettingFragment = new MapSettingFragment();
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.container, mapSettingFragment,
                                mapSettingFragment.FRAGMENT_TAG).commit();
                break;
            //realTime
            case 4:
                if (datePickingFragment != null) {
                    fragmentManager.beginTransaction()
                            .remove(datePickingFragment).commit();
                }
                if (friendListFragment != null) {
                    fragmentManager.beginTransaction()
                            .remove(friendListFragment).commit();
                }
                if (mapSettingFragment != null) {
                    fragmentManager.beginTransaction().remove(mapSettingFragment).commit();
                }
                createDialog(SIGN_UP_DIALOG);
                break;

        }
//        fragmentManager.beginTransaction()
//                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
//                .commit();
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

            if (friendListFragment != null) {
                if (friendListFragment.isAdded()) {
                    menu.add(0, Menu.FIRST, Menu.NONE, "ADD").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                } else {
//                    getMenuInflater().inflate(R.menu.main_map, menu);
                }
            } else {
//                getMenuInflater().inflate(R.menu.main_map, menu);
            }


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
        String menuTitle = (String) item.getTitle();
        if (menuTitle != null) {
            if (menuTitle.equalsIgnoreCase("ADD")) {
//            NewToast.toastS(this, "add is clicked");
                createDialog(ADD_FRIEND_DIALOG);
            }
        }

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void getDateData(long startTime, long endTime) {
        mapFragment.getDateData(startTime, endTime);
    }


    private void signUpWithMail (final String mail) {
        NewLog.logD("the mail is " + mail);


        String signUpURL = getString(R.string.sign_up_url);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, signUpURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            int userID = Integer.parseInt(response.toString());
                            NewLog.logD("sign up response " + userID);

                            mDBHelper.mLocationDB = mDBHelper.getWritableDatabase();
                            long flag = mDBHelper.saveAccountMailToDB(userID, mail);

                            //receive the user ID and update the DB
//                            int flag = mDBHelper.updateAccountID(userID, mail);
                            mDBHelper.close();
                            if (flag != -1) {
                                NewLog.logD("sign up succeed.");
                                AppController.USER_MAIL = mail;
                                mTitle = AppController.USER_MAIL;
                                restoreActionBar();
                            } else {
                                NewLog.logD("sign up failed.");
                            }
                        } catch (NumberFormatException e) {
                            NewLog.logD("the data from server is strange. " + e.toString());

                        }


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
                params.put("user_mail", mail);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);
    }
//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            switch (sectionNumber){
//                case 1:
//                    break;
//                case 2:
//                    DatePickingFragment datePickingFragment = new DatePickingFragment();
//                    return datePickingFragment;
//                    break;
//                case 3:
//                    break;
//            }
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main_map, container, false);
//            return rootView;
//        }
//
//        @Override
//        public void onAttach(Activity activity) {
//            super.onAttach(activity);
//            ((MainMapActivity) activity).onSectionAttached(
//                    getArguments().getInt(ARG_SECTION_NUMBER));
//        }
//    }

    public void createDialog (int dialogType) {
        LayoutInflater inflater = (LayoutInflater)this.
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout;
        AlertDialog.Builder builder;

        switch (dialogType) {
            case SIGN_UP_DIALOG:
                layout = inflater.inflate(R.layout.sign_up_dialog,
                        (ViewGroup) findViewById(R.id.signup_dialog_root));
                builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.sign_up_dialog_title));
                builder.setView(layout);
                builder.setPositiveButton(getString(R.string.confirm_btn),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText userMail = (EditText) layout.findViewById(R.id.sign_up_email);
                                String userMailText = userMail.getText().toString();
                                if (userMailText != null) {
                                    signUpWithMail(userMailText);
                                }

                            }
                });
                builder.setNegativeButton(getString(R.string.cancel_btn),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.create().show();
                break;
            case ADD_FRIEND_DIALOG:
                layout = inflater.inflate(R.layout.add_friend_dialog,
                        (ViewGroup) findViewById(R.id.add_friend_dialog_root));
                builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.add_friend_dialog_title));
                builder.setView(layout);
                builder.setPositiveButton(getString(R.string.confirm_btn),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText friendMail = (EditText) layout.findViewById(R.id.friend_email);
                                String friendMailText = friendMail.getText().toString();
                                if (friendMailText != null) {
                                    addFriendMail(friendMailText);
                                }

                            }
                        });
                builder.setNegativeButton(getString(R.string.cancel_btn),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.create().show();
                break;
        }

    }


    private void addFriendMail(String mail) {
        mDBHelper.mLocationDB = mDBHelper.getWritableDatabase();
        if (-1 == mDBHelper.saveFriendInfo(mail, "")) {
            NewLog.logD("friend mail insert failed");
            return;
        }
        if (friendListFragment != null) {
            //refresh friend list data after adding friend
            friendListFragment.friendListUpdate();
        }
        NewLog.logD("friend mail is " + mail);
    }

}
