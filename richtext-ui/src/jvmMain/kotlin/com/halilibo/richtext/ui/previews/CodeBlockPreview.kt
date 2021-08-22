package com.halilibo.richtext.ui.previews

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.ui.CodeBlock
import com.halilibo.richtext.ui.LocalInternalContentColor
import com.halilibo.richtext.ui.RichTextScope

@Preview
@Composable
private fun CodeBlockPreviewOnWhite() {
  CodeBlockPreview(backgroundColor = Color.White, contentColor = Color.Black)
}

@Preview
@Composable
private fun CodeBlockPreviewOnBlack() {
  CodeBlockPreview(backgroundColor = Color.Black, contentColor = Color.White)
}

@Composable
private fun CodeBlockPreview(
  backgroundColor: Color,
  contentColor: Color
) {
  CompositionLocalProvider(LocalInternalContentColor provides contentColor) {
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
