package com.zachklipp.richtext.ui.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.zachklipp.richtext.ui.*
import com.zachklipp.richtext.ui.string.InlineContent
import com.zachklipp.richtext.ui.string.RichTextString
import com.zachklipp.richtext.ui.string.withFormat
import dev.chrisbanes.accompanist.coil.CoilImage
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.*
import org.commonmark.node.*
import org.commonmark.parser.Parser

@Composable
private fun getMarkdownAst(
    text: String,
    log: Boolean = true
): Node {
    val parser = remember { Parser.builder()
        .extensions(
            listOf(
                TablesExtension.create(),
                StrikethroughExtension.create()
            )
        ).build() }
    return remember(text) {
        parser.parse(text)
    }.also { node ->
        if(log) {
            traversePreOrder(node)
        }
    }
}

@Composable
internal fun MarkdownTextScope.visitChildren(node: Node) {
    var iterator = node.firstChild
    while(iterator != null) {
        val next = iterator.next
        RecursiveRenderMarkdownAst(
            node = iterator
        )
        iterator = next
    }
}

@Composable
private fun MarkdownTextScope.RecursiveRenderMarkdownAst(node: Node) {
    when(node) {
        is BlockQuote -> {
            BlockQuote {
                richTextBlock {
                    visitChildren(node)
                }
            }
        }
        is BulletList -> {
            // Formatted List API should allow me to pass a generic composable
            // that would be called for every child, rather than vararg argument
            FormattedList(
                listType = ListType.Unordered,
                items = node.filterChildren<Node>()
            ) {
                richTextBlock {
                    visitChildren(node)
                }
            }
        }
        is Code -> {
            updateRichText {
                withFormat(RichTextString.Format.Code) {
                    append(node.literal)
                }
            }
        }
        is Document -> {
            visitChildren(node)
        }
        is Emphasis -> {
            updateRichText {
                withFormat(RichTextString.Format.Italic) {
                    visitChildren(node)
                }
            }
        }
        is FencedCodeBlock -> {
            CodeBlock(text = node.literal)
        }
        is HardLineBreak -> {
            updateRichText {
                append("\n")
                visitChildren(node)
            }
        }
        is Heading -> {
            Heading(level = node.level) {
                richTextBlock {
                    visitChildren(node)
                }
            }
        }
        is ThematicBreak -> {
            HorizontalRule()
        }
        is HtmlInline -> {
            visitChildren(node)
        }
        is HtmlBlock -> {
            renderHtmlBlock(node)
        }
        is Image -> {
            updateRichText {
                appendInlineContent(content = InlineContent {
                    CoilImage(
                        data = node.destination,
                        loading = {
                            Text("Loading Image...")
                        },
                        error = {
                            Text("Image failed to load")
                        }
                    )
                })
            }
        }
        is IndentedCodeBlock -> {
            CodeBlock(text = node.literal)
        }
        is Link -> {
            updateRichText {
                withFormat(RichTextString.Format.Link {
                    onLinkClick(node.destination)
                }) {
                    visitChildren(node)
                }
            }
        }
        is ListItem -> {
            richTextBlock {
                visitChildren(node)
            }
        }
        is OrderedList -> {
            richTextBlock {
                FormattedList(
                    listType = ListType.Ordered,
                    items = node.filterChildren<ListItem>()
                ) {
                    richTextBlock {
                        visitChildren(node)
                    }
                }
            }
        }
        is Paragraph -> {
            richTextBlock {
                visitChildren(node)
            }
        }
        is SoftLineBreak -> {
            updateRichText {
                append(" ")
                visitChildren(node)
            }
        }
        is StrongEmphasis -> {
            updateRichText {
                withFormat(RichTextString.Format.Bold) {
                    visitChildren(node)
                }
            }
        }
        is Text -> {
            updateRichText {
                append(node.literal)
            }
        }
        is LinkReferenceDefinition -> {
            updateRichText {
                withFormat(RichTextString.Format.Link {
                    onLinkClick(node.destination)
                }) {
                    visitChildren(node)
                }
            }
        }
        is CustomBlock -> {
            richTextBlock {
                if (node is TableBlock) {
                    renderTable(node)
                }
            }
        }
        is CustomNode -> {
            if (node is Strikethrough) {
                updateRichText {
                    withFormat(RichTextString.Format.Strikethrough) {
                        visitChildren(node)
                    }
                }
            }
        }
    }
}

typealias OnLinkClick = (String) -> Unit

internal interface MarkdownTextScope: RichTextScope {
    val richTextStringBuilderHelper: RichTextStringBuilderHelper

    val onLinkClick: OnLinkClick
}

@Composable fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier,
    style: RichTextStyle? = null,
    onLinkClick: OnLinkClick = {}
) {
    RichText(
        modifier = modifier,
        style = style
    ) {
        with(object: MarkdownTextScope {

            override val richTextStringBuilderHelper = RichTextStringBuilderHelper()

            override val onLinkClick = onLinkClick

        }) {
            val markdownAst = getMarkdownAst(text = content)
            RecursiveRenderMarkdownAst(
                node = markdownAst
            )
        }
    }
}