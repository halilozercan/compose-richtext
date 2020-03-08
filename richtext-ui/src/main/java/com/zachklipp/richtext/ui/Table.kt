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
import androidx.ui.foundation.contentColor
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Canvas
import androidx.ui.graphics.Color
import androidx.ui.graphics.Paint
import androidx.ui.graphics.PaintingStyle.stroke
import androidx.ui.layout.Column
import androidx.ui.layout.LayoutPadding
import androidx.ui.layout.LayoutSize
import androidx.ui.layout.TableDecorationChildren
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Density
import androidx.ui.unit.PxSize
import androidx.ui.unit.dp
import kotlin.math.max
import androidx.ui.layout.Table as ComposeTable

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
  val header = headerRow?.let { RowBuilder().apply(headerRow).row }
  val rows = TableBuilder().apply(bodyRows).rows.map { it.row }
  val columns = max(
    header?.cells?.size ?: 0,
    rows.maxBy { it.cells.size }?.cells?.size ?: 0
  )
  val headerStyle = currentTextStyle().merge(
    TextStyle(
      fontWeight = FontWeight.Bold
    )
  )

  ComposeTable(columns = columns) {
    // For some reason this decoration doesn't get drawn in the Preview, but
    // it works on-device.
    tableDecoration(overlay = false) {
      DrawTableBorders()
    }

    if (header != null) {
      tableRow {
        // TODO factor out style config
        CurrentTextStyleProvider(headerStyle) {
          header.cells.forEach { cell ->
            DrawCell(cell)
          }
        }
      }
    }

    rows.forEach { row ->
      tableRow {
        row.cells.forEach { cell ->
          DrawCell(cell)
        }
      }
    }
  }
}

@Composable
private fun TableDecorationChildren.DrawTableBorders() {
  // TODO factor out to style config
  val borderWidth = with(DensityAmbient.current) {
    1.dp.toPx()
  }
  val paint = Paint().apply {
    style = stroke
    strokeWidth = borderWidth.value
    // TODO factor out to style config
    color = contentColor()
    alpha = .3f
  }
  val modifier = remember(contentColor()) {
    TableBorderModifier(this, paint)
  }
  Column(LayoutSize.Fill + modifier) {
    Text("Verts: " + verticalOffsets.joinToString())
    Text("HOriz: " + horizontalOffsets.joinToString())
  }
}

class TableBorderModifier(
  cells: TableDecorationChildren,
  private val paint: Paint
) : DrawModifier {

  private val verticalOffsets = cells.verticalOffsets.map { it.value.toFloat() }
  private val horizontalOffsets = cells.horizontalOffsets.map { it.value.toFloat() }

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

@Composable
private fun RichTextScope.DrawCell(cell: @Composable() RichTextScope.() -> Unit) {
  RichText(
    modifier = LayoutPadding(8.dp),
    children = cell
  )
}

@Preview @Composable private fun TablePreview() {
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
