@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.halilibo.richtext.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.resolveDefaults
import androidx.compose.ui.unit.sp


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
  level: Int,
  text: String
) {
  Heading(level) {
    Text(text, Modifier.semantics { heading() })
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

  val incomingStyle = currentTextStyle.let {
    it.copy(color = it.color.takeOrElse { currentContentColor })
  }
  val currentTextStyle = resolveDefaults(incomingStyle, LocalLayoutDirection.current)

  val headingStyleFunction = currentRichTextStyle.resolveDefaults().headingStyle!!
  val headingTextStyle = headingStyleFunction(level, currentTextStyle)
  val mergedTextStyle = currentTextStyle.merge(headingTextStyle)

  textStyleBackProvider(mergedTextStyle) {
    children()
  }
}
