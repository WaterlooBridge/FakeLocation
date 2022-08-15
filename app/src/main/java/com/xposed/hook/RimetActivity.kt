package com.xposed.hook

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telephony.CellLocation
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.telephony.gsm.GsmCellLocation
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.xposed.hook.config.Constants
import com.xposed.hook.config.PkgConfig
import com.xposed.hook.entity.AppInfo
import com.xposed.hook.theme.AppTheme
import com.xposed.hook.utils.SharedPreferencesHelper

class RimetActivity : AppCompatActivity() {

    private lateinit var sp: SharedPreferences
    private lateinit var appInfo: AppInfo
    private var isDingTalk = false

    private lateinit var tm: TelephonyManager
    private lateinit var l: GsmCellLocation
    private lateinit var lm: LocationManager
    private lateinit var gpsL: Location

    private val _currentLatitude = MutableLiveData("")
    private val _currentLongitude = MutableLiveData("")
    private val _currentLac = MutableLiveData("")
    private val _currentCid = MutableLiveData("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appInfo = intent.getSerializableExtra("appInfo") as? AppInfo ?: return
        title = appInfo.title
        isDingTalk = PkgConfig.pkg_dingtalk == appInfo.packageName
        tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        lm = getSystemService(LOCATION_SERVICE) as LocationManager

        sp = getSharedPreferences(Constants.PREF_FILE_NAME, MODE_PRIVATE)

        setContent { Container() }

        requestPermissions()
    }

    @Composable
    fun Container() {
        val prefix = appInfo.packageName + "_"
        val defaultLatitude = if (isDingTalk) "" else Constants.DEFAULT_LATITUDE
        val defaultLongitude = if (isDingTalk) "" else Constants.DEFAULT_LONGITUDE
        var latitude by remember {
            mutableStateOf(sp.getString(prefix + "latitude", null) ?: defaultLatitude)
        }
        var longitude by remember {
            mutableStateOf(sp.getString(prefix + "longitude", null) ?: defaultLongitude)
        }
        var lac by remember {
            sp.getInt(prefix + "lac", Constants.DEFAULT_LAC).let {
                mutableStateOf(if (it == Constants.DEFAULT_LAC) "" else it.toString())
            }
        }
        var cid by remember {
            sp.getInt(prefix + "cid", Constants.DEFAULT_CID).let {
                mutableStateOf(if (it == Constants.DEFAULT_CID) "" else it.toString())
            }
        }
        var isChecked by remember {
            mutableStateOf(sp.getBoolean(appInfo.packageName, false))
        }
        val currentLatitude by _currentLatitude.observeAsState("")
        val currentLongitude by _currentLongitude.observeAsState("")
        val currentLac by _currentLac.observeAsState("")
        val currentCid by _currentCid.observeAsState("")

        AppTheme {
            Column(
                Modifier
                    .padding(15.dp, 0.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.gps_location),
                    modifier = Modifier.padding(0.dp, 16.dp),
                    color = colorResource(R.color.textColorPrimary)
                )
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text(text = "latitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(5.dp))
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text(text = "longitude") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.current_latitude, currentLatitude))
                        Text(text = stringResource(R.string.current_longitude, currentLongitude))
                    }
                    if (currentLatitude.isNotEmpty())
                        TextButton(onClick = {
                            latitude = currentLatitude
                            longitude = currentLongitude
                        }) {
                            Text(
                                text = stringResource(R.string.auto_fill),
                                color = colorResource(R.color.textColorPrimary)
                            )
                        }
                }
                Text(
                    text = stringResource(R.string.cell_location),
                    modifier = Modifier.padding(0.dp, 16.dp),
                    color = colorResource(R.color.textColorPrimary)
                )
                OutlinedTextField(
                    value = lac,
                    onValueChange = { lac = it },
                    label = { Text(text = "lac") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(5.dp))
                OutlinedTextField(
                    value = cid,
                    onValueChange = { cid = it },
                    label = { Text(text = "cid") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.current_lac, currentLac))
                        Text(text = stringResource(R.string.current_cid, currentCid))
                    }
                    if (currentLac.isNotEmpty())
                        TextButton(onClick = {
                            lac = currentLac
                            cid = currentCid
                        }) {
                            Text(
                                text = stringResource(R.string.auto_fill),
                                color = colorResource(R.color.textColorPrimary)
                            )
                        }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.height(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.open_location_hook),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = isChecked, onCheckedChange = { isChecked = it })
                }
                Row(Modifier.padding(0.dp, 16.dp)) {
                    Button(
                        onClick = {
                            sp.edit().putString(prefix + "latitude", latitude)
                                .putString(prefix + "longitude", longitude)
                                .putInt(prefix + "lac", parseInt(lac))
                                .putInt(prefix + "cid", parseInt(cid))
                                .putLong(prefix + "time", System.currentTimeMillis())
                                .putBoolean(appInfo.packageName, isChecked)
                                .commit()
                            SharedPreferencesHelper.makeWorldReadable(sp)
                            Toast.makeText(
                                applicationContext,
                                R.string.save_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00975C)),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                    ) {
                        Text(text = stringResource(R.string.save))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            try {
                                val intent = Intent()
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                intent.data = Uri.fromParts("package", appInfo.packageName, null)
                                startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE9686B)),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                    ) {
                        Text(text = stringResource(R.string.reboot_app))
                    }
                }
            }
        }
    }

    private fun parseInt(str: String): Int {
        return try {
            str.toInt()
        } catch (e: Exception) {
            -1
        }
    }

    override fun finish() {
        stopLocation()
        super.finish()
    }

    private var listener: PhoneStateListener = object : PhoneStateListener() {
        override fun onCellLocationChanged(location: CellLocation) {
            if (location is GsmCellLocation) {
                l = location
                _currentLac.value = l.lac.toString()
                _currentCid.value = l.cid.toString()
            }
        }
    }

    private var gpsListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            gpsL = location
            _currentLatitude.value = location.latitude.toString()
            _currentLongitude.value = location.longitude.toString()
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), 101
            )
            return
        }
        startLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocation()
        }
    }

    private fun startLocation() {
        tm.listen(listener, PhoneStateListener.LISTEN_CELL_LOCATION)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, gpsListener, null)
    }

    private fun stopLocation() {
        tm.listen(listener, PhoneStateListener.LISTEN_NONE)
        lm.removeUpdates(gpsListener)
    }
}