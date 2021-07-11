package com.xposed.hook.location;

import android.location.Location;

import java.util.Map;

import mirror.MethodParams;
import mirror.RefClass;
import mirror.RefMethod;
import mirror.RefObject;

public class LocationManager {
    public static Class<?> TYPE = RefClass.load(LocationManager.class, "android.location.LocationManager");
    public static RefObject<Object> mGnssStatusListenerManager;
    public static RefObject<Map> mGnssNmeaListeners;
    public static RefObject<Map> mGpsNmeaListeners;
    public static RefObject<Map> mListeners;
    public static RefObject<Map> mNmeaListeners;

    public static class GnssStatusListenerManager {
        public static Class<?> TYPE = RefClass.load(GnssStatusListenerManager.class, "android.location.LocationManager$GnssStatusListenerManager");
        public static RefObject<Object> mListenerTransport;
    }

    public static class GnssStatusListener {
        public static Class<?> TYPE = RefClass.load(GnssStatusListener.class, "android.location.LocationManager$GnssStatusListenerManager$GnssStatusListener");
        @MethodParams({long.class, String.class})
        public static RefMethod<Void> onNmeaReceived;
    }

    public static class GnssStatusListenerTransport {
        public static Class<?> TYPE = RefClass.load(GnssStatusListenerTransport.class, "android.location.LocationManager$GnssStatusListenerTransport");
        @MethodParams({long.class, String.class})
        public static RefMethod<Void> onNmeaReceived;
    }

    public static class GpsStatusListenerTransport {
        public static Class<?> TYPE = RefClass.load(GpsStatusListenerTransport.class, "android.location.LocationManager$GpsStatusListenerTransport");
        @MethodParams({long.class, String.class})
        public static RefMethod<Void> onNmeaReceived;
    }

    public static class ListenerTransport {
        public static Class<?> TYPE = RefClass.load(ListenerTransport.class, "android.location.LocationManager$ListenerTransport");
        @MethodParams({Location.class})
        public static RefMethod<Void> onLocationChanged;
    }

    public static class LocationListenerTransport {
        public static Class<?> TYPE = RefClass.load(LocationListenerTransport.class, "android.location.LocationManager$LocationListenerTransport");
        @MethodParams({Location.class})
        public static RefMethod<Void> onLocationChanged;
    }
}