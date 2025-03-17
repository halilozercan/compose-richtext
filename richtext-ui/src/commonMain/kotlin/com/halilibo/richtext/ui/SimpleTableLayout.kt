package com.halilibo.richtext.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrain
import kotlin.math.roundToInt

/**
 * The offsets of rows and columns of a [SimpleTableLayout], centered inside their spacing.
 *
 * E.g. If a table is given a cell spacing of 2px, then the first column and row offset will each
 * be 1px.
 */
@Immutable
internal data class TableLayoutResult(
  val rowOffsets: List<Float>,
  val columnOffsets: List<Float>
)

/**
 * A simple table that sizes all columns equally.
 *
 * @param cellSpacing The space in between each cell, and between each outer cell and the edge of
 * the table.
 */
@OptIn(ExperimentalStdlibApi::class)
@Composable
internal fun SimpleTableLayout(
  columns: Int,
  rows: List<List<@Composable () -> Unit>>,
  drawDecorations: (TableLayoutResult) -> Modifier,
  cellSpacing: Float,
  modifier: Modifier
) {
  SubcomposeLayout(modifier = modifier) { constraints ->
    val measurables = subcompose(false) {
      rows.forEach { row ->
        check(row.size == columns)
        row.forEach { cell ->
          cell()
        }
      }
    }

    val rowMeasurables = measurables.chunked(columns)
    check(rowMeasurables.size == rows.size)

    check(constraints.hasBoundedWidth) { "Table must have bounded width" }
    // Divide the width by the number of columns, then leave room for the padding.
    val cellSpacingWidth = cellSpacing * (columns + 1)
    val cellWidth = minOf((constraints.maxWidth - cellSpacingWidth) / columns, MinCellWidth)
    val cellSpacingHeight = cellSpacing * (rowMeasurables.size + 1)
    // TODO Handle bounded height constraints.
    // val cellMaxHeight = if (!constraints.hasBoundedHeight) {
    //   Float.MAX_VALUE
    // } else {
    //   // Divide the height by the number of rows, then leave room for the padding.
    //   (constraints.maxHeight - cellSpacingHeight) / rowMeasurables.size
    // }
    val cellConstraints = Constraints(maxWidth = cellWidth.roundToInt()).constrain(constraints)

    val rowPlaceables = rowMeasurables.map { cellMeasurables ->
      cellMeasurables.map { cell ->
        cell.measure(cellConstraints)
      }
    }
    val rowHeights = rowPlaceables.map { row -> row.maxByOrNull { it.height }!!.height }

    val tableWidth = constraints.maxWidth
    val tableHeight = (rowHeights.sumOf { it } + cellSpacingHeight).roundToInt()
    layout(tableWidth, tableHeight) {
      var y = cellSpacing
      val rowOffsets = mutableListOf<Float>()
      val columnOffsets = mutableListOf<Float>()

      rowPlaceables.forEachIndexed { rowIndex, cellPlaceables ->
        rowOffsets += y - cellSpacing / 2f
        var x = cellSpacing

        cellPlaceables.forEach { cell ->
          if (rowIndex == 0) {
            columnOffsets.add(x - cellSpacing / 2f)
          }
          cell.place(x.roundToInt(), y.roundToInt())
          x += cellWidth + cellSpacing
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

private const val MinCellWidth = 10f
