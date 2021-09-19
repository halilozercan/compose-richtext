package com.halilibo.richtext.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Defines how [CodeBlock]s are rendered.
 *
 * @param textStyle The [TextStyle] to use for the block.
 * @param background The [Color] of a code block, drawn behind the text.
 * @param padding The amount of space between the edge of the text and the edge of the background.
 * @param wordWrap Whether a code block breaks the lines or scrolls horizontally.
 */
@Immutable
public data class CodeBlockStyle(
  val textStyle: TextStyle? = null,
  // TODO Make background just a modifier instead?
  val background: Color? = null,
  val padding: TextUnit? = null,
  val wordWrap: Boolean? = null
) {
  public companion object {
    public val Default: CodeBlockStyle = CodeBlockStyle()
  }
}

private val DefaultCodeBlockTextStyle = TextStyle(
  fontFamily = FontFamily.Monospace
)
internal val DefaultCodeBlockBackground: Color = Color.LightGray.copy(alpha = .5f)
private val DefaultCodeBlockPadding: TextUnit = 16.sp
private const val DefaultCodeWordWrap: Boolean = true

internal fun CodeBlockStyle.resolveDefaults() = CodeBlockStyle(
  textStyle = textStyle ?: DefaultCodeBlockTextStyle,
  background = background ?: DefaultCodeBlockBackground,
  padding = padding ?: DefaultCodeBlockPadding,
  wordWrap = wordWrap ?: DefaultCodeWordWrap
)

/**
 * A specially-formatted block of text that typically uses a monospace font with a tinted
 * background.
 *
 * @param wordWrap Overrides word wrap preference coming from [CodeBlockStyle]
 */
@Composable public fun RichTextScope.CodeBlock(
  text: String,
  wordWrap: Boolean? = null
) {
  CodeBlock(wordWrap = wordWrap) {
    Text(text)
  }
}

/**
 * A specially-formatted block of text that typically uses a monospace font with a tinted
 * background.
 *
 * @param wordWrap Overrides word wrap preference coming from [CodeBlockStyle]
 */
@Composable public fun RichTextScope.CodeBlock(
  wordWrap: Boolean? = null,
  children: @Composable RichTextScope.() -> Unit
) {
  val codeBlockStyle = currentRichTextStyle.resolveDefaults().codeBlockStyle!!
  val textStyle = currentTextStyle.merge(codeBlockStyle.textStyle)
  val blockPadding = with(LocalDensity.current) {
    codeBlockStyle.padding!!.toDp()
  }
  val resolvedWordWrap = wordWrap ?: codeBlockStyle.wordWrap!!

  CodeBlockLayout(
    wordWrap = resolvedWordWrap
  ) { modifier ->
    Box(
      modifier = modifier
        .background(color = codeBlockStyle.background!!)
        .padding(blockPadding)
    ) {
      ProvideTextStyle(textStyle) {
        children()
      }
    }
  }
}

/**
 * Desktop composable adds an optional horizontal scrollbar.
 */
@Composable
internal expect fun RichTextScope.CodeBlockLayout(
  wordWrap: Boolean,
  children: @Composable RichTextScope.(Modifier) -> Unit
)
