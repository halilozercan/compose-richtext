@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.compose.Immutable
import androidx.compose.getValue
import androidx.compose.remember
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.Constraints
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Layout
import androidx.ui.core.Modifier
import androidx.ui.core.clipToBounds
import androidx.ui.core.drawBehind
import androidx.ui.core.enforce
import androidx.ui.core.hasBoundedHeight
import androidx.ui.core.hasBoundedWidth
import androidx.ui.foundation.ProvideTextStyle
import androidx.ui.foundation.Text
import androidx.ui.foundation.currentTextStyle
import androidx.ui.foundation.drawBackground
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Color
import androidx.ui.graphics.drawscope.Stroke
import androidx.ui.layout.padding
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Px
import androidx.ui.unit.TextUnit
import androidx.ui.unit.px
import androidx.ui.unit.round
import androidx.ui.unit.sp
import androidx.ui.unit.toPx
import kotlin.math.max

@Immutable
data class TableStyle(
  val headerTextStyle: TextStyle? = null,
  val cellPadding: TextUnit? = null,
  val borderColor: Color? = null,
  val borderStroke: Stroke? = null
) {
  companion object {
    val Default = TableStyle()
  }
}

private val DefaultTableHeaderTextStyle = TextStyle(fontWeight = FontWeight.Bold)
private val DefaultCellPadding = 8.sp
private val DefaultBorderColor = Color.Black
private val DefaultBorderStroke = Stroke(width = 1f)

internal fun TableStyle.resolveDefaults() = TableStyle(
    headerTextStyle = headerTextStyle ?: DefaultTableHeaderTextStyle,
    cellPadding = cellPadding ?: DefaultCellPadding,
    borderColor = borderColor ?: DefaultBorderColor,
    borderStroke = borderStroke ?: DefaultBorderStroke
)

interface RichTextTableRowScope {
  fun row(children: RichTextTableCellScope.() -> Unit)
}

interface RichTextTableCellScope {
  fun cell(children: @Composable() RichTextScope.() -> Unit)
}

@Immutable
private data class TableRow(val cells: List<@Composable() RichTextScope.() -> Unit>)

private class TableBuilder : RichTextTableRowScope {
  val rows = mutableListOf<RowBuilder>()

  override fun row(children: RichTextTableCellScope.() -> Unit) {
    rows += RowBuilder().apply(children)
  }
}

private class RowBuilder : RichTextTableCellScope {
  var row = TableRow(emptyList())

  override fun cell(children: @Composable() RichTextScope.() -> Unit) {
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
        rows.maxBy { it.cells.size }?.cells?.size ?: 0
    )
  }
  val headerStyle = currentTextStyle().merge(tableStyle.headerTextStyle)
  val cellPadding = with(DensityAmbient.current) {
    tableStyle.cellPadding!!.toDp()
  }
  val cellModifier = Modifier.clipToBounds()
      .padding(cellPadding)

  val styledRows = remember(header, rows, cellModifier) {
    buildList {
      header?.let { headerRow ->
        // Type inference seems to puke without explicit parameters.
        @Suppress("RemoveExplicitTypeArguments")
        add(headerRow.cells.map<@Composable() RichTextScope.() -> Unit, @Composable() () -> Unit> { cell ->
          @Composable {
            ProvideTextStyle(headerStyle) {
              RichText(modifier = cellModifier, children = cell)
            }
          }
        })
      }

      rows.mapTo(this) { row ->
        @Suppress("RemoveExplicitTypeArguments")
        row.cells.map<@Composable() RichTextScope.() -> Unit, @Composable() () -> Unit> { cell ->
          @Composable {
            RichText(modifier = cellModifier, children = cell)
          }
        }
      }
    }
  }

  var tableLayoutResult by state<TableLayoutResult?> { null }
  val tableBorderModifier = remember<Modifier>(tableLayoutResult, tableStyle) {
    tableLayoutResult?.let {
      Modifier.drawTableBorders(
          rowOffsets = it.rowOffsets,
          columnOffsets = it.columnOffsets,
          borderColor = tableStyle.borderColor!!,
          borderStroke = tableStyle.borderStroke!!
      )
    } ?: Modifier
  }

  // For some reason borders don't get drawn in the Preview, but they work on-device.
  SimpleTableLayout(
      columns = columns,
      rows = styledRows,
      cellSpacing = tableStyle.borderStroke!!.width.px,
      onTableLayoutResult = { tableLayoutResult = it },
      modifier = modifier + tableBorderModifier
  )
}

private fun Modifier.drawTableBorders(
  rowOffsets: List<Px>,
  columnOffsets: List<Px>,
  borderColor: Color,
  borderStroke: Stroke
) = drawBehind {
  // Draw horizontal borders.
  rowOffsets.forEach { position ->
    drawLine(
        borderColor,
        Offset(0f, position.value),
        Offset(size.width, position.value),
        borderStroke
    )
  }

  // Draw vertical borders.
  columnOffsets.forEach { position ->
    drawLine(
        borderColor,
        Offset(position.value, 0f),
        Offset(position.value, size.height),
        borderStroke
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
  val rowOffsets: List<Px>,
  val columnOffsets: List<Px>
)

/**
 * A simple table that sizes all columns equally.
 *
 * @param cellSpacing The space in between each cell, and between each outer cell and the edge of
 * the table.
 */
@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun SimpleTableLayout(
  columns: Int,
  rows: List<List<@Composable() () -> Unit>>,
  cellSpacing: Px,
  onTableLayoutResult: (TableLayoutResult) -> Unit,
  modifier: Modifier
) {
  Layout(
      children = {
        rows.forEach { row ->
          check(row.size == columns)
          row.forEach { cell ->
            cell()
          }
        }
      },
      modifier = modifier
  ) { measurables, constraints, _ ->
    val rowMeasurables = measurables.chunked(columns)
    check(rowMeasurables.size == rows.size)

    check(constraints.hasBoundedWidth) { "Table must have bounded width" }
    // Divide the width by the number of columns, then leave room for the padding.
    val cellSpacingWidth = cellSpacing * (columns + 1)
    val cellWidth = (constraints.maxWidth.toPx() - cellSpacingWidth) / columns
    val cellSpacingHeight = cellSpacing * (rowMeasurables.size + 1)
    val cellMaxHeight = if (!constraints.hasBoundedHeight) {
      Px.Infinity
    } else {
      // Divide the height by the number of rows, then leave room for the padding.
      (constraints.maxHeight.toPx() - cellSpacingHeight) / rowMeasurables.size
    }
    val cellConstraints = constraints.enforce(Constraints(maxWidth = cellWidth.round()))

    val rowPlaceables = rowMeasurables.map { cellMeasurables ->
      cellMeasurables.map { cell ->
        cell.measure(cellConstraints)
      }
    }
    val rowHeights = rowPlaceables.map { row -> row.maxBy { it.height }!!.height }

    val tableHeight = rowHeights.sumBy { it.value }.px + cellSpacingHeight
    layout(constraints.maxWidth, tableHeight.round()) {
      var y = cellSpacing
      val rowOffsets = mutableListOf<Px>()
      val columnOffsets = mutableListOf<Px>()

      rowPlaceables.forEachIndexed { rowIndex, cellPlaceables ->
        rowOffsets += y - cellSpacing / 2f
        var x = cellSpacing

        cellPlaceables.forEach { cell ->
          if (rowIndex == 0) {
            columnOffsets.add(x - cellSpacing / 2f)
          }
          cell.place(x, y)
          x += cellWidth + cellSpacing
        }

        if (rowIndex == 0) {
          // Add the right-most edge.
          columnOffsets.add(x - cellSpacing / 2f)
        }

        y += rowHeights[rowIndex].toPx() + cellSpacing
      }

      rowOffsets.add(y - cellSpacing / 2f)
      onTableLayoutResult(
          TableLayoutResult(
              rowOffsets = rowOffsets,
              columnOffsets = columnOffsets
          )
      )
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
  RichTextScope.Table(
      modifier = modifier.drawBackground(Color.White),
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
