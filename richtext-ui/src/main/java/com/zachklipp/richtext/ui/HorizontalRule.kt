package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.contentColor
import androidx.ui.foundation.drawBackground
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.height
import androidx.ui.layout.padding
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
      Modifier.padding(top = spacing, bottom = spacing)
          .fillMaxWidth()
          .height(1.dp)
          .drawBackground(color)
  )
}
