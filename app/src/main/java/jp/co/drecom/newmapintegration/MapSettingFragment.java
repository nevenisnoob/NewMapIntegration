package jp.co.drecom.newmapintegration;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import jp.co.drecom.newmapintegration.utils.LocationDBHelper;
import jp.co.drecom.newmapintegration.utils.NewLog;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapSettingFragment extends Fragment {

    public static String FRAGMENT_TAG = "MapSettingFragment";

    private LocationDBHelper mSettingDBHelper;

    private Switch mFootprint;
    private Switch mShareLocation;
    private RadioGroup mUpdateIntervalGroup;
    private RadioButton mUpdateIntervalBtn1;
    private RadioButton mUpdateIntervalBtn2;
    private RadioButton mUpdateIntervalBtn3;
    private RadioButton mUpdateIntervalBtn4;


    public MapSettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingDBHelper = new LocationDBHelper(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map_setting, container, false);
        mFootprint = (Switch) view.findViewById(R.id.show_footprint);
        mShareLocation = (Switch) view.findViewById(R.id.share_location);
        mUpdateIntervalGroup = (RadioGroup) view.findViewById(R.id.update_radio_group);
        mUpdateIntervalBtn1 = (RadioButton) view.findViewById(R.id.radio_5sec);
        mUpdateIntervalBtn2 = (RadioButton) view.findViewById(R.id.radio_30sec);
        mUpdateIntervalBtn3 = (RadioButton) view.findViewById(R.id.radio_5min);
        mUpdateIntervalBtn4 = (RadioButton) view.findViewById(R.id.radio_30min);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //TODO
        readMapSettingFromDB();

        //read the data from db, then set the UI component
    }

    @Override
    public void onPause() {
        super.onPause();
        //TODO
        saveMapSetting();

        //save the data to db
        //send broadcast to locationRequestChanger
    }

    private void readMapSettingFromDB() {
        mSettingDBHelper.mLocationDB = mSettingDBHelper.getReadableDatabase();
        String footprint = mSettingDBHelper.getMySettingDataFootprint();
        String locationShare = mSettingDBHelper.getMySettingDataShareLocation();
        String updateInterval = mSettingDBHelper.getMySettingDataInterval();
        mSettingDBHelper.mLocationDB.close();
        if (footprint.equalsIgnoreCase("1")) {
            mFootprint.setChecked(true);
            NewLog.logD("checked");
        } else {
            mFootprint.setChecked(false);
        }
        if (locationShare.equalsIgnoreCase("1")) {
            mShareLocation.setChecked(true);
            NewLog.logD("checked");
        } else {
            mShareLocation.setChecked(false);
        }
        switch (updateInterval) {
            case "1":
                mUpdateIntervalBtn1.setChecked(true);
                break;
            case "2":
                mUpdateIntervalBtn2.setChecked(true);
                break;
            case "3":
                mUpdateIntervalBtn3.setChecked(true);
                break;
            case "4":
                mUpdateIntervalBtn4.setChecked(true);
                break;
        }
    }

    private void saveMapSetting() {
        //save the data to DB
        String footprint = mFootprint.isChecked() ? "1" : "0";
        AppController.SHOW_FOOT_PRINT = mFootprint.isChecked() ? true : false;


        String locationShare = mShareLocation.isChecked() ? "1" : "0";
        AppController.SHARE_LOCATION = mShareLocation.isChecked() ? true : false;

        String updateInterval = "2";

        Intent updateIntervalIntent = new Intent(AppController.BROADCAST_UPDATE_INTERVAL);

        if (mUpdateIntervalBtn1.isChecked()) {
            updateInterval = "1";
            AppController.UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
        } else if (mUpdateIntervalBtn2.isChecked()) {
            AppController.UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
            updateInterval = "2";
        } else if (mUpdateIntervalBtn3.isChecked()) {
            AppController.UPDATE_INTERVAL_IN_MILLISECONDS = 300000;
            updateInterval = "3";
        } else if (mUpdateIntervalBtn4.isChecked()) {
            AppController.UPDATE_INTERVAL_IN_MILLISECONDS = 1800000;
            updateInterval = "4";
        }
        getActivity().sendBroadcast(updateIntervalIntent);



        mSettingDBHelper.mLocationDB = mSettingDBHelper.getWritableDatabase();
        mSettingDBHelper.updateSettingInfo(
                mSettingDBHelper.mLocationDB, footprint, locationShare, updateInterval);
        mSettingDBHelper.mLocationDB.close();

        //
        updateOnlineStatusToServer(AppController.USER_MAIL, locationShare);
    }

    //update online/offline status to server
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
    }



}
