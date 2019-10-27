package com.xposed.hook.location;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Lody
 */
public class MockLocationHelper {

    public static void invokeNmeaReceived(Object listener) {
        if (listener != null) {
            VirtualGPSSatalines satalines = VirtualGPSSatalines.get();
            try {
                Location location = LocationHandler.createLocation(LocationHandler.latitude, LocationHandler.longitude);
                if (location != null) {
                    String date = new SimpleDateFormat("HHmmss:SS", Locale.US).format(new Date());
                    String lat = getGPSLat(LocationHandler.latitude);
                    String lon = getGPSLat(LocationHandler.longitude);
                    String latNW = getNorthWest(LocationHandler.latitude);
                    String lonSE = getSouthEast(LocationHandler.longitude);
                    String $GPGGA = checksum(String.format("$GPGGA,%s,%s,%s,%s,%s,1,%s,692,.00,M,.00,M,,,", date, lat, latNW, lon, lonSE, satalines.getSvCount()));
                    String $GPRMC = checksum(String.format("$GPRMC,%s,A,%s,%s,%s,%s,0,0,260717,,,A,", date, lat, latNW, lon, lonSE));
                    if (LocationManager.GnssStatusListenerTransport.onNmeaReceived != null) {
                        LocationManager.GnssStatusListenerTransport.onNmeaReceived.call(listener, System.currentTimeMillis(), "$GPGSV,1,1,04,12,05,159,36,15,41,087,15,19,38,262,30,31,56,146,19,*73");
                        LocationManager.GnssStatusListenerTransport.onNmeaReceived.call(listener, System.currentTimeMillis(), $GPGGA);
                        LocationManager.GnssStatusListenerTransport.onNmeaReceived.call(listener, System.currentTimeMillis(), "$GPVTG,0,T,0,M,0,N,0,K,A,*25");
                        LocationManager.GnssStatusListenerTransport.onNmeaReceived.call(listener, System.currentTimeMillis(), $GPRMC);
                        LocationManager.GnssStatusListenerTransport.onNmeaReceived.call(listener, System.currentTimeMillis(), "$GPGSA,A,2,12,15,19,31,,,,,,,,,604,712,986,*27");
                    } else if (LocationManager.GpsStatusListenerTransport.onNmeaReceived != null) {
                        LocationManager.GpsStatusListenerTransport.onNmeaReceived.call(listener, System.currentTimeMillis(), "$GPGSV,1,1,04,12,05,159,36,15,41,087,15,19,38,262,30,31,56,146,19,*73");
                        LocationManager.GpsStatusListenerTransport.onNmeaReceived.call(listener, System.currentTimeMillis(), $GPGGA);
                        LocationManager.GpsStatusListenerTransport.onNmeaReceived.call(listener, System.currentTimeMillis(), "$GPVTG,0,T,0,M,0,N,0,K,A,*25");
                        LocationManager.GpsStatusListenerTransport.onNmeaReceived.call(listener, System.currentTimeMillis(), $GPRMC);
                        LocationManager.GpsStatusListenerTransport.onNmeaReceived.call(listener, System.currentTimeMillis(), "$GPGSA,A,2,12,15,19,31,,,,,,,,,604,712,986,*27");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void invokeSvStatusChanged(Object transport) {
        if (transport != null) {
            VirtualGPSSatalines satalines = VirtualGPSSatalines.get();
            try {
                Class<?> aClass = transport.getClass();
                int svCount;
                float[] snrs;
                float[] elevations;
                float[] azimuths;
                if (aClass == LocationManager.GnssStatusListenerTransport.TYPE) {
                    svCount = satalines.getSvCount();
                    int[] prnWithFlags = satalines.getPrnWithFlags();
                    snrs = satalines.getSnrs();
                    elevations = satalines.getElevations();
                    azimuths = satalines.getAzimuths();
                    LocationManager.GnssStatusListenerTransport.onSvStatusChanged.call(transport, svCount, prnWithFlags, snrs, elevations, azimuths);
                } else if (aClass == LocationManager.GpsStatusListenerTransport.TYPE) {
                    svCount = satalines.getSvCount();
                    int[] prns = satalines.getPrns();
                    snrs = satalines.getSnrs();
                    elevations = satalines.getElevations();
                    azimuths = satalines.getAzimuths();
                    int ephemerisMask = satalines.getEphemerisMask();
                    int almanacMask = satalines.getAlmanacMask();
                    int usedInFixMask = satalines.getUsedInFixMask();
                    if (LocationManager.GpsStatusListenerTransport.onSvStatusChanged != null) {
                        LocationManager.GpsStatusListenerTransport.onSvStatusChanged.call(transport, svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
                    } else if (LocationManager.GpsStatusListenerTransportVIVO.onSvStatusChanged != null) {
                        LocationManager.GpsStatusListenerTransportVIVO.onSvStatusChanged.call(transport, svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask, new long[svCount]);
                    } else if (LocationManager.GpsStatusListenerTransportSumsungS5.onSvStatusChanged != null) {
                        LocationManager.GpsStatusListenerTransportSumsungS5.onSvStatusChanged.call(transport, svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask, new int[svCount]);
                    } else if (LocationManager.GpsStatusListenerTransportOPPO_R815T.onSvStatusChanged != null) {
                        int len = prns.length;
                        int[] ephemerisMasks = new int[len];
                        for (int i = 0; i < len; i++) {
                            ephemerisMasks[i] = satalines.getEphemerisMask();
                        }
                        int[] almanacMasks = new int[len];
                        for (int i = 0; i < len; i++) {
                            almanacMasks[i] = satalines.getAlmanacMask();
                        }
                        int[] usedInFixMasks = new int[len];
                        for (int i = 0; i < len; i++) {
                            usedInFixMasks[i] = satalines.getUsedInFixMask();
                        }
                        LocationManager.GpsStatusListenerTransportOPPO_R815T.onSvStatusChanged.call(transport, svCount, prns, snrs, elevations, azimuths, ephemerisMasks, almanacMasks, usedInFixMasks, svCount);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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