package com.halilibo.richtext.ui

import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrain
import com.halilibo.richtext.ui.TableMeasurer.Measurements
import kotlin.math.roundToInt

private const val MinCellWidth = 10

internal interface TableMeasurer {
  fun measure(constraints: Constraints, rowMeasurables: List<List<Measurable>>): Measurements

  data class Measurements(
    val rowPlaceables: List<List<Placeable>>,
    val columnWidths: List<Int>,
    val rowHeights: List<Int>
  )
}

internal class AdaptiveTableMeasurer(
  private val maxCellWidthPx: Int,
): TableMeasurer {
  override fun measure(
    constraints: Constraints,
    rowMeasurables: List<List<Measurable>>
  ): Measurements {
    val columns = rowMeasurables[0].size
    val cellConstraints = Constraints(maxWidth = maxCellWidthPx)
    val rowPlaceables = rowMeasurables.map { row ->
      row.map { measurable ->
        measurable.measure(cellConstraints)
      }
    }

    // Determine the width for each column
    val columnWidths = (0 until columns).map { colIndex ->
      val measuredMax = rowPlaceables.maxOfOrNull { it[colIndex].width } ?: 0
      maxOf(measuredMax, MinCellWidth)
    }

    // Each row’s height is the maximum cell height in that row.
    val rowHeights = rowPlaceables.map { row ->
      row.maxOf { it.height }
    }

    return Measurements(rowPlaceables, columnWidths, rowHeights)
  }
}

internal class UniformTableMeasurer(
  private val cellSpacing: Float
) : TableMeasurer {
  override fun measure(
    constraints: Constraints,
    rowMeasurables: List<List<Measurable>>
  ): Measurements {
    check(constraints.hasBoundedWidth) { "Uniform tables must have bounded width" }

    val columns = rowMeasurables[0].size
    // Divide the width by the number of columns, then leave room for the padding.
    val cellSpacingWidth = cellSpacing * (columns + 1)
    val cellWidth = maxOf(
      (constraints.maxWidth - cellSpacingWidth) / columns,
      MinCellWidth.toFloat()
    )
    // TODO Handle bounded height constraints.
    //val cellSpacingHeight = cellSpacing * (rowMeasurables.size + 1)
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
    val columnWidths = List(columns) { cellWidth.roundToInt() }

    return Measurements(rowPlaceables, columnWidths, rowHeights)
  }
}
