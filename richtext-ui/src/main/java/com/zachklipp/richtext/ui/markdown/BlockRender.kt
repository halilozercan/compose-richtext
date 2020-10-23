package com.zachklipp.richtext.ui.markdown

import android.os.Build
import android.text.Html
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.zachklipp.richtext.ui.RichText
import com.zachklipp.richtext.ui.Table
import com.zachklipp.richtext.ui.string.InlineContent
import com.zachklipp.richtext.ui.string.Text
import com.zachklipp.richtext.ui.string.richTextString
import org.commonmark.ext.gfm.tables.*
import org.commonmark.node.HtmlBlock

internal inline fun <reified T: ASTTable> ASTNode.filterTable(): List<ASTCustomNode> {
    return this.filterChildrenIsInstance<ASTCustomNode>().filter {
        it.data is T
    }
}

@Composable
internal fun MarkdownTextScope.renderTable(node: ASTCustomBlock) {
    Table(
        headerRow = {
            node.filterTable<ASTTable.ASTTableHeader>()
                .firstOrNull()
                ?.filterTable<ASTTable.ASTTableRow>()
                ?.firstOrNull()
                ?.filterTable<ASTTable.ASTTableCell>()
                ?.forEach { tableCell ->
                    cell {
                        MarkdownRichText(tableCell)
                    }
                }
        }
    ) {
        node.filterTable<ASTTable.ASTTableBody>()
            .firstOrNull()
            ?.filterTable<ASTTable.ASTTableRow>()
            ?.forEach { tableRow ->
                row {
                    tableRow.filterTable<ASTTable.ASTTableCell>()
                        .forEach { tableCell ->
                            cell {
                                MarkdownRichText(tableCell)
                            }
                        }
                }
            }
    }
}

@Composable
internal fun MarkdownTextScope.renderHtmlBlock(node: ASTHtmlBlock) {
    Text(text = richTextString {
        appendInlineContent(content = InlineContent {
            AndroidView(viewBlock = { context ->
                // TODO: pass current styling to legacy TextView
                TextView(context).apply {
                    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(node.literal, 0)
                    } else {
                        Html.fromHtml(node.literal)
                    }
                }
            })
        })
    })
}