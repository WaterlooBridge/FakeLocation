package com.xposed.hook

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xposed.hook.theme.AppTheme
import com.xposed.hook.utils.SharedPreferencesHelper
import com.xposed.hook.wechat.LuckyMoneyHook

/**
 * Created by lin on 2018/2/4.
 */
class LuckMoneySetting : AppCompatActivity() {

    private lateinit var sp: SharedPreferences
    private lateinit var luckyMoneyDelay: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.wechat_hook)

        sp = getSharedPreferences("lucky_money", MODE_PRIVATE)
        luckyMoneyDelay = sp.getInt("lucky_money_delay", 0).toString()

        setContent {
            LuckyMoney()
        }
    }

    @Composable
    fun LuckyMoney() {
        var quickOpen by remember { mutableStateOf(sp.getBoolean("quick_open", false)) }
        var autoReceive by remember { mutableStateOf(sp.getBoolean("auto_receive", false)) }
        var recalled by remember { mutableStateOf(sp.getBoolean("recalled", false)) }
        var momentsLimit by remember { mutableStateOf(sp.getBoolean("3_days_Moments", false)) }
        var delay by remember { mutableStateOf(luckyMoneyDelay) }
        AppTheme {
            Column(modifier = Modifier.padding(start = 15.dp, end = 15.dp)) {
                Row(
                    modifier = Modifier.height(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.quick_open_lucky_money),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(quickOpen, {
                        quickOpen = it
                        sp.edit().putBoolean("quick_open", it).apply()
                    })
                }
                Row(
                    modifier = Modifier.height(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.show_lucky_money_coming_toast),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(autoReceive, {
                        autoReceive = it
                        sp.edit().putBoolean("auto_receive", it).apply()
                    })
                }
                Row(
                    modifier = Modifier.height(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.prevent_message_recalled),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(recalled, {
                        recalled = it
                        sp.edit().putBoolean("recalled", it).apply()
                    })
                }
                Row(
                    modifier = Modifier.height(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.disable_3_days_of_Moments_limit),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(momentsLimit, {
                        momentsLimit = it
                        sp.edit().putBoolean("3_days_Moments", it).apply()
                    })
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = delay, onValueChange = {
                    luckyMoneyDelay = it
                    delay = it
                }, label = { Text(text = stringResource(R.string.lucky_money_delay)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(Modifier.height(30.dp))
                Button(
                    onClick = {
                        saveLuckyMoneyDelay()
                        try {
                            val intent = Intent()
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            intent.data =
                                Uri.fromParts("package", LuckyMoneyHook.WECHAT_PACKAGE_NAME, null)
                            startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    Text(text = stringResource(R.string.reboot_app), color = Color.White)
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        saveLuckyMoneyDelay()
    }

    private fun saveLuckyMoneyDelay() {
        try {
            sp.edit().putInt("lucky_money_delay", luckyMoneyDelay.toInt()).commit()
            SharedPreferencesHelper.makeWorldReadable(sp)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}