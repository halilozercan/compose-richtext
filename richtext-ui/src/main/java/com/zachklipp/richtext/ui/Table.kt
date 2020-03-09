@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.compose.Immutable
import androidx.compose.remember
import androidx.ui.core.CurrentTextStyleProvider
import androidx.ui.core.DensityAmbient
import androidx.ui.core.DrawModifier
import androidx.ui.core.Text
import androidx.ui.core.currentTextStyle
import androidx.ui.foundation.Box
import androidx.ui.foundation.DrawBackground
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Canvas
import androidx.ui.graphics.Color
import androidx.ui.graphics.Paint
import androidx.ui.layout.LayoutPadding
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Density
import androidx.ui.unit.IntPx
import androidx.ui.unit.PxSize
import androidx.ui.unit.TextUnit
import androidx.ui.unit.sp
import kotlin.math.max
import androidx.ui.layout.Table as ComposeTable

@Immutable
data class TableStyle(
  val headerTextStyle: TextStyle? = null,
  val cellPadding: TextUnit? = null,
  val borderPaint: Paint? = null
) {
  companion object {
    val Default = TableStyle()
  }
}

private val DefaultTableHeaderTextStyle = TextStyle(fontWeight = FontWeight.Bold)
private val DefaultCellPadding = 8.sp
private val DefaultTableBorderPaint = Paint().apply {
  strokeWidth = 1f
  color = Color.Black
  alpha = .3f
}

internal fun TableStyle.resolveDefaults() = TableStyle(
  headerTextStyle = headerTextStyle ?: DefaultTableHeaderTextStyle,
  cellPadding = cellPadding ?: DefaultCellPadding,
  borderPaint = borderPaint ?: DefaultTableBorderPaint
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
@Composable fun RichTextScope.Table(
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
  val cellModifier = LayoutPadding(cellPadding)

  ComposeTable(columns = columns) {
    // For some reason this decoration doesn't get drawn in the Preview, but
    // it works on-device.
    tableDecoration(overlay = false) {
      DrawTableBorders(verticalOffsets, horizontalOffsets, tableStyle.borderPaint!!)
    }

    if (header != null) {
      tableRow {
        CurrentTextStyleProvider(headerStyle) {
          header.cells.forEach { cell ->
            RichText(
              modifier = cellModifier,
              children = cell
            )
          }
        }
      }
    }

    rows.forEach { row ->
      tableRow {
        row.cells.forEach { cell ->
          RichText(
            modifier = cellModifier,
            children = cell
          )
        }
      }
    }
  }
}

@Composable
private fun DrawTableBorders(
  verticalOffsets: List<IntPx>,
  horizontalOffsets: List<IntPx>,
  borderPaint: Paint
) {
  val modifier = remember(verticalOffsets, horizontalOffsets, borderPaint) {
    TableBorderModifier(verticalOffsets, horizontalOffsets, borderPaint)
  }
  Box(modifier)
}

private class TableBorderModifier(
  verticalOffsets: List<IntPx>,
  horizontalOffsets: List<IntPx>,
  private val paint: Paint
) : DrawModifier {

  private val verticalOffsets = verticalOffsets.map { it.value.toFloat() }
  private val horizontalOffsets = horizontalOffsets.map { it.value.toFloat() }

  override fun draw(
    density: Density,
    drawContent: () -> Unit,
    canvas: Canvas,
    size: PxSize
  ) {
    val width = size.width.value
    val height = size.height.value

    // Draw horizontal borders.
    verticalOffsets.forEach { position ->
      canvas.drawLine(
        Offset(0f, position),
        Offset(width, position),
        paint
      )
    }

    // Draw vertical borders.
    horizontalOffsets.forEach { position ->
      canvas.drawLine(
        Offset(position, 0f),
        Offset(position, height),
        paint
      )
    }
  }
}

@Preview @Composable private fun TablePreview() {
  TablePreviewContents()
}

@Preview(widthDp = 300)
@Composable private fun TablePreviewFixedWidth() {
  TablePreviewContents()
}

@Composable private fun TablePreviewContents() {
  Box(DrawBackground(Color.White)) {
    RichTextScope.Table(headerRow = {
      cell { Text("Column 1") }
      cell { Text("Column 2") }
    }) {
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
}
