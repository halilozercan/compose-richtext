package com.zachklipp.richtext.markdown

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.zachklipp.richtext.markdown.extensions.AstTableRoot
import com.zachklipp.richtext.ui.BasicRichText
import com.zachklipp.richtext.ui.BlockQuote
import com.zachklipp.richtext.ui.CodeBlock
import com.zachklipp.richtext.ui.FormattedList
import com.zachklipp.richtext.ui.Heading
import com.zachklipp.richtext.ui.HorizontalRule
import com.zachklipp.richtext.ui.ListType
import com.zachklipp.richtext.ui.RichTextScope
import com.zachklipp.richtext.ui.currentBasicTextStyle
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser

/**
 * A composable that renders Markdown content in [BasicRichText].
 *
 * @param content Markdown text. No restriction on length.
 * @param configuration Markdown specific configuration e.g. how to render images
 */
@Composable
public fun RichTextScope.Markdown(
  content: String,
  configuration: MarkdownConfiguration = MarkdownConfiguration.Default
) {
  CompositionLocalProvider(LocalMarkdownConfiguration provides configuration) {
    val markdownAst = parsedMarkdownAst(text = content)
    RecursiveRenderMarkdownAst(markdownAst)
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
 * the content. [BasicRichText] API is highly compatible with this methodology.
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
internal fun RichTextScope.RecursiveRenderMarkdownAst(
  astNode: AstNode?
) {
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
      Heading(level = astNode.level - 1) {
        MarkdownRichText(astNode)
      }
    }
    is AstThematicBreak -> {
      HorizontalRule()
    }
    is AstHtmlBlock -> {
      LocalMarkdownConfiguration.current.resolveDefaults().htmlBlock!!.onDraw(
        html = astNode.literal
      )
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
      BasicText(astNode.literal, style = currentBasicTextStyle)
    }
    is AstTableRoot -> {
      MarkdownTable(astNode)
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
@Composable
internal fun RichTextScope.visitChildren(node: AstNode?) {
  node?.childrenSequence()?.forEach {
    RecursiveRenderMarkdownAst(astNode = it)
  }
}
