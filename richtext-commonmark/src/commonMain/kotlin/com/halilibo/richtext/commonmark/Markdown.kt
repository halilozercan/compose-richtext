package com.halilibo.richtext.commonmark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.halilibo.richtext.commonmark.MarkdownParseOptions.Companion
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.ui.RichTextScope

/**
 * A composable that renders Markdown content according to Commonmark specification using RichText.
 *
 * @param content Markdown text. No restriction on length.
 * @param markdownParseOptions Options for the Markdown parser.
 * @param onLinkClicked A function to invoke when a link is clicked from rendered content.
 */
@Composable
public fun RichTextScope.Markdown(
  content: String,
  markdownParseOptions: MarkdownParseOptions = Companion.Default
) {
  val commonmarkAstNodeParser = remember(markdownParseOptions) {
    CommonmarkAstNodeParser(markdownParseOptions)
  }

  val astRootNode by produceState<AstNode?>(
    initialValue = null,
    key1 = commonmarkAstNodeParser,
    key2 = content
  ) {
    value = commonmarkAstNodeParser.parse(content)
  }

  astRootNode?.let {
    BasicMarkdown(astNode = it)
  }
}

/**
 * A helper class that can convert any text content into an ASTNode tree and return its root.
 */
public expect class CommonmarkAstNodeParser(
  options: MarkdownParseOptions = MarkdownParseOptions.Default
) {

  /**
   * Parse markdown content and return Abstract Syntax Tree(AST).
   *
   * @param text Markdown text to be parsed.
   * @param options Options for the Commonmark Markdown parser.
   */
  public fun parse(text: String): AstNode
}