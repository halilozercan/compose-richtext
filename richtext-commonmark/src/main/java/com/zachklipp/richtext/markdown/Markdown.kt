package com.zachklipp.richtext.markdown

import android.annotation.SuppressLint
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.Html
import android.util.Log
import android.widget.TextView
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.viewinterop.AndroidView
import com.zachklipp.richtext.markdown.node.AstBlockQuote
import com.zachklipp.richtext.markdown.node.AstBulletList
import com.zachklipp.richtext.markdown.node.AstDocument
import com.zachklipp.richtext.markdown.node.AstFencedCodeBlock
import com.zachklipp.richtext.markdown.node.AstHeading
import com.zachklipp.richtext.markdown.node.AstHtmlBlock
import com.zachklipp.richtext.markdown.node.AstIndentedCodeBlock
import com.zachklipp.richtext.markdown.node.AstInlineNodeType
import com.zachklipp.richtext.markdown.node.AstLinkReferenceDefinition
import com.zachklipp.richtext.markdown.node.AstListItem
import com.zachklipp.richtext.markdown.node.AstNode
import com.zachklipp.richtext.markdown.node.AstOrderedList
import com.zachklipp.richtext.markdown.node.AstParagraph
import com.zachklipp.richtext.markdown.node.AstTableBody
import com.zachklipp.richtext.markdown.node.AstTableCell
import com.zachklipp.richtext.markdown.node.AstTableHeader
import com.zachklipp.richtext.markdown.node.AstTableRoot
import com.zachklipp.richtext.markdown.node.AstTableRow
import com.zachklipp.richtext.markdown.node.AstText
import com.zachklipp.richtext.markdown.node.AstThematicBreak
import com.zachklipp.richtext.markdown.commonmark.convert
import com.zachklipp.richtext.ui.BlockQuote
import com.zachklipp.richtext.ui.CodeBlock
import com.zachklipp.richtext.ui.FormattedList
import com.zachklipp.richtext.ui.Heading
import com.zachklipp.richtext.ui.HorizontalRule
import com.zachklipp.richtext.ui.ListType.Ordered
import com.zachklipp.richtext.ui.ListType.Unordered
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.string.InlineContent
import com.zachklipp.richtext.ui.string.Text
import com.zachklipp.richtext.ui.string.richTextString
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser

/**
 * A composable that renders Markdown content using RichText.
 *
 * @param content Markdown text. No restriction on length.
 * @param onLinkClicked A function to invoke when a link is clicked from rendered content.
 */
@Composable
public fun RichTextScope.Markdown(
  content: String,
  onLinkClicked: ((String) -> Unit)? = null
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
    val markdownAst = parsedMarkdownAst(text = content)
    RecursiveRenderMarkdownAst(astNode = markdownAst)
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
    is AstBulletList -> {
      FormattedList(
        listType = Unordered,
        items = astNode.filterChildrenType<AstListItem>().toList()
      ) {
        visitChildren(it)
      }
    }
    is AstOrderedList -> {
      FormattedList(
        listType = Ordered,
        items = astNode.childrenSequence().toList()
      ) { astListItem ->
        visitChildren(astListItem)
      }
    }
    is AstThematicBreak -> {
      HorizontalRule()
    }
    is AstHeading -> {
      Heading(level = astNodeType.level) {
        MarkdownRichText(astNode)
      }
    }
    is AstIndentedCodeBlock -> {
      CodeBlock(text = astNodeType.literal)
    }
    is AstFencedCodeBlock -> {
      CodeBlock(text = astNodeType.literal)
    }
    is AstHtmlBlock -> {
      Text(text = richTextString {
        appendInlineContent(content = InlineContent {
          AndroidView(
            factory = { context ->
              // TODO: pass current styling to legacy TextView
              TextView(context)
            },
            update = {
              it.text = if (VERSION.SDK_INT >= VERSION_CODES.N) {
                Html.fromHtml(astNodeType.literal, 0)
              } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(astNodeType.literal)
              }
            }
          )
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
      Log.e("MarkdownRichText", "Unexpected raw text while traversing the Abstract Syntax Tree.")
      Text(astNodeType.literal)
    }
    is AstListItem -> {
      Log.e("MarkdownRichText", "Unexpected AstListItem while traversing the Abstract Syntax Tree.")
    }
    is AstInlineNodeType -> {
      // ignore
      Log.e("MarkdownRichText", "Unexpected AstInlineNodeType ${astNodeType} while traversing the Abstract Syntax Tree.")
    }
    AstTableBody,
    AstTableHeader,
    AstTableRow,
    is AstTableCell -> {
      Log.e("MarkdownRichText", "Unexpected Table node while traversing the Abstract Syntax Tree.")
    }
  }.let {}
}

/**
 * Parse markdown content and return Abstract Syntax Tree(AST).
 * Composable is efficient thanks to remember construct.
 *
 * @param text Markdown text to be parsed.
 */
@Composable
internal fun parsedMarkdownAst(text: String): AstNode? {
  val parser = remember {
    Parser.builder()
      .extensions(
        listOf(
          TablesExtension.create(),
          StrikethroughExtension.create()
        )
      ).build()
  }

  val rootASTNode by produceState<AstNode?>(null, text) {
    value = convert(parser.parse(text))
  }

  return rootASTNode
}

/**
 * Visit and render children from first to last.
 *
 * @param node Root ASTNode whose children will be visited.
 */
@SuppressLint("ComposableNaming")
@Composable
internal fun RichTextScope.visitChildren(node: AstNode?) {
  node?.childrenSequence()?.forEach {
    RecursiveRenderMarkdownAst(astNode = it)
  }
}

/**
 * An internal ambient to pass through OnLinkClicked function from root [Markdown] composable
 * to children that render links. Although being explicit is preferred, recursive calls to
 * [visitChildren] increases verbosity with each extra argument.
 */
internal val LocalOnLinkClicked =
  compositionLocalOf<(String) -> Unit> { error("OnLinkClicked is not provided") }
