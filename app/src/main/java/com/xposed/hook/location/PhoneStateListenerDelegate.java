package com.xposed.hook.location;

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.gsm.GsmCellLocation;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Created by lin on 2018/1/25.
 */

public class PhoneStateListenerDelegate extends PhoneStateListener {

    private static WeakHashMap<PhoneStateListener, WeakReference<PhoneStateListener>> map = new WeakHashMap<>();

    public static PhoneStateListener convert(PhoneStateListener listener, int lac, int cid) {
        PhoneStateListener delegate;
        WeakReference<PhoneStateListener> wr = map.get(listener);
        if (wr == null || wr.get() == null) {
            delegate = new PhoneStateListenerDelegate(listener, lac, cid);
            map.put(listener, new WeakReference<>(delegate));
        } else {
            delegate = wr.get();
        }
        return delegate;
    }

    private PhoneStateListener delegate;
    private int lac;
    private int cid;

    private PhoneStateListenerDelegate(PhoneStateListener delegate, int lac, int cid) {
        this.delegate = delegate;
        this.lac = lac;
        this.cid = cid;
    }

    @Override
    public void onCellLocationChanged(CellLocation location) {
        if (location instanceof GsmCellLocation) {
            ((GsmCellLocation) location).setLacAndCid(lac, cid);
            delegate.onCellLocationChanged(location);
        }
    }
}
