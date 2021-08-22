package com.halilibo.richtext.ui.previews

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.halilibo.richtext.ui.BlockQuote
import com.halilibo.richtext.ui.LocalInternalContentColor
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.Text

@Preview
@Composable
private fun BlockQuotePreviewOnWhite() {
  BlockQuotePreview(backgroundColor = Color.White, contentColor = Color.Black)
}

@Preview
@Composable private fun BlockQuotePreviewOnBlack() {
  BlockQuotePreview(backgroundColor = Color.Black, contentColor = Color.White)
}

@Composable private fun BlockQuotePreview(
  backgroundColor: Color,
  contentColor: Color
) {
  CompositionLocalProvider(LocalInternalContentColor provides contentColor) {
    Box(Modifier.background(backgroundColor)) {
      RichTextScope.BlockQuote {
        Text("Some text.")
        Text("Another paragraph.")
        BlockQuote {
          Text("Nested block quote.")
        }
      }
    }
  }
}
