package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.markdown.node.AstTableBody
import com.halilibo.richtext.markdown.node.AstTableCell
import com.halilibo.richtext.markdown.node.AstTableHeader
import com.halilibo.richtext.markdown.node.AstTableRow
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.Table

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
