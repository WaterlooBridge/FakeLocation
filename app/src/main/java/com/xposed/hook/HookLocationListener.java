package com.xposed.hook;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lin on 2017/8/6.
 */

public class HookLocationListener implements LocationListener {

    public List<WeakReference<LocationListener>> list;
    public double latitude, longitude;

    @Override
    public void onLocationChanged(Location location) {
        Log.e(HookUtils.TAG, "before hook location: " + location.toString());
        for (int i = 0; i < list.size(); i++) {
            LocationListener listener = list.get(i).get();
            if (listener != null) {
                Location l = new Location(location);
                l.setLatitude(latitude);
                l.setLongitude(longitude);
                listener.onLocationChanged(l);
                Log.e(HookUtils.TAG, "hook location: " + l.toString());
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        for (int i = 0; i < list.size(); i++) {
            LocationListener listener = list.get(i).get();
            if (listener != null) {
                listener.onStatusChanged(provider, status, extras);
            }
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        for (int i = 0; i < list.size(); i++) {
            LocationListener listener = list.get(i).get();
            if (listener != null) {
                listener.onProviderEnabled(provider);
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        for (int i = 0; i < list.size(); i++) {
            LocationListener listener = list.get(i).get();
            if (listener != null) {
                listener.onProviderDisabled(provider);
            }
        }
    }
}
