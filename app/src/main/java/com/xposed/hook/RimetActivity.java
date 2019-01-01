package com.xposed.hook;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class RimetActivity extends AppCompatActivity {

    private SharedPreferences sp;

    private EditText etLatitude;
    private EditText etLongitude;
    private EditText etLac;
    private EditText etCid;
    private CheckBox cb;
    private TextView tvLac;
    private TextView tvCid;
    private Button btnAutoFill;

    TelephonyManager tm;
    GsmCellLocation l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rimet);
        setTitle("钉钉");
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        etLatitude = (EditText) findViewById(R.id.et_latitude);
        etLongitude = (EditText) findViewById(R.id.et_longitude);
        etLac = (EditText) findViewById(R.id.et_lac);
        etCid = (EditText) findViewById(R.id.et_cid);
        cb = (CheckBox) findViewById(R.id.cb);
        tvLac = (TextView) findViewById(R.id.tv_lac);
        tvCid = (TextView) findViewById(R.id.tv_cid);
        btnAutoFill = (Button) findViewById(R.id.btn_auto_fill);
        sp = getSharedPreferences("location", MODE_WORLD_READABLE);
        etLatitude.setText(sp.getString("dingding_latitude", "0"));
        etLongitude.setText(sp.getString("dingding_longitude", "0"));
        int lac = sp.getInt("dingding_lac", -1);
        int cid = sp.getInt("dingding_cid", -1);
        if (lac != -1)
            etLac.setText(String.valueOf(lac));
        if (cid != -1)
            etCid.setText(String.valueOf(cid));
        cb.setChecked(sp.getBoolean(PkgConfig.pkg_dingding, false));
        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.edit().putString("dingding_latitude", etLatitude.getText().toString())
                        .putString("dingding_longitude", etLongitude.getText().toString())
                        .putInt("dingding_lac", parseInt(etLac.getText().toString()))
                        .putInt("dingding_cid", parseInt(etCid.getText().toString()))
                        .putBoolean(PkgConfig.pkg_dingding, cb.isChecked())
                        .commit();
                finish();
            }
        });
        btnAutoFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etLac.setText(String.valueOf(l.getLac()));
                etCid.setText(String.valueOf(l.getCid()));
            }
        });
        requestPermissions();
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public void finish() {
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        super.finish();
    }

    PhoneStateListener listener = new PhoneStateListener() {

        @Override
        public void onCellLocationChanged(CellLocation location) {
            if (location instanceof GsmCellLocation) {
                l = (GsmCellLocation) location;
                tvLac.setText("Current Lac:" + l.getLac());
                tvCid.setText("Current Cid:" + l.getCid());
                btnAutoFill.setVisibility(View.VISIBLE);
            }
        }
    };

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        else
            tm.listen(listener, PhoneStateListener.LISTEN_CELL_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            tm.listen(listener, PhoneStateListener.LISTEN_CELL_LOCATION);
    }
}
