package com.halilibo.richtext.markdown.node

import androidx.compose.runtime.Immutable

/**
 * All the pointers that can exist for a node in an AST.
 *
 * Links are mutable to make it possible to instantiate a Node which can then reconfigure its
 * children and siblings. Please do not modify the links after an ASTNode is created and the scope
 * is finished.
 */
@Immutable
public class AstNodeLinks(
  public var parent: AstNode? = null,
  public var firstChild: AstNode? = null,
  public var lastChild: AstNode? = null,
  public var previous: AstNode? = null,
  public var next: AstNode? = null
) {

  override fun equals(other: Any?): Boolean {
    if (other !is AstNodeLinks) return false

    return parent === other.parent &&
        firstChild === other.firstChild &&
        lastChild === other.lastChild &&
        previous === other.previous &&
        next === other.next
  }

  /**
   * Stop infinite loop and only calculate towards bottom-right direction
   */
  override fun hashCode(): Int {
    return (firstChild ?: 0).hashCode() * 11 + (next ?: 0).hashCode() * 7
  }
}