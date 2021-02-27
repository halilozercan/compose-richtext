package com.zachklipp.richtext.markdown

import android.os.Build
import android.text.Html
import android.widget.TextView
import androidx.compose.foundation.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.ambientOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.UriHandlerAmbient
import androidx.compose.ui.viewinterop.AndroidView
import com.zachklipp.richtext.markdown.extensions.AstTableRoot
import com.zachklipp.richtext.ui.BlockQuote
import com.zachklipp.richtext.ui.CodeBlock
import com.zachklipp.richtext.ui.FormattedList
import com.zachklipp.richtext.ui.Heading
import com.zachklipp.richtext.ui.HorizontalRule
import com.zachklipp.richtext.ui.ListStyle
import com.zachklipp.richtext.ui.ListType
import com.zachklipp.richtext.ui.RichText
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.RichTextStyle
import com.zachklipp.richtext.ui.UnorderedMarkers
import com.zachklipp.richtext.ui.WithStyle
import com.zachklipp.richtext.ui.string.InlineContent
import com.zachklipp.richtext.ui.string.Text
import com.zachklipp.richtext.ui.string.richTextString
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser

/**
 * A composable that renders Markdown content using [RichText].
 *
 * @param content Markdown text. No restriction on length.
 * @param style [RichTextStyle] that will be used to style markdown rendering.
 * @param onLinkClicked A function to invoke when a link is clicked from rendered content.
 */
@Composable
public fun Markdown(
  content: String,
  modifier: Modifier = Modifier,
  style: RichTextStyle? = null,
  onLinkClicked: ((String) -> Unit)? = null
) {
  RichText(
    modifier = modifier,
    style = style
  ) {
    // Can't use UriHandlerAmbient.current::openUri here,
    // see https://issuetracker.google.com/issues/172366483
    val realLinkClickedHandler = onLinkClicked ?: UriHandlerAmbient.current.let {
      remember {
        { url -> it.openUri(url) }
      }
    }

    Providers(AmbientOnLinkClicked provides realLinkClickedHandler) {
      val markdownAst = parsedMarkdownAst(text = content)
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
@Composable
internal fun RichTextScope.RecursiveRenderMarkdownAst(astNode: AstNode?) {
  astNode ?: return

  when (astNode) {
    is AstBlockQuote -> {
      BlockQuote {
        visitChildren(astNode)
      }
    }
    is AstBulletList -> {
      FormattedList(
        listType = ListType.Unordered,
        items = astNode.childrenSequence().toList()
      ) { astListItem ->
        visitChildren(astListItem)
      }
    }
    is AstFencedCodeBlock -> {
      CodeBlock(text = astNode.literal)
    }
    is AstHeading -> {
      Heading(level = astNode.level) {
        MarkdownRichText(astNode)
      }
    }
    is AstThematicBreak -> {
      HorizontalRule()
    }
    is AstHtmlBlock -> {
      Text(text = richTextString {
        appendInlineContent(content = InlineContent {
          AndroidView(
            viewBlock = { context ->
              // TODO: pass current styling to legacy TextView
              TextView(context)
            },
            update = {
              it.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(astNode.literal, 0)
              } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(astNode.literal)
              }
            }
          )
        })
      })
    }
    is AstIndentedCodeBlock -> {
      CodeBlock(text = astNode.literal)
    }
    is AstOrderedList -> {
      FormattedList(
        listType = ListType.Ordered,
        items = astNode.childrenSequence().toList()
      ) { astListItem ->
        visitChildren(astListItem)
      }
    }
    is AstParagraph -> {
      MarkdownRichText(astNode)
    }
    // This should almost never happen. All the possible text
    // nodes must be under either Heading, Paragraph or CustomNode
    // In any case, we should include it here to prevent any
    // non-rendered text problems.
    is AstText -> {
      Text(astNode.literal)
    }
    is AstTableRoot -> {
      renderTable(astNode)
    }
    else -> visitChildren(astNode)
  }
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

  val rootASTNode by produceState<AstNode?>(
    initialValue = null,
    source = text
  ) {
    value = convert(parser.parse(text))
  }

  return rootASTNode
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

/**
 * An internal ambient to pass through OnLinkClicked function from root [Markdown] composable
 * to children that render links. Although being explicit is preferred, recursive calls to
 * [visitChildren] increases verbosity with each extra argument.
 */
internal val AmbientOnLinkClicked =
  ambientOf<(String) -> Unit> { error("OnLinkClicked is not provided") }
