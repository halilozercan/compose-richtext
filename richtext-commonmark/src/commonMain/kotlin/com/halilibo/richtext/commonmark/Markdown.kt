package com.halilibo.richtext.commonmark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.halilibo.richtext.markdown.AstBlockNodeComposer
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.ui.RichTextScope

/**
 * A composable that renders Markdown content according to Commonmark specification using RichText.
 *
 * @param content Markdown text. No restriction on length.
 * @param markdownParseOptions Options for the Markdown parser.
 * @param astBlockNodeComposer An interceptor to take control of composing any block type node's
 * rendering. Use it to render images, html text, tables with your own components.
 */
@Composable
public fun RichTextScope.Markdown(
  content: String,
  markdownParseOptions: CommonMarkdownParseOptions = CommonMarkdownParseOptions.Default,
  astBlockNodeComposer: AstBlockNodeComposer? = null
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
    BasicMarkdown(astNode = it, astBlockNodeComposer = astBlockNodeComposer)
  }
}

/**
 * A helper class that can convert any text content into an ASTNode tree and return its root.
 */
public expect class CommonmarkAstNodeParser(
  options: CommonMarkdownParseOptions = CommonMarkdownParseOptions.Default
) {

  /**
   * Parse markdown content and return Abstract Syntax Tree(AST).
   *
   * @param text Markdown text to be parsed.
   * @param options Options for the Commonmark Markdown parser.
   */
  public fun parse(text: String): AstNode
}