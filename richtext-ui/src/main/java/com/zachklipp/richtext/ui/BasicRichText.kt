@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle

/**
 * Draws some rich text. Entry point to the compose-richtext library.
 */
@Composable
public fun BasicRichText(
  modifier: Modifier = Modifier,
  richTextStyle: RichTextStyle? = null,
  textStyle: TextStyle? = null,
  contentColor: Color? = null,
  alignment: Alignment.Horizontal = Alignment.Start,
  children: @Composable RichTextScope.() -> Unit
) {
  with(RichTextScope) {
    // Nested RichTexts should not continue list leveling from the parent.
    RestartListLevel {
      WithStyle(richTextStyle) {
        val resolvedStyle = currentRichTextStyle.resolveDefaults()
        val blockSpacing = with(LocalDensity.current) {
          resolvedStyle.paragraphSpacing!!.toDp()
        }

        ProvideBasicTextStyle(textStyle) {
          val currentContentColor = LocalContentColor.current
          CompositionLocalProvider(LocalContentColor provides (contentColor ?: currentContentColor)) {
            Column(
              modifier = modifier,
              verticalArrangement = spacedBy(blockSpacing),
              horizontalAlignment = alignment
            ) {
              children()
            }
          }
        }
      }
    }
  }
}
