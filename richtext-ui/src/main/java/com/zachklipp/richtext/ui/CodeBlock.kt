@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.compose.Immutable
import androidx.compose.Providers
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.ContentColorAmbient
import androidx.ui.foundation.ProvideTextStyle
import androidx.ui.foundation.Text
import androidx.ui.foundation.currentTextStyle
import androidx.ui.foundation.drawBackground
import androidx.ui.graphics.Color
import androidx.ui.layout.padding
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
internal val DefaultCodeBlockBackground: Color = Color.LightGray.copy(alpha = .5f)
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
  val background = Modifier.drawBackground(color = richTextStyle.background!!)
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
  Providers(ContentColorAmbient provides contentColor) {
    Box(modifier = Modifier.drawBackground(color = backgroundColor)) {
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
