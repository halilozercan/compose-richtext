package com.zachklipp.richtext.markdown

import androidx.compose.runtime.Composable
import com.zachklipp.richtext.markdown.node.AstNode
import com.zachklipp.richtext.markdown.node.AstTableBody
import com.zachklipp.richtext.markdown.node.AstTableCell
import com.zachklipp.richtext.markdown.node.AstTableHeader
import com.zachklipp.richtext.markdown.node.AstTableRoot
import com.zachklipp.richtext.markdown.node.AstTableRow
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.Table

@Composable
internal fun RichTextScope.RenderTable(node: AstNode) {
  Table(
    headerRow = {
      node.filterChildrenType<AstTableHeader>()
        .firstOrNull()
        ?.filterChildrenType<AstTableRow>()
        ?.firstOrNull()
        ?.filterChildrenType<AstTableCell>()
        ?.forEach { tableCell ->
          cell {
            MarkdownRichText(tableCell)
          }
        }
    }
  ) {
    node.filterChildrenType<AstTableBody>()
      .firstOrNull()
      ?.filterChildrenType<AstTableRow>()
      ?.forEach { tableRow ->
        row {
          tableRow.filterChildrenType<AstTableCell>()
            .forEach { tableCell ->
              cell {
                MarkdownRichText(tableCell)
              }
            }
        }
      }
  }
}
