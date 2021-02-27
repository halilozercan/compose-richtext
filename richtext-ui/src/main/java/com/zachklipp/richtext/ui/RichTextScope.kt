@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable

/**
 * Scope object for composables that can draw rich text.
 */
@Immutable
public object RichTextScope

/**
 * The current [RichTextStyle].
 */
public val RichTextScope.currentRichTextStyle: RichTextStyle
  @Composable get() = LocalRichTextStyle.current

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
    val mergedStyle = LocalRichTextStyle.current.merge(style)
    CompositionLocalProvider(LocalRichTextStyle provides mergedStyle) {
      children()
    }
  }
}
