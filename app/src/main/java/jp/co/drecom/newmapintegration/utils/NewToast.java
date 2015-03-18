package jp.co.drecom.newmapintegration.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by huang_liangjin on 2015/03/17.
 */
public class NewToast {
    public static void toastL(Context context,String message) {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }

    public static void toastS(Context context,String message) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }
}
