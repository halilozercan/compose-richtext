package com.zachklipp.richtext.ui

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLifecycleObserver
import androidx.compose.runtime.Providers
import androidx.compose.runtime.remember
import androidx.compose.ui.Layout
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ExperimentalSubcomposeLayoutApi
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.selection.DisableSelection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.offset
import androidx.ui.tooling.preview.Preview
import com.zachklipp.richtext.ui.ListSubcomposeSlot.LIST_CONTENT
import com.zachklipp.richtext.ui.ListSubcomposeSlot.MEASURE_MARKERS
import com.zachklipp.richtext.ui.ListType.Ordered
import com.zachklipp.richtext.ui.ListType.Unordered
import kotlin.math.max

/**
 * TODO write documentation
 */
interface FormattedListScope {
  @Composable fun ListItem(
    // Can't have a default due to https://issuetracker.google.com/issues/170882174.
    modifier: Modifier,
    children: @Composable () -> Unit
  )
}

private enum class ListSubcomposeSlot {
  MEASURE_MARKERS,
  LIST_CONTENT
}

/**
 * TODO write documentation
 */
@OptIn(ExperimentalSubcomposeLayoutApi::class)
@Composable fun RichTextScope.FormattedList2(
  listType: ListType,
  modifier: Modifier = Modifier,
  children: @Composable FormattedListScope.() -> Unit
) {
  val listStyle = currentRichTextStyle.resolveDefaults().listStyle!!
  val listLevel = ListLevelAmbient.current
  val listState = remember { FormattedListState(listStyle, listType, listLevel) }
  listState.listType = listType
  listState.listStyle = listStyle
  listState.listLevel = listLevel

  val startPadding = with(DensityAmbient.current) {
    listStyle.markerIndent!!.toDp()
  }

  SubcomposeLayout<ListSubcomposeSlot>(
    modifier = modifier.padding(start = startPadding),
  ) { constraints ->
    // Compose the list items first, so we know how many there are.
    // Note that this won't trigger a layout pass yet.
    val itemMeasurables = subcompose(LIST_CONTENT) {
      Providers(ListLevelAmbient provides listLevel + 1) {
        children(listState)
      }
    }

    // Compose list markers for all items just to measure them – this subcomposition will never be
    // drawn, since individual list items need to be responsible for drawing their own compositions.
    val contentsIndent = listStyle.contentsIndent!!.toIntPx()
    listState.markerConstraints = constraints.offset(horizontal = -contentsIndent)
    val markersForMeasuring = subcompose(MEASURE_MARKERS) {
      when (listType) {
        Unordered -> listStyle.unorderedMarkers!!.drawMarker(listLevel)
        Ordered -> {
          for (i in 0 until listState.itemCount) {
            listStyle.orderedMarkers!!.drawMarker(listLevel, i)
          }
        }
      }
    }.map { it.measure(listState.markerConstraints) }
    val maxMarkerWidth = markersForMeasuring.maxOfOrNull { it.width } ?: 0

    // Now we know the width of all the markers, we can calculate the total gutter width.
    val gutterWidth = maxMarkerWidth + contentsIndent

    // Now that we know how wide the gutter has to be, we can measure the items to fill the
    // remaining space.
    val itemConstraints = constraints.offset(horizontal = -gutterWidth)
    // TODO Handle bounded height constraint.
    val items = itemMeasurables.map { it.measure(itemConstraints) }
    val maxItemWidth = items.maxOfOrNull { it.width } ?: 0

    val width = constraints.constrainWidth(gutterWidth + maxItemWidth)
    val height = constraints.constrainHeight(items.sumOf { it.height })
    layout(width, height) {
      items.fold(0) { y, item ->
        item.placeRelative(gutterWidth, y)
        y + item.height
      }
    }
  }
}

private class FormattedListState(
  var listStyle: ListStyle,
  var listType: ListType,
  var listLevel: Int
) : FormattedListScope {

  var markerConstraints: Constraints = Constraints()

  /**
   * The number of ListItems that are being drawn. This will only be equal to the number of children
   * of FormattedList if all direct children are ListItems, and no ListItems are nested inside of
   * other direct children.
   */
  val itemCount: Int get() = itemTokens.size

  /**
   * Used to track the order in which [ListItem]s are initially laid out, in order to determine
   * ordinals.
   */
  private val itemTokens = mutableListOf<ItemToken>()

  private inner class ItemToken : CompositionLifecycleObserver {

    init {
      // Doesn't enter composition until commit, so we need to do this in init so the value is
      // available for the rest of the composition.
      // TODO Make this thread safe.
      // TODO This introduces a potential memory leak if the token never enters composition.
      //  Pretty sure this needs to be in onEnter() and ListItem needs to use SubcomposeLayout to
      //  do the right thing.
      itemTokens += this

      println("OMG registered new list token: ${getOrdinal()}")
    }

    /**
     * Returns the current ordinal of this token. It may change over time, e.g. previous items were
     * skipped since the last layout pass.
     */
    fun getOrdinal(): Int {
      return itemTokens.indexOf(this)
    }

    override fun onLeave() {
      itemTokens -= this
    }
  }

  @OptIn(ExperimentalSubcomposeLayoutApi::class)
  @Composable override fun ListItem(
    modifier: Modifier,
    children: @Composable () -> Unit
  ) {
    val token = remember { ItemToken() }

    Layout(modifier = modifier, children = {
      // Simplify our code by guaranteeing only two direct children.
      Box {
        ListMarker(token.getOrdinal())
        println("OMG composed list marker for ${token.getOrdinal()}")
      }
      Column { children() }
    }) { measurables, constraints ->
      val (markerMeasurable, contentMeasurable) = measurables

      // The constraints we get will be the constraints of the (possibly nested) item, but we need
      // to measure the marker with the same constraints the parent did, since the parent is
      // reserving space for our marker.
      val marker = markerMeasurable.measure(markerConstraints)
      val content = contentMeasurable.measure(constraints)
      val height = constraints.constrainHeight(max(marker.height, content.height))
      val indent = listStyle.contentsIndent!!.toIntPx()

      layout(content.width, height) {
        marker.placeRelative(-marker.width - indent, 0)
        content.placeRelative(0, 0)
      }
    }
  }

  @Composable private fun ListMarker(ordinal: Int) {
    // Disable selection for list markers.
    DisableSelection {
      when (listType) {
        Unordered -> listStyle.unorderedMarkers!!.drawMarker(listLevel)
        Ordered -> listStyle.orderedMarkers!!.drawMarker(listLevel, ordinal)
      }
    }
  }
}

@Preview(showBackground = true)
@Composable private fun FormattedListPreview() {
  RichText {
    FormattedList2(Ordered) {
      Text("Row one")
      ListItem(Modifier) {
        Text("Row Two")
      }
      Text("Row three")
    }
  }
}
