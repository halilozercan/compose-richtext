package com.zachklipp.richtext.ui.markdown

import androidx.compose.runtime.Immutable
import org.commonmark.ext.gfm.tables.*
import org.commonmark.node.*

/**
 * Converts common-markdown tree to ASTNode tree.
 */
fun convert(
    node: Node?,
    parentNode: ASTNode? = null,
    previousNode: ASTNode? = null,
): ASTNode? {
    if (node == null) return null

    val newNode = when (node) {
        is BlockQuote -> {
            ASTBlockQuote(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is BulletList -> {
            ASTBulletList(
                bulletMarker = node.bulletMarker,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Code -> {
            ASTCode(
                literal = node.literal,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Document -> {
            ASTDocument(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Emphasis -> {
            ASTEmphasis(
                delimiter = node.openingDelimiter,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is FencedCodeBlock -> {
            ASTFencedCodeBlock(
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
            ASTHardLineBreak(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Heading -> {
            ASTHeading(
                level = node.level,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is ThematicBreak -> {
            ASTThematicBreak(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is HtmlInline -> {
            ASTHtmlInline(
                literal = node.literal,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is HtmlBlock -> {
            ASTHtmlBlock(
                literal = node.literal,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Image -> {
            ASTImage(
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
            ASTIndentedCodeBlock(
                literal = node.literal,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Link -> {
            ASTLink(
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
            ASTListItem(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is OrderedList -> {
            ASTOrderedList(
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
            ASTParagraph(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is SoftLineBreak -> {
            ASTSoftLineBreak(
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is StrongEmphasis -> {
            ASTStrongEmphasis(
                delimiter = node.openingDelimiter,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is Text -> {
            ASTText(
                literal = node.literal,
                parent = parentNode,
                previous = previousNode,
                firstChild = null,
                lastChild = null,
                next = null
            )
        }
        is LinkReferenceDefinition -> {
            ASTLinkReferenceDefinition(
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
                is TableBlock -> ASTTable.ASTTableBlock
                else -> Unit
            }

            ASTCustomBlock(
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
                is TableHead -> ASTTable.ASTTableHeader
                is TableBody -> ASTTable.ASTTableBody
                is TableRow -> ASTTable.ASTTableRow
                is TableCell -> ASTTable.ASTTableCell(
                    header = node.isHeader,
                    alignment = when(node.alignment) {
                        TableCell.Alignment.LEFT -> ASTTable.ASTTableCellAlignment.LEFT
                        TableCell.Alignment.CENTER -> ASTTable.ASTTableCellAlignment.CENTER
                        TableCell.Alignment.RIGHT -> ASTTable.ASTTableCellAlignment.RIGHT
                        null -> ASTTable.ASTTableCellAlignment.LEFT
                    }
                )
                else -> Unit
            }

            ASTCustomNode(
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
sealed class ASTNode {
    abstract var parent: ASTNode?
    abstract var firstChild: ASTNode?
    abstract var lastChild: ASTNode?
    abstract var previous: ASTNode?
    abstract var next: ASTNode?
}

@Immutable
data class ASTBlockQuote(
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTBulletList(
    val bulletMarker: Char,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTCode(
    val literal: String,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTDocument(
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTEmphasis(
    private val delimiter: String,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    val openingDelimiter: String
        get() = delimiter

    val closingDelimiter: String
        get() = delimiter

    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTFencedCodeBlock(
    val fenceChar: Char,
    val fenceLength: Int,
    val fenceIndent: Int,
    val info: String,
    val literal: String,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTHardLineBreak(
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTHeading(
    val level: Int,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTThematicBreak(
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTHtmlInline(
    val literal: String,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTHtmlBlock(
    val literal: String,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTImage(
    val title: String,
    val destination: String,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTIndentedCodeBlock(
    val literal: String,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTLink(
    val destination: String,
    val title: String,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTListItem(
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTOrderedList(
    val startNumber: Int,
    val delimiter: Char,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTParagraph(
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTSoftLineBreak(
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTStrongEmphasis(
    private val delimiter: String,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    val openingDelimiter: String
        get() = delimiter

    val closingDelimiter: String
        get() = delimiter

    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTText(
    val literal: String,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return other is ASTText && this.literal == other.literal
    }
}

@Immutable
data class ASTLinkReferenceDefinition(
    val label: String,
    val destination: String,
    val title: String,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTCustomBlock(
    val data: Any,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}

@Immutable
data class ASTCustomNode(
    val data: Any,
    override var parent: ASTNode?,
    override var firstChild: ASTNode?,
    override var lastChild: ASTNode?,
    override var previous: ASTNode?,
    override var next: ASTNode?
) : ASTNode() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }
}