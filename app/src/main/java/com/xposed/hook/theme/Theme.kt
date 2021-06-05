package com.xposed.hook.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Created by lin on 2021/6/5.
 */

private val LightThemeColors = lightColors(
    primary = pink500,
    primaryVariant = pink600,
    onPrimary = Color.Black,
    secondary = pink500,
    secondaryVariant = pink600,
    onSecondary = Color.Black
)

private val DarkThemeColors = darkColors(
    primary = pink200,
    secondary = pink200,
    surface = pinkDarkPrimary
)

private val LightTextStyle = TextStyle(
    color = Color(0xff333333)
)

private val DarkTextStyle = TextStyle(
    color = Color.White
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    ProvideTextStyle(if (darkTheme) DarkTextStyle else LightTextStyle) {
        MaterialTheme(
            colors = if (darkTheme) DarkThemeColors else LightThemeColors,
            content = content
        )
    }
}

