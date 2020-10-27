@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.foundation.AmbientContentColor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.remember
import androidx.compose.ui.Layout
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.offset
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import com.zachklipp.richtext.ui.BlockQuoteGutter.BarGutter

internal val DefaultBlockQuoteGutter = BarGutter()

interface BlockQuoteGutter {
  @Composable fun drawGutter()

  @Immutable
  data class BarGutter(
    val startMargin: TextUnit = 6.sp,
    val barWidth: TextUnit = 3.sp,
    val endMargin: TextUnit = 6.sp,
    val color: (contentColor: Color) -> Color = { it.copy(alpha = .25f) }
  ) : BlockQuoteGutter {
    @Composable override fun drawGutter() {
      with(DensityAmbient.current) {
        val color = color(AmbientContentColor.current)
        val modifier = remember(startMargin, endMargin, barWidth, color) {
          // Padding must come before width.
          Modifier.padding(
            start = startMargin.toDp(),
            end = endMargin.toDp()
          )
            .width(barWidth.toDp())
            .background(color, RoundedCornerShape(50))
        }

        Box(modifier = modifier)
      }
    }
  }
}

/**
 * Draws a block quote.
 */
@Composable fun RichTextScope.BlockQuote(children: @Composable RichTextScope.() -> Unit) {
  val gutter = currentRichTextStyle.resolveDefaults().blockQuoteGutter!!
  val spacing = with(DensityAmbient.current) {
    currentRichTextStyle.resolveDefaults().paragraphSpacing!!.toDp() / 2
  }

  Layout(children = {
    gutter.drawGutter()
    RichText(
      modifier = Modifier.padding(top = spacing, bottom = spacing),
      children = children
    )
  }) { measurables, constraints ->
    val gutterMeasurable = measurables[0]
    val contentsMeasurable = measurables[1]

    // First get the width of the gutter, so we can measure the contents with
    // the smaller width if bounded.
    val gutterWidth = gutterMeasurable.minIntrinsicWidth(constraints.maxHeight)

    // Measure the contents with the confined width.
    // This must be done before measuring the gutter so that the gutter gets
    // the correct height.
    val contentsConstraints = constraints.offset(horizontal = -gutterWidth)
    val contentsPlaceable = contentsMeasurable.measure(contentsConstraints)
    val layoutWidth = contentsPlaceable.width + gutterWidth
    val layoutHeight = contentsPlaceable.height

    // Measure the gutter to fit in its min intrinsic width and exactly the
    // height of the contents.
    val gutterConstraints = constraints.copy(
        maxWidth = gutterWidth,
        minHeight = layoutHeight,
        maxHeight = layoutHeight
    )
    val gutterPlaceable = gutterMeasurable.measure(gutterConstraints)

    layout(layoutWidth, layoutHeight) {
      gutterPlaceable.place(IntOffset.Zero)
      contentsPlaceable.place(gutterWidth, 0)
    }
  }
}

@Preview @Composable private fun BlockQuotePreviewOnWhite() {
  BlockQuotePreview(backgroundColor = Color.White, contentColor = Color.Black)
}

@Preview @Composable private fun BlockQuotePreviewOnBlack() {
  BlockQuotePreview(backgroundColor = Color.Black, contentColor = Color.White)
}

@Composable private fun BlockQuotePreview(
  backgroundColor: Color,
  contentColor: Color
) {
  Providers(AmbientContentColor provides contentColor) {
    Box(Modifier.background(backgroundColor)) {
      RichTextScope.BlockQuote {
        Text("Some text.")
        Text("Another paragraph.")
        BlockQuote {
          Text("Nested block quote.")
        }
      }
    }
  }
}
