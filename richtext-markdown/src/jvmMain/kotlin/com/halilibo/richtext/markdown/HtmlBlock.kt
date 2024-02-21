package com.halilibo.richtext.markdown

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
public actual fun HtmlBlock(content: String) {
  DisposableEffect(Unit) {
    println("Html blocks are rendered literally in Compose Desktop!")
    onDispose {  }
  }
  BasicText(content)
}
