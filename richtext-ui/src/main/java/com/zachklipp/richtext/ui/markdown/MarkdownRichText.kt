package com.zachklipp.richtext.ui.markdown

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.zachklipp.richtext.ui.*
import com.zachklipp.richtext.ui.string.InlineContent
import com.zachklipp.richtext.ui.string.RichTextString
import com.zachklipp.richtext.ui.string.withFormat
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.*
import org.commonmark.node.*
import org.commonmark.parser.Parser

@Composable
private fun getMarkdownAst(text: String): ASTNode? {
    val parser = remember {
        Parser.builder()
            .extensions(
                listOf(
                    TablesExtension.create(),
                    StrikethroughExtension.create()
                )
            ).build()
    }
    var rootASTNode by remember { mutableStateOf<ASTNode?>(null) }

    val coroutineScope = rememberCoroutineScope() + Dispatchers.Default

    onCommit(text) {
        coroutineScope.launch {
            rootASTNode = convert(parser.parse(text))
        }
    }

    return rootASTNode
}

@Composable
internal fun MarkdownTextScope.visitChildren(node: ASTNode?) {
    var iterator = node?.firstChild
    while (iterator != null) {
        val next = iterator.next
        RecursiveRenderMarkdownAst(
            node = iterator
        )
        iterator = next
    }
}

@Composable
private fun MarkdownTextScope.RecursiveRenderMarkdownAst(node: ASTNode?) {
    if (node == null) return

    when (node) {
        is ASTBlockQuote -> {
            BlockQuote {
                richTextBlock {
                    visitChildren(node)
                }
            }
        }
        is ASTBulletList -> {
            // Formatted List API should allow me to pass a generic composable
            // that would be called for every child, rather than vararg argument
            FormattedList(
                listType = ListType.Unordered,
                items = node.filterChildren<ASTNode>()
            ) {
                richTextBlock {
                    visitChildren(node)
                }
            }
        }
        is ASTCode -> {
            updateRichText {
                withFormat(RichTextString.Format.Code) {
                    append(node.literal)
                }
            }
        }
        is ASTDocument -> {
            visitChildren(node)
        }
        is ASTEmphasis -> {
            updateRichText {
                withFormat(RichTextString.Format.Italic) {
                    visitChildren(node)
                }
            }
        }
        is ASTFencedCodeBlock -> {
            CodeBlock(text = node.literal)
        }
        is ASTHardLineBreak -> {
            updateRichText {
                append("\n")
                visitChildren(node)
            }
        }
        is ASTHeading -> {
            Heading(level = node.level) {
                richTextBlock {
                    visitChildren(node)
                }
            }
        }
        is ASTThematicBreak -> {
            HorizontalRule()
        }
        is ASTHtmlInline -> {
            visitChildren(node)
        }
        is ASTHtmlBlock -> {
            renderHtmlBlock(node)
        }
        is ASTImage -> {
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
        is ASTIndentedCodeBlock -> {
            CodeBlock(text = node.literal)
        }
        is ASTLink -> {
            updateRichText {
                withFormat(RichTextString.Format.Link {
                    onLinkClick(node.destination)
                }) {
                    visitChildren(node)
                }
            }
        }
        is ASTListItem -> {
            richTextBlock {
                visitChildren(node)
            }
        }
        is ASTOrderedList -> {
            richTextBlock {
                FormattedList(
                    listType = ListType.Ordered,
                    items = node.filterChildren<ASTListItem>()
                ) {
                    richTextBlock {
                        visitChildren(node)
                    }
                }
            }
        }
        is ASTParagraph -> {
            richTextBlock {
                visitChildren(node)
            }
        }
        is ASTSoftLineBreak -> {
            updateRichText {
                append(" ")
                visitChildren(node)
            }
        }
        is ASTStrongEmphasis -> {
            updateRichText {
                withFormat(RichTextString.Format.Bold) {
                    visitChildren(node)
                }
            }
        }
        is ASTText -> {
            updateRichText {
                append(node.literal)
            }
        }
        is ASTLinkReferenceDefinition -> {
            updateRichText {
                withFormat(RichTextString.Format.Link {
                    onLinkClick(node.destination)
                }) {
                    visitChildren(node)
                }
            }
        }
        is ASTCustomBlock -> {
            richTextBlock {
                if (node is TableBlock) {
//                    renderTable(node)
                }
            }
        }
        is ASTCustomNode -> {
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

@Immutable
internal interface MarkdownTextScope : RichTextScope {
    val richTextStringBuilderHelper: RichTextStringBuilderHelper

    val onLinkClick: OnLinkClick
}

@Composable
fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier,
    style: RichTextStyle? = null,
    onLinkClick: OnLinkClick = {}
) {
    RichText(
        modifier = modifier,
        style = style
    ) {
        val markdownTextScope = remember(content, onLinkClick) {
            object : MarkdownTextScope {

                override val richTextStringBuilderHelper = RichTextStringBuilderHelper()

                override val onLinkClick = onLinkClick

            }
        }

        with(markdownTextScope) {
//            val markdownAst = getMarkdownAst(text = content)
            val parser = remember {
                Parser.builder()
                    .extensions(
                        listOf(
                            TablesExtension.create(),
                            StrikethroughExtension.create()
                        )
                    ).build()
            }
            var rootASTNode by remember { mutableStateOf<ASTNode?>(null) }

            val coroutineScope = rememberCoroutineScope() + Dispatchers.Default

            onCommit(content) {
                coroutineScope.launch {
                    rootASTNode = convert(parser.parse(content))
                }
            }

            RecursiveRenderMarkdownAst(node = rootASTNode)
        }
    }
}