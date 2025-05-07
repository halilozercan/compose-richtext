package com.halilibo.richtext.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun RichTextScope.CodeBlockLayout(
  wordWrap: Boolean,
  children: @Composable RichTextScope.(Modifier) -> Unit
) {
  if (!wordWrap) {
    val scrollState = rememberScrollState()
    children(Modifier.horizontalScroll(scrollState))
  } else {
    children(Modifier)
  }
}