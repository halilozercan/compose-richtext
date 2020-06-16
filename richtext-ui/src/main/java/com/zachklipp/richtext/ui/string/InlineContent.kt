@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry", "FunctionName")

package com.zachklipp.richtext.ui.string

import androidx.compose.Composable
import androidx.compose.StructurallyEqual
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.Constraints
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Layout
import androidx.ui.geometry.Size
import androidx.ui.text.InlineTextContent
import androidx.ui.text.Placeholder
import androidx.ui.text.PlaceholderVerticalAlign.AboveBaseline
import androidx.ui.unit.Density
import androidx.ui.unit.px
import androidx.ui.unit.sp
import androidx.ui.unit.toPx

/**
 * A Composable that can be embedded inline in a [RichTextString] by passing to
 * [RichTextString.Builder.appendInlineContent].
 *
 * @param initialSize Optional function to calculate the initial size of the content. Not specifying
 * this may cause flicker.
 */
class InlineContent(
  internal val initialSize: (Density.() -> Size)? = null,
  internal val content: @Composable() Density.(alternateText: String) -> Unit
)

/**
 * Converts a map of [InlineContent]s into a map of [InlineTextContent] that is ready to pass to
 * the core Text composable. Whenever any of the contents resize themselves, or if the map changes,
 * a new map will be returned with updated [Placeholder]s.
 */
@Composable internal fun ManageInlineTextContents(
  inlineContents: Map<String, InlineContent>,
  textConstraints: () -> Constraints,
  forceTextRelayout: () -> Unit
): Map<String, InlineTextContent> {
  val density = DensityAmbient.current

  // The content must fit inside the text's max bounds, but can be as small as it wants.
  val contentConstraints = fun(): Constraints = textConstraints().let {
    Constraints(maxWidth = it.maxWidth, maxHeight = it.maxHeight)
  }

  return inlineContents.mapValues { (_, content) ->
    ReifyInlineContent(
      content,
      contentConstraints,
      density,
      forceTextRelayout
    )
  }
}

/**
 * Given an [InlineContent] function, wraps it in a [InlineTextContent] that will allow the content
 * to measure itself inside the enclosing layout's maximum constraints, and automatically return a
 * new [InlineTextContent] whenever the content changes size to update how much space is reserved
 * in the text layout for the content.
 */
@Composable private fun ReifyInlineContent(
  content: InlineContent,
  contentConstraints: () -> Constraints,
  density: Density,
  forceTextRelayout: () -> Unit
): InlineTextContent {
  var size by state(StructurallyEqual) { content.initialSize?.invoke(density) }

  with(density) {
    // If size is null, content hasn't been measured yet, so just draw with zero width for now.
    // Set the height to 1 em so we can calculate how many pixels in an EM.
    val placeholder = Placeholder(
      width = size?.width?.toSp() ?: 0.sp,
      height = size?.height?.toSp() ?: 1.sp,
      placeholderVerticalAlign = AboveBaseline
    )

    return InlineTextContent(placeholder) { alternateText ->
      Layout(children = { content.content(this, alternateText) }) { measurables, _, _ ->
        // Measure the content with the constraints for the parent Text layout, not the actual.
        // This allows it to determine exactly how large it needs to be so we can update the
        // placeholder.
        val contentPlaceable = measurables.single().measure(contentConstraints())
        if (contentPlaceable.width.toPx().value != size?.width
          || contentPlaceable.height.toPx().value != size?.height
        ) {
          size = Size(contentPlaceable.width.toPx().value, contentPlaceable.height.toPx().value)
          forceTextRelayout()
        }

        layout(contentPlaceable.width, contentPlaceable.height) {
          contentPlaceable.place(0.px, 0.px)
        }
      }
    }
  }
}
