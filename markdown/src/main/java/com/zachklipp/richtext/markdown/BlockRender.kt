package com.zachklipp.richtext.markdown

import android.os.Build
import android.text.Html
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.zachklipp.richtext.ui.Table
import com.zachklipp.richtext.ui.string.InlineContent
import com.zachklipp.richtext.ui.string.Text
import com.zachklipp.richtext.ui.string.richTextString

internal inline fun <reified T: AstTableSection> AstNode.filterTable(): Sequence<AstCustomNode> {
    return this.filterChildrenIsInstance<AstCustomNode>().filter {
        it.data is T
    }
}

@Composable
internal fun MarkdownTextScope.renderTable(node: AstCustomBlock) {
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
