@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.ui.core.DensityAmbient
import androidx.ui.core.LayoutDirection
import androidx.ui.core.Modifier
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Arrangement.Top
import androidx.ui.layout.Arrangement.Vertical
import androidx.ui.layout.Column
import androidx.ui.unit.IntPx

/**
 * Draws some rich text. Entry point to the rich text library.
 */
@Composable
fun RichText(
  modifier: Modifier = Modifier.None,
  style: RichTextStyle? = null,
  children: @Composable() RichTextScope.() -> Unit
) {
  with(RichTextScope) {
    // Nested RichTexts should not continue list leveling from the parent.
    RestartListLevel {
      WithStyle(style) {
        val resolvedStyle = currentRichTextStyle.resolveDefaults()
        val blockSpacing = with(DensityAmbient.current) {
          resolvedStyle.paragraphSpacing!!.toIntPx()
        }

        Column(modifier = modifier) {
          children()
        }
      }
    }
  }
}
