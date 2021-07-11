package com.xposed.hook.location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mirror.RefMethod;

/**
 * @author Lody
 */
public class MockLocationHelper {

    public static void invokeNmeaReceived(Object listener) {
        if (listener == null)
            return;
        RefMethod<Void> method;
        if (LocationManager.GnssStatusListener.onNmeaReceived != null)
            method = LocationManager.GnssStatusListener.onNmeaReceived;
        else if (LocationManager.GnssStatusListenerTransport.onNmeaReceived != null)
            method = LocationManager.GnssStatusListenerTransport.onNmeaReceived;
        else
            method = LocationManager.GpsStatusListenerTransport.onNmeaReceived;
        if (method == null)
            return;
        VirtualGPSSatalines satalines = VirtualGPSSatalines.get();
        String date = new SimpleDateFormat("HHmmss:SS", Locale.US).format(new Date());
        String lat = getGPSLat(LocationHandler.latitude);
        String lon = getGPSLat(LocationHandler.longitude);
        String latNW = getNorthWest(LocationHandler.latitude);
        String lonSE = getSouthEast(LocationHandler.longitude);
        String $GPGGA = checksum(String.format("$GPGGA,%s,%s,%s,%s,%s,1,%s,692,.00,M,.00,M,,,", date, lat, latNW, lon, lonSE, satalines.getSvCount()));
        String $GPRMC = checksum(String.format("$GPRMC,%s,A,%s,%s,%s,%s,0,0,260717,,,A,", date, lat, latNW, lon, lonSE));
        callNmeaReceived(method, listener, "$GPGSV,1,1,04,12,05,159,36,15,41,087,15,19,38,262,30,31,56,146,19,*73");
        callNmeaReceived(method, listener, $GPGGA);
        callNmeaReceived(method, listener, "$GPVTG,0,T,0,M,0,N,0,K,A,*25");
        callNmeaReceived(method, listener, $GPRMC);
        callNmeaReceived(method, listener, "$GPGSA,A,2,12,15,19,31,,,,,,,,,604,712,986,*27");
    }

    private static void callNmeaReceived(RefMethod<Void> method, Object listener, String nmea) {
        method.call(listener, System.currentTimeMillis(), nmea);
    }

    private static String getSouthEast(double longitude) {
        if (longitude > 0.0d) {
            return "E";
        }
        return "W";
    }

    private static String getNorthWest(double latitude) {
        if (latitude > 0.0d) {
            return "N";
        }
        return "S";
    }

    public static String getGPSLat(double v) {
        int du = (int) v;
        double fen = (v - (double) du) * 60.0d;
        return du + leftZeroPad((int) fen, 2) + ":" + String.valueOf(fen).substring(2);
    }

    private static String leftZeroPad(int num, int size) {
        return leftZeroPad(String.valueOf(num), size);
    }

    private static String leftZeroPad(String num, int size) {
        StringBuilder sb = new StringBuilder(size);
        int i;
        if (num == null) {
            for (i = 0; i < size; i++) {
                sb.append('0');
            }
        } else {
            for (i = 0; i < size - num.length(); i++) {
                sb.append('0');
            }
            sb.append(num);
        }
        return sb.toString();
    }

    public static String checksum(String nema) {
        String checkStr = nema;
        if (nema.startsWith("$")) {
            checkStr = nema.substring(1);
        }
        int sum = 0;
        for (int i = 0; i < checkStr.length(); i++) {
            sum ^= (byte) checkStr.charAt(i);
        }
        return nema + "*" + String.format("%02X", sum).toLowerCase();
    }
}