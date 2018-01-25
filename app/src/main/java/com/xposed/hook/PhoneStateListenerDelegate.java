package com.xposed.hook;

import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.gsm.GsmCellLocation;

/**
 * Created by lin on 2018/1/25.
 */

public class PhoneStateListenerDelegate extends PhoneStateListener {

    private PhoneStateListener delegate;
    private int lac;
    private int cid;

    public PhoneStateListenerDelegate(PhoneStateListener delegate, int lac, int cid) {
        this.delegate = delegate;
        this.lac = lac;
        this.cid = cid;
    }

    @Override
    public void onCellLocationChanged(CellLocation location) {
        if(location instanceof GsmCellLocation) {
            ((GsmCellLocation) location).setLacAndCid(lac, cid);
            delegate.onCellLocationChanged(location);
        }
    }
}
