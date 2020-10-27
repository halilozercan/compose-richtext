package com.zachklipp.richtext.markdown

import androidx.compose.runtime.Composable
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.Table

internal inline fun <reified T: AstTableSection> AstNode.filterTable(): Sequence<AstCustomNode> {
    return this.filterChildrenIsInstance<AstCustomNode>().filter {
        it.data is T
    }
}

@Composable
internal fun RichTextScope.renderTable(node: AstCustomBlock) {
    Table(
        headerRow = {
            node.filterTable<AstTableSection.Header>()
                .firstOrNull()
                ?.filterTable<AstTableSection.Row>()
                ?.firstOrNull()
                ?.filterTable<AstTableSection.Cell>()
                ?.forEach { tableCell ->
                    cell {
                        MarkdownRichText(tableCell)
                    }
                }
        }
    ) {
        node.filterTable<AstTableSection.Body>()
            .firstOrNull()
            ?.filterTable<AstTableSection.Row>()
            ?.forEach { tableRow ->
                row {
                    tableRow.filterTable<AstTableSection.Cell>()
                        .forEach { tableCell ->
                            cell {
                                MarkdownRichText(tableCell)
                            }
                        }
                }
            }
    }
}
