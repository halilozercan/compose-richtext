package com.halilibo.richtext.markdown

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.halilibo.richtext.markdown.node.AstBlockQuote
import com.halilibo.richtext.markdown.node.AstBulletList
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
import com.halilibo.richtext.ui.BlockQuote
import com.halilibo.richtext.ui.CodeBlock
import com.halilibo.richtext.ui.FormattedList
import com.halilibo.richtext.ui.Heading
import com.halilibo.richtext.ui.HorizontalRule
import com.halilibo.richtext.ui.ListType.Ordered
import com.halilibo.richtext.ui.ListType.Unordered
import com.halilibo.richtext.ui.RichTextScope
import com.halilibo.richtext.ui.string.InlineContent
import com.halilibo.richtext.ui.string.RichTextString
import com.halilibo.richtext.ui.string.Text
import com.halilibo.richtext.ui.string.richTextString
import org.commonmark.node.Node

/**
 * Overrides block content such as code blocks.
 */
public typealias ContentOverride = @Composable (
  node: AstNode,
  visitChildren: @Composable (node: AstNode) -> Unit,
) -> Boolean

/**
 * Overrides content that is inline in a paragraph such as a link, or text.
 *
 * @param defaultContent Call this function if you want to render the default content for this node.
 *
 * @return the last formatIndex from any richTextStringBuilder if you pushed a style to the builder
 * and you want it to be popped when the node leaves the rendering scope.
 */
public typealias InlineContentOverride = RichTextScope.(
  node: AstNode,
  richTextStringBuilder: RichTextString.Builder,
  defaultContent: () -> Int?,
  onLinkClicked: (String) -> Unit,
) -> Int?

/**
 * A composable that renders Markdown content using RichText.
 *
 * @param content Markdown text. No restriction on length.
 * @param markdownParseOptions Options for the Markdown parser.
 * @param onLinkClicked A function to invoke when a link is clicked from rendered content.
 */
@Composable
public fun RichTextScope.Markdown(
  content: String,
  markdownParseOptions: MarkdownParseOptions = MarkdownParseOptions.Default,
  markdownRenderOptions: MarkdownRenderOptions = MarkdownRenderOptions.Default,
  onLinkClicked: ((String) -> Unit)? = null,
  contentOverride: ContentOverride? = null,
  inlineContentOverride: InlineContentOverride? = null,
) {
  val markdown = parsedMarkdown(text = content, options = markdownParseOptions)
  markdown?.let { Markdown(it, onLinkClicked, contentOverride, inlineContentOverride, markdownRenderOptions) }
}

/**
 * A composable that renders Markdown node using RichText.
 *
 * @param content CommonMark node to render.
 * @param onLinkClicked A function to invoke when a link is clicked from rendered content.
 */
@Composable
public fun RichTextScope.Markdown(
  content: Node,
  onLinkClicked: ((String) -> Unit)? = null,
  contentOverride: ContentOverride? = null,
  inlineContentOverride: InlineContentOverride? = null,
  markdownRenderOptions: MarkdownRenderOptions = MarkdownRenderOptions.Default,
) {
  val onLinkClickedState = rememberUpdatedState(onLinkClicked)
  // Can't use UriHandlerAmbient.current::openUri here,
  // see https://issuetracker.google.com/issues/172366483
  val realLinkClickedHandler = onLinkClickedState.value ?: LocalUriHandler.current.let {
    remember {
      { url -> it.openUri(url) }
    }
  }
  CompositionLocalProvider(LocalOnLinkClicked provides realLinkClickedHandler) {
    RecursiveRenderMarkdownAst(content.toAstNode(), contentOverride, inlineContentOverride, markdownRenderOptions)
  }
}

/**
 * Convert CommonMark [Node] to [AstNode].
 */
@Composable
internal expect fun Node.toAstNode(): AstNode?

/**
 * Parse markdown content and return Abstract Syntax Tree(AST).
 * Composable is efficient thanks to remember construct.
 *
 * @param text Markdown text to be parsed.
 * @param options Options for the Markdown parser.
 */
@Composable
internal expect fun parsedMarkdown(text: String, options: MarkdownParseOptions): Node?

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
internal fun RichTextScope.RecursiveRenderMarkdownAst(
  astNode: AstNode?,
  contentOverride: ContentOverride?,
  inlineContentOverride: InlineContentOverride?,
  markdownRenderOptions: MarkdownRenderOptions,
) {
  astNode ?: return

  if (contentOverride?.invoke(astNode) {
      visitChildren(it, contentOverride, inlineContentOverride, markdownRenderOptions)
    } == true) {
    return
  }

  when (val astNodeType = astNode.type) {
    is AstDocument -> visitChildren(node = astNode, contentOverride, inlineContentOverride, markdownRenderOptions)
    is AstBlockQuote -> {
      BlockQuote {
        visitChildren(astNode, contentOverride, inlineContentOverride, markdownRenderOptions)
      }
    }

    is AstBulletList -> {
      FormattedList(
        listType = Unordered,
        items = astNode.filterChildrenType<AstListItem>().toList()
      ) {
        visitChildren(it, contentOverride, inlineContentOverride, markdownRenderOptions)
      }
    }

    is AstOrderedList -> {
      FormattedList(
        listType = Ordered,
        items = astNode.childrenSequence().toList()
      ) { astListItem ->
        visitChildren(astListItem, contentOverride, inlineContentOverride, markdownRenderOptions)
      }
    }

    is AstThematicBreak -> {
      HorizontalRule()
    }

    is AstHeading -> {
      Heading(level = astNodeType.level) {
        MarkdownRichText(astNode, inlineContentOverride, markdownRenderOptions, Modifier.semantics { heading() })
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
      MarkdownRichText(astNode, inlineContentOverride, markdownRenderOptions)
    }

    is AstTableRoot -> {
      RenderTable(astNode, inlineContentOverride, markdownRenderOptions)
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
internal fun RichTextScope.visitChildren(
  node: AstNode?,
  contentOverride: ContentOverride?,
  inlineContentOverride: InlineContentOverride?,
  markdownRenderOptions: MarkdownRenderOptions,
) {
  node?.childrenSequence()?.forEach {
    RecursiveRenderMarkdownAst(
      astNode = it,
      contentOverride,
      inlineContentOverride,
      markdownRenderOptions,
    )
  }
}

/**
 * An internal ambient to pass through OnLinkClicked function from root [Markdown] composable
 * to children that render links. Although being explicit is preferred, recursive calls to
 * [visitChildren] increases verbosity with each extra argument.
 */
internal val LocalOnLinkClicked =
  compositionLocalOf<(String) -> Unit> { error("OnLinkClicked is not provided") }
