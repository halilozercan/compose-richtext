package com.zachklipp.richtext.ui

import androidx.compose.Immutable
import androidx.compose.ambientOf
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
  val blockQuoteGutter: BlockQuoteGutter? = null,
  val codeBlockStyle: CodeBlockStyle? = null,
  val tableStyle: TableStyle? = null
) {
  companion object {
    val Default = RichTextStyle()
  }
}

fun RichTextStyle.merge(otherStyle: RichTextStyle?): RichTextStyle = RichTextStyle(
    paragraphSpacing = otherStyle?.paragraphSpacing ?: paragraphSpacing,
    headingStyle = otherStyle?.headingStyle ?: headingStyle,
    listStyle = otherStyle?.listStyle ?: listStyle,
    blockQuoteGutter = otherStyle?.blockQuoteGutter ?: blockQuoteGutter,
    codeBlockStyle = otherStyle?.codeBlockStyle ?: codeBlockStyle,
    tableStyle = otherStyle?.tableStyle ?: tableStyle
)

fun RichTextStyle.resolveDefaults(): RichTextStyle = RichTextStyle(
    paragraphSpacing = paragraphSpacing ?: DefaultParagraphSpacing,
    headingStyle = headingStyle ?: DefaultHeadingStyle,
    listStyle = (listStyle ?: ListStyle.Default).resolveDefaults(),
    blockQuoteGutter = blockQuoteGutter ?: DefaultBlockQuoteGutter,
    codeBlockStyle = (codeBlockStyle ?: CodeBlockStyle.Default).resolveDefaults(),
    tableStyle = (tableStyle ?: TableStyle.Default).resolveDefaults()
)
