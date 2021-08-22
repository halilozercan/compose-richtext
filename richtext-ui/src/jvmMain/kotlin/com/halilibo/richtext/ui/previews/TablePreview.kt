package com.halilibo.richtext.ui.previews

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.ui.BlockQuote
import com.halilibo.richtext.ui.CodeBlock
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.Table
import com.halilibo.richtext.ui.Text

@Preview
@Composable
private fun TablePreview() {
  TablePreviewContents()
}

@Preview
@Composable
private fun TablePreviewFixedWidth() {
  TablePreviewContents()
}

@Composable
private fun TablePreviewContents(modifier: Modifier = Modifier) {
  RichTextScope.Table(
    modifier = modifier
      .background(Color.White)
      .padding(4.dp),
    headerRow = {
      cell { Text("Column 1") }
      cell { Text("Column 2") }
    }
  ) {
    row {
      cell { Text("Hello") }
      cell {
        CodeBlock("Foo bar")
      }
    }
    row {
      cell {
        BlockQuote {
          Text("Stuff")
        }
      }
      cell { Text("Hello world this is a really long line that is going to wrap hopefully") }
    }
  }
}
