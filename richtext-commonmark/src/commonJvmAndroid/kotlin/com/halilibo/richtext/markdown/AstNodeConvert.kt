package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.halilibo.richtext.markdown.node.AstBlockQuote
import com.halilibo.richtext.markdown.node.AstBulletList
import com.halilibo.richtext.markdown.node.AstCode
import com.halilibo.richtext.markdown.node.AstDocument
import com.halilibo.richtext.markdown.node.AstEmphasis
import com.halilibo.richtext.markdown.node.AstFencedCodeBlock
import com.halilibo.richtext.markdown.node.AstHardLineBreak
import com.halilibo.richtext.markdown.node.AstHeading
import com.halilibo.richtext.markdown.node.AstHtmlBlock
import com.halilibo.richtext.markdown.node.AstHtmlInline
import com.halilibo.richtext.markdown.node.AstImage
import com.halilibo.richtext.markdown.node.AstIndentedCodeBlock
import com.halilibo.richtext.markdown.node.AstLink
import com.halilibo.richtext.markdown.node.AstLinkReferenceDefinition
import com.halilibo.richtext.markdown.node.AstListItem
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.markdown.node.AstNodeLinks
import com.halilibo.richtext.markdown.node.AstNodeType
import com.halilibo.richtext.markdown.node.AstOrderedList
import com.halilibo.richtext.markdown.node.AstParagraph
import com.halilibo.richtext.markdown.node.AstSoftLineBreak
import com.halilibo.richtext.markdown.node.AstStrikethrough
import com.halilibo.richtext.markdown.node.AstStrongEmphasis
import com.halilibo.richtext.markdown.node.AstTableBody
import com.halilibo.richtext.markdown.node.AstTableCell
import com.halilibo.richtext.markdown.node.AstTableCellAlignment
import com.halilibo.richtext.markdown.node.AstTableHeader
import com.halilibo.richtext.markdown.node.AstTableRoot
import com.halilibo.richtext.markdown.node.AstTableRow
import com.halilibo.richtext.markdown.node.AstText
import com.halilibo.richtext.markdown.node.AstThematicBreak
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableCell.Alignment.CENTER
import org.commonmark.ext.gfm.tables.TableCell.Alignment.LEFT
import org.commonmark.ext.gfm.tables.TableCell.Alignment.RIGHT
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.image.attributes.ImageAttributesExtension
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
import org.commonmark.parser.Parser

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
    is Image -> {
      if (node.destination == null) {
        null
      }
      else {
        AstImage(
          title = node.title ?: "",
          destination = node.destination
        )
      }
    }
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
        else -> AstTableCellAlignment.LEFT
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

internal fun Node.convert() = convert(this)

@Composable
internal actual fun parsedMarkdownAst(text: String, options: MarkdownParseOptions): AstNode? {
  val parser = remember(options) {
    Parser.builder()
      .extensions(
        listOfNotNull(
          TablesExtension.create(),
          StrikethroughExtension.create(),
          ImageAttributesExtension.create(),
          if (options.autolink) AutolinkExtension.create() else null
        )
      )
      .build()
  }

  val astRootNode by produceState<AstNode?>(null, text, parser) {
    value = parser.parse(text).convert()
  }

  return astRootNode
}

