package com.zachklipp.richtext.markdown

import androidx.compose.runtime.Composable
import com.zachklipp.richtext.markdown.extensions.*
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.Table

@Composable
internal fun RichTextScope.renderTable(node: AstTableRoot) {
    Table(
        headerRow = {
            node.filterChildrenIsInstance<AstTableHeader>()
                .firstOrNull()
                ?.filterChildrenIsInstance<AstTableRow>()
                ?.firstOrNull()
                ?.filterChildrenIsInstance<AstTableCell>()
                ?.forEach { tableCell ->
                    cell {
                        MarkdownRichText(tableCell)
                    }
                }
        }
    ) {
        node.filterChildrenIsInstance<AstTableBody>()
            .firstOrNull()
            ?.filterChildrenIsInstance<AstTableRow>()
            ?.forEach { tableRow ->
                row {
                    tableRow.filterChildrenIsInstance<AstTableCell>()
                        .forEach { tableCell ->
                            cell {
                                MarkdownRichText(tableCell)
                            }
                        }
                }
            }
    }
}
