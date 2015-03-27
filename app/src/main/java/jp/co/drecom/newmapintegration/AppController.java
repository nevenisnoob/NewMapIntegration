package jp.co.drecom.newmapintegration;

import android.app.AlertDialog;
import android.app.Application;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by huang_liangjin on 2015/03/23.
 */
public class AppController extends Application {

    public final static String BROADCAST_SELF_ACTION= "jp.co.drecom.newmapintegration.location";
    public final static String BROADCAST_ELSE_ACTION = "jp.co.drecom.newmapintegration.otherLocation";

    public static final int SIGN_UP_DIALOG = 1;

    public static final String TAG = "NewMapIntegration";

    public static String USER_MAIL = "Please Sign Up";

    //the distance of two points (35.631260n 139.712820w), (35.631269n 139.712829w) is 1.3m
    //the distance of two points (35.631260n 139.712820w), (35.631280n 139.712840w) is 2.9m
    public static double NORMAL_TOLERANCE = 0.00002;
    public static double ACCURATE_TOLERANCE = 0.000009;

    public static int MAX_SPOT_PER_POLYLINE = 1000;




    private static AppController mInstance;

    private RequestQueue mRequestQueue;

    public static AppController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public RequestQueue getRequestQueue () {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
