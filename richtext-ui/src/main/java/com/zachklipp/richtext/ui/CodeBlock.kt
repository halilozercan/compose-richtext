@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.foundation.AmbientContentColor
import androidx.compose.foundation.AmbientTextStyle
import androidx.compose.foundation.ProvideTextStyle
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview

/**
 * Defines how [CodeBlock]s are rendered.
 *
 * @param textStyle The [TextStyle] to use for the block.
 * @param background The [Color] of a code block, drawn behind the text.
 * @param padding The amount of space between the edge of the text and the edge of the background.
 */
@Immutable
data class CodeBlockStyle(
  val textStyle: TextStyle? = null,
  // TODO Make background just a modifier instead?
  val background: Color? = null,
  val padding: TextUnit? = null
) {
  companion object {
    val Default = CodeBlockStyle()
  }
}

private val DefaultCodeBlockTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace
)
internal val DefaultCodeBlockBackground: Color = Color.LightGray.copy(alpha = .5f)
private val DefaultCodeBlockPadding: TextUnit = 16.sp

internal fun CodeBlockStyle.resolveDefaults() = CodeBlockStyle(
    textStyle = textStyle ?: DefaultCodeBlockTextStyle,
    background = background ?: DefaultCodeBlockBackground,
    padding = padding ?: DefaultCodeBlockPadding
)

/**
 * A specially-formatted block of text that typically uses a monospace font with a tinted
 * background.
 */
@Composable fun RichTextScope.CodeBlock(text: String) {
  CodeBlock {
    Text(text)
  }
}

/**
 * A specially-formatted block of text that typically uses a monospace font with a tinted
 * background.
 */
@Composable fun RichTextScope.CodeBlock(children: @Composable RichTextScope.() -> Unit) {
  val richTextStyle = currentRichTextStyle.resolveDefaults().codeBlockStyle!!
  val textStyle = AmbientTextStyle.current.merge(richTextStyle.textStyle)
  val background = Modifier.background(color = richTextStyle.background!!)
  val blockPadding = with(DensityAmbient.current) {
    richTextStyle.padding!!.toDp()
  }

  Box(modifier = background) {
    // Can't use Box(padding=) because that property doesn't seem affect the intrinsic size.
    Box(Modifier.padding(blockPadding)) {
      ProvideTextStyle(textStyle) {
        children()
      }
    }
  }
}

@Preview @Composable
private fun CodeBlockPreviewOnWhite() {
  CodeBlockPreview(backgroundColor = Color.White, contentColor = Color.Black)
}

@Preview @Composable
private fun CodeBlockPreviewOnBlack() {
  CodeBlockPreview(backgroundColor = Color.Black, contentColor = Color.White)
}

@Composable
private fun CodeBlockPreview(
  backgroundColor: Color,
  contentColor: Color
) {
  Providers(AmbientContentColor provides contentColor) {
    Box(modifier = Modifier.background(color = backgroundColor)) {
      Box(modifier = Modifier.padding(24.dp)) {
        RichTextScope.CodeBlock(
            """
              data class Hello(
                val name: String
              )
            """.trimIndent()
        )
      }
    }
  }
}
