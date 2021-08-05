@file:Suppress("ComposableNaming")

package com.zachklipp.richtext.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
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
 * Creates an [OrderedMarkers] that will cycle through the values in [markers] for each
 * indentation level given the index.
 */
public fun RichTextScope.textOrderedMarkers(
  vararg markers: (index: Int) -> String
): OrderedMarkers =
  OrderedMarkers { level, index ->
    Text(markers[level % markers.size](index))
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
     * Creates an [UnorderedMarkers] from an arbitrary composable given the indentation level.
     */
    public operator fun invoke(drawMarker: @Composable (level: Int) -> Unit): UnorderedMarkers =
      object : UnorderedMarkers {
        @Composable override fun drawMarker(level: Int) = drawMarker(level)
      }
  }
}

/**
 * Creates an [UnorderedMarkers] that will cycle through the values in [markers] for each
 * indentation level.
 */
public fun @Composable RichTextScope.textUnorderedMarkers(
  vararg markers: String
): UnorderedMarkers = UnorderedMarkers {
  Text(markers[it % markers.size])
}

/**
 * Creates an [UnorderedMarkers] that will cycle through the values in [painters] for each
 * indentation level.
 */
public fun painterUnorderedMarkers(vararg painters: Painter): UnorderedMarkers = UnorderedMarkers {
  Box(Modifier.paint(painters[it % painters.size]))
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
  val orderedMarkers: (RichTextScope.() -> OrderedMarkers)? = null,
  val unorderedMarkers: (RichTextScope.() -> UnorderedMarkers)? = null
) {
  public companion object {
    public val Default: ListStyle = ListStyle()
  }
}

private val DefaultMarkerIndent = 8.sp
private val DefaultContentsIndent = 4.sp
private val DefaultOrderedMarkers: RichTextScope.() -> OrderedMarkers = {
  textOrderedMarkers(
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
}
private val DefaultUnorderedMarkers: RichTextScope.() -> UnorderedMarkers = {
  textUnorderedMarkers("•", "◦", "▸", "▹")
}

internal fun ListStyle.resolveDefaults(): ListStyle = ListStyle(
  markerIndent = markerIndent ?: DefaultMarkerIndent,
  contentsIndent = contentsIndent ?: DefaultContentsIndent,
  orderedMarkers = orderedMarkers ?: DefaultOrderedMarkers,
  unorderedMarkers = unorderedMarkers ?: DefaultUnorderedMarkers
)

private val LocalListLevel = compositionLocalOf { 0 }

/**
 * Composes [children] with their [LocalListLevel] reset back to 0.
 */
@Composable internal fun RestartListLevel(children: @Composable () -> Unit) {
  CompositionLocalProvider(LocalListLevel provides 0) {
    children()
  }
}

/**
 * Creates a formatted list such as a bullet list or numbered list.
 *
 * @sample com.zachklipp.richtext.ui.OrderedListPreview
 * @sample com.zachklipp.richtext.ui.UnorderedListPreview
 */
// inline is required for https://github.com/halilozercan/compose-richtext/issues/7
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
  val density = LocalDensity.current
  val markerIndent = with(density) { listStyle.markerIndent!!.toDp() }
  val contentsIndent = with(density) { listStyle.contentsIndent!!.toDp() }
  val currentLevel = LocalListLevel.current

  PrefixListLayout(
    count = items.size,
    prefixPadding = PaddingValues(start = markerIndent, end = contentsIndent),
    prefixForIndex = { index ->
      when (listType) {
        Ordered -> listStyle.orderedMarkers!!().drawMarker(currentLevel, index)
        Unordered -> listStyle.unorderedMarkers!!().drawMarker(currentLevel)
      }
    },
    itemForIndex = { index ->
      BasicRichText {
        CompositionLocalProvider(LocalListLevel provides currentLevel + 1) {
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
  Layout(content = {
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
    val listHeight = itemPlaceables.sumOf { it.height }
    layout(listWidth, listHeight) {
      var y = 0

      // Flow the rows vertically, much like Column.
      for (i in 0 until count) {
        val prefix = prefixPlaceables[i]
        val item = itemPlaceables[i]
        val rowHeight = max(prefix.height, item.height)
        val size = IntSize(
          width = widestPrefix.width - prefix.width,
          height = rowHeight - prefix.height
        )
        val prefixOffset = Alignment.TopEnd.align(
          size = size,
          space = size,
          layoutDirection = layoutDirection
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
  CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
    Box(Modifier.background(color = Color.White)) {
      RichTextScope.Default.FormattedList(
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
          FormattedList(listType, @Composable {
            Text("indented $text")
            FormattedList(listType, @Composable {
              Text("indented $text")
              FormattedList(listType, @Composable {
                Text("indented $text")
              })
            })
          })
        }
      }
    }
  }
}
