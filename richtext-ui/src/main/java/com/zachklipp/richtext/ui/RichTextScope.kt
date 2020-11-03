@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Providers

/**
 * Scope object for composables that can draw rich text.
 */
@Immutable
public object RichTextScope

/**
 * The current [RichTextStyle].
 */
@Composable
public val RichTextScope.currentRichTextStyle: RichTextStyle
  get() = RichTextStyleAmbient.current

/**
 * Sets the [RichTextStyle] for its [children].
 */
@Composable
public fun RichTextScope.WithStyle(
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
