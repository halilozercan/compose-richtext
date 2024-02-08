package com.halilibo.richtext.ui

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

private val LocalScrollbarEnabled = compositionLocalOf { true }

@Composable
internal actual fun RichTextScope.CodeBlockLayout(
  wordWrap: Boolean,
  children: @Composable RichTextScope.(Modifier) -> Unit
) {
  if (!wordWrap) {
    val scrollState = rememberScrollState()
    Column {
      children(Modifier.horizontalScroll(scrollState))
      if (LocalScrollbarEnabled.current) {
        val horizontalScrollbarAdapter = rememberScrollbarAdapter(scrollState)
        HorizontalScrollbar(adapter = horizontalScrollbarAdapter)
      }
    }
  } else {
    children(Modifier)
  }
}

/**
 * Contextually disables scrollbar for Desktop CodeBlocks under [content] tree.
 */
@Composable
public fun DisableScrollbar(
  content: @Composable () -> Unit
) {
  CompositionLocalProvider(LocalScrollbarEnabled provides false) {
    content()
  }
}
