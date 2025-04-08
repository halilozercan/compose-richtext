package com.halilibo.richtext.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val MinCellWidth = 10
private val MaxCellWidth = 200.dp

/**
 * The offsets of rows and columns of a [SimpleTableLayout], centered inside their spacing.
 *
 * E.g. If a table is given a cell spacing of 2px, then the first column and row offset will each
 * be 1px.
 */

@Composable
internal fun ScrollableTableLayout(
  columns: Int,
  rows: List<List<@Composable () -> Unit>>,
  drawDecorations: (TableLayoutResult) -> Modifier,
  cellSpacing: Float,
  modifier: Modifier = Modifier
) {
  SubcomposeLayout(
    modifier = modifier
      .horizontalScroll(rememberScrollState())
  ) { constraints ->

    // Subcompose all cells in one pass.
    val measurables = subcompose("cells") {
      rows.forEach { row ->
        check(row.size == columns)
        row.forEach { cell -> cell() }
      }
    }
    // Organize the cells into rows.
    val rowMeasurables = measurables.chunked(columns)

    val cellConstraints = Constraints(maxWidth = MaxCellWidth.roundToPx())
    val rowPlaceables: List<List<Placeable>> = rowMeasurables.map { row ->
      row.map { measurable ->
        measurable.measure(cellConstraints)
      }
    }

    // Determine the width for each column:
    // For each column, take the maximum measured width among all its cells and compare with the 200dp minimum.
    val columnWidths = (0 until columns).map { colIndex ->
      val measuredMax = rowPlaceables.map { it[colIndex].width }.maxOrNull() ?: 0
      maxOf(measuredMax, MinCellWidth)
    }

    // Each row’s height is the maximum cell height in that row.
    val rowHeights = rowPlaceables.map { row ->
      row.maxOf { it.height }
    }

    // Compute total table dimensions, adding the cellSpacing between cells and around the edges.
    val tableWidth = columnWidths.sum() + (cellSpacing * (columns + 1)).roundToInt()
    val tableHeight = rowHeights.sum() + (cellSpacing * (rowHeights.size + 1)).roundToInt()
    layout(tableWidth, tableHeight) {
      var y = cellSpacing
      val rowOffsets = mutableListOf<Float>()
      val columnOffsets = mutableListOf<Float>()

      rowPlaceables.forEachIndexed { rowIndex, cellPlaceables ->
        rowOffsets += y - cellSpacing / 2f
        var x = cellSpacing

        cellPlaceables.forEachIndexed { columnIndex, cell ->
          if (rowIndex == 0) {
            columnOffsets.add(x - cellSpacing / 2f)
          }
          cell.place(x.roundToInt(), y.roundToInt())
          x += columnWidths[columnIndex] + cellSpacing
        }

        if (rowIndex == 0) {
          // Add the right-most edge.
          columnOffsets.add(x - cellSpacing / 2f)
        }

        y += rowHeights[rowIndex] + cellSpacing
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