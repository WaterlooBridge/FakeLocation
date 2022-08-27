package com.xposed.hook

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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

    private var appList by mutableStateOf(emptyList<AppInfo>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppScaffold(appList) }
        lifecycleScope.launch {
            appList = AppHelper.getAppList()
        }
    }

    @Composable
    fun AppScaffold(list: List<AppInfo>) {
        AppTheme {
            Column {
                var textState by remember { mutableStateOf(TextFieldValue()) }
                UserInputText(onTextChanged = {
                    textState = it
                }, textFieldValue = textState)
                AppList(if (textState.text.isNotEmpty()) list.filter { info ->
                    info.title.contains(textState.text, true)
                } else list)
            }
        }
    }

    @Composable
    fun AppList(list: List<AppInfo>) {
        LazyColumn {
            items(list) { item ->
                AppItem(item)
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

    @Composable
    private fun UserInputText(
        keyboardType: KeyboardType = KeyboardType.Text,
        onTextChanged: (TextFieldValue) -> Unit,
        textFieldValue: TextFieldValue
    ) {
        Surface {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(64.dp)
                        .weight(1f)
                        .align(Alignment.Bottom)
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { onTextChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp)
                            .align(Alignment.CenterStart),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = keyboardType,
                            imeAction = ImeAction.Search
                        ),
                        maxLines = 1,
                        cursorBrush = SolidColor(LocalContentColor.current),
                        textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current)
                    )

                    val disableContentColor = MaterialTheme.colors.onSurface
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 32.dp),
                            text = stringResource(id = R.string.search_app),
                            style = MaterialTheme.typography.body1.copy(color = disableContentColor)
                        )
                    }
                }
                IconButton(onClick = {
                    startActivity(
                        Intent(this@MainActivity, LuckMoneySetting::class.java)
                    )
                }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.wechat_hook)
                    )
                }
            }
        }
    }
}