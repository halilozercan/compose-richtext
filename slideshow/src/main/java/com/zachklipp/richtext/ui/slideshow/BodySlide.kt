@file:Suppress("unused")

package com.zachklipp.richtext.ui.slideshow

import androidx.compose.foundation.ProvideTextStyle
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.ui.tooling.preview.Preview

/**
 * A composable to define a [Slideshow] slide which displays a large header at the top of the
 * slide, an optional footer at the bottom, and some content in the middle. All children are
 * start-aligned.
 *
 * See [SlideDivider] and [SlideNumberFooter].
 */
@Composable public fun SlideScope.BodySlide(
  header: @Composable () -> Unit,
  body: @Composable () -> Unit,
  footer: @Composable () -> Unit = { SlideNumberFooter() }
) {
  val theme = SlideshowThemeAmbient.current
  Column(
      Modifier.fillMaxSize().padding(theme.gap),
      verticalArrangement = spacedBy(theme.gap)
  ) {
    ProvideTextStyle(theme.headerStyle, header)
    Column(
        Modifier
            .fillMaxWidth()
            .weight(1f, fill = true)
    ) { body() }
    ProvideTextStyle(theme.footerStyle, footer)
  }
}

/**
 * A simple horizontal divider line which uses the [SlideshowTheme] content color.
 */
@Composable public fun SlideScope.SlideDivider() {
  Divider(color = SlideshowThemeAmbient.current.contentColor)
}

/**
 * A text composable which displays the current slide number from the [SlideScope], right-aligned in
 * its parent.
 */
@Composable public fun SlideScope.SlideNumberFooter() {
  Text(
      slideNumber.toString(),
      Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
  )
}

@Preview
@Composable private fun BodySlidePreview() {
  PreviewSlideScope.BodySlide(
      header = {
        Text("Header")
      },
      body = {
        Text("Content 1")
        Text("Content 2")
        Text("Content 3")
      },
      footer = {
        Text("Footer")
      }
  )
}
