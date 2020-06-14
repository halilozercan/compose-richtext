@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.compose.Providers
import androidx.ui.core.LayoutDirectionAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.ContentColorAmbient
import androidx.ui.foundation.ProvideTextStyle
import androidx.ui.foundation.Text
import androidx.ui.foundation.currentTextStyle
import androidx.ui.foundation.drawBackground
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontStyle.Italic
import androidx.ui.text.font.FontWeight
import androidx.ui.text.resolveDefaults
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.sp

/**
 * Function that computes the [TextStyle] for the given header level, given the current [TextStyle]
 * for this point in the composition. Note that the [TextStyle] passed into this function will be
 * fully resolved. The returned style will then be _merged_ with the passed-in text style, so any
 * unspecified properties will be inherited.
 */
// TODO factor a generic "block style" thing out, use for code block, quote block, and this, to
// also allow controlling top/bottom space.
typealias HeadingStyle = (level: Int, textStyle: TextStyle) -> TextStyle

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
 * TODO write documentation
 */
@Composable fun RichTextScope.Heading(
  level: Int,
  text: String
) {
  Heading(level) {
    Text(text)
  }
}

/**
 * TODO write documentation
 */
@Composable fun RichTextScope.Heading(
  level: Int,
  children: @Composable() RichTextScope.() -> Unit
) {
  require(level >= 0) { "Level must be at least 0" }

  val richTextStyle = currentRichTextStyle.resolveDefaults()
  val headingStyleFunction = richTextStyle.headingStyle!!
  val currentTextStyle = resolveDefaults(currentTextStyle(), LayoutDirectionAmbient.current)
  val headingTextStyle = headingStyleFunction(level, currentTextStyle)
  val mergedTextStyle = currentTextStyle.merge(headingTextStyle)

  ProvideTextStyle(mergedTextStyle) {
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
  Providers(ContentColorAmbient provides contentColor) {
    Box(Modifier.drawBackground(color = backgroundColor)) {
      Column {
        for (level in 0 until 10) {
          RichTextScope.Heading(level, "Heading ${level + 1}")
        }
      }
    }
  }
}
