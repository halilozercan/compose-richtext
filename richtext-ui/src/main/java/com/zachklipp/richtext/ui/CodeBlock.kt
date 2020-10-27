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
