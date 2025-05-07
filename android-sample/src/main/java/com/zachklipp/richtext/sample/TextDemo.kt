package com.zachklipp.richtext.sample

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.ui.material3.RichText
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

@Preview(showBackground = true)
@Composable fun TextPreview() {
  val context = LocalContext.current
  var toggleLink by remember { mutableStateOf(false) }
  val text = remember(context, toggleLink) {
    richTextString {
      appendPreviewSentence(Bold)
      appendPreviewSentence(Italic)
      appendPreviewSentence(Underline)
      appendPreviewSentence(Strikethrough)
      appendPreviewSentence(Subscript)
      appendPreviewSentence(Superscript)
      appendPreviewSentence(Code)
      appendPreviewSentence(
        Link("") { toggleLink = !toggleLink },
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
  RichText {
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
      val strokeCap = Round
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

@Composable
fun ProvideToastUriHandler(context: Context, content: @Composable () -> Unit) {
  val uriHandler = remember(context) {
    object : UriHandler {
      override fun openUri(uri: String) {
        Toast.makeText(context, uri, Toast.LENGTH_SHORT).show()
      }
    }
  }

  CompositionLocalProvider(LocalUriHandler provides uriHandler, content)
}
