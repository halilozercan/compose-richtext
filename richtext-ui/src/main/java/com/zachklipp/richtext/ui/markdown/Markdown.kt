package com.zachklipp.richtext.ui.markdown

import androidx.compose.foundation.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.zachklipp.richtext.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser

/**
 * A composable that renders Markdown content using [RichText].
 *
 * @param content: Markdown text. No restriction on length.
 * @param modifier: A generic [Modifier] for this composable.
 * @param style: RichTextStyle that will be used to style markdown rendering.
 * @param onLinkClick: A lambda to invoke when a link is clicked from rendered content.
 */
@Composable
fun Markdown(
    content: String,
    modifier: Modifier = Modifier,
    style: RichTextStyle? = null,
    onLinkClick: OnLinkClick = {}
) {
    RichText(
        modifier = modifier,
        style = style
    ) {
        val markdownTextScope = remember(onLinkClick) {
            object : MarkdownTextScope {
                override val onLinkClick = onLinkClick
            }
        }

        with(markdownTextScope) {
            val markdownAst = getMarkdownAst(text = content)
            RecursiveRenderMarkdownAst(astNode = markdownAst)
        }
    }
}

/**
 * When parsed, markdown content or any other rich text can be represented as a tree.
 * The default markdown parser that is used in this project `common-markdown` also
 * utilizes the said approach. Although there are ways to iteratively traverse a tree,
 * it is more readable and compose-friendly to do it recursively.
 *
 * This function basically receives a node from the tree, root or any node, and then
 * recursively travels along the nodes while spitting out or wrapping composables around
 * the content. [RichText] API is highly compatible with this methodology.
 *
 * However, there are multiple assumptions to increase predictability. Despite the fact
 * that every [ASTNode] can have another [ASTNode] as a child, it should not be that
 * generic in Markdown content. For example, a Text node must not have any other children.
 * That's why this function does not have 100% coverage for all [ASTNode] types.
 *
 * Heading, Paragraph are considered to be main text containers. Their content is regarded
 * as one block and children traversal happens separately.
 *
 * FormattedList, OrderedList are also content blocks. Their children are filtered before
 * being traversed. Only ListItems are accepted as valid children for these blocks.
 *
 * For now, only tables are rendered from CustomBlock or CustomNode.
 *
 * @param astNode: Root node to start rendering.
 */
@Composable
internal fun MarkdownTextScope.RecursiveRenderMarkdownAst(astNode: ASTNode?) {
    if (astNode == null) return

    when (astNode) {
        is ASTBlockQuote -> {
            BlockQuote {
                visitChildren(astNode)
            }
        }
        is ASTBulletList -> {
            FormattedList(
                listType = ListType.Unordered,
                items = astNode.filterChildrenIsInstance<ASTListItem>()
            ) { astListItem ->
                visitChildren(astListItem)
            }
        }
        is ASTFencedCodeBlock -> {
            CodeBlock(text = astNode.literal)
        }
        is ASTHeading -> {
            Heading(level = astNode.level) {
                MarkdownRichText(
                    astNode = astNode
                )
            }
        }
        is ASTThematicBreak -> {
            HorizontalRule()
        }
        is ASTHtmlBlock -> {
            renderHtmlBlock(astNode)
        }
        is ASTIndentedCodeBlock -> {
            CodeBlock(text = astNode.literal)
        }
        is ASTOrderedList -> {
            FormattedList(
                listType = ListType.Ordered,
                items = astNode.filterChildrenIsInstance<ASTListItem>()
            ) { astListItem ->
                visitChildren(astListItem)
            }
        }
        is ASTParagraph -> {
            MarkdownRichText(astNode)
        }
        // This should almost never happen. All the possible text
        // nodes must be under either Heading, Paragraph or CustomNode
        // In any case, we should include it here to prevent any
        // non-rendered text problems.
        is ASTText -> {
            Text(astNode.literal)
        }
        is ASTCustomBlock -> {
            if (astNode.data is ASTTable.ASTTableBlock) {
                renderTable(astNode)
            }
        }
        is ASTCustomNode -> {
            // TODO
            // Don't even visit children. No idea what would come out
        }
        else -> visitChildren(astNode)
    }
}

/**
 * Parse markdown content and return Abstract Syntax Tree(AST).
 * Composable is efficient thanks to remember construct.
 *
 * @param text: Markdown text to be parsed.
 */
@Composable
internal fun getMarkdownAst(text: String): ASTNode? {
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
            rootASTNode = convert(parser.parse(text)).also { traversePreOrder(it) }
        }
    }

    return rootASTNode
}

/**
 * Visit and render children from first to last.
 *
 * @param node: Root ASTNode whose children will be visited.
 */
@Composable
internal fun MarkdownTextScope.visitChildren(node: ASTNode?) {
    var iterator = node?.firstChild
    while (iterator != null) {
        val next = iterator.next
        RecursiveRenderMarkdownAst(
            astNode = iterator
        )
        iterator = next
    }
}