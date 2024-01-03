@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package com.halilibo.richtext.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.max

/**
 * Defines the visual style for a [Table].
 *
 * @param headerTextStyle The [TextStyle] used for header rows.
 * @param cellPadding The spacing between the contents of each cell and the borders.
 * @param borderColor The [Color] of the table border.
 * @param borderStrokeWidth The width of the table border.
 */
@Immutable
public data class TableStyle(
  val headerTextStyle: TextStyle? = null,
  val cellPadding: TextUnit? = null,
  val borderColor: Color? = null,
  val borderStrokeWidth: Float? = null
) {
  public companion object {
    public val Default: TableStyle = TableStyle()
  }
}

private val DefaultTableHeaderTextStyle = TextStyle(fontWeight = FontWeight.Bold)
private val DefaultCellPadding = 8.sp
private val DefaultBorderColor = Color.Unspecified
private const val DefaultBorderStrokeWidth = 1f

internal fun TableStyle.resolveDefaults() = TableStyle(
    headerTextStyle = headerTextStyle ?: DefaultTableHeaderTextStyle,
    cellPadding = cellPadding ?: DefaultCellPadding,
    borderColor = borderColor ?: DefaultBorderColor,
    borderStrokeWidth = borderStrokeWidth ?: DefaultBorderStrokeWidth
)

public interface RichTextTableRowScope {
  public fun row(children: RichTextTableCellScope.() -> Unit)
}

public interface RichTextTableCellScope {
  public fun cell(children: @Composable RichTextScope.() -> Unit)
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
 * Draws a table with an optional header row, and an arbitrary number of body rows.
 *
 * The style of the table is defined by the [RichTextStyle.tableStyle]&nbsp;[TableStyle].
 */
@OptIn(ExperimentalStdlibApi::class)
@Composable
public fun RichTextScope.Table(
  modifier: Modifier = Modifier,
  headerRow: (RichTextTableCellScope.() -> Unit)? = null,
  bodyRows: RichTextTableRowScope.() -> Unit
) {
  val tableStyle = currentRichTextStyle.resolveDefaults().tableStyle!!
  val contentColor = currentContentColor
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
  val headerStyle = currentTextStyle.merge(tableStyle.headerTextStyle)
  val cellPadding = with(LocalDensity.current) {
    tableStyle.cellPadding!!.toDp()
  }
  val cellModifier = Modifier
      .clipToBounds()
      .padding(cellPadding)

  val styledRows = remember(header, rows, cellModifier) {
    buildList {
      header?.let { headerRow ->
        // Type inference seems to puke without explicit parameters.
        @Suppress("RemoveExplicitTypeArguments")
        add(headerRow.cells.map<@Composable RichTextScope.() -> Unit, @Composable () -> Unit> { cell ->
          @Composable {
            textStyleBackProvider(headerStyle) {
              BasicRichText(
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
            BasicRichText(
              modifier = cellModifier,
              children = cell
            )
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
            borderColor = tableStyle.borderColor!!.takeOrElse { contentColor },
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