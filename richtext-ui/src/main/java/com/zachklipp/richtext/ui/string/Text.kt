package com.zachklipp.richtext.ui.string

import androidx.animation.Infinite
import androidx.animation.KeyframesBuilder
import androidx.animation.LinearEasing
import androidx.animation.RepeatableBuilder
import androidx.animation.TweenBuilder
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.launchInComposition
import androidx.compose.onActive
import androidx.compose.remember
import androidx.compose.setValue
import androidx.compose.state
import androidx.compose.stateFor
import androidx.ui.animation.animatedColor
import androidx.ui.animation.animatedFloat
import androidx.ui.core.Alignment
import androidx.ui.core.Constraints
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Layout
import androidx.ui.core.Modifier
import androidx.ui.core.Ref
import androidx.ui.core.drawOpacity
import androidx.ui.core.gesture.tapGestureFilter
import androidx.ui.foundation.Canvas
import androidx.ui.foundation.ContentColorAmbient
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.foundation.contentColor
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Color
import androidx.ui.graphics.StrokeCap
import androidx.ui.graphics.drawscope.Stroke
import androidx.ui.graphics.drawscope.withTransform
import androidx.ui.layout.Stack
import androidx.ui.layout.padding
import androidx.ui.layout.size
import androidx.ui.layout.wrapContentSize
import androidx.ui.savedinstancestate.savedInstanceState
import androidx.ui.text.TextLayoutResult
import androidx.ui.text.TextStyle
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.em
import androidx.ui.unit.px
import androidx.ui.unit.sp
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
  val layoutResult = state<TextLayoutResult?> { null }
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
  var hack by stateFor(annotated) { annotated }
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
  ) { measurables, constraints, _ ->
    // Update the inline content before measuring text, so content will get its constraints before
    // being measured.
    constraintsRef.value = constraints

    val p = measurables.single().measure(constraints)
    layout(p.width, p.height) {
      p.place(0.px, 0.px)
    }
  }
}

@Preview(showBackground = true)
@Composable internal fun TextPreview() {
  val context = ContextAmbient.current
  var toggleLink by state { false }
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
    val angleAnim = RepeatableBuilder<Float>().apply {
      iterations = Infinite
      animation = TweenBuilder<Float>().apply {
        duration = 1000
        easing = LinearEasing
      }
    }
    angle.animateTo(360f, angleAnim)

    val colorAnim = RepeatableBuilder<Color>().apply {
      iterations = Infinite
      animation = KeyframesBuilder<Color>().apply {
        duration = 2500
        Color.Blue at 500
        Color.Cyan at 1000
        Color.Green at 1500
        Color.Magenta at 2000
      }
    }
    color.animateTo(Color.Yellow, colorAnim)
  }

  Canvas(modifier = Modifier.size(12.sp.toDp(), 12.sp.toDp()).padding(2.dp)) {
    withTransform({ rotate(angle.value) }) {
      val stroke = Stroke(width = 3.dp.toPx().value, cap = StrokeCap.round)
      drawLine(
        color.value,
        Offset(0f, size.height / 2),
        Offset(size.width, size.height / 2),
        stroke
      )
      drawLine(
        color.value,
        Offset(size.width / 2, 0f),
        Offset(size.width / 2, size.height),
        stroke
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
        modifier = Modifier.padding(3.dp).gravity(Alignment.Center),
        fontSize = 8.sp,
        style = TextStyle(background = Color.LightGray)
      )
    }
  }
}

@Composable private fun LoadingSpinner() {
  val alpha = animatedFloat(initVal = 1f)
  onActive {
    val anim = RepeatableBuilder<Float>().apply {
      iterations = Infinite
      animation = KeyframesBuilder<Float>().apply {
        duration = 500
        0f at 250
        1f at 500
      }
    }
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
    drawLine(Color.Red, Offset(0f, 0f), Offset(size.width, size.height), Stroke())
    drawLine(Color.Red, Offset(0f, size.height), Offset(size.width, 0f), Stroke())
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
