package com.halilibo.richtext.commonmark

import androidx.compose.runtime.Composable
import com.halilibo.richtext.markdown.node.AstNode
import org.commonmark.node.Node

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
internal expect fun parsedMarkdown(text: String, options: CommonMarkdownParseOptions): AstNode?
