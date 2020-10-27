package com.zachklipp.richtext.markdown

import androidx.compose.runtime.Immutable
import com.zachklipp.richtext.markdown.extensions.*
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.tables.*
import org.commonmark.node.*

/**
 * Converts common-markdown tree to AstNode tree in a recursive fashion.
 */
internal fun convert(
    node: Node?,
    parentNode: AstNode? = null,
    previousNode: AstNode? = null,
): AstNode? {
    node ?: return null

    val nodeLinks = AstNodeLinks(
        parent = parentNode,
        previous = previousNode,
    )

    val newNode: AstNode? = when (node) {
        is BlockQuote -> AstBlockQuote(nodeLinks = nodeLinks)
        is BulletList -> AstBulletList(
            bulletMarker = node.bulletMarker,
            nodeLinks = nodeLinks
        )
        is Code -> AstCode(
            literal = node.literal,
            nodeLinks = nodeLinks
        )
        is Document -> AstDocument(nodeLinks = nodeLinks)
        is Emphasis -> AstEmphasis(
            delimiter = node.openingDelimiter,
            nodeLinks = nodeLinks
        )
        is FencedCodeBlock -> AstFencedCodeBlock(
            literal = node.literal,
            fenceChar = node.fenceChar,
            fenceIndent = node.fenceIndent,
            fenceLength = node.fenceLength,
            info = node.info,
            nodeLinks = nodeLinks
        )
        is HardLineBreak -> AstHardLineBreak(nodeLinks = nodeLinks)
        is Heading -> AstHeading(
            level = node.level,
            nodeLinks = nodeLinks
        )
        is ThematicBreak -> AstThematicBreak(nodeLinks = nodeLinks)
        is HtmlInline -> AstHtmlInline(
            literal = node.literal,
            nodeLinks = nodeLinks
        )
        is HtmlBlock -> AstHtmlBlock(
            literal = node.literal,
            nodeLinks = nodeLinks
        )
        is Image -> AstImage(
            title = node.title,
            destination = node.destination,
            nodeLinks = nodeLinks
        )
        is IndentedCodeBlock -> AstIndentedCodeBlock(
            literal = node.literal,
            nodeLinks = nodeLinks
        )
        is Link -> AstLink(
            title = node.title ?: "",
            destination = node.destination,
            nodeLinks = nodeLinks
        )
        is ListItem -> AstListItem(nodeLinks = nodeLinks)
        is OrderedList -> AstOrderedList(
            startNumber = node.startNumber,
            delimiter = node.delimiter,
            nodeLinks = nodeLinks
        )
        is Paragraph -> AstParagraph(nodeLinks = nodeLinks)
        is SoftLineBreak -> AstSoftLineBreak(nodeLinks = nodeLinks)
        is StrongEmphasis -> AstStrongEmphasis(
            delimiter = node.openingDelimiter,
            nodeLinks = nodeLinks
        )
        is Text -> AstText(
            literal = node.literal,
            nodeLinks = nodeLinks
        )
        is LinkReferenceDefinition -> AstLinkReferenceDefinition(
            title = node.title ?: "",
            destination = node.destination,
            label = node.label,
            nodeLinks = nodeLinks
        )
        is TableBlock -> AstTableRoot(nodeLinks = nodeLinks)
        is TableHead -> AstTableHeader(nodeLinks = nodeLinks)
        is TableBody -> AstTableBody(nodeLinks = nodeLinks)
        is TableRow -> AstTableRow(nodeLinks = nodeLinks)
        is TableCell -> AstTableCell(
            header = node.isHeader,
            alignment = when (node.alignment) {
                TableCell.Alignment.LEFT -> AstTableCellAlignment.LEFT
                TableCell.Alignment.CENTER -> AstTableCellAlignment.CENTER
                TableCell.Alignment.RIGHT -> AstTableCellAlignment.RIGHT
                null -> AstTableCellAlignment.LEFT
            },
            nodeLinks = nodeLinks
        )
        is Strikethrough -> AstStrikethrough(
            node.openingDelimiter,
            nodeLinks = nodeLinks
        )
        is CustomNode -> null
        is CustomBlock -> null
        else -> null
    }

    if (newNode != null) {
        newNode.firstChild = convert(node.firstChild, parentNode = newNode, previousNode = null)
        newNode.next = convert(node.next, parentNode = parentNode, previousNode = newNode)
    }

    if (node.next == null) {
        parentNode?.lastChild = newNode
    }

    return newNode
}

/**
 * Generic AstNode implementation that can define any node in Abstract Syntax Tree.
 * All the possible node types extend this interface, including extension types.
 */
interface AstNode {
    var parent: AstNode?
    var firstChild: AstNode?
    var lastChild: AstNode?
    var previous: AstNode?
    var next: AstNode?
}

/**
 * Used to pass AstNode links to child classes by delegation.
 */
@Immutable
data class AstNodeLinks(
    override var parent: AstNode? = null,
    override var firstChild: AstNode? = null,
    override var lastChild: AstNode? = null,
    override var previous: AstNode? = null,
    override var next: AstNode? = null
): AstNode

//region Default AstNodes

@Immutable
data class AstBlockQuote(
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstBulletList(
    val bulletMarker: Char,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstCode(
    val literal: String,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstDocument(
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstEmphasis(
    private val delimiter: String,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstFencedCodeBlock(
    val fenceChar: Char,
    val fenceLength: Int,
    val fenceIndent: Int,
    val info: String,
    val literal: String,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstHardLineBreak(
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstHeading(
    val level: Int,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstThematicBreak(
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstHtmlInline(
    val literal: String,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstHtmlBlock(
    val literal: String,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstImage(
    val title: String,
    val destination: String,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstIndentedCodeBlock(
    val literal: String,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstLink(
    val destination: String,
    val title: String,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstListItem(
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstOrderedList(
    val startNumber: Int,
    val delimiter: Char,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstParagraph(
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstSoftLineBreak(
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstStrongEmphasis(
    private val delimiter: String,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstText(
    val literal: String,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

@Immutable
data class AstLinkReferenceDefinition(
    val label: String,
    val destination: String,
    val title: String,
    private val nodeLinks: AstNodeLinks
) : AstNode by nodeLinks

//endregion