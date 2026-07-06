package com.halilibo.richtext.markdown

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import com.halilibo.richtext.ui.RichTextScope

@Composable
internal actual fun RichTextScope.HtmlBlock(content: String) {
  BasicText(content)
}
