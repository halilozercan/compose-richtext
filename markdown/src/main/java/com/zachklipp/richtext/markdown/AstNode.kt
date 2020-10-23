package com.zachklipp.richtext.markdown

import androidx.compose.runtime.Immutable
import org.commonmark.ext.gfm.tables.*
import org.commonmark.node.*

/**
 * Converts common-markdown tree to ASTNode tree.
 */
internal fun convert(
    node: Node?,
    parentNode: AstNode? = null,
    previousNode: AstNode? = null,
): AstNode? {
    node ?: return null

    val newNode = when (node) {
        is BlockQuote -> {
            AstBlockQuote(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is BulletList -> {
            AstBulletList(
                bulletMarker = node.bulletMarker,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Code -> {
            AstCode(
                literal = node.literal,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Document -> {
            AstDocument(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Emphasis -> {
            AstEmphasis(
                delimiter = node.openingDelimiter,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is FencedCodeBlock -> {
            AstFencedCodeBlock(
                literal = node.literal,
                fenceChar = node.fenceChar,
                fenceIndent = node.fenceIndent,
                fenceLength = node.fenceLength,
                info = node.info,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is HardLineBreak -> {
            AstHardLineBreak(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Heading -> {
            AstHeading(
                level = node.level,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is ThematicBreak -> {
            AstThematicBreak(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is HtmlInline -> {
            AstHtmlInline(
                literal = node.literal,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is HtmlBlock -> {
            AstHtmlBlock(
                literal = node.literal,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Image -> {
            AstImage(
                title = node.title,
                destination = node.destination,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is IndentedCodeBlock -> {
            AstIndentedCodeBlock(
                literal = node.literal,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Link -> {
            AstLink(
                title = node.title ?: "",
                destination = node.destination,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is ListItem -> {
            AstListItem(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is OrderedList -> {
            AstOrderedList(
                startNumber = node.startNumber,
                delimiter = node.delimiter,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Paragraph -> {
            AstParagraph(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is SoftLineBreak -> {
            AstSoftLineBreak(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is StrongEmphasis -> {
            AstStrongEmphasis(
                delimiter = node.openingDelimiter,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Text -> {
            AstText(
                literal = node.literal,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is LinkReferenceDefinition -> {
            AstLinkReferenceDefinition(
                title = node.title ?: "",
                destination = node.destination,
                label = node.label,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is CustomBlock -> {
            val data: Any = when(node) {
                is TableBlock -> AstTableSection.Root
                else -> Unit
            }

            AstCustomBlock(
                data = data,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is CustomNode -> {
            val data: Any = when(node) {
                is TableHead -> AstTableSection.Header
                is TableBody -> AstTableSection.Body
                is TableRow -> AstTableSection.Row
                is TableCell -> AstTableSection.Cell(
                    header = node.isHeader,
                    alignment = when (node.alignment) {
                        TableCell.Alignment.LEFT -> AstTableSection.AstTableCellAlignment.LEFT
                        TableCell.Alignment.CENTER -> AstTableSection.AstTableCellAlignment.CENTER
                        TableCell.Alignment.RIGHT -> AstTableSection.AstTableCellAlignment.RIGHT
                        null -> AstTableSection.AstTableCellAlignment.LEFT
                    }
                )
                else -> Unit
            }

            AstCustomNode(
                data = data,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
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

@Immutable
sealed class AstNode {
    abstract var parent: AstNode?
    abstract var firstChild: AstNode?
    abstract var lastChild: AstNode?
    abstract var previous: AstNode?
    abstract var next: AstNode?
}

@Immutable
data class AstBlockQuote(
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstBulletList(
    val bulletMarker: Char,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstCode(
    val literal: String,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstDocument(
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstEmphasis(
    private val delimiter: String,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstFencedCodeBlock(
    val fenceChar: Char,
    val fenceLength: Int,
    val fenceIndent: Int,
    val info: String,
    val literal: String,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstHardLineBreak(
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstHeading(
    val level: Int,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstThematicBreak(
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstHtmlInline(
    val literal: String,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstHtmlBlock(
    val literal: String,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstImage(
    val title: String,
    val destination: String,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstIndentedCodeBlock(
    val literal: String,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstLink(
    val destination: String,
    val title: String,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstListItem(
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstOrderedList(
    val startNumber: Int,
    val delimiter: Char,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstParagraph(
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstSoftLineBreak(
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstStrongEmphasis(
    private val delimiter: String,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstText(
    val literal: String,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstLinkReferenceDefinition(
    val label: String,
    val destination: String,
    val title: String,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstCustomBlock(
    val data: Any,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()

@Immutable
data class AstCustomNode(
    val data: Any,
    override var parent: AstNode?,
    override var firstChild: AstNode?,
    override var lastChild: AstNode?,
    override var previous: AstNode?,
    override var next: AstNode?
) : AstNode()