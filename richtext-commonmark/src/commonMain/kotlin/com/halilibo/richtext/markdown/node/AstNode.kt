package com.halilibo.richtext.markdown.node

/**
 * Generic AstNode implementation that can define any node in Abstract Syntax Tree.
 *
 * @param type A sealed class which is categorized into block, container, and leaf nodes.
 * @param links Pointers to parent, sibling, child nodes.
 */
public data class AstNode(
  val type: AstNodeType,
  val links: AstNodeLinks
)
