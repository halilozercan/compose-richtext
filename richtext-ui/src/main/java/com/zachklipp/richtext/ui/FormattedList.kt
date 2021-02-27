@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.ambientOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Layout
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.platform.LayoutDirectionAmbient
import androidx.compose.ui.selection.DisableSelection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import com.zachklipp.richtext.ui.ListType.Ordered
import com.zachklipp.richtext.ui.ListType.Unordered
import kotlin.math.max

public enum class ListType {
  /**
   * An ordered (numbered) list.
   */
  Ordered,

  /**
   * An unordered (bullet) list.
   */
  Unordered
}

/**
 * Defines how to draw list markers for [FormattedList]s that are [Ordered].
 *
 * These are typically some sort of ordinal text.
 */
public interface OrderedMarkers {
  @Composable public fun drawMarker(
    level: Int,
    index: Int
  )

  public companion object {
    /**
     * Creates an [OrderedMarkers] that will cycle through the values in [markers] for each
     * indentation level given the index.
     */
    public fun text(vararg markers: (index: Int) -> String): OrderedMarkers =
      OrderedMarkers { level, index ->
        Text(markers[level % markers.size](index))
      }

    /**
     * Creates an [OrderedMarkers] from an arbitrary composable given the indentation level and
     * the index.
     */
    public operator fun invoke(
      drawMarker: @Composable (level: Int, index: Int) -> Unit
    ): OrderedMarkers = object : OrderedMarkers {
      @Composable override fun drawMarker(
        level: Int,
        index: Int
      ) {
        drawMarker(level, index)
      }
    }
  }
}

/**
 * Defines how to draw list markers for [FormattedList]s that are [Unordered].
 *
 * These are typically some sort of bullet point.
 */
public interface UnorderedMarkers {
  @Composable public fun drawMarker(level: Int)

  public companion object {
    /**
     * Creates an [UnorderedMarkers] that will cycle through the values in [markers] for each
     * indentation level.
     */
    public fun text(vararg markers: String): UnorderedMarkers = UnorderedMarkers {
      Text(markers[it % markers.size])
    }

    /**
     * Creates an [UnorderedMarkers] that will cycle through the values in [painters] for each
     * indentation level.
     */
    public fun painters(vararg painters: Painter): UnorderedMarkers = UnorderedMarkers {
      Box(Modifier.paint(painters[it % painters.size]))
    }

    /**
     * Creates an [UnorderedMarkers] from an arbitrary composable given the indentation level.
     */
    public operator fun invoke(drawMarker: @Composable (level: Int) -> Unit): UnorderedMarkers =
      object : UnorderedMarkers {
        @Composable override fun drawMarker(level: Int) = drawMarker(level)
      }
  }
}

/**
 * Defines how [FormattedList]s should look.
 *
 * @param markerIndent The padding before each marker.
 * @param contentsIndent The padding after each marker.
 */
@Immutable
public data class ListStyle(
  val markerIndent: TextUnit? = null,
  val contentsIndent: TextUnit? = null,
  val orderedMarkers: OrderedMarkers? = null,
  val unorderedMarkers: UnorderedMarkers? = null
) {
  public companion object {
    public val Default: ListStyle = ListStyle()
  }
}

private val DefaultMarkerIndent = 8.sp
private val DefaultContentsIndent = 4.sp
private val DefaultOrderedMarkers = OrderedMarkers.text(
  { "${it + 1}." },
  {
    ('a'..'z').drop(it % 26)
      .first() + "."
  },
  { "${it + 1})" },
  {
    ('a'..'z').drop(it % 26)
      .first() + ")"
  }
)
private val DefaultUnorderedMarkers = UnorderedMarkers.text("•", "◦", "▸", "▹")

internal fun ListStyle.resolveDefaults(): ListStyle = ListStyle(
  markerIndent = markerIndent ?: DefaultMarkerIndent,
  contentsIndent = contentsIndent ?: DefaultContentsIndent,
  orderedMarkers = orderedMarkers ?: DefaultOrderedMarkers,
  unorderedMarkers = unorderedMarkers ?: DefaultUnorderedMarkers
)

private val ListLevelAmbient = ambientOf { 0 }

/**
 * Composes [children] with their [ListLevelAmbient] reset back to 0.
 */
@Composable internal fun RestartListLevel(children: @Composable () -> Unit) {
  Providers(ListLevelAmbient provides 0) {
    children()
  }
}

/**
 * Creates a formatted list such as a bullet list or numbered list.
 *
 * @sample com.zachklipp.richtext.ui.OrderedListPreview
 * @sample com.zachklipp.richtext.ui.UnorderedListPreview
 */
// inline is required for https://github.com/zach-klippenstein/compose-richtext/issues/7
@Suppress("NOTHING_TO_INLINE")
@Composable public inline fun RichTextScope.FormattedList(
  listType: ListType,
  vararg children: @Composable RichTextScope.() -> Unit
): Unit = FormattedList(listType, children.asList()) { it() }

/**
 * Creates a formatted list such as a bullet list or numbered list.
 *
 * @sample com.zachklipp.richtext.ui.OrderedListPreview
 * @sample com.zachklipp.richtext.ui.UnorderedListPreview
 */
@Composable public fun <T> RichTextScope.FormattedList(
  listType: ListType,
  items: List<T>,
  drawItem: @Composable RichTextScope.(T) -> Unit
) {
  val listStyle = currentRichTextStyle.resolveDefaults().listStyle!!
  val density = DensityAmbient.current
  val markerIndent = with(density) { listStyle.markerIndent!!.toDp() }
  val contentsIndent = with(density) { listStyle.contentsIndent!!.toDp() }
  val currentLevel = ListLevelAmbient.current

  PrefixListLayout(
    count = items.size,
    prefixPadding = PaddingValues(start = markerIndent, end = contentsIndent),
    prefixForIndex = { index ->
      when (listType) {
        Ordered -> listStyle.orderedMarkers!!.drawMarker(currentLevel, index)
        Unordered -> listStyle.unorderedMarkers!!.drawMarker(currentLevel)
      }
    },
    itemForIndex = { index ->
      RichText {
        Providers(ListLevelAmbient provides currentLevel + 1) {
          drawItem(items[index])
        }
      }
    }
  )
}

@Composable private fun PrefixListLayout(
  count: Int,
  prefixPadding: PaddingValues,
  prefixForIndex: @Composable (index: Int) -> Unit,
  itemForIndex: @Composable (index: Int) -> Unit
) {
  Layout(children = {
    // List markers aren't selectable.
    DisableSelection {
      // Draw the markers first.
      for (i in 0 until count) {
        // TODO Use the padding in the calculation directly instead of wrapping.
        Box(Modifier.padding(prefixPadding)) {

          prefixForIndex(i)
        }
      }
    }

    // Then draw the items.
    for (i in 0 until count) {
      itemForIndex(i)
    }
  }) { measurables, constraints ->
    check(measurables.size == count * 2)
    val prefixMeasureables = measurables.asSequence()
      .take(count)
    val itemMeasurables = measurables.asSequence()
      .drop(count)

    // Measure the prefixes first.
    val prefixPlaceables = prefixMeasureables.map { marker ->
      marker.measure(Constraints())
    }
      .toList()
    val widestPrefix = prefixPlaceables.maxByOrNull { it.width }!!

    // Then measure the items, offset to the right to allow space for the prefixes and gap.
    val itemConstraints = constraints.copy(
      maxWidth = (constraints.maxWidth - widestPrefix.width).coerceAtLeast(0)
    )
    val itemPlaceables = itemMeasurables.map { item ->
      item.measure(itemConstraints)
    }
      .toList()
    val widestItem = itemPlaceables.maxByOrNull { it.width }!!

    val listWidth = widestPrefix.width + widestItem.width
    val listHeight = itemPlaceables.sumBy { it.height }
    layout(listWidth, listHeight) {
      var y = 0

      // Flow the rows vertically, much like Column.
      for (i in 0 until count) {
        val prefix = prefixPlaceables[i]
        val item = itemPlaceables[i]
        val rowHeight = max(prefix.height, item.height)
        val prefixOffset = Alignment.TopEnd.align(
          IntSize(
            width = widestPrefix.width - prefix.width,
            height = rowHeight - prefix.height
          ),
          layoutDirection
        )

        prefix.place(prefixOffset.x, y + prefixOffset.y)
        item.place(widestPrefix.width, y)
        y += rowHeight
      }
    }
  }
}

@Preview(heightDp = 400)
@Composable private fun UnorderedListPreview() {
  ListPreview(listType = Unordered, layoutDirection = LayoutDirection.Ltr)
}

@Preview(heightDp = 400)
@Composable private fun UnorderedListPreviewRtl() {
  ListPreview(listType = Unordered, layoutDirection = LayoutDirection.Rtl)
}

@Preview(heightDp = 400)
@Composable private fun OrderedListPreview() {
  ListPreview(listType = Ordered, layoutDirection = LayoutDirection.Ltr)
}

@Preview(heightDp = 400)
@Composable private fun OrderedListPreviewRtl() {
  ListPreview(listType = Ordered, layoutDirection = LayoutDirection.Rtl)
}

@Composable private fun ListPreview(
  listType: ListType,
  layoutDirection: LayoutDirection
) {
  Providers(LayoutDirectionAmbient provides layoutDirection) {
    Box(Modifier.background(color = Color.White)) {
      RichTextScope.FormattedList(
        listType = listType,
        items = listOf(
          "Foo",
          "Bar",
          "Baz",
          "Foo",
          "Bar",
          "Baz",
          "Foo",
          "Bar",
          "Foo\nBar\nBaz",
          "Foo"
        ).withIndex()
          .toList()
      ) { (index, text) ->
        Text(text)
        if (index == 0) {
          FormattedList(listType, @Composable() {
            Text("indented $text")
            FormattedList(listType, @Composable() {
              Text("indented $text")
              FormattedList(listType, @Composable() {
                Text("indented $text")
              })
            })
          })
        }
      }
    }
  }
}
