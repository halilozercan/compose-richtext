@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry", "FunctionName")

package com.zachklipp.richtext.ui.string

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.*
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.PlaceholderVerticalAlign.Companion.AboveBaseline
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp

/**
 * A Composable that can be embedded inline in a [RichTextString] by passing to
 * [RichTextString.Builder.appendInlineContent].
 *
 * @param initialSize Optional function to calculate the initial size of the content. Not specifying
 * this may cause flicker.
 * @param placeholderVerticalAlign Used to specify how a placeholder is vertically aligned within a
 * text line.
 */
public class InlineContent(
  internal val initialSize: (Density.() -> IntSize)? = null,
  internal val placeholderVerticalAlign: PlaceholderVerticalAlign = AboveBaseline,
  internal val content: @Composable Density.(alternateText: String) -> Unit
)

/**
 * Converts a map of [InlineContent]s into a map of [InlineTextContent] that is ready to pass to
 * the core Text composable. Whenever any of the contents resize themselves, or if the map changes,
 * a new map will be returned with updated [Placeholder]s.
 */
@Composable internal fun manageInlineTextContents(
  inlineContents: Map<String, InlineContent>,
  textConstraints: Constraints
): Map<String, InlineTextContent> {
  val density = LocalDensity.current

  return inlineContents.mapValues { (_, content) ->
    reifyInlineContent(
      content,
      Constraints(maxWidth = textConstraints.maxWidth, maxHeight = textConstraints.maxHeight),
      density
    )
  }
}

/**
 * Given an [InlineContent] function, wraps it in a [InlineTextContent] that will allow the content
 * to measure itself inside the enclosing layout's maximum constraints, and automatically return a
 * new [InlineTextContent] whenever the content changes size to update how much space is reserved
 * in the text layout for the content.
 */
@Composable private fun reifyInlineContent(
  content: InlineContent,
  contentConstraints: Constraints,
  density: Density
): InlineTextContent {
  var size by remember {
    mutableStateOf(
      content.initialSize?.invoke(density),
      structuralEqualityPolicy()
    )
  }

  with(density) {
    // If size is null, content hasn't been measured yet, so just draw with zero width for now.
    // Set the height to 1 em so we can calculate how many pixels in an EM.
    val placeholder = Placeholder(
      width = size?.width?.toSp() ?: 0.sp,
      height = size?.height?.toSp() ?: 1.sp,
      placeholderVerticalAlign = content.placeholderVerticalAlign
    )

    return InlineTextContent(placeholder) { alternateText ->
      Layout(content = { content.content(this, alternateText) }) { measurables, _ ->
        // Measure the content with the constraints for the parent Text layout, not the actual.
        // This allows it to determine exactly how large it needs to be so we can update the
        // placeholder.
        val contentPlaceable = measurables.singleOrNull()?.measure(contentConstraints)
          ?: return@Layout layout(0, 0) {}

        if (contentPlaceable.width != size?.width
          || contentPlaceable.height != size?.height
        ) {
          size = IntSize(contentPlaceable.width, contentPlaceable.height)
        }

        layout(contentPlaceable.width, contentPlaceable.height) {
          contentPlaceable.place(0, 0)
        }
      }
    }
  }
}
