package com.halilibo.richtext.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

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
 * @param textStyleProvider Returns the current text style.
 * @param textStyleBackProvider RichText sometimes updates the current text style
 * e.g. Heading, CodeBlock, and etc. New style should be passed to the outer
 * theming to indicate that there is a need for update, so that children Text
 * composables use the correct styling.
 * @param contentColorProvider Returns the current content color.
 * @param contentColorBackProvider Similar to [textStyleBackProvider], does the same job
 * for content color.
 */
@Composable
public fun RichTextThemeProvider(
  textStyleProvider: @Composable (() -> TextStyle)? = null,
  textStyleBackProvider: @Composable ((TextStyle, @Composable () -> Unit) -> Unit)? = null,
  contentColorProvider: @Composable (() -> Color)? = null,
  contentColorBackProvider: @Composable ((Color, @Composable () -> Unit) -> Unit)? = null,
  content: @Composable () -> Unit
) {
  CompositionLocalProvider(
    LocalRichTextThemeConfiguration provides
        RichTextThemeConfiguration(
          textStyleProvider = textStyleProvider
            ?: RichTextThemeConfiguration.Default.textStyleProvider,
          textStyleBackProvider = textStyleBackProvider
            ?: RichTextThemeConfiguration.Default.textStyleBackProvider,
          contentColorProvider = contentColorProvider
            ?: RichTextThemeConfiguration.Default.contentColorProvider,
          contentColorBackProvider = contentColorBackProvider
            ?: RichTextThemeConfiguration.Default.contentColorBackProvider,
        )
  ) {
    content()
  }
}
