package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.ui.foundation.Box
import androidx.ui.foundation.DrawBackground
import androidx.ui.foundation.contentColor
import androidx.ui.layout.LayoutHeight
import androidx.ui.layout.LayoutWidth
import androidx.ui.unit.dp

/**
 * TODO write documentation
 */
@Composable fun RichTextScope.HorizontalRule() {
  val color = contentColor().copy(alpha = .2f)
  Box(LayoutWidth.Fill + LayoutHeight(1.dp) + DrawBackground(color))
}
