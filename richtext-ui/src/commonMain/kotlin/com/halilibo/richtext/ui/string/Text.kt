package com.halilibo.richtext.ui.string

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import com.halilibo.richtext.ui.ClickableText
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.currentContentColor
import com.halilibo.richtext.ui.currentRichTextStyle
import com.halilibo.richtext.ui.string.RichTextString.Format

/**
 * Renders a [RichTextString] as created with [richTextString].
 *
 * @sample com.halilibo.richtext.ui.previews.TextPreview
 */
@Composable
public fun RichTextScope.Text(
  text: RichTextString,
  modifier: Modifier = Modifier,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  softWrap: Boolean = true,
  overflow: TextOverflow = TextOverflow.Clip,
  maxLines: Int = Int.MAX_VALUE
) {
  val style = currentRichTextStyle.stringStyle
  val contentColor = currentContentColor
  val annotated = remember(text, style, contentColor) {
    val resolvedStyle = (style ?: RichTextStringStyle.Default).resolveDefaults()
    text.toAnnotatedString(resolvedStyle, contentColor)
  }

  val inlineContents = remember(text) { text.getInlineContents() }

  BoxWithConstraints(modifier = modifier) {
    val inlineTextContents = manageInlineTextContents(
      inlineContents = inlineContents,
      textConstraints = constraints,
    )

    ClickableText(
      text = annotated,
      onTextLayout = onTextLayout,
      inlineContent = inlineTextContents,
      softWrap = softWrap,
      overflow = overflow,
      maxLines = maxLines,
      isOffsetClickable = { offset ->
        // When you click past the end of the string, the offset is where the caret should be
        // placed. However, when it is at the end, offset == text.length but parent links will at
        // most end at length - 1. So we need to coerce the offset to be at most length - 1.
        // This fixes an image where only the left side of an image wrapped with a link was only
        // clickable on the left side.
        // However, if a paragraph ends with a link, the link will be clickable past the
        // end of the last line.
        annotated.getConsumableAnnotations(text.formatObjects, offset.coerceAtMost(annotated.length - 1)).any()
      },
      onClick = { offset ->
        annotated.getConsumableAnnotations(text.formatObjects, offset.coerceAtMost(annotated.length - 1))
          .firstOrNull()
          ?.let { link -> link.onClick() }
      }
    )
  }
}

private fun AnnotatedString.getConsumableAnnotations(textFormatObjects: Map<String, Any>, offset: Int): Sequence<Format.Link> =
  getStringAnnotations(Format.FormatAnnotationScope, offset, offset)
    .asSequence()
    .mapNotNull {
      Format.findTag(
        it.item,
        textFormatObjects
      ) as? Format.Link
    }
