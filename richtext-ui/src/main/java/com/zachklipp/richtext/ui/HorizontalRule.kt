package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.ui.core.DensityAmbient
import androidx.ui.foundation.Box
import androidx.ui.foundation.DrawBackground
import androidx.ui.foundation.contentColor
import androidx.ui.layout.LayoutHeight
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.LayoutWidth
import androidx.ui.unit.dp

/**
 * TODO write documentation
 */
@Composable fun RichTextScope.HorizontalRule() {
  val color = contentColor().copy(alpha = .2f)
  val spacing = with(DensityAmbient.current) {
    currentRichTextStyle.resolveDefaults().paragraphSpacing!!.toDp()
  }
  Box(
    LayoutPadding(top = spacing, bottom = spacing) +
        LayoutWidth.Fill + LayoutHeight(1.dp) + DrawBackground(color)
  )
}
