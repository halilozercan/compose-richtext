package com.halilibo.richtext.markdown

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.halilibo.richtext.markdown.node.AstBlockQuote
import com.halilibo.richtext.markdown.node.AstDocument
import com.halilibo.richtext.markdown.node.AstFencedCodeBlock
import com.halilibo.richtext.markdown.node.AstHeading
import com.halilibo.richtext.markdown.node.AstHtmlBlock
import com.halilibo.richtext.markdown.node.AstIndentedCodeBlock
import com.halilibo.richtext.markdown.node.AstInlineNodeType
import com.halilibo.richtext.markdown.node.AstLinkReferenceDefinition
import com.halilibo.richtext.markdown.node.AstListItem
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.markdown.node.AstOrderedList
import com.halilibo.richtext.markdown.node.AstParagraph
import com.halilibo.richtext.markdown.node.AstTableBody
import com.halilibo.richtext.markdown.node.AstTableCell
import com.halilibo.richtext.markdown.node.AstTableHeader
import com.halilibo.richtext.markdown.node.AstTableRoot
import com.halilibo.richtext.markdown.node.AstTableRow
import com.halilibo.richtext.markdown.node.AstText
import com.halilibo.richtext.markdown.node.AstThematicBreak
import com.halilibo.richtext.markdown.node.AstUnorderedList
import com.halilibo.richtext.ui.BlockQuote
import com.halilibo.richtext.ui.CodeBlock
import com.halilibo.richtext.ui.FormattedList
import com.halilibo.richtext.ui.Heading
import com.halilibo.richtext.ui.HorizontalRule
import com.halilibo.richtext.ui.ListType.Ordered
import com.halilibo.richtext.ui.ListType.Unordered
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.string.InlineContent
import com.halilibo.richtext.ui.string.Text
import com.halilibo.richtext.ui.string.richTextString

/**
 * A composable that renders Markdown content pointed by [astNode] into this [RichTextScope].
 * Designed to be a building block that should be wrapped with a specific parser.
 *
 * @param astNode Root node of Markdown tree. This can be obtained via a parser.
 */
@Composable
public fun RichTextScope.BasicMarkdown(astNode: AstNode) {
  RecursiveRenderMarkdownAst(astNode)
}

/**
 * When parsed, markdown content or any other rich text can be represented as a tree.
 * The default markdown parser that is used in this project `common-markdown` also
 * utilizes the said approach. Although there are ways to iteratively traverse a tree,
 * it is more readable and compose-friendly to do it recursively.
 *
 * This function basically receives a node from the tree, root or any node, and then
 * recursively travels along the nodes while spitting out or wrapping composables around
 * the content. RichText API is highly compatible with this method.
 *
 * However, there are multiple assumptions to increase predictability. Despite the fact
 * that every [AstNode] can have another [AstNode] as a child, it should not be that
 * generic in Markdown content. For example, a Text node must not have any other children.
 * That's why this function does not have 100% coverage for all [AstNode] types.
 *
 * Heading, Paragraph are considered to be main text containers. Their content is regarded
 * as one block and children traversal happens separately.
 *
 * FormattedList, OrderedList are also content blocks. Their children are filtered before
 * being traversed. Only ListItems are accepted as valid children for these blocks.
 *
 * For now, only tables are rendered from CustomBlock or CustomNode.
 *
 * @param astNode Root node to start rendering.
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
@Composable
internal fun RichTextScope.RecursiveRenderMarkdownAst(astNode: AstNode?) {
  astNode ?: return

  when (val astNodeType = astNode.type) {
    is AstDocument -> visitChildren(node = astNode)
    is AstBlockQuote -> {
      BlockQuote {
        visitChildren(astNode)
      }
    }
    is AstUnorderedList -> {
      FormattedList(
        listType = Unordered,
        items = astNode.filterChildrenType<AstListItem>().toList()
      ) { astListItem ->
        // if this list item has no child, it should at least emit a single pixel layout.
        if (astListItem.links.firstChild == null) {
          BasicText("")
        } else {
          visitChildren(astListItem)
        }
      }
    }
    is AstOrderedList -> {
      FormattedList(
        listType = Ordered,
        items = astNode.childrenSequence().toList(),
        startIndex = astNodeType.startNumber - 1,
      ) { astListItem ->
        // if this list item has no child, it should at least emit a single pixel layout.
        if (astListItem.links.firstChild == null) {
          BasicText("")
        } else {
          visitChildren(astListItem)
        }
      }
    }
    is AstThematicBreak -> {
      HorizontalRule()
    }
    is AstHeading -> {
      Heading(level = astNodeType.level) {
        MarkdownRichText(astNode, Modifier.semantics { heading() } )
      }
    }
    is AstIndentedCodeBlock -> {
      CodeBlock(text = astNodeType.literal.trim())
    }
    is AstFencedCodeBlock -> {
      CodeBlock(text = astNodeType.literal.trim())
    }
    is AstHtmlBlock -> {
      Text(text = richTextString {
        appendInlineContent(content = InlineContent {
          HtmlBlock(astNodeType.literal)
        })
      })
    }
    is AstLinkReferenceDefinition -> {
      // TODO(halilozercan)
      /* no-op */
    }
    is AstParagraph -> {
      MarkdownRichText(astNode)
    }
    is AstTableRoot -> {
      RenderTable(astNode)
    }
    // This should almost never happen. All the possible text
    // nodes must be under either Heading, Paragraph or CustomNode
    // In any case, we should include it here to prevent any
    // non-rendered text problems.
    is AstText -> {
      // TODO(halilozercan) use multiplatform compatible stderr logging
      println("Unexpected raw text while traversing the Abstract Syntax Tree.")
      Text(richTextString { append(astNodeType.literal) })
    }
    is AstListItem -> {
      println("MarkdownRichText: Unexpected AstListItem while traversing the Abstract Syntax Tree.")
    }
    is AstInlineNodeType -> {
      // ignore
      println("MarkdownRichText: Unexpected AstInlineNodeType $astNodeType while traversing the Abstract Syntax Tree.")
    }
    AstTableBody,
    AstTableHeader,
    AstTableRow,
    is AstTableCell -> {
      println("MarkdownRichText: Unexpected Table node while traversing the Abstract Syntax Tree.")
    }
  }.let {}
}

/**
 * Visit and render children from first to last.
 *
 * @param node Root ASTNode whose children will be visited.
 */
@Composable
internal fun RichTextScope.visitChildren(node: AstNode?) {
  node?.childrenSequence()?.forEach {
    RecursiveRenderMarkdownAst(astNode = it)
  }
}
