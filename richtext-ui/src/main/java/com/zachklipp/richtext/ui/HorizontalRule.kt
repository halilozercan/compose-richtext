package com.zachklipp.richtext.ui

import androidx.compose.foundation.AmbientContentColor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.dp

/**
 * TODO write documentation
 */
@Composable fun RichTextScope.HorizontalRule() {
  val color = AmbientContentColor.current.copy(alpha = .2f)
  val spacing = with(DensityAmbient.current) {
    currentRichTextStyle.resolveDefaults().paragraphSpacing!!.toDp()
  }
  Box(
    Modifier.padding(top = spacing, bottom = spacing)
      .fillMaxWidth()
      .height(1.dp)
      .background(color)
  )
}
