@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.compose.Immutable
import androidx.compose.Providers
import androidx.compose.ambientOf
import androidx.ui.core.Alignment
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Text
import androidx.ui.core.toModifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.DrawBackground
import androidx.ui.graphics.Color
import androidx.ui.graphics.painter.Painter
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.Table
import androidx.ui.layout.TableColumnWidth
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.TextUnit
import androidx.ui.unit.sp
import com.zachklipp.richtext.ui.ListType.Ordered
import com.zachklipp.richtext.ui.ListType.Unordered

enum class ListType {
  Ordered,
  Unordered
}

interface OrderedMarkers {
  @Composable fun drawMarker(level: Int, index: Int)

  companion object {
    fun text(vararg markers: (index: Int) -> String) = OrderedMarkers { level, index ->
      Text(markers[level % markers.size](index))
    }

    operator fun invoke(drawMarker: @Composable() (level: Int, index: Int) -> Unit): OrderedMarkers =
      object : OrderedMarkers {
        @Composable override fun drawMarker(level: Int, index: Int) {
          drawMarker(level, index)
        }
      }
  }
}

interface UnorderedMarkers {
  @Composable fun drawMarker(level: Int)

  companion object {
    fun text(vararg markers: String) = UnorderedMarkers {
      Text(markers[it % markers.size])
    }

    fun painters(vararg painters: Painter) = UnorderedMarkers {
      Box(painters[it % painters.size].toModifier())
    }

    operator fun invoke(drawMarker: @Composable() (level: Int) -> Unit): UnorderedMarkers =
      object : UnorderedMarkers {
        @Composable override fun drawMarker(level: Int) = drawMarker(level)
      }
  }
}

@Immutable
data class ListStyle(
  val markerIndent: TextUnit? = null,
  val contentsIndent: TextUnit? = null,
  val orderedMarkers: OrderedMarkers? = null,
  val unorderedMarkers: UnorderedMarkers? = null
) {
  companion object {
    val Default = ListStyle()
  }
}

private val DefaultMarkerIndent = 8.sp
private val DefaultContentsIndent = 4.sp
private val DefaultOrderedMarkers = OrderedMarkers.text(
  { "${it + 1}." },
  { "${('a'..'z').drop(it % 26).first()}." },
  { "${it + 1})" },
  { "${('a'..'z').drop(it % 26).first()})" }
)
private val DefaultUnorderedMarkers = UnorderedMarkers.text("•", "◦", "▸", "▹")

internal fun ListStyle.resolveDefaults(): ListStyle = ListStyle(
  markerIndent = markerIndent ?: DefaultMarkerIndent,
  contentsIndent = contentsIndent ?: DefaultContentsIndent,
  orderedMarkers = orderedMarkers ?: DefaultOrderedMarkers,
  unorderedMarkers = unorderedMarkers ?: DefaultUnorderedMarkers
)

private val ListLevelAmbient = ambientOf { 0 }

@Composable internal fun RestartListLevel(children: @Composable() () -> Unit) {
  Providers(ListLevelAmbient provides 0) {
    children()
  }
}

/**
 * TODO write documentation
 */
@Composable fun RichTextScope.FormattedList(
  listType: ListType,
  vararg children: @Composable() RichTextScope.() -> Unit
) = FormattedList(listType, children.asList()) { it() }

/**
 * TODO write documentation
 */
@Composable fun <T> RichTextScope.FormattedList(
  listType: ListType,
  items: Iterable<T>,
  drawItem: @Composable() RichTextScope.(T) -> Unit
) {
  val listStyle = currentRichTextStyle.resolveDefaults().listStyle!!
  val density = DensityAmbient.current
  val markerIndent = with(density) { listStyle.markerIndent!!.toDp() }
  val contentsIndent = with(density) { listStyle.contentsIndent!!.toDp() }
  val currentLevel = ListLevelAmbient.current

  Table(
    columns = 2,
    alignment = { column ->
      when (column) {
        0 -> Alignment.TopEnd
        else -> Alignment.TopStart
      }
    },
    columnWidth = { column ->
      when (column) {
        0 -> TableColumnWidth.Wrap
        else -> TableColumnWidth.Flex(1f)
      }
    }
  ) {
    items.forEachIndexed { index, listItem ->
      tableRow {
        // Draw the marker.
        Box(LayoutPadding(start = markerIndent)) {
          when (listType) {
            Ordered -> listStyle.orderedMarkers!!.drawMarker(currentLevel, index)
            Unordered -> listStyle.unorderedMarkers!!.drawMarker(currentLevel)
          }
        }

        // Draw the item contents.
        RichText(LayoutPadding(start = contentsIndent)) {
          Providers(ListLevelAmbient provides currentLevel + 1) {
            drawItem(listItem)
          }
        }
      }
    }
  }
}

@Preview @Composable private fun UnorderedListPreview() {
  ListPreview(listType = Unordered)
}

@Preview @Composable private fun OrderedListPreview() {
  ListPreview(listType = Ordered)
}

@Composable
private fun ListPreview(listType: ListType) {
  Box(DrawBackground(color = Color.White)) {
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
        "Baz",
        "Foo",
        "Bar",
        "Baz"
      ).withIndex()
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
