package com.zachklipp.richtext.adf

import androidx.compose.runtime.Composable
import com.zachklipp.richtext.adf.model.AdfTable
import com.zachklipp.richtext.adf.model.AdfTableUnit.AdfTableCell
import com.zachklipp.richtext.adf.model.AdfTableUnit.AdfTableHeader
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.Table

@Composable
internal fun RichTextScope.AdfTableBlock(node: AdfTable) {
  Table(
    headerRow = {
      node.content.firstOrNull()
        ?.filterChildrenIsInstance<AdfTableHeader>()
        ?.forEach { tableCell ->
          cell {
            RenderChildren(tableCell)
          }
        }
    }
  ) {
    node.content.forEach { tableRow ->
      if (tableRow.filterChildrenIsInstance<AdfTableCell>().toList().isNotEmpty()) {
        row {
          tableRow.filterChildrenIsInstance<AdfTableCell>()
            .forEach { tableCell ->
              cell {
                RenderChildren(tableCell)
              }
            }
        }
      }
    }
  }
}