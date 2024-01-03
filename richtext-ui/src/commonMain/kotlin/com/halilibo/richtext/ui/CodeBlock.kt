package com.halilibo.richtext.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
 * @param modifier The [Modifier] to use for the block.
 * @param padding The amount of space between the edge of the text and the edge of the background.
 * @param wordWrap Whether a code block breaks the lines or scrolls horizontally.
 */
@Immutable
public data class CodeBlockStyle(
  val textStyle: TextStyle? = null,
  val modifier: Modifier? = null,
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
internal val DefaultCodeBlockBackgroundColor: Color = Color.LightGray.copy(alpha = .5f)
private val DefaultCodeBlockModifier: Modifier =
  Modifier.background(color = DefaultCodeBlockBackgroundColor)
private val DefaultCodeBlockPadding: TextUnit = 16.sp
private const val DefaultCodeWordWrap: Boolean = true

internal fun CodeBlockStyle.resolveDefaults() = CodeBlockStyle(
  textStyle = textStyle ?: DefaultCodeBlockTextStyle,
  modifier = modifier ?: DefaultCodeBlockModifier,
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
  val modifier = codeBlockStyle.modifier!!
  val blockPadding = with(LocalDensity.current) {
    codeBlockStyle.padding!!.toDp()
  }
  val resolvedWordWrap = wordWrap ?: codeBlockStyle.wordWrap!!

  CodeBlockLayout(
    wordWrap = resolvedWordWrap
  ) { layoutModifier ->
    Box(
      modifier = layoutModifier
        .then(modifier)
        .padding(blockPadding)
    ) {
      textStyleBackProvider(textStyle) {
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
