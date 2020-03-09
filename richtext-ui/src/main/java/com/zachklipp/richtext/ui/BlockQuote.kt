@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.compose.Immutable
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Layout
import androidx.ui.core.Text
import androidx.ui.core.offset
import androidx.ui.foundation.Box
import androidx.ui.foundation.DrawBackground
import androidx.ui.foundation.ProvideContentColor
import androidx.ui.foundation.contentColor
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.LayoutWidth
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.IntPxPosition
import androidx.ui.unit.TextUnit
import androidx.ui.unit.ipx
import androidx.ui.unit.sp
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
        val color = color(contentColor())
        val padding = LayoutPadding(
          start = startMargin.toDp(),
          end = endMargin.toDp()
        )
        val background = DrawBackground(color, RoundedCornerShape(50))

        // Padding must come before width.
        Box(modifier = padding + LayoutWidth(barWidth.toDp()) + background)
      }
    }
  }
}

/**
 * Draws a block quote.
 */
@Composable fun RichTextScope.BlockQuote(children: @Composable() RichTextScope.() -> Unit) {
  val gutter = currentRichTextStyle.resolveDefaults().blockQuoteGutter!!
  val spacing = with(DensityAmbient.current) {
    currentRichTextStyle.resolveDefaults().paragraphSpacing!!.toDp() / 2
  }

  Layout(children = {
    gutter.drawGutter()
    RichText(
      modifier = LayoutPadding(top = spacing, bottom = spacing),
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
      gutterPlaceable.place(IntPxPosition.Origin)
      contentsPlaceable.place(gutterWidth, 0.ipx)
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
  ProvideContentColor(contentColor) {
    Box(DrawBackground(backgroundColor)) {
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
