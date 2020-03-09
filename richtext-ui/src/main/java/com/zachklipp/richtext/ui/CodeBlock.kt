@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.compose.Immutable
import androidx.ui.core.CurrentTextStyleProvider
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Text
import androidx.ui.core.currentTextStyle
import androidx.ui.foundation.Box
import androidx.ui.foundation.DrawBackground
import androidx.ui.foundation.ProvideContentColor
import androidx.ui.graphics.Color
import androidx.ui.layout.LayoutPadding
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.TextUnit
import androidx.ui.unit.dp
import androidx.ui.unit.sp

@Immutable
data class CodeBlockStyle(
  val textStyle: TextStyle? = null,
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
private val DefaultCodeBlockBackground: Color = Color.LightGray.copy(alpha = .5f)
private val DefaultCodeBlockPadding: TextUnit = 16.sp

internal fun CodeBlockStyle.resolveDefaults() = CodeBlockStyle(
  textStyle = textStyle ?: DefaultCodeBlockTextStyle,
  background = background ?: DefaultCodeBlockBackground,
  padding = padding ?: DefaultCodeBlockPadding
)

/**
 * TODO write documentation
 */
@Composable fun RichTextScope.CodeBlock(text: String) {
  CodeBlock {
    Text(text)
  }
}

/**
 * TODO write documentation
 */
@Composable fun RichTextScope.CodeBlock(children: @Composable() RichTextScope.() -> Unit) {
  val richTextStyle = currentRichTextStyle.resolveDefaults().codeBlockStyle!!
  val textStyle = currentTextStyle().merge(richTextStyle.textStyle)
  val background = DrawBackground(color = richTextStyle.background!!)
  val blockPadding = with(DensityAmbient.current) {
    richTextStyle.padding!!.toDp()
  }

  Box(modifier = background) {
    // Can't use Box(padding=) because that property doesn't seem affect the intrinsic size.
    Box(LayoutPadding(blockPadding)) {
      CurrentTextStyleProvider(textStyle) {
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
  ProvideContentColor(color = contentColor) {
    Box(modifier = DrawBackground(color = backgroundColor)) {
      Box(modifier = LayoutPadding(24.dp)) {
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
