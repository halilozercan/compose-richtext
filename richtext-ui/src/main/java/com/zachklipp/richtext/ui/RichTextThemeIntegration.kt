package com.zachklipp.richtext.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

internal data class RichTextThemeIntegration(
  val textStyle: @Composable () -> TextStyle = { LocalInternalTextStyle.current },
  val ProvideTextStyle: @Composable (TextStyle, @Composable () -> Unit) -> Unit = { newTextStyle, content ->
    CompositionLocalProvider(LocalInternalTextStyle provides newTextStyle) {
      content()
    }
  },
  val contentColor: @Composable () -> Color = { LocalInternalContentColor.current },
  val ProvideContentColor: @Composable (Color, @Composable () -> Unit) -> Unit = { newColor, content ->
    CompositionLocalProvider(LocalInternalContentColor provides newColor) {
      content()
    }
  }
)

internal val LocalRichTextThemeIntegration: ProvidableCompositionLocal<RichTextThemeIntegration> =
  compositionLocalOf { RichTextThemeIntegration() }

/**
 * Entry point for integrating app's own typography and theme system with RichText.
 *
 * API for this integration is highly influenced by how compose-material theming
 * is designed. RichText library assumes that almost all Theme/Design systems would
 * have composition locals that provide text style downstream.
 *
 * Moreover, text style should not include text color by best practice. Content color
 * exists to figure text color in current context. Light/Dark theming leverages content
 * color to influence not just text but other parts of theming as well.
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
@Composable
public fun RichTextThemeIntegration(
  textStyle: @Composable (() -> TextStyle)? = null,
  ProvideTextStyle: @Composable ((TextStyle, @Composable () -> Unit) -> Unit)? = null,
  contentColor: @Composable (() -> Color)? = null,
  ProvideContentColor: @Composable ((Color, @Composable () -> Unit) -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val defaultRichTextThemeIntegration = RichTextThemeIntegration()
  CompositionLocalProvider(LocalRichTextThemeIntegration provides
      RichTextThemeIntegration(
        textStyle = textStyle ?: defaultRichTextThemeIntegration.textStyle,
        ProvideTextStyle = ProvideTextStyle ?: defaultRichTextThemeIntegration.ProvideTextStyle,
        contentColor = contentColor ?: defaultRichTextThemeIntegration.contentColor,
        ProvideContentColor = ProvideContentColor ?: defaultRichTextThemeIntegration.ProvideContentColor,
      )
  ) {
    content()
  }
}

/**
 * Easy access delegations for [RichTextThemeIntegration] within [RichTextScope]
 */
internal val RichTextScope.textStyle: @Composable () -> TextStyle
  @Composable get() = LocalRichTextThemeIntegration.current.textStyle

internal val RichTextScope.ProvideTextStyle: @Composable (TextStyle, @Composable () -> Unit) -> Unit
  @Composable get() = LocalRichTextThemeIntegration.current.ProvideTextStyle

internal val RichTextScope.contentColor: @Composable () -> Color
  @Composable get() = LocalRichTextThemeIntegration.current.contentColor

internal val RichTextScope.ProvideContentColor: @Composable (Color, @Composable () -> Unit) -> Unit
  @Composable get() = LocalRichTextThemeIntegration.current.ProvideContentColor