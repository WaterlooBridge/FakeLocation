package com.xposed.hook;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class RimetActivity extends Activity {

    private SharedPreferences sp;

    private EditText etLatitude;
    private EditText etLongitude;
    private EditText etLac;
    private EditText etCid;
    private CheckBox cb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rimet);
        setTitle("钉钉");
        etLatitude = (EditText) findViewById(R.id.et_latitude);
        etLongitude = (EditText) findViewById(R.id.et_longitude);
        etLac = (EditText) findViewById(R.id.et_lac);
        etCid = (EditText) findViewById(R.id.et_cid);
        cb = (CheckBox) findViewById(R.id.cb);
        sp = getSharedPreferences("location", MODE_WORLD_READABLE);
        etLatitude.setText(sp.getString("dingding_latitude", "34.752600"));
        etLongitude.setText(sp.getString("dingding_longitude", "113.662000"));
        etLac.setText(String.valueOf(sp.getInt("dingding_lac", -1)));
        etCid.setText(String.valueOf(sp.getInt("dingding_cid", -1)));
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
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return -1;
        }
    }
}
