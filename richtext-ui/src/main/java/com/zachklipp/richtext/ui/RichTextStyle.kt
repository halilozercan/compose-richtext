package com.zachklipp.richtext.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ambientOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.zachklipp.richtext.ui.string.RichTextStringStyle

internal val RichTextStyleAmbient = ambientOf { RichTextStyle.Default }
internal val DefaultParagraphSpacing: TextUnit = 8.sp

/**
 * Configures all formatting attributes for drawing rich text.
 *
 * @param paragraphSpacing The amount of space in between blocks of text.
 * @param headingStyle The [HeadingStyle] that defines how [Heading]s are drawn.
 * @param listStyle The [ListStyle] used to format [FormattedList]s.
 * @param blockQuoteGutter The [BlockQuoteGutter] used to draw [BlockQuote]s.
 * @param codeBlockStyle The [CodeBlockStyle] that defines how [CodeBlock]s are drawn.
 * @param tableStyle The [TableStyle] used to render [Table]s.
 * @param stringStyle The [RichTextStringStyle] used to render
 * [RichTextString][com.zachklipp.richtext.ui.string.RichTextString]s
 */
@Immutable
data class RichTextStyle(
  val paragraphSpacing: TextUnit? = null,
  val headingStyle: HeadingStyle? = null,
  val listStyle: ListStyle? = null,
  val blockQuoteGutter: BlockQuoteGutter? = null,
  val codeBlockStyle: CodeBlockStyle? = null,
  val tableStyle: TableStyle? = null,
  val stringStyle: RichTextStringStyle? = null
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
    tableStyle = otherStyle?.tableStyle ?: tableStyle,
    stringStyle = stringStyle?.merge(otherStyle?.stringStyle)
)

fun RichTextStyle.resolveDefaults(): RichTextStyle = RichTextStyle(
    paragraphSpacing = paragraphSpacing ?: DefaultParagraphSpacing,
    headingStyle = headingStyle ?: DefaultHeadingStyle,
    listStyle = (listStyle ?: ListStyle.Default).resolveDefaults(),
    blockQuoteGutter = blockQuoteGutter ?: DefaultBlockQuoteGutter,
    codeBlockStyle = (codeBlockStyle ?: CodeBlockStyle.Default).resolveDefaults(),
    tableStyle = (tableStyle ?: TableStyle.Default).resolveDefaults(),
    stringStyle = (stringStyle ?: RichTextStringStyle.Default).resolveDefaults()
)
