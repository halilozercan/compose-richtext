@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Layout
import androidx.ui.core.Text
import androidx.ui.core.offset
import androidx.ui.foundation.Box
import androidx.ui.foundation.DrawBackground
import androidx.ui.foundation.contentColor
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.LayoutHeight
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.LayoutWidth
import androidx.ui.layout.Spacer
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.IntPxPosition
import androidx.ui.unit.ipx
import androidx.ui.unit.sp

/**
 * TODO write documentation
 */
@Composable fun RichTextScope.BlockQuote(children: @Composable() RichTextScope.() -> Unit) {
  Layout(children = {
    QuoteGutter()
    RichText {
      val spacing = with(DensityAmbient.current) {
        currentRichTextStyle.resolveDefaults().paragraphSpacing!!.toDp() / 2
      }
      Spacer(LayoutHeight(spacing))
      children()
      Spacer(LayoutHeight(spacing))
    }
  }) { measurables, constraints ->
    val gutter = measurables[0]
    val contents = measurables[1]

    // First get the width of the gutter, so we can measure the contents with
    // the smaller width if bounded.
    val gutterWidth = gutter.minIntrinsicWidth(constraints.maxHeight)

    // Measure the contents with the confined width.
    // This must be done before measuring the gutter so that the gutter gets
    // the correct height.
    val contentsConstraints = constraints.offset(horizontal = -gutterWidth)
    val contentsPlaceable = contents.measure(contentsConstraints)
    val layoutWidth = contentsPlaceable.width + gutterWidth
    val layoutHeight = contentsPlaceable.height

    // Measure the gutter to fit in its min intrinsic width and exactly the
    // height of the contents.
    val gutterConstraints = constraints.copy(
      maxWidth = gutterWidth,
      minHeight = layoutHeight,
      maxHeight = layoutHeight
    )
    val gutterPlaceable = gutter.measure(gutterConstraints)

    layout(layoutWidth, layoutHeight) {
      gutterPlaceable.place(IntPxPosition.Origin)
      contentsPlaceable.place(gutterWidth, 0.ipx)
    }
  }
}

@Preview @Composable private fun BlockQuotePreview() {
  Box(DrawBackground(Color.White)) {
    RichTextScope.BlockQuote {
      Text("Some text.")
      Text("Another paragraph.")
      BlockQuote {
        Text("Nested block quote.")
      }
    }
  }
}

@Composable private fun QuoteGutter() {
  val startPadding = 6.sp
  val gutterWidth = 3.sp
  val endPadding = 6.sp
  val totalWidth = startPadding + gutterWidth + endPadding
  val color = contentColor().copy(alpha = .25f)

  with(DensityAmbient.current) {
    Box(
      LayoutWidth(totalWidth.toDp()) +
          LayoutPadding(start = startPadding.toDp(), end = endPadding.toDp()) +
          DrawBackground(color, RoundedCornerShape(50))
    )
  }
}
