@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle

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
 * The current [TextStyle].
 */
public val RichTextScope.currentBasicTextStyle: TextStyle
  @Composable get() = with(LocalBasicTextStyle.current) {
    copy(color = color.takeOrElse { LocalContentColor.current })
  }

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

/**
 * Some RichText components receive `text: String` parameters and directly
 * render the content as a comfortable alternative to `@Composable` block.
 *
 * These components use [androidx.compose.foundation.text.BasicText] to render
 * the received content. However, BasicText has no notion of provided text style.
 *
 * [RichTextScope.BasicText] provides an alternative BasicText which takes
 * [currentRichTextStyle] into consideration. It's only meant for internal usage.
 */
@Composable
internal fun RichTextScope.BasicText(text: String) {
  androidx.compose.foundation.text.BasicText(text = text, style = currentBasicTextStyle)
}
