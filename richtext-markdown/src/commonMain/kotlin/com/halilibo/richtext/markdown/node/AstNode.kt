package com.halilibo.richtext.markdown.node

/**
 * Generic AstNode implementation that can define any node in Abstract Syntax Tree.
 *
 * @param type A sealed class which is categorized into block and inline nodes.
 * @param links Pointers to parent, sibling, child nodes.
 */
public class AstNode(
  public val type: AstNodeType,
  public val links: AstNodeLinks
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AstNode) return false

    if (type != other.type) return false
    if (links != other.links) return false

    return true
  }

  override fun hashCode(): Int {
    var result = type.hashCode()
    result = 31 * result + links.hashCode()
    return result
  }
}
