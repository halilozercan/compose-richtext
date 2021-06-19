package com.zachklipp.richtext.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * A simple horizontal line drawn with the current content color.
 */
@Composable public fun RichTextScope.HorizontalRule() {
  val color = currentContentColor.copy(alpha = .2f)
  val spacing = with(LocalDensity.current) {
    currentRichTextStyle.resolveDefaults().paragraphSpacing!!.toDp()
  }
  Box(
    Modifier
      .padding(top = spacing, bottom = spacing)
      .fillMaxWidth()
      .height(1.dp)
      .background(color)
  )
}
