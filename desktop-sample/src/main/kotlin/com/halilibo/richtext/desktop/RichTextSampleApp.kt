package com.halilibo.richtext.desktop

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.singleWindowApplication
import com.halilibo.richtext.ui.BlockQuote
import com.halilibo.richtext.ui.CodeBlock
import com.halilibo.richtext.ui.CodeBlockStyle
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
import com.halilibo.richtext.ui.material.RichText
import com.halilibo.richtext.ui.resolveDefaults
import com.halilibo.richtext.ui.string.InlineContent
import com.halilibo.richtext.ui.string.RichTextString.Builder
import com.halilibo.richtext.ui.string.RichTextString.Format
import com.halilibo.richtext.ui.string.RichTextString.Format.Bold
import com.halilibo.richtext.ui.string.RichTextString.Format.Code
import com.halilibo.richtext.ui.string.RichTextString.Format.Italic
import com.halilibo.richtext.ui.string.RichTextString.Format.Link
import com.halilibo.richtext.ui.string.RichTextString.Format.Strikethrough
import com.halilibo.richtext.ui.string.RichTextString.Format.Subscript
import com.halilibo.richtext.ui.string.RichTextString.Format.Superscript
import com.halilibo.richtext.ui.string.RichTextString.Format.Underline
import com.halilibo.richtext.ui.string.Text
import com.halilibo.richtext.ui.string.richTextString
import com.halilibo.richtext.ui.string.withFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun main(): Unit = singleWindowApplication(
  title = "RichText KMP"
) {
  var richTextStyle by remember {
    mutableStateOf(
      RichTextStyle(
        codeBlockStyle = CodeBlockStyle(wordWrap = true)
      ).resolveDefaults()
    )
  }

  Surface {
    CompositionLocalProvider(
      LocalScrollbarStyle provides defaultScrollbarStyle().copy(
        hoverColor = Color.DarkGray,
        unhoverColor = Color.Gray
      )
    ) {
      SelectionContainer {
        Row(
          modifier = Modifier
            .padding(32.dp)
            .fillMaxSize(),
          horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
          Column(modifier = Modifier.weight(1f)) {
            RichTextStyleConfig(richTextStyle = richTextStyle, onChanged = { richTextStyle = it })
          }
          Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            RichTextDemo(style = richTextStyle)
          }
        }
      }
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
      Text("")
    },
    @Composable {
      Text("hello")
    },
    @Composable {
      Text("Second list item.")
      FormattedList(listType,
        @Composable { Text("Indented 2") }
      )
    }
  )
}

@Composable fun TextPreview() {
  var toggleLink by remember { mutableStateOf(false) }
  val text = remember(toggleLink) {
    richTextString {
      appendPreviewSentence(Bold)
      appendPreviewSentence(Italic)
      appendPreviewSentence(Underline)
      appendPreviewSentence(Strikethrough)
      appendPreviewSentence(Subscript)
      appendPreviewSentence(Superscript)
      appendPreviewSentence(Code)
      appendPreviewSentence(
        Link(""),
        if (toggleLink) "clicked link" else "link"
      )
      append("Here, ")
      appendInlineContent(content = spinningCross)
      append(", is an inline image. ")
      append("And here, ")
      appendInlineContent(content = slowLoadingImage)
      append(", is an inline image that loads after some delay.")
      append("\n\n")

      append("Here ")
      withFormat(Underline) {
        append("is a ")
        withFormat(Italic) {
          append("longer sentence ")
          withFormat(Bold) {
            append("with many ")
            withFormat(Code) {
              append("different ")
              withFormat(Strikethrough) {
                append("nested")
              }
              append(" ")
            }
          }
          append("styles.")
        }
      }
    }
  }
  RichText(linkClickHandler = { toggleLink = !toggleLink }) {
    Text(text)
  }
}

private val spinningCross = InlineContent {
  val angle = remember { Animatable(0f) }
  val color = remember { Animatable(Color.Red) }
  LaunchedEffect(Unit) {
    val angleAnim = infiniteRepeatable<Float>(
      animation = tween(durationMillis = 1000, easing = LinearEasing)
    )
    launch { angle.animateTo(360f, angleAnim) }

    val colorAnim = infiniteRepeatable<Color>(
      animation = keyframes {
        durationMillis = 2500
        Color.Blue at 500
        Color.Cyan at 1000
        Color.Green at 1500
        Color.Magenta at 2000
      }
    )
    launch { color.animateTo(Color.Yellow, colorAnim) }
  }

  Canvas(modifier = Modifier
    .size(12.sp.toDp(), 12.sp.toDp())
    .padding(2.dp)) {
    withTransform({ rotate(angle.value, center) }) {
      val strokeWidth = 3.dp.toPx()
      val strokeCap = StrokeCap.Round
      drawLine(
        color.value,
        start = Offset(0f, size.height / 2),
        end = Offset(size.width, size.height / 2),
        strokeWidth = strokeWidth,
        cap = strokeCap
      )
      drawLine(
        color.value,
        start = Offset(size.width / 2, 0f),
        end = Offset(size.width / 2, size.height),
        strokeWidth = strokeWidth,
        cap = strokeCap
      )
    }
  }
}

val slowLoadingImage = InlineContent {
  var loaded by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(loaded) {
    if (!loaded) {
      delay(3000)
      loaded = true
    }
  }

  if (!loaded) {
    LoadingSpinner()
  } else {
    Box(Modifier.clickable(onClick = { loaded = false })) {
      val size = remember { Animatable(16f) }
      LaunchedEffect(Unit) { size.animateTo(100f) }
      Picture(Modifier.size(size.value.sp.toDp()))
      Text(
        "click to refresh",
        modifier = Modifier
          .padding(3.dp)
          .align(Alignment.Center),
        fontSize = 8.sp,
        style = TextStyle(background = Color.LightGray)
      )
    }
  }
}

@Composable private fun LoadingSpinner() {
  val alpha = remember { Animatable(1f) }
  LaunchedEffect(Unit) {
    val anim = infiniteRepeatable<Float>(
      animation = keyframes {
        durationMillis = 500
        0f at 250
        1f at 500
      })
    alpha.animateTo(0f, anim)
  }
  Text(
    "⏳",
    fontSize = 3.em,
    modifier = Modifier
      .wrapContentSize(Alignment.Center)
      .graphicsLayer(alpha = alpha.value)
  )
}

@Composable private fun Picture(modifier: Modifier) {
  Canvas(modifier) {
    drawRect(Color.LightGray)
    drawLine(Color.Red, Offset(0f, 0f), Offset(size.width, size.height))
    drawLine(Color.Red, Offset(0f, size.height), Offset(size.width, 0f))
  }
}

@OptIn(ExperimentalStdlibApi::class)
private fun Builder.appendPreviewSentence(
  format: Format,
  text: String = format.javaClass.simpleName.replaceFirstChar { it.lowercase() }
) {
  append("Here is some ")
  withFormat(format) {
    append(text)
  }
  append(" text. ")
}

