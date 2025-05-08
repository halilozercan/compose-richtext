package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import com.halilibo.richtext.ui.RichTextScope

/**
 * Android and JVM can have different WebView or HTML rendering implementations.
 * We are leaving HTML rendering to platform side.
 */
@Composable
internal expect fun RichTextScope.HtmlBlock(content: String)
