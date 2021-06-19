@file:Suppress("unused")

package com.zachklipp.richtext.ui.slideshow

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.tooling.preview.Preview

/**
 * A composable to define a [Slideshow] slide that shows a large, bold title in the center of the
 * slide with an optional subtitle below it.
 */
@Composable public fun SlideScope.TitleSlide(
  title: @Composable () -> Unit,
  subtitle: (@Composable () -> Unit)? = null
) {
  val theme = SlideshowThemeAmbient.current
  Column(horizontalAlignment = CenterHorizontally) {
    ProvideTextStyle(TextStyle(textAlign = Center)) {
      ProvideTextStyle(theme.titleStyle, content = title)
      if (subtitle != null) {
        ProvideTextStyle(theme.subtitleStyle, content = subtitle)
      }
    }
  }
}

@Preview
@Composable private fun TitlePreview() {
  PreviewSlideScope.TitleSlide(
    title = { Text("Title") },
    subtitle = { Text("Subtitle") }
  )
}
