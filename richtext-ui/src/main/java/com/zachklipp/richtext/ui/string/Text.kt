package com.zachklipp.richtext.ui.string

import androidx.compose.animation.animatedColor
import androidx.compose.animation.animatedFloat
import androidx.compose.animation.core.AnimationConstants.Infinite
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ContentColorAmbient
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.launchInComposition
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.onActive
import androidx.compose.runtime.remember
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Layout
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap.Round
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.RichTextStyleAmbient
import com.zachklipp.richtext.ui.string.RichTextString.Builder
import com.zachklipp.richtext.ui.string.RichTextString.Format
import com.zachklipp.richtext.ui.string.RichTextString.Format.Bold
import com.zachklipp.richtext.ui.string.RichTextString.Format.Code
import com.zachklipp.richtext.ui.string.RichTextString.Format.Italic
import com.zachklipp.richtext.ui.string.RichTextString.Format.Link
import com.zachklipp.richtext.ui.string.RichTextString.Format.Strikethrough
import com.zachklipp.richtext.ui.string.RichTextString.Format.Subscript
import com.zachklipp.richtext.ui.string.RichTextString.Format.Superscript
import com.zachklipp.richtext.ui.string.RichTextString.Format.Underline
import kotlinx.coroutines.delay
import java.util.Locale

private const val ZERO_WIDTH_CHAR = "\u200B"

/**
 * TODO kdoc
 *
 * @sample com.zachklipp.richtext.ui.string.TextPreview
 */
@Suppress("unused")
@Composable
fun RichTextScope.Text(
  text: RichTextString,
  modifier: Modifier = Modifier,
  onTextLayout: (TextLayoutResult) -> Unit = {}
) {
  val style = RichTextStyleAmbient.current.stringStyle
  val contentColor = ContentColorAmbient.current
  val annotated = remember(text, style, contentColor) {
    val resolvedStyle = (style ?: RichTextStringStyle.Default).resolveDefaults()
    text.toAnnotatedString(resolvedStyle, contentColor)
  }
  val layoutResult = remember<MutableState<TextLayoutResult?>> { mutableStateOf(null) }
  val pressIndicator = Modifier.tapGestureFilter { position ->
    layoutResult.value?.let { layoutResult ->
      val offset = layoutResult.getOffsetForPosition(position)
      annotated.getStringAnnotations(Format.FormatAnnotationScope, offset, offset)
        .asSequence()
        .mapNotNull { Format.findTag(it.item, text.formatObjects) as? Link }
        .firstOrNull()
        ?.let { link -> link.onClick() }
    }
  }

  val constraintsRef = remember { Ref<Constraints>() }
  var hack by remember(annotated) { mutableStateOf(annotated) }
  val inlineContents = remember(text) { text.getInlineContents() }
  // The constraints function won't be called until the content is actually composed and EM is
  // measured, which won't happen until the text is composed.
  val inlineTextContents = ManageInlineTextContents(
    inlineContents = inlineContents,
    textConstraints = { constraintsRef.value!! },
    forceTextRelayout = {
      // Modifying the actual string will cause Text to realize it needs to relayout.
      // We use a special unicode character that doesn't render so there's no visual effect.
      hack = hack.copy(text = hack.text + ZERO_WIDTH_CHAR)
    }
  )

  // This is a giant hack to work around inline content limitations:
  // 1. We have to ask content to measure themselves with our constraints so we can generate the
  //    correct Placeholders.
  // 2. Text doesn't re-layout the text when the placeholders change.
  Layout(
    modifier = modifier + pressIndicator,
    children = {
      Text(
        text = hack,
        onTextLayout = { result ->
          layoutResult.value = result
          onTextLayout(result)
        },
        inlineContent = inlineTextContents
      )
    }
  ) { measurables, constraints ->
    // Update the inline content before measuring text, so content will get its constraints before
    // being measured.
    constraintsRef.value = constraints

    val p = measurables.single().measure(constraints)
    layout(p.width, p.height) {
      p.place(0, 0)
    }
  }
}

@Preview(showBackground = true)
@Composable internal fun TextPreview() {
  val context = ContextAmbient.current
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
        Link { toggleLink = !toggleLink },
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
  RichTextScope.Text(text)
}

private val spinningCross = InlineContent {
  val angle = animatedFloat(0f)
  val color = animatedColor(Color.Red)
  onActive {
    val angleAnim = repeatable<Float>(
      iterations = Infinite,
      animation = tween(durationMillis = 1000, easing = LinearEasing)
    )
    angle.animateTo(360f, angleAnim)

    val colorAnim = repeatable<Color>(
      iterations = Infinite,
      animation = keyframes {
        durationMillis = 2500
        Color.Blue at 500
        Color.Cyan at 1000
        Color.Green at 1500
        Color.Magenta at 2000
      }
    )
    color.animateTo(Color.Yellow, colorAnim)
  }

  Canvas(modifier = Modifier.size(12.sp.toDp(), 12.sp.toDp()).padding(2.dp)) {
    withTransform({ rotate(angle.value) }) {
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
  var loaded by savedInstanceState { false }
  launchInComposition(loaded) {
    if (!loaded) {
      delay(3000)
      loaded = true
    }
  }

  if (!loaded) {
    LoadingSpinner()
  } else {
    Stack(Modifier.clickable(onClick = { loaded = false })) {
      val size = animatedFloat(16f)
      onActive { size.animateTo(100f) }
      Picture(Modifier.size(size.value.sp.toDp()))
      Text(
        "click to refresh",
        modifier = Modifier.padding(3.dp).align(Alignment.Center),
        fontSize = 8.sp,
        style = TextStyle(background = Color.LightGray)
      )
    }
  }
}

@Composable private fun LoadingSpinner() {
  val alpha = animatedFloat(initVal = 1f)
  onActive {
    val anim = repeatable<Float>(
      iterations = Infinite,
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
    modifier = Modifier.wrapContentSize(Alignment.Center)
      .drawOpacity(alpha.value)
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
  text: String = format.javaClass.simpleName.decapitalize(Locale.US)
) {
  append("Here is some ")
  withFormat(format) {
    append(text)
  }
  append(" text. ")
}
