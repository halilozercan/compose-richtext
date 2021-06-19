@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.annotation.IntRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.resolveDefaults
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.zachklipp.richtext.ui.string.InternalText

/**
 * Function that computes the [TextStyle] for the given header level, given the current [TextStyle]
 * for this point in the composition. Note that the [TextStyle] passed into this function will be
 * fully resolved. The returned style will then be _merged_ with the passed-in text style, so any
 * unspecified properties will be inherited.
 */
// TODO factor a generic "block style" thing out, use for code block, quote block, and this, to
// also allow controlling top/bottom space.
public typealias HeadingStyle = (level: Int, textStyle: TextStyle) -> TextStyle

internal val DefaultHeadingStyle: HeadingStyle = { level, textStyle ->
  when (level) {
    0 -> TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold
    )
    1 -> TextStyle(
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold
    )
    2 -> TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = textStyle.color.copy(alpha = .7F)
    )
    3 -> TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        fontStyle = Italic
    )
    4 -> TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = textStyle.color.copy(alpha = .7F)
    )
    5 -> TextStyle(
        fontWeight = FontWeight.Bold,
        color = textStyle.color.copy(alpha = .5f)
    )
    else -> textStyle
  }
}

/**
 * A section heading.
 *
 * @param level The non-negative rank of the header, with 0 being the most important.
 */
@Composable public fun RichTextScope.Heading(
  @IntRange(from = 0L, to = Long.MAX_VALUE) level: Int,
  text: String
) {
  Heading(level) {
    InternalText(text)
  }
}

/**
 * A section heading.
 *
 * @param level The non-negative rank of the header, with 0 being the most important.
 */
@Composable public fun RichTextScope.Heading(
  level: Int,
  children: @Composable RichTextScope.() -> Unit
) {
  require(level >= 0) { "Level must be at least 0" }

  val richTextStyle = currentRichTextStyle.resolveDefaults()
  val headingStyleFunction = richTextStyle.headingStyle!!

  // According to [Text] composable's documentation:
  //   "Additionally, for [color], if [color] is not set, and [style] does not have a color, then
  //   [AmbientContentColor] will be used - this allows this [Text] or element containing this [Text]
  //   to adapt to different background colors and still maintain contrast and accessibility."
  // However, [resolveDefaults] uses a static default color which is [Color.Black].
  // To fix this issue, we are specifying the text color according to [Text] documentation
  // before calling [resolveDefaults].
  val incomingStyle = currentTextStyle.let {
    it.copy(color = it.color.takeOrElse { currentContentColor })
  }
  val updatedTextStyle = resolveDefaults(incomingStyle, LocalLayoutDirection.current)

  val headingTextStyle = headingStyleFunction(level, updatedTextStyle)
  val mergedTextStyle = updatedTextStyle.merge(headingTextStyle)

  CompositionLocalProvider(LocalRichTextLocals.current.localTextStyle provides mergedTextStyle) {
    children()
  }
}

@Preview @Composable private fun HeadingPreviewOnWhite() {
  HeadingPreview(backgroundColor = Color.White, contentColor = Color.Black)
}

@Preview @Composable private fun HeadingPreviewOnBlack() {
  HeadingPreview(backgroundColor = Color.Black, contentColor = Color.White)
}

@Composable private fun HeadingPreview(
  backgroundColor: Color,
  contentColor: Color
) {
  BasicLocalsProvider(contentColor = contentColor) {
    Box(Modifier.background(color = backgroundColor)) {
      Column {
        for (level in 0 until 10) {
          RichTextScope.Heading(level, "Heading ${level + 1}")
        }
      }
    }
  }
}
