@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * Scope object for composables that can draw rich text.
 *
 * Even [BasicRichText] is called from a RichTextScope to point
 * the fact that [RichTextScope] is a corner stone for this library.
 *
 * RichTextScope facilitates a context for RichText elements. It does not
 * behave like a [State] or a [CompositionLocal]. Starting from [BasicRichText],
 * this scope carries information that should not be passed down as a state.
 *
 * Developers who are looking to wrap [BasicRichText] within their own design
 * should start from here.
 *
 * @param textStyle Returns the current text style.
 * @param ProvideTextStyle RichText sometimes updates the current text style
 * e.g. Heading, CodeBlock, and etc. New style should be passed to the outer
 * theming to indicate that there is a need for update, so that children Text
 * composables use the correct styling.
 * @param contentColor Returns the current content color.
 * @param ProvideContentColor Similar to [ProvideTextStyle], does the same job
 * for content color.
 */
@Immutable
public data class RichTextScope(
  public val textStyle: @Composable () -> TextStyle,
  public val ProvideTextStyle: @Composable (TextStyle, @Composable () -> Unit) -> Unit,
  public val contentColor: @Composable () -> Color,
  public val ProvideContentColor: @Composable (Color, @Composable () -> Unit) -> Unit
) {
  public companion object {
    /**
     * BasicRichText also offers a default design system that developers
     * can fallback into. This design system is initialized with [TextStyle.Default]
     * and [Color.Black].
     */
    public val Default: RichTextScope = RichTextScope(
      textStyle = { LocalTextStyle.current },
      ProvideTextStyle = { newTextStyle, content ->
        CompositionLocalProvider(LocalTextStyle provides newTextStyle) {
          content()
        }
      },
      contentColor = { LocalContentColor.current },
      ProvideContentColor = { newColor, content ->
        CompositionLocalProvider(LocalContentColor provides newColor) {
          content()
        }
      },
    )
  }
}

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
