@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.zachklipp.richtext.ui

import androidx.compose.Composable
import androidx.compose.Immutable
import androidx.compose.remember
import androidx.ui.core.ContentDrawScope
import androidx.ui.core.DensityAmbient
import androidx.ui.core.DrawModifier
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
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
import androidx.ui.unit.IntPx
import androidx.ui.unit.TextUnit
import androidx.ui.unit.sp
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
private val DefaultBorderColor = Color.Black.copy(alpha = .3f)
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
@Suppress("UNUSED_VARIABLE")
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
  val cellModifier = Modifier.padding(cellPadding)

  // TODO Table was temporarily removed, fork it.
//  ComposeTable(columns = columns) {
//    // For some reason this decoration doesn't get drawn in the Preview, but
//    // it works on-device.
//    tableDecoration(overlay = false) {
//      DrawTableBorders(verticalOffsets, horizontalOffsets, tableStyle.borderPaint!!)
//    }
//
//    if (header != null) {
//      tableRow {
//        ProvideTextStyle(headerStyle) {
//          header.cells.forEach { cell ->
//            RichText(
//              modifier = cellModifier,
//              children = cell
//            )
//          }
//        }
//      }
//    }
//
//    rows.forEach { row ->
//      tableRow {
//        row.cells.forEach { cell ->
//          RichText(
//            modifier = cellModifier,
//            children = cell
//          )
//        }
//      }
//    }
//  }
}

@Composable
private fun DrawTableBorders(
  verticalOffsets: List<IntPx>,
  horizontalOffsets: List<IntPx>,
  borderColor: Color,
  borderStroke: Stroke
) {
  val modifier = remember(verticalOffsets, horizontalOffsets, borderColor, borderStroke) {
    TableBorderModifier(verticalOffsets, horizontalOffsets, borderColor, borderStroke)
  }
  Box(modifier)
}

private class TableBorderModifier(
  verticalOffsets: List<IntPx>,
  horizontalOffsets: List<IntPx>,
  private val borderColor: Color,
  private val borderStroke: Stroke
) : DrawModifier {

  private val verticalOffsets = verticalOffsets.map { it.value.toFloat() }
  private val horizontalOffsets = horizontalOffsets.map { it.value.toFloat() }

  override fun ContentDrawScope.draw() {
    val width = size.width
    val height = size.height

    // Draw horizontal borders.
    verticalOffsets.forEach { position ->
      drawLine(
          borderColor,
          Offset(0f, position),
          Offset(width, position),
          borderStroke
      )
    }

    // Draw vertical borders.
    horizontalOffsets.forEach { position ->
      drawLine(
          borderColor,
          Offset(position, 0f),
          Offset(position, height),
          borderStroke
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
  Box(Modifier.drawBackground(Color.White)) {
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
