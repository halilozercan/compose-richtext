package com.halilibo.richtext.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
public actual fun RichTextScope.CodeBlockLayout(
  modifier: Modifier,
  wordWrap: Boolean,
  children: @Composable RichTextScope.(Modifier) -> Unit
) {
  if (!wordWrap) {
    val scrollState = rememberScrollState()
    children(
      modifier.horizontalScroll(scrollState)
    )
  } else {
    children(modifier)
  }
}