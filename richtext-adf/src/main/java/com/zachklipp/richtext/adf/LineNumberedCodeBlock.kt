package com.zachklipp.richtext.adf

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.zachklipp.richtext.adf.model.AdfCodeBlock
import com.zachklipp.richtext.ui.CodeBlock
import com.zachklipp.richtext.ui.CodeBlockStyle
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.RichTextStyle
import com.zachklipp.richtext.ui.WithStyle

@Composable internal fun RichTextScope.LineNumberedCodeBlock(
  adfCodeBlock: AdfCodeBlock
) {
  val background = Color(0xFFDFE1E6)
  var lineCount by remember { mutableStateOf(0) }

  Row {
    if (lineCount != 0) {
      WithStyle(style = RichTextStyle(codeBlockStyle = CodeBlockStyle(
        background = background
      ))) {
        DisableSelection {
          CodeBlock(
            text = (1..lineCount).joinToString("\n") { "$it" }
          )
        }
      }
    }
    CodeBlock(
      text = adfCodeBlock.content.joinToString("\n") { it.text },
      onTextLayout = { textLayoutResult ->
        lineCount = textLayoutResult.lineCount
      },
      modifier = Modifier.horizontalScroll(rememberScrollState())
    )
  }
}