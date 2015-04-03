package jp.co.drecom.newmapintegration.utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by huang_liangjin on 2015/03/30.
 */
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */

public interface LatLngInterpolator {
    public LatLng interpolate(float fraction, LatLng a, LatLng b);

    public class Linear implements LatLngInterpolator {

        @Override
        public LatLng interpolate(float fraction, LatLng a, LatLng b) {

            double lat = (b.latitude - a.latitude) * fraction + a.latitude;
            double lngDelta = b.longitude - a.longitude;

            // Take the shortest path across the 180th meridian.
            if (Math.abs(lngDelta) > 180) {
                lngDelta -= Math.signum(lngDelta) * 360;
            }
            double lng = lngDelta * fraction + a.longitude;
            return new LatLng(lat, lng);
        }
    }
}
