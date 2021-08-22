package com.halilibo.richtext.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.ui.BlockQuoteGutter
import com.halilibo.richtext.ui.DefaultBlockQuoteGutter
import com.halilibo.richtext.ui.string.RichTextStringStyle

internal val LocalRichTextStyle = compositionLocalOf { RichTextStyle.Default }
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
public data class RichTextStyle(
  val paragraphSpacing: TextUnit? = null,
  val headingStyle: HeadingStyle? = null,
  val listStyle: ListStyle? = null,
  val blockQuoteGutter: BlockQuoteGutter? = null,
  val codeBlockStyle: CodeBlockStyle? = null,
  val tableStyle: TableStyle? = null,
  val stringStyle: RichTextStringStyle? = null
) {
  public companion object {
    public val Default: RichTextStyle = RichTextStyle()
  }
}

public fun RichTextStyle.merge(otherStyle: RichTextStyle?): RichTextStyle = RichTextStyle(
  paragraphSpacing = otherStyle?.paragraphSpacing ?: paragraphSpacing,
  headingStyle = otherStyle?.headingStyle ?: headingStyle,
  listStyle = otherStyle?.listStyle ?: listStyle,
  blockQuoteGutter = otherStyle?.blockQuoteGutter ?: blockQuoteGutter,
  codeBlockStyle = otherStyle?.codeBlockStyle ?: codeBlockStyle,
  tableStyle = otherStyle?.tableStyle ?: tableStyle,
  stringStyle = stringStyle?.merge(otherStyle?.stringStyle) ?: otherStyle?.stringStyle
)

public fun RichTextStyle.resolveDefaults(): RichTextStyle = RichTextStyle(
  paragraphSpacing = paragraphSpacing ?: DefaultParagraphSpacing,
  headingStyle = headingStyle ?: DefaultHeadingStyle,
  listStyle = (listStyle ?: ListStyle.Default).resolveDefaults(),
  blockQuoteGutter = blockQuoteGutter ?: DefaultBlockQuoteGutter,
  codeBlockStyle = (codeBlockStyle ?: CodeBlockStyle.Default).resolveDefaults(),
  tableStyle = (tableStyle ?: TableStyle.Default).resolveDefaults(),
  stringStyle = (stringStyle ?: RichTextStringStyle.Default).resolveDefaults()
)

/**
 * The current [RichTextStyle].
 */
public val RichTextScope.currentRichTextStyle: RichTextStyle
  @Composable get() = LocalRichTextStyle.current

/**
 * Sets the [RichTextStyle] for its [children].
 */
@Composable
public fun RichTextScope.WithStyle(
  style: RichTextStyle?,
  children: @Composable RichTextScope.() -> Unit
) {
  if (style == null) {
    children()
  } else {
    val mergedStyle = LocalRichTextStyle.current.merge(style)
    CompositionLocalProvider(LocalRichTextStyle provides mergedStyle) {
      children()
    }
  }
}
