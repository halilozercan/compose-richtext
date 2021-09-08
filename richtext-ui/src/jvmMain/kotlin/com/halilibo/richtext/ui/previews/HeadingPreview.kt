package com.halilibo.richtext.ui.previews

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.halilibo.richtext.ui.Heading
import com.halilibo.richtext.ui.LocalInternalContentColor
import com.halilibo.richtext.ui.RichTextScope

@Preview
@Composable
private fun HeadingPreviewOnWhite() {
  HeadingPreview(backgroundColor = Color.White, contentColor = Color.Black)
}

@Preview
@Composable
private fun HeadingPreviewOnBlack() {
  HeadingPreview(backgroundColor = Color.Black, contentColor = Color.White)
}

@Composable
private fun HeadingPreview(
  backgroundColor: Color,
  contentColor: Color
) {
  CompositionLocalProvider(LocalInternalContentColor provides contentColor) {
    Box(Modifier.background(color = backgroundColor)) {
      Column {
        for (level in 0 until 10) {
          RichTextScope.Heading(level, "Heading ${level + 1}")
        }
      }
    }
  }
}
