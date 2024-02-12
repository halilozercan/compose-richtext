@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.ui.BlockQuote
import com.halilibo.richtext.ui.CodeBlock
import com.halilibo.richtext.ui.FormattedList
import com.halilibo.richtext.ui.Heading
import com.halilibo.richtext.ui.HorizontalRule
import com.halilibo.richtext.ui.InfoPanel
import com.halilibo.richtext.ui.InfoPanelType
import com.halilibo.richtext.ui.ListType
import com.halilibo.richtext.ui.ListType.Ordered
import com.halilibo.richtext.ui.ListType.Unordered
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.Table
import com.halilibo.richtext.ui.material3.RichText

@Preview(widthDp = 300, heightDp = 1000)
@Composable fun RichTextDemoOnWhite() {
  Box(Modifier.background(color = Color.White)) {
    RichTextDemo()
  }
}

@Preview(widthDp = 300, heightDp = 1000)
@Composable fun RichTextDemoOnBlack() {
  CompositionLocalProvider(LocalContentColor provides Color.White) {
    Box(Modifier.background(color = Color.Black)) {
      RichTextDemo()
    }
  }
}

@Composable fun RichTextDemo(
  style: RichTextStyle? = null,
  header: String = ""
) {
  RichText(
    modifier = Modifier.padding(8.dp),
    style = style
  ) {
    Heading(0, "Paragraphs $header")
    Text("Simple paragraph.")
    Text("Paragraph with\nmultiple lines.")
    Text("Paragraph with really long line that should be getting wrapped.")
    TextPreview()

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

    Heading(0, "Info Panel")
    InfoPanel(InfoPanelType.Primary, "Only text primary info panel")
    InfoPanel(InfoPanelType.Success) {
      Column {
        Text("Successfully sent some data")
        HorizontalRule()
        BlockQuote {
          Text("This is a quote")
        }
      }
    }

    Heading(0, "Table")
    Table(
      modifier = Modifier.fillMaxWidth(),
      headerRow = {
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
