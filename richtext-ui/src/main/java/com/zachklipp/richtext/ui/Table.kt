@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.foundation.AmbientTextStyle
import androidx.compose.foundation.ProvideTextStyle
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.currentTextStyle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ExperimentalSubcomposeLayoutApi
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.enforce
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import kotlin.math.max
import kotlin.math.roundToInt

@Immutable
data class TableStyle(
  val headerTextStyle: TextStyle? = null,
  val cellPadding: TextUnit? = null,
  val borderColor: Color? = null,
  val borderStrokeWidth: Float? = null
) {
  companion object {
    val Default = TableStyle()
  }
}

private val DefaultTableHeaderTextStyle = TextStyle(fontWeight = FontWeight.Bold)
private val DefaultCellPadding = 8.sp
private val DefaultBorderColor = Color.Black
private const val DefaultBorderStrokeWidth = 1f

internal fun TableStyle.resolveDefaults() = TableStyle(
  headerTextStyle = headerTextStyle ?: DefaultTableHeaderTextStyle,
  cellPadding = cellPadding ?: DefaultCellPadding,
  borderColor = borderColor ?: DefaultBorderColor,
  borderStrokeWidth = borderStrokeWidth ?: DefaultBorderStrokeWidth
)

interface RichTextTableRowScope {
  fun row(children: RichTextTableCellScope.() -> Unit)
}

interface RichTextTableCellScope {
  fun cell(children: @Composable RichTextScope.() -> Unit)
}

@Immutable
private data class TableRow(val cells: List<@Composable RichTextScope.() -> Unit>)

private class TableBuilder : RichTextTableRowScope {
  val rows = mutableListOf<RowBuilder>()

  override fun row(children: RichTextTableCellScope.() -> Unit) {
    rows += RowBuilder().apply(children)
  }
}

private class RowBuilder : RichTextTableCellScope {
  var row = TableRow(emptyList())

  override fun cell(children: @Composable RichTextScope.() -> Unit) {
    row = TableRow(row.cells + children)
  }
}

/**
 * TODO write documentation
 */
@OptIn(ExperimentalStdlibApi::class)
@Composable
fun RichTextScope.Table(
  modifier: Modifier = Modifier,
  headerRow: (RichTextTableCellScope.() -> Unit)? = null,
  bodyRows: RichTextTableRowScope.() -> Unit
) {
  val tableStyle = currentRichTextStyle.resolveDefaults().tableStyle!!
  val header = remember(headerRow) {
    headerRow?.let { RowBuilder().apply(headerRow).row }
  }
  val rows = remember(bodyRows) {
    TableBuilder().apply(bodyRows).rows.map { it.row }
  }
  val columns = remember(header, rows) {
    max(
      header?.cells?.size ?: 0,
      rows.maxByOrNull { it.cells.size }?.cells?.size ?: 0
    )
  }
  val headerStyle = AmbientTextStyle.current.merge(tableStyle.headerTextStyle)
  val cellPadding = with(DensityAmbient.current) {
    tableStyle.cellPadding!!.toDp()
  }
  val cellModifier = Modifier.fillMaxWidth()
      .clipToBounds()
      .padding(cellPadding)

  val styledRows = remember(header, rows, cellModifier) {
    buildList {
      header?.let { headerRow ->
        // Type inference seems to puke without explicit parameters.
        @Suppress("RemoveExplicitTypeArguments")
        add(headerRow.cells.map<@Composable RichTextScope.() -> Unit, @Composable () -> Unit> { cell ->
          @Composable {
            ProvideTextStyle(headerStyle) {
              RichText(
                modifier = cellModifier,
                children = cell
              )
            }
          }
        })
      }

      rows.mapTo(this) { row ->
        @Suppress("RemoveExplicitTypeArguments")
        row.cells.map<@Composable RichTextScope.() -> Unit, @Composable () -> Unit> { cell ->
          @Composable {
            RichText(modifier = cellModifier, children = cell)
          }
        }
      }
    }
  }

  // For some reason borders don't get drawn in the Preview, but they work on-device.
  SimpleTableLayout(
    columns = columns,
    rows = styledRows,
    cellSpacing = tableStyle.borderStrokeWidth!!,
    drawDecorations = { layoutResult ->
      Modifier.drawTableBorders(
        rowOffsets = layoutResult.rowOffsets,
        columnOffsets = layoutResult.columnOffsets,
        borderColor = tableStyle.borderColor!!,
        borderStrokeWidth = tableStyle.borderStrokeWidth
      )
    },
    modifier = modifier
  )
}

private fun Modifier.drawTableBorders(
  rowOffsets: List<Float>,
  columnOffsets: List<Float>,
  borderColor: Color,
  borderStrokeWidth: Float
) = drawBehind {
  // Draw horizontal borders.
  rowOffsets.forEach { position ->
    drawLine(
      borderColor,
      start = Offset(0f, position),
      end = Offset(size.width, position),
      borderStrokeWidth
    )
  }

  // Draw vertical borders.
  columnOffsets.forEach { position ->
    drawLine(
      borderColor,
      Offset(position, 0f),
      Offset(position, size.height),
      borderStrokeWidth
    )
  }
}

/**
 * The offsets of rows and columns of a [SimpleTableLayout], centered inside their spacing.
 *
 * E.g. If a table is given a cell spacing of 2px, then the first column and row offset will each
 * be 1px.
 */
@Immutable
private data class TableLayoutResult(
  val rowOffsets: List<Float>,
  val columnOffsets: List<Float>
)

/**
 * A simple table that sizes all columns equally.
 *
 * @param cellSpacing The space in between each cell, and between each outer cell and the edge of
 * the table.
 */
@OptIn(ExperimentalStdlibApi::class, ExperimentalSubcomposeLayoutApi::class)
@Composable
private fun SimpleTableLayout(
  columns: Int,
  rows: List<List<@Composable () -> Unit>>,
  drawDecorations: (TableLayoutResult) -> Modifier,
  cellSpacing: Float,
  modifier: Modifier
) {
  SubcomposeLayout<Boolean>(modifier = modifier) { constraints ->
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
    val cellWidth = (constraints.maxWidth - cellSpacingWidth) / columns
    val cellSpacingHeight = cellSpacing * (rowMeasurables.size + 1)
    // TODO Handle bounded height constraints.
    // val cellMaxHeight = if (!constraints.hasBoundedHeight) {
    //   Float.MAX_VALUE
    // } else {
    //   // Divide the height by the number of rows, then leave room for the padding.
    //   (constraints.maxHeight - cellSpacingHeight) / rowMeasurables.size
    // }
    val cellConstraints = constraints.enforce(Constraints(maxWidth = cellWidth.roundToInt()))

    val rowPlaceables = rowMeasurables.map { cellMeasurables ->
      cellMeasurables.map { cell ->
        cell.measure(cellConstraints)
      }
    }
    val rowHeights = rowPlaceables.map { row -> row.maxByOrNull { it.height }!!.height }

    val tableWidth = constraints.maxWidth
    val tableHeight = (rowHeights.sumBy { it } + cellSpacingHeight).roundToInt()

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

@Preview
@Composable
private fun TablePreview() {
  TablePreviewContents()
}

@Preview(widthDp = 300)
@Composable
private fun TablePreviewFixedWidth() {
  TablePreviewContents()
}

@Composable
private fun TablePreviewContents(modifier: Modifier = Modifier) {
  RichTextScope().Table(
    modifier = modifier.background(Color.White).padding(4.dp),
    headerRow = {
      cell { Text("Column 1") }
      cell { Text("Column 2") }
    }
  ) {
    row {
      cell { Text("Hello") }
      cell {
        CodeBlock("Foo bar")
      }
    }
    row {
      cell {
        BlockQuote {
          Text("Stuff")
        }
      }
      cell { Text("Hello world this is a really long line that is going to wrap hopefully") }
    }
  }
}
