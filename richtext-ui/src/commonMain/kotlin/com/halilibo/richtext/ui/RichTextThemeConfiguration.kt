package com.halilibo.richtext.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

internal typealias TextStyleProvider = @Composable () -> TextStyle
internal typealias TextStyleBackProvider = @Composable (TextStyle, @Composable () -> Unit) -> Unit
internal typealias ContentColorProvider = @Composable () -> Color
internal typealias ContentColorBackProvider = @Composable (Color, @Composable () -> Unit) -> Unit

internal data class RichTextThemeConfiguration(
  val textStyleProvider: TextStyleProvider = { LocalInternalTextStyle.current },
  val textStyleBackProvider: TextStyleBackProvider = { newTextStyle, content ->
    CompositionLocalProvider(LocalInternalTextStyle provides newTextStyle) {
      content()
    }
  },
  val contentColorProvider: ContentColorProvider = { LocalInternalContentColor.current },
  val contentColorBackProvider: ContentColorBackProvider = { newColor, content ->
    CompositionLocalProvider(LocalInternalContentColor provides newColor) {
      content()
    }
  }
) {
  companion object {
    internal val Default = RichTextThemeConfiguration()
  }
}

internal val LocalRichTextThemeConfiguration: ProvidableCompositionLocal<RichTextThemeConfiguration> =
  compositionLocalOf { RichTextThemeConfiguration() }

/**
 * Easy access delegations for [RichTextThemeProvider] within [RichTextScope]
 */
internal val RichTextScope.textStyleProvider: @Composable () -> TextStyle
  @Composable get() = LocalRichTextThemeConfiguration.current.textStyleProvider

internal val RichTextScope.textStyleBackProvider: @Composable (TextStyle, @Composable () -> Unit) -> Unit
  @Composable get() = LocalRichTextThemeConfiguration.current.textStyleBackProvider

internal val RichTextScope.contentColorProvider: @Composable () -> Color
  @Composable get() = LocalRichTextThemeConfiguration.current.contentColorProvider

internal val RichTextScope.contentColorBackProvider: @Composable (Color, @Composable () -> Unit) -> Unit
  @Composable get() = LocalRichTextThemeConfiguration.current.contentColorBackProvider