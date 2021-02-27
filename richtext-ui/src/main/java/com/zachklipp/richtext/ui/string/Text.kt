package com.zachklipp.richtext.ui.string

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.node.Ref
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import com.zachklipp.richtext.ui.LocalRichTextStyle
import com.zachklipp.richtext.ui.RichText
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.string.RichTextString.Format
import com.zachklipp.richtext.ui.string.RichTextString.Format.Bold
import com.zachklipp.richtext.ui.string.RichTextString.Format.Link

private const val ZERO_WIDTH_CHAR = "\u200B"

@Preview(showBackground = true)
@Composable private fun TextPreview() {
  RichText {
    Text(richTextString {
      append("I'm ")
      withFormat(Bold) {
        append("bold!")
      }
    })
  }
}

/**
 * Renders a [RichTextString] as created with [richTextString].
 *
 * @sample com.zachklipp.richtext.ui.string.TextPreview
 */
@Suppress("unused")
@Composable
public fun RichTextScope.Text(
  text: RichTextString,
  modifier: Modifier = Modifier,
  onTextLayout: (TextLayoutResult) -> Unit = {}
) {
  val style = LocalRichTextStyle.current.stringStyle
  val contentColor = LocalContentColor.current
  val annotated = remember(text, style, contentColor) {
    val resolvedStyle = (style ?: RichTextStringStyle.Default).resolveDefaults()
    text.toAnnotatedString(resolvedStyle, contentColor)
  }
  val layoutResult = remember<MutableState<TextLayoutResult?>> { mutableStateOf(null) }
  val pressIndicator = Modifier.pointerInput(Unit) {
    detectTapGestures { position ->
      layoutResult.value?.let { layoutResult ->
        val offset = layoutResult.getOffsetForPosition(position)
        annotated.getStringAnnotations(Format.FormatAnnotationScope, offset, offset)
          .asSequence()
          .mapNotNull { Format.findTag(it.item, text.formatObjects) as? Link }
          .firstOrNull()
          ?.let { link -> link.onClick() }
      }
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
      hack += AnnotatedString(ZERO_WIDTH_CHAR)
    }
  )

  // This is a giant hack to work around inline content limitations:
  // 1. We have to ask content to measure themselves with our constraints so we can generate the
  //    correct Placeholders.
  // 2. Text doesn't re-layout the text when the placeholders change.
  // TODO Can this be done less hackily with SubcomposeLayout?
  Layout(
    modifier = modifier.then(pressIndicator),
    content = {
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
