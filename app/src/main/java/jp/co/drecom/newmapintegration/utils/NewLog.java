package jp.co.drecom.newmapintegration.utils;

import android.util.Log;

import jp.co.drecom.newmapintegration.R;

/**
 * Created by huang_liangjin on 2015/03/17.
 */
public class NewLog {
    private static final String TAG = "NewMapIntegration";

    public static void logV(String text) {
        Log.v(TAG, text);
    }
    public static void logD(String text) {
        Log.d(TAG, text);
    }
    public static void logI(String text) {
        Log.i(TAG, text);
    }
    public static void logE(String text) {
        Log.e(TAG, text);
    }
    public static void logW(String text) {
        Log.w(TAG, text);
    }
}
