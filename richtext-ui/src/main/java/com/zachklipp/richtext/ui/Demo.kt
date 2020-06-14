@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.compose.Providers
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.ContentColorAmbient
import androidx.ui.foundation.Text
import androidx.ui.foundation.drawBackground
import androidx.ui.graphics.Color
import androidx.ui.layout.padding
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.zachklipp.richtext.ui.ListType.Ordered
import com.zachklipp.richtext.ui.ListType.Unordered

@Preview(widthDp = 300, heightDp = 1000)
@Composable fun RichTextDemoOnWhite() {
  Box(Modifier.drawBackground(color = Color.White)) {
    RichTextDemo()
  }
}

@Preview(widthDp = 300, heightDp = 1000)
@Composable fun RichTextDemoOnBlack() {
  Providers(ContentColorAmbient provides Color.White) {
    Box(Modifier.drawBackground(color = Color.Black)) {
      RichTextDemo()
    }
  }
}

@Composable fun RichTextDemo(style: RichTextStyle? = null) {
  RichText(modifier = Modifier.padding(8.dp), style = style) {
    Heading(0, "Paragraphs")
    Text("Simple paragraph.")
    Text("Paragraph with\nmultiple lines.")
    Text("Paragraph with really long line that should be getting wrapped.")

    Heading(0, "Lists")
    Heading(1, "Unordered")
    ListDemo(listType = Unordered)
    Heading(1, "Ordered")
    ListDemo(listType = Ordered)

    Heading(0, "Horizontal Line")
    Text("Above line")
    HorizontalRule()
    Text("Below line")

    Heading(0, "Code Block")
    CodeBlock(
        """
        {
          "Hello": "world!"
        }
      """.trimIndent()
    )

    Heading(0, "Block Quote")
    BlockQuote {
      Text("These paragraphs are quoted.")
      Text("More text.")
      BlockQuote {
        Text("Nested block quote.")
      }
    }

    Heading(0, "Table")
    Table(headerRow = {
      cell { Text("Column 1") }
      cell { Text("Column 2") }
    }) {
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
}

@Composable private fun RichTextScope.ListDemo(listType: ListType) {
  FormattedList(listType,
      @Composable {
        Text("First list item")
        FormattedList(listType,
            @Composable { Text("Indented 1") }
        )
      },
      @Composable {
        Text("Second list item.")
        FormattedList(listType,
            @Composable { Text("Indented 2") }
        )
      }
  )
}
