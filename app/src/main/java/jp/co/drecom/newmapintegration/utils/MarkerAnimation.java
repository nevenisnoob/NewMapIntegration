package jp.co.drecom.newmapintegration.utils;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.util.Property;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by huang_liangjin on 2015/03/30.
 */
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
public class MarkerAnimation {
     public static void animateMarker(final Marker marker, final LatLng finalPosition,
                                      final LatLngInterpolator latLngInterpolator) {
         final LatLng startPosition = marker.getPosition();

         ValueAnimator valueAnimator = new ValueAnimator();
         valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
             @Override
             public void onAnimationUpdate(ValueAnimator animation) {
                 float v = animation.getAnimatedFraction();
                 LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition);
                 marker.setPosition(newPosition);
             }
         });
         valueAnimator.setFloatValues(0, 1); // Ignored.
         valueAnimator.setDuration(1000);
         valueAnimator.start();
    }


}
