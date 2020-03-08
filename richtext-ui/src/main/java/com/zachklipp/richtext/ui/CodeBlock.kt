@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.ui.core.CurrentTextStyleProvider
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Text
import androidx.ui.core.currentTextStyle
import androidx.ui.foundation.Box
import androidx.ui.foundation.DrawBackground
import androidx.ui.graphics.Color
import androidx.ui.layout.LayoutPadding
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.TextUnit
import androidx.ui.unit.dp
import androidx.ui.unit.sp

internal val DefaultCodeBlockTextStyle = TextStyle(
  fontFamily = FontFamily.Monospace
)
internal val DefaultCodeBlockBackground: Color = Color.LightGray.copy(alpha = .5f)
internal val DefaultCodeBlockPadding: TextUnit = 16.sp

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
  val richTextStyle = currentRichTextStyle.resolveDefaults()
  val textStyle = currentTextStyle().merge(richTextStyle.codeBlockTextStyle)
  val background = DrawBackground(color = richTextStyle.codeBlockBackground!!)
  val blockPadding = with(DensityAmbient.current) {
    richTextStyle.codeBlockPadding!!.toDp()
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
private fun CodeBlockPreviewOnBlack() {
  CodeBlockPreview(containerColor = Color.Black)
}

@Preview @Composable
private fun CodeBlockPreviewOnWhite() {
  CodeBlockPreview(containerColor = Color.White)
}

@Composable
private fun CodeBlockPreview(
  containerColor: Color
) {
  Box(modifier = DrawBackground(color = containerColor)) {
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
