package com.zachklipp.richtext.sample

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit

@Composable
fun SampleTheme(
  isDarkTheme: Boolean = isSystemInDarkTheme(),
  shapes: Shapes = MaterialTheme.shapes,
  typography: Typography = MaterialTheme.typography,
  content: @Composable () -> Unit
) {
  val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

  val lightColorScheme = lightColorScheme(primary = Color(0xFF1EB980))

  val darkColorScheme = darkColorScheme(primary = Color(0xFF66ffc7))

  val colorScheme =
    when {
      supportsDynamicColor && isDarkTheme -> {
        dynamicDarkColorScheme(LocalContext.current)
      }
      supportsDynamicColor && !isDarkTheme -> {
        dynamicLightColorScheme(LocalContext.current)
      }
      isDarkTheme -> darkColorScheme
      else -> lightColorScheme
    }
  MaterialTheme(colorScheme, shapes, typography) {
    val textStyle = LocalTextStyle.current.copy(lineHeight = TextUnit.Unspecified)
    CompositionLocalProvider(LocalTextStyle provides textStyle) {
      content()
    }
  }
}