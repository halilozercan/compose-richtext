package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable

/**
 * Android and JVM can have different WebView or HTML rendering implementations.
 * We are leaving HTML rendering to platform side.
 */
@Composable
public expect fun HtmlBlock(content: String)
