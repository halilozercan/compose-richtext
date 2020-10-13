@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.DensityAmbient

/**
 * Draws some rich text. Entry point to the rich text library.
 */
@Composable
fun RichText(
  modifier: Modifier = Modifier,
  style: RichTextStyle? = null,
  children: @Composable() RichTextScope.() -> Unit
) {
  with(RichTextScope()) {
    // Nested RichTexts should not continue list leveling from the parent.
    RestartListLevel {
      WithStyle(style) {
        val resolvedStyle = currentRichTextStyle.resolveDefaults()
        val blockSpacing = with(DensityAmbient.current) {
          resolvedStyle.paragraphSpacing!!.toDp()
        }

        Column(modifier = modifier, verticalArrangement = spacedBy(blockSpacing)) {
          children()
        }
      }
    }
  }
}
