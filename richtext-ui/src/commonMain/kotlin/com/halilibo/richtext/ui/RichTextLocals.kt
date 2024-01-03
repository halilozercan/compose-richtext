package com.halilibo.richtext.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.halilibo.richtext.ui.util.detectTapGesturesIf

/**
 * Carries the text style in Composition tree. [Heading], [CodeBlock],
 * [BlockQuote] are designed to change the ongoing [TextStyle] in composition,
 * so that their children can use the modified text style implicitly.
 *
 * LocalTextStyle also exists in Material package but this one is internal
 * to RichText.
 */
internal val LocalInternalTextStyle = compositionLocalOf { TextStyle.Default }

/**
 * Carries the content color in Composition tree. Default TextStyle
 * does not have text color specified. It defaults to [Color.Black]
 * in the "resolve chain" but Dark Mode is an exception. To also resolve
 * for Dark Mode, content color should be passed to [RichTextScope].
 */
internal val LocalInternalContentColor = compositionLocalOf { Color.Black }

/**
 * The current [TextStyle].
 */
internal val RichTextScope.currentTextStyle: TextStyle
  @Composable get() = textStyleProvider()

/**
 * The current content [Color].
 */
internal val RichTextScope.currentContentColor: Color
  @Composable get() = contentColorProvider()

/**
 * Intended for preview composables.
 */
@Composable
internal fun RichTextScope.Text(
  text: String,
  modifier: Modifier = Modifier,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  overflow: TextOverflow = TextOverflow.Clip,
  softWrap: Boolean = true,
  maxLines: Int = Int.MAX_VALUE
) {
  val textColor = currentTextStyle.color.takeOrElse { currentContentColor }
  val style = currentTextStyle.copy(color = textColor)

  BasicText(
    text = text,
    modifier = modifier,
    style = style,
    onTextLayout = onTextLayout,
    overflow = overflow,
    softWrap = softWrap,
    maxLines = maxLines
  )
}

@Composable
internal fun RichTextScope.Text(
  text: AnnotatedString,
  modifier: Modifier = Modifier,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  overflow: TextOverflow = TextOverflow.Clip,
  softWrap: Boolean = true,
  maxLines: Int = Int.MAX_VALUE,
  inlineContent: Map<String, InlineTextContent> = mapOf(),
) {
  val textColor = currentTextStyle.color.takeOrElse { currentContentColor }
  val style = currentTextStyle.copy(color = textColor)

  BasicText(
    text = text,
    modifier = modifier,
    style = style,
    onTextLayout = onTextLayout,
    overflow = overflow,
    softWrap = softWrap,
    maxLines = maxLines,
    inlineContent = inlineContent
  )
}

/**
 * Default ClickableText implementation from Compose does not take inlineContent for some reason.
 * Instead of replicating the same behavior inside [Text] that takes [RichTextString], we separate
 * the logic into our own [ClickableText].
 */
@Composable
internal fun RichTextScope.ClickableText(
  text: AnnotatedString,
  modifier: Modifier = Modifier,
  softWrap: Boolean = true,
  overflow: TextOverflow = TextOverflow.Clip,
  maxLines: Int = Int.MAX_VALUE,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  inlineContent: Map<String, InlineTextContent> = mapOf(),
  isOffsetClickable: (Int) -> Boolean,
  onClick: (Int) -> Unit
) {
  val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
  val shouldHandle = { pos: Offset ->
    layoutResult.value?.getOffsetForPosition(pos)?.let { isOffsetClickable(it) } ?: false
  }
  val pressIndicator = Modifier.pointerInput(onClick) {
    detectTapGesturesIf(predicate = shouldHandle) { pos ->
      layoutResult.value?.let { layoutResult ->
        onClick(layoutResult.getOffsetForPosition(pos))
      }
    }
  }

  Text(
    text = text,
    modifier = modifier.then(pressIndicator),
    softWrap = softWrap,
    overflow = overflow,
    maxLines = maxLines,
    onTextLayout = {
      layoutResult.value = it
      onTextLayout(it)
    },
    inlineContent = inlineContent
  )
}
