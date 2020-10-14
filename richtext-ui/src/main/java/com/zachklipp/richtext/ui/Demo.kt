@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.foundation.AmbientContentColor
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.zachklipp.richtext.ui.ListType.Ordered
import com.zachklipp.richtext.ui.ListType.Unordered
import com.zachklipp.richtext.ui.string.TextPreview

@Preview(widthDp = 300, heightDp = 1000)
@Composable fun RichTextDemoOnWhite() {
  Box(Modifier.background(color = Color.White)) {
    RichTextDemo()
  }
}

@Preview(widthDp = 300, heightDp = 1000)
@Composable fun RichTextDemoOnBlack() {
  Providers(AmbientContentColor provides Color.White) {
    Box(Modifier.background(color = Color.Black)) {
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
    TextPreview()

    Heading(0, "Lists")
    Heading(1, "Unordered")
    ListDemo(listType = Unordered)
    Heading(1, "Ordered")
    ListDemo(listType = Ordered)
//    Heading(1, "Unordered 2")
//    List2Demo(listType = Unordered)
    Heading(1, "Ordered 2")
    List2Demo(listType = Ordered)

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

@Composable private fun RichTextScope.List2Demo(listType: ListType) {
  FormattedList2(listType, Modifier.border(1.dp, Color.Cyan)) {
    Text("First list item", Modifier.border(1.dp, Color.Red))
    Box(Modifier.border(1.dp, Color.Blue)) {
      ListItem(Modifier) { Text("Second list item", Modifier.border(1.dp, Color.Red)) }
    }
    Box(Modifier.border(1.dp, Color.Blue)) {
      ListItem(Modifier) {
        Text(
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
          Modifier.border(1.dp, Color.Red)
        )
      }
    }
    Text(
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
      Modifier.border(1.dp, Color.Red)
    )
    FormattedList2(listType) {
      Text("Indented 1")
      ListItem(Modifier) { Text("Indented 2") }
    }
    repeat(10) {
      Box(
        Modifier
          .border(1.dp, Color.Blue.copy(alpha = .2f))
          .drawLayer(rotationZ = it * .2f)
          .border(1.dp, Color.Blue)
      ) {
        ListItem(Modifier) { Text("List item ${it + 4}", Modifier.border(1.dp, Color.Red)) }
      }
    }
  }
}
