package com.zachklipp.richtext.ui.string

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.BasicText as ComposeBasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.resolveDefaults
import androidx.compose.ui.text.style.TextOverflow
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.currentContentColor
import com.zachklipp.richtext.ui.currentTextStyle

/**
 * This composable is mostly used by Previews as well as public Composables which take String as
 * a direct argument instead of children Composable.
 *
 * [InternalText] is created because Material Text is not available and calling BasicText would
 * require each time to add `style = currentTextStyle`
 *
 * Arguments are taken directly from [ComposeBasicText] with the exception of [TextStyle]
 */
@Composable
internal fun RichTextScope.InternalText(
  text: String,
  modifier: Modifier = Modifier,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  overflow: TextOverflow = TextOverflow.Clip,
  softWrap: Boolean = true,
  maxLines: Int = Int.MAX_VALUE,
) {
  InternalText(
    AnnotatedString(text),
    modifier,
    onTextLayout,
    overflow,
    softWrap,
    maxLines
  )
}

/**
 * This composable is mostly used by Previews as well as public Composables which take String as
 * a direct argument instead of children Composable.
 *
 * [InternalText] is created because Material Text is not available and calling BasicText would
 * require each time to add `style = currentTextStyle`
 *
 * Arguments are taken directly from [ComposeBasicText] with the exception of [TextStyle]
 */
@Composable
internal fun RichTextScope.InternalText(
  text: AnnotatedString,
  modifier: Modifier = Modifier,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  overflow: TextOverflow = TextOverflow.Clip,
  softWrap: Boolean = true,
  maxLines: Int = Int.MAX_VALUE,
  inlineContent: Map<String, InlineTextContent> = mapOf(),
) {
  val incomingStyle = currentTextStyle.let {
    it.copy(color = it.color.takeOrElse { currentContentColor })
  }
  val updatedTextStyle = resolveDefaults(incomingStyle, LocalLayoutDirection.current)

  ComposeBasicText(
    text = text,
    modifier = modifier,
    style = updatedTextStyle,
    onTextLayout = onTextLayout,
    overflow = overflow,
    softWrap = softWrap,
    maxLines = maxLines,
    inlineContent = inlineContent
  )
}