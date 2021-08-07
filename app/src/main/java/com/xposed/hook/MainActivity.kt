package com.xposed.hook

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.xposed.hook.entity.AppInfo
import com.xposed.hook.extension.dpInPx
import com.xposed.hook.extension.toBitmap
import com.xposed.hook.theme.AppTheme
import com.xposed.hook.utils.AppHelper
import kotlinx.coroutines.launch

/**
 * Created by lin on 2021/8/7.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val list = AppHelper.getAppList()
            setContent { AppList(list) }
        }
    }

    @Composable
    fun AppList(list: List<AppInfo>) {
        AppTheme {
            LazyColumn {
                items(list) { item ->
                    AppItem(item)
                }
            }
        }
    }

    @Composable
    fun AppItem(item: AppInfo) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable {
                    startActivity(Intent(this, RimetActivity::class.java).putExtra("appInfo", item))
                }
                .padding(12.dp, 12.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Image(item.icon.toBitmap(36.dpInPx, 36.dpInPx), item.title)
            Column(Modifier.padding(10.dp, 0.dp)) {
                Text(item.title)
                Text(item.packageName, Modifier.padding(0.dp, 3.dp, 0.dp, 0.dp), fontSize = 12.sp)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.item_luck_money) {
            startActivity(Intent(this, LuckMoneySetting::class.java))
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}