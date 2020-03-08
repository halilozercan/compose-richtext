package com.zachklipp.richtext.ui

import androidx.compose.Immutable
import androidx.compose.ambientOf
import androidx.ui.graphics.Color
import androidx.ui.text.TextStyle
import androidx.ui.unit.TextUnit
import androidx.ui.unit.sp

internal val RichTextStyleAmbient = ambientOf { RichTextStyle.Default }

internal val DefaultParagraphSpacing: TextUnit = 8.sp

/**
 * Configures all formatting attributes for drawing rich text.
 */
@Immutable
data class RichTextStyle(
  val paragraphSpacing: TextUnit? = null,
  val headingStyle: HeadingStyle? = null,
  val listStyle: ListStyle? = null,
  val codeBlockTextStyle: TextStyle? = null,
  val codeBlockBackground: Color? = null,
  val codeBlockPadding: TextUnit? = null
) {
  companion object {
    val Default = RichTextStyle()
  }
}

fun RichTextStyle.merge(otherStyle: RichTextStyle?): RichTextStyle {
  TODO()
}

internal fun RichTextStyle.resolveDefaults(): RichTextStyle = RichTextStyle(
  paragraphSpacing = paragraphSpacing ?: DefaultParagraphSpacing,
  headingStyle = headingStyle ?: DefaultHeadingStyle,
  listStyle = listStyle ?: ListStyle.Default,
  codeBlockTextStyle = codeBlockTextStyle ?: DefaultCodeBlockTextStyle,
  codeBlockBackground = codeBlockBackground ?: DefaultCodeBlockBackground,
  codeBlockPadding = codeBlockPadding ?: DefaultCodeBlockPadding
)
