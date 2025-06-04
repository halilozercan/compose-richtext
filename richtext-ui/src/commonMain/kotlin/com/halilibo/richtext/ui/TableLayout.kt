package com.halilibo.richtext.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt


/**
 * The offsets of rows and columns of a [TableLayout], centered inside their spacing.
 *
 * E.g. If a table is given a cell spacing of 2px, then the first column and row offset will each
 * be 1px.
 */
@Immutable
internal data class TableLayoutResult(
  val rowOffsets: List<Float>,
  val columnOffsets: List<Float>
)

@Composable
internal fun TableLayout(
  columns: Int,
  rows: List<List<@Composable () -> Unit>>,
  hasHeader: Boolean,
  drawDecorations: (TableLayoutResult) -> Modifier,
  cellSpacing: Float,
  tableMeasurer: TableMeasurer,
  modifier: Modifier = Modifier
) {
  SubcomposeLayout(modifier = modifier) { constraints ->
    // Subcompose all cells in one pass.
    val measurables = subcompose("cells") {
      rows.forEach { row ->
        check(row.size == columns)
        row.forEach { cell -> cell() }
      }
    }

    val rowMeasurables = measurables.chunked(columns)
    val measurements = tableMeasurer.measure(rowMeasurables, constraints)

    val tableWidth = measurements.columnWidths.sum() +
        (cellSpacing * (columns + 1)).roundToInt()

    val tableHeight = measurements.rowHeights.sum() +
        (cellSpacing * (measurements.rowHeights.size + 1)).roundToInt()

    layout(tableWidth, tableHeight) {
      var y = cellSpacing
      val rowOffsets = mutableListOf<Float>()
      val columnOffsets = mutableListOf<Float>()

      measurements.rowPlaceables.forEachIndexed { rowIndex, cellPlaceables ->
        rowOffsets += y - cellSpacing / 2f
        var x = cellSpacing

        cellPlaceables.forEachIndexed { columnIndex, cell ->
          if (rowIndex == 0) {
            columnOffsets.add(x - cellSpacing / 2f)
          }

          val cellY = if (hasHeader && rowIndex == 0) {
            // Header is bottom-aligned
            y + (measurements.rowHeights[0] - cell.height)
          } else {
            y
          }
          cell.place(x.roundToInt(), cellY.roundToInt())
          x += measurements.columnWidths[columnIndex] + cellSpacing
        }

        if (rowIndex == 0) {
          // Add the right-most edge.
          columnOffsets.add(x - cellSpacing / 2f)
        }

        y += measurements.rowHeights[rowIndex] + cellSpacing
      }

      rowOffsets.add(y - cellSpacing / 2f)

      // Compose and draw the borders.
      val layoutResult = TableLayoutResult(rowOffsets, columnOffsets)
      subcompose(true) {
        Box(modifier = drawDecorations(layoutResult))
      }.single()
        .measure(Constraints.fixed(tableWidth, tableHeight))
        .placeRelative(0, 0)
    }
  }
}
