package com.zachklipp.richtext.markdown.commonmark

import com.zachklipp.richtext.markdown.node.AstBlockQuote
import com.zachklipp.richtext.markdown.node.AstBulletList
import com.zachklipp.richtext.markdown.node.AstCode
import com.zachklipp.richtext.markdown.node.AstDocument
import com.zachklipp.richtext.markdown.node.AstEmphasis
import com.zachklipp.richtext.markdown.node.AstFencedCodeBlock
import com.zachklipp.richtext.markdown.node.AstHardLineBreak
import com.zachklipp.richtext.markdown.node.AstHeading
import com.zachklipp.richtext.markdown.node.AstHtmlBlock
import com.zachklipp.richtext.markdown.node.AstHtmlInline
import com.zachklipp.richtext.markdown.node.AstImage
import com.zachklipp.richtext.markdown.node.AstIndentedCodeBlock
import com.zachklipp.richtext.markdown.node.AstLink
import com.zachklipp.richtext.markdown.node.AstLinkReferenceDefinition
import com.zachklipp.richtext.markdown.node.AstListItem
import com.zachklipp.richtext.markdown.node.AstNode
import com.zachklipp.richtext.markdown.node.AstNodeLinks
import com.zachklipp.richtext.markdown.node.AstNodeType
import com.zachklipp.richtext.markdown.node.AstOrderedList
import com.zachklipp.richtext.markdown.node.AstParagraph
import com.zachklipp.richtext.markdown.node.AstSoftLineBreak
import com.zachklipp.richtext.markdown.node.AstStrikethrough
import com.zachklipp.richtext.markdown.node.AstStrongEmphasis
import com.zachklipp.richtext.markdown.node.AstTableBody
import com.zachklipp.richtext.markdown.node.AstTableCell
import com.zachklipp.richtext.markdown.node.AstTableCellAlignment
import com.zachklipp.richtext.markdown.node.AstTableHeader
import com.zachklipp.richtext.markdown.node.AstTableRoot
import com.zachklipp.richtext.markdown.node.AstTableRow
import com.zachklipp.richtext.markdown.node.AstText
import com.zachklipp.richtext.markdown.node.AstThematicBreak
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableCell.Alignment.CENTER
import org.commonmark.ext.gfm.tables.TableCell.Alignment.LEFT
import org.commonmark.ext.gfm.tables.TableCell.Alignment.RIGHT
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.CustomBlock
import org.commonmark.node.CustomNode
import org.commonmark.node.Document
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Image
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.LinkReferenceDefinition
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak

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

  val newNodeType: AstNodeType? = when (node) {
    is BlockQuote -> AstBlockQuote
    is BulletList -> AstBulletList(bulletMarker = node.bulletMarker)
    is Code -> AstCode(literal = node.literal)
    is Document -> AstDocument
    is Emphasis -> AstEmphasis(delimiter = node.openingDelimiter)
    is FencedCodeBlock -> AstFencedCodeBlock(
      literal = node.literal,
      fenceChar = node.fenceChar,
      fenceIndent = node.fenceIndent,
      fenceLength = node.fenceLength,
      info = node.info
    )
    is HardLineBreak -> AstHardLineBreak
    is Heading -> AstHeading(
      level = node.level
    )
    is ThematicBreak -> AstThematicBreak
    is HtmlInline -> AstHtmlInline(
      literal = node.literal
    )
    is HtmlBlock -> AstHtmlBlock(
      literal = node.literal
    )
    is Image -> AstImage(
      title = node.title,
      destination = node.destination
    )
    is IndentedCodeBlock -> AstIndentedCodeBlock(
      literal = node.literal
    )
    is Link -> AstLink(
      title = node.title ?: "",
      destination = node.destination
    )
    is ListItem -> AstListItem
    is OrderedList -> AstOrderedList(
      startNumber = node.startNumber,
      delimiter = node.delimiter
    )
    is Paragraph -> AstParagraph
    is SoftLineBreak -> AstSoftLineBreak
    is StrongEmphasis -> AstStrongEmphasis(
      delimiter = node.openingDelimiter
    )
    is Text -> AstText(
      literal = node.literal
    )
    is LinkReferenceDefinition -> AstLinkReferenceDefinition(
      title = node.title ?: "",
      destination = node.destination,
      label = node.label
    )
    is TableBlock -> AstTableRoot
    is TableHead -> AstTableHeader
    is TableBody -> AstTableBody
    is TableRow -> AstTableRow
    is TableCell -> AstTableCell(
      header = node.isHeader,
      alignment = when (node.alignment) {
        LEFT -> AstTableCellAlignment.LEFT
        CENTER -> AstTableCellAlignment.CENTER
        RIGHT -> AstTableCellAlignment.RIGHT
        null -> AstTableCellAlignment.LEFT
      }
    )
    is Strikethrough -> AstStrikethrough(
      node.openingDelimiter
    )
    is CustomNode -> null
    is CustomBlock -> null
    else -> null
  }

  val newNode = newNodeType?.let {
    AstNode(newNodeType, nodeLinks)
  }

  if (newNode != null) {
    newNode.links.firstChild = convert(node.firstChild, parentNode = newNode, previousNode = null)
    newNode.links.next = convert(node.next, parentNode = parentNode, previousNode = newNode)
  }

  if (node.next == null) {
    parentNode?.links?.lastChild = newNode
  }

  return newNode
}