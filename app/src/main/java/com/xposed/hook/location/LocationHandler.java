package com.xposed.hook.location;

import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XposedHelpers;
import mirror.RefMethod;

/**
 * Created by lin on 2017/8/6.
 */

public class LocationHandler extends Handler {

    private static LocationHandler instance;

    public static LocationHandler getInstance() {
        if (instance == null) {
            synchronized (LocationHandler.class) {
                if (instance == null)
                    instance = new LocationHandler();
            }
        }
        return instance;
    }

    private Context context;

    private LocationHandler() {
        super(Looper.getMainLooper());
    }

    public Context requireContext() throws ClassNotFoundException {
        if (context != null)
            return context;
        context = (Context) XposedHelpers.callStaticMethod(Class.forName("android.app.ActivityThread"), "currentApplication");
        return context;
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            Object transport = requireContext().getSystemService(Context.LOCATION_SERVICE);
            notifyNmeaReceived(transport);
            notifyLocation(LocationManager.mListeners.get(transport));
            sendEmptyMessageDelayed(0, 10000);
            Log.d(LocationHook.TAG, "Avalon Hook Location Success");
        } catch (Throwable e) {
            Log.d(LocationHook.TAG, e.toString(), e);
        }
    }

    public static Location createLocation(double latitude, double longitude) {
        Location l = new Location(android.location.LocationManager.GPS_PROVIDER);
        l.setLatitude(latitude);
        l.setLongitude(longitude);
        l.setAccuracy((float) Math.random() + 8);
        l.setBearing((int) (360 * Math.random()));
        l.setTime(System.currentTimeMillis());
        l.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        Bundle extraBundle = new Bundle();
        int svCount = VirtualGPSSatalines.get().getSvCount();
        extraBundle.putInt("satellites", svCount);
        extraBundle.putInt("satellitesvalue", svCount);
        l.setExtras(extraBundle);
        return l;
    }

    public static void updateLocation(Location location, double latitude, double longitude) {
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
    }

    public void start() {
        removeMessages(0);
        sendEmptyMessageDelayed(0, 1000);
    }

    private void notifyLocation(Map listeners) {
        if (listeners == null || listeners.isEmpty())
            return;
        Location location = createLocation(LocationConfig.getLatitude(), LocationConfig.getLongitude());
        //noinspection unchecked
        Set<Map.Entry> entries = listeners.entrySet();
        RefMethod<Void> method;
        if (LocationManager.ListenerTransport.onLocationChanged != null)
            method = LocationManager.ListenerTransport.onLocationChanged;
        else
            method = LocationManager.LocationListenerTransport.onLocationChanged;
        if (method == null)
            return;
        for (Map.Entry entry : entries) {
            Object value = entry.getValue();
            if (value == null)
                continue;
            method.call(value, location);
        }
    }

    private void notifyNmeaReceived(Object transport) {
        try {
            if (LocationManager.mGnssStatusListenerManager != null) {
                Object manager = LocationManager.mGnssStatusListenerManager.get(transport);
                notifyNmeaListener(LocationManager.GnssStatusListenerManager.mListenerTransport.get(manager));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                notifyNmeaListener(LocationManager.mGnssNmeaListeners.get(transport));
                notifyNmeaListener(LocationManager.mGpsNmeaListeners.get(transport));
            } else {
                notifyNmeaListener(LocationManager.mNmeaListeners.get(transport));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void notifyNmeaListener(Map listeners) {
        if (listeners != null && !listeners.isEmpty()) {
            //noinspection unchecked
            Set<Map.Entry> entries = listeners.entrySet();
            for (Map.Entry entry : entries)
                notifyNmeaListener(entry.getValue());
        }
    }

    private void notifyNmeaListener(Object object) {
        if (object == null)
            return;
        try {
            MockLocationHelper.invokeNmeaReceived(object);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
