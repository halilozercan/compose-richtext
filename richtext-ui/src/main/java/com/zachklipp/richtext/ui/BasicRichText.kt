@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity

/**
 * Draws some rich text. Entry point to the compose-richtext library.
 *
 * Calling [BasicRichText] requires a [RichTextScope] instance as a receiving context.
 * Please refer to [RichTextScope] for more information.
 */
@Composable
public fun RichTextScope.BasicRichText(
  modifier: Modifier = Modifier,
  style: RichTextStyle? = null,
  children: @Composable RichTextScope.() -> Unit
) {
  RestartListLevel {
    WithStyle(style) {
      val resolvedStyle = currentRichTextStyle.resolveDefaults()
      val blockSpacing = with(LocalDensity.current) {
        resolvedStyle.paragraphSpacing!!.toDp()
      }

      Column(modifier = modifier, verticalArrangement = spacedBy(blockSpacing)) {
        children()
      }
    }
  }
}
