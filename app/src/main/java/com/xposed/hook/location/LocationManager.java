package com.xposed.hook.location;

import android.location.Location;
import android.location.LocationListener;

import java.util.Map;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefMethod;
import mirror.RefObject;

public class LocationManager {
    public static Class<?> TYPE = RefClass.load(LocationManager.class, "android.location.LocationManager");
    public static RefObject<Map> mGnssNmeaListeners;
    public static RefObject<Map> mGnssStatusListeners;
    public static RefObject<Map> mGpsNmeaListeners;
    public static RefObject<Map> mGpsStatusListeners;
    public static RefObject<Map> mListeners;
    public static RefObject<Map> mNmeaListeners;

    public static class GnssStatusListenerTransport {
        public static Class<?> TYPE = RefClass.load(GnssStatusListenerTransport.class, "android.location.LocationManager$GnssStatusListenerTransport");
        public static RefObject<Object> mGpsListener;
        public static RefObject<Object> mGpsNmeaListener;
        @MethodParams({long.class, String.class})
        public static RefMethod<Void> onNmeaReceived;
    }

    public static class GpsStatusListenerTransport {
        public static Class<?> TYPE = RefClass.load(GpsStatusListenerTransport.class, "android.location.LocationManager$GpsStatusListenerTransport");
        public static RefObject<Object> mListener;
        public static RefObject<Object> mNmeaListener;
        @MethodParams({long.class, String.class})
        public static RefMethod<Void> onNmeaReceived;
    }

    public static class ListenerTransport {
        public static Class<?> TYPE = RefClass.load(ListenerTransport.class, "android.location.LocationManager$ListenerTransport");
        public static RefObject<LocationListener> mListener;
        @MethodParams({Location.class})
        public static RefMethod<Void> onLocationChanged;
    }

    public static class LocationListenerTransport {
        public static Class<?> TYPE = RefClass.load(LocationListenerTransport.class, "android.location.LocationManager$LocationListenerTransport");
        @MethodParams({Location.class})
        public static RefMethod<Void> onLocationChanged;
    }
}