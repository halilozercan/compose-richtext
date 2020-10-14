@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Providers

/**
 * Marker interface for Composable functions that can draw rich text.
 */
@Immutable
interface RichTextScope {
  companion object {
    operator fun invoke(): RichTextScope = object: RichTextScope {}
  }
}

/**
 * The current [RichTextStyle].
 */
@Composable
val RichTextScope.currentRichTextStyle: RichTextStyle
  get() = RichTextStyleAmbient.current

/**
 * Sets the [RichTextStyle] for its [children].
 */
@Composable
fun RichTextScope.WithStyle(
  style: RichTextStyle?,
  children: @Composable RichTextScope.() -> Unit
) {
  if (style == null) {
    children()
  } else {
    val mergedStyle = RichTextStyleAmbient.current.merge(style)
    Providers(RichTextStyleAmbient provides mergedStyle) {
      children()
    }
  }
}
