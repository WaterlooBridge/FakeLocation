package com.xposed.hook;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xposed.hook.config.Constants;
import com.xposed.hook.config.PkgConfig;
import com.xposed.hook.entity.AppInfo;

public class RimetActivity extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences sp;

    private TabLayout tabLayout;
    private TabLayout.Tab gpsTab;
    private TabLayout.Tab cellTab;

    private View ll_gps;
    private EditText etLatitude;
    private EditText etLongitude;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private Button btnAutoFillGps;

    private View ll_cell;
    private EditText etLac;
    private EditText etCid;
    private TextView tvLac;
    private TextView tvCid;
    private Button btnAutoFillCell;

    private CompoundButton cb;

    private AppInfo appInfo;
    private boolean isDingTalk;

    TelephonyManager tm;
    GsmCellLocation l;
    LocationManager lm;
    Location gpsL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appInfo = (AppInfo) getIntent().getSerializableExtra("appInfo");
        if (appInfo == null)
            return;
        setContentView(R.layout.activity_rimet);
        setTitle(appInfo.title);
        isDingTalk = PkgConfig.pkg_dingtalk.equals(appInfo.packageName);
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        String prefix = appInfo.packageName + "_";
        sp = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_WORLD_READABLE);
        String defaultLatitude = Constants.DEFAULT_LATITUDE;
        String defaultLongitude = Constants.DEFAULT_LONGITUDE;
        if (isDingTalk) {
            defaultLatitude = "";
            defaultLongitude = "";
        }

        ll_gps = findViewById(R.id.ll_gps);
        etLatitude = (EditText) findViewById(R.id.et_latitude);
        etLongitude = (EditText) findViewById(R.id.et_longitude);
        tvLatitude = (TextView) findViewById(R.id.tv_latitude);
        tvLongitude = (TextView) findViewById(R.id.tv_longitude);
        btnAutoFillGps = (Button) findViewById(R.id.btn_auto_fill_gps);
        etLatitude.setText(sp.getString(prefix + "latitude", defaultLatitude));
        etLongitude.setText(sp.getString(prefix + "longitude", defaultLongitude));
        btnAutoFillGps.setOnClickListener(this);

        ll_cell = findViewById(R.id.ll_cell);
        etLac = (EditText) findViewById(R.id.et_lac);
        etCid = (EditText) findViewById(R.id.et_cid);
        tvLac = (TextView) findViewById(R.id.tv_lac);
        tvCid = (TextView) findViewById(R.id.tv_cid);
        btnAutoFillCell = (Button) findViewById(R.id.btn_auto_fill_cell);
        int lac = sp.getInt(prefix + "lac", Constants.DEFAULT_LAC);
        int cid = sp.getInt(prefix + "cid", Constants.DEFAULT_CID);
        if (lac != Constants.DEFAULT_LAC)
            etLac.setText(String.valueOf(lac));
        if (cid != Constants.DEFAULT_CID)
            etCid.setText(String.valueOf(cid));
        btnAutoFillCell.setOnClickListener(this);

        initTabLayout();
        cb = (CompoundButton) findViewById(R.id.cb);
        cb.setChecked(sp.getBoolean(appInfo.packageName, false));
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_reboot_app).setOnClickListener(this);

        requestPermissions();
    }

    private void initTabLayout() {
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        gpsTab = tabLayout.newTab().setText(R.string.gps_location);
        cellTab = tabLayout.newTab().setText(R.string.cell_location);
        if (isDingTalk) {
            tabLayout.addTab(cellTab);
            tabLayout.addTab(gpsTab);
            ll_gps.setVisibility(View.GONE);
        } else {
            tabLayout.addTab(gpsTab);
            tabLayout.addTab(cellTab);
            ll_cell.setVisibility(View.GONE);
        }
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ll_gps.setVisibility(tab == gpsTab ? View.VISIBLE : View.GONE);
                ll_cell.setVisibility(tab == gpsTab ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_auto_fill_cell:
                etLac.setText(String.valueOf(l.getLac()));
                etCid.setText(String.valueOf(l.getCid()));
                break;
            case R.id.btn_auto_fill_gps:
                etLatitude.setText(String.valueOf(gpsL.getLatitude()));
                etLongitude.setText(String.valueOf(gpsL.getLongitude()));
                break;
            case R.id.btn_save:
                String prefix = appInfo.packageName + "_";
                sp.edit().putString(prefix + "latitude", etLatitude.getText().toString())
                        .putString(prefix + "longitude", etLongitude.getText().toString())
                        .putInt(prefix + "lac", parseInt(etLac.getText().toString()))
                        .putInt(prefix + "cid", parseInt(etCid.getText().toString()))
                        .putBoolean(appInfo.packageName, cb.isChecked())
                        .commit();
                Toast.makeText(getApplicationContext(), R.string.save_success, Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_reboot_app:
                try {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", appInfo.packageName, null));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
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
        stopLocation();
        super.finish();
    }

    PhoneStateListener listener = new PhoneStateListener() {

        @Override
        public void onCellLocationChanged(CellLocation location) {
            if (location instanceof GsmCellLocation) {
                l = (GsmCellLocation) location;
                tvLac.setText(getString(R.string.current_lac, String.valueOf(l.getLac())));
                tvCid.setText(getString(R.string.current_cid, String.valueOf(l.getCid())));
                btnAutoFillCell.setVisibility(View.VISIBLE);
            }
        }
    };

    LocationListener gpsListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            gpsL = location;
            tvLatitude.setText(getString(R.string.current_latitude, String.valueOf(location.getLatitude())));
            tvLongitude.setText(getString(R.string.current_longitude, String.valueOf(location.getLongitude())));
            btnAutoFillGps.setVisibility(View.VISIBLE);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }
        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        }
    }

    private void startLocation() {
        tm.listen(listener, PhoneStateListener.LISTEN_CELL_LOCATION);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, gpsListener, null);
    }

    private void stopLocation() {
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        lm.removeUpdates(gpsListener);
    }
}
