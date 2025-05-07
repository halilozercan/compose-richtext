package com.zachklipp.richtext.sample

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.TextUnit

@Composable
fun SampleTheme(
  colorScheme: ColorScheme = MaterialTheme.colorScheme,
  shapes: Shapes = MaterialTheme.shapes,
  typography: Typography = MaterialTheme.typography,
  content: @Composable () -> Unit
) {
  MaterialTheme(colorScheme, shapes, typography) {
    val textStyle = LocalTextStyle.current.copy(lineHeight = TextUnit.Unspecified)
    CompositionLocalProvider(LocalTextStyle provides textStyle) {
      content()
    }
  }
}