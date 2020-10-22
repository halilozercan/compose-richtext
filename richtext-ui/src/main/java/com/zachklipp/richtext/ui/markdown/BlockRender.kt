package com.zachklipp.richtext.ui.markdown

import android.os.Build
import android.text.Html
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.zachklipp.richtext.ui.Table
import com.zachklipp.richtext.ui.string.InlineContent
import org.commonmark.ext.gfm.tables.*
import org.commonmark.node.HtmlBlock

/*@Composable
internal fun MarkdownTextScope.renderTable(node: ASTTableBlock) {
    Table(
        headerRow = {
            node.filterChildren<TableHead>()
                .firstOrNull()
                ?.filterChildren<TableRow>()
                ?.firstOrNull()
                ?.filterChildren<TableCell>()
                ?.forEach { tableCell ->
                    cell {
                        richTextBlock {
                            visitChildren(tableCell)
                        }
                    }
                }
        }
    ) {
        node.filterChildren<TableBody>()
            .firstOrNull()
            ?.filterChildren<TableRow>()
            ?.forEach { tableRow ->
                row {
                    tableRow.filterChildren<TableCell>()
                        .forEach { tableCell ->
                            cell {
                                richTextBlock {
                                    visitChildren(tableCell)
                                }
                            }
                        }
                }
            }
    }
}*/

@Composable
internal fun MarkdownTextScope.renderHtmlBlock(node: ASTHtmlBlock) {
    richTextBlock {
        updateRichText {
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
        }
    }
}